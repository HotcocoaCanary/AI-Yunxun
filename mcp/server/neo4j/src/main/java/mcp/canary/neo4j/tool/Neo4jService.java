package mcp.canary.neo4j.tool;

import org.neo4j.driver.*;
import org.neo4j.driver.exceptions.Neo4jException;
import org.neo4j.driver.summary.ResultSummary;
import org.neo4j.driver.summary.SummaryCounters;
import org.neo4j.driver.types.MapAccessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

@Service
public class Neo4jService implements AutoCloseable {

    private static final Logger logger = LoggerFactory.getLogger(Neo4jService.class);
    private final Driver driver;
    private final String databaseName;
    private static final String SCHEMA = """
            调用 apoc.meta.data() 返回 label、property、type、other、unique、index、elementType
            其中 elementType = 'node' 且 label 不以 '_' 开头
            使用 label，
            收集所有 type 不等于 'RELATIONSHIP' 的属性，格式为 [property, type + 如果 unique 为真则添加 " unique" 否则为空 + 如果 index 为真则添加 " indexed" 否则为空]，并将结果命名为 attributes，
            收集所有 type 等于 'RELATIONSHIP' 的属性，格式为 [property, head(other)]，并将结果命名为 relationships
            返回 label、apoc.map.fromPairs(attributes) 作为 attributes、apoc.map.fromPairs(relationships) 作为 relationships
            """;

    // 检查 Cypher 查询是否包含常见地写入子句。
    private static final Pattern WRITE_QUERY_PATTERN = Pattern.compile(
            "\\b(MERGE|CREATE|SET|DELETE|REMOVE|ADD)\\b", Pattern.CASE_INSENSITIVE
    );

    /**
     * 初始化与 Neo4j 数据库的连接。
     *
     * @param uri          Neo4j 连接 URI（例如：“neo4j://localhost:7687”）
     * @param username     Database username
     * @param password     Database password
     * @param databaseName Database name (e.g., "neo4j")
     */
    public Neo4jService(
            @Value("${neo4j.uri}") String uri,
            @Value("${neo4j.username}") String username,
            @Value("${neo4j.password}") String password,
            @Value("${neo4j.database:neo4j}") String databaseName) {
        logger.debug("正在初始化与 {} 数据库的连接，数据库名称为 {}", uri, databaseName);
        this.driver = GraphDatabase.driver(uri, AuthTokens.basic(username, password), config());
        try {
            driver.verifyConnectivity();
            logger.info("已成功连接到位于 {} 的 Neo4j 数据库 {}。", uri, databaseName);
        } catch (Exception e) {
            logger.error("无法验证与 Neo4j 的连接：{}", e.getMessage());
            throw new Neo4jException("Neo4j 连接失败: ", e);
        }
        this.databaseName = databaseName;
    }

    /**
     * 定义 Neo4j 驱动程序的配置。
     *
     * @return Neo4j 配置对象。
     */
    private Config config() {
        return Config.builder()
                .withConnectionTimeout(300, TimeUnit.SECONDS)
                .withMaxConnectionPoolSize(100)
                .withMaxConnectionLifetime(1, TimeUnit.HOURS)
                .withConnectionAcquisitionTimeout(600, TimeUnit.SECONDS)
                .withMaxTransactionRetryTime(300, TimeUnit.SECONDS)
                .build();
    }

    /**
     * 检查 Cypher 查询是否包含常见的写入子句。
     *
     * @param query Cypher 查询字符串。
     * @return 如果查询包含写入子句，则返回 true；否则返回 false。
     */
    public boolean isWriteQuery(String query) {
        return WRITE_QUERY_PATTERN.matcher(query).find();
    }

    /**
     * 执行 Cypher 查询并将结果作为字典（Map）列表返回。
     * 对于写入查询，返回一个包含单个计数器映射的列表。
     * 对于读取查询，返回一个映射列表，其中每个映射代表一条记录。
     *
     * @param query  Cypher 查询字符串。
     * @param params 查询的可选参数。
     * @return 一个包含表示查询结果或写入计数器的映射列表。
     * @throws RuntimeException 如果发生数据库错误。
     */
    public List<Map<String, Object>> executeQuery(String query, Map<String, Object> params) {
        logger.info("正在执行查询: {}", query);
        if (params == null) {
            params = Collections.emptyMap();
        }
        try (Session session = driver.session(SessionConfig.forDatabase(databaseName))) {
            Result result = session.run(query, params);

            // 对于写入查询，返回一个表示计数器的映射表。
            if (isWriteQuery(query)) {
                ResultSummary summary = result.consume(); // 使用结果即可获取摘要。
                SummaryCounters counters = summary.counters();
                Map<String, Object> counterMap = new HashMap<>();
                counterMap.put("nodesCreated", counters.nodesCreated());
                counterMap.put("nodesDeleted", counters.nodesDeleted());
                counterMap.put("relationshipsCreated", counters.relationshipsCreated());
                counterMap.put("relationshipsDeleted", counters.relationshipsDeleted());
                counterMap.put("propertiesSet", counters.propertiesSet());
                counterMap.put("labelsAdded", counters.labelsAdded());
                counterMap.put("labelsRemoved", counters.labelsRemoved());
                counterMap.put("indexesAdded", counters.indexesAdded());
                counterMap.put("indexesRemoved", counters.indexesRemoved());
                counterMap.put("constraintsAdded", counters.constraintsAdded());
                counterMap.put("constraintsRemoved", counters.constraintsRemoved());
                counterMap.put("systemUpdates", counters.systemUpdates());
                counterMap.put("containsSystemUpdates", counters.containsSystemUpdates());
                counterMap.put("containsUpdates", counters.containsUpdates());
                logger.debug("写入查询受影响: {}", counterMap);
                return List.of(counterMap);
            } else {
                List<Map<String, Object>> records = result.list(MapAccessor::asMap);
                logger.info("读取查询返回 {} 行", records.size());
                return records;
            }
        } catch (Neo4jException e) {
            logger.error("执行查询时发生数据库错误：{}\n查询语句：{}", e.getMessage(), query, e);
            return Collections.emptyList();
        }
    }

    /**
     * 关闭 Neo4j 驱动程序。
     */
    @Override
    public void close() {
        if (driver != null) {
            try {
                driver.close();
                logger.info("Neo4j 驱动程序已成功关闭。");
            } catch (Exception e) {
                logger.error("关闭 Neo4j 驱动程序时出错：{}", e.getMessage(), e);
            }
        }
    }

    @Tool(name = "get-neo4j-schema", description = "列出 Neo4j 数据库中的所有节点类型、它们的属性以及它们与其他节点类型之间的关系。")
    public List<Map<String, Object>> neo4jSchema() {
        return executeQuery(SCHEMA, Collections.emptyMap());
    }

    @Tool(name = "read-neo4j-cypher", description = "在 neo4j 数据库上执行 Cypher 查询。")
    public List<Map<String, Object>> neo4jRead(@ToolParam(description = "Cypher 读取要执行的查询") String query) {
        if (isWriteQuery(query)) {
            throw new IllegalArgumentException("读取查询只允许使用 MATCH 查询。");
        }
        return executeQuery(query, Collections.emptyMap());
    }

    @Tool(name = "write-neo4j-cypher", description = "在 Neo4j 数据库上执行写入 Cypher 查询。")
    public List<Map<String, Object>> neo4jWrite(@ToolParam(description = "编写 Cypher 查询语句并执行") String query) {
        if (!isWriteQuery(query)) {
            throw new IllegalArgumentException("仅允许对写入查询执行写入操作。");
        }
        return executeQuery(query, Collections.emptyMap());
    }

}