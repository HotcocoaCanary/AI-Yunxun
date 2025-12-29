package mcp.canary.neo4j.service;

import mcp.canary.neo4j.db.Neo4jConnection;
import org.neo4j.driver.Result;
import org.neo4j.driver.exceptions.Neo4jException;
import org.neo4j.driver.summary.ResultSummary;
import org.neo4j.driver.summary.SummaryCounters;
import org.neo4j.driver.types.MapAccessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

@Service
public class Neo4jService {

    private static final Logger logger = LoggerFactory.getLogger(Neo4jService.class);
    private final Neo4jConnection neo4jConnection;

    // 检查 Cypher 查询是否包含常见的写入子句
    private static final Pattern WRITE_QUERY_PATTERN = Pattern.compile(
            "\\b(MERGE|CREATE|SET|DELETE|REMOVE|ADD)\\b", Pattern.CASE_INSENSITIVE
    );

    /**
     * 构造函数，注入 Neo4j 连接
     *
     * @param neo4jConnection Neo4j 数据库连接
     */
    public Neo4jService(Neo4jConnection neo4jConnection) {
        this.neo4jConnection = neo4jConnection;
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
        try (var session = neo4jConnection.createSession()) {
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

}