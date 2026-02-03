package mcp.canary.neo4j.service;

import mcp.canary.neo4j.db.Neo4jConnection;
import mcp.canary.shared.GraphSeries;
import mcp.canary.shared.data.GraphCategory;
import mcp.canary.shared.data.GraphEdge;
import mcp.canary.shared.data.GraphNode;
import org.neo4j.driver.Result;
import org.neo4j.driver.Value;
import org.neo4j.driver.exceptions.Neo4jException;
import org.neo4j.driver.summary.ResultSummary;
import org.neo4j.driver.summary.SummaryCounters;
import org.neo4j.driver.types.Node;
import org.neo4j.driver.types.Path;
import org.neo4j.driver.types.Relationship;
import org.neo4j.driver.types.MapAccessor;
import org.neo4j.driver.Record;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

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

    // 在 Neo4jService 类中添加以下方法
    public GraphSeries executeGraphQuery(String query, Map<String, Object> params) {
        GraphSeries series = new GraphSeries();
        series.setLayout("force");

        // 使用 elementId 字符串作为 Map 的 Key
        Map<String, GraphNode> nodeMap = new HashMap<>();
        Map<String, GraphEdge> edgeMap = new HashMap<>();
        Set<String> categoryNames = new HashSet<>();

        try (var session = neo4jConnection.createSession()) {
            // 执行并获取结果列表
            List<Record> records = session.run(query, params != null ? params : Collections.emptyMap()).list();

            for (Record record : records) {
                // record.values() 返回的是 List<Value>
                for (Value value : record.values()) {

                    // 根据驱动 5.x 的类型判断方式
                    if (value.type().name().equals("NODE")) {
                        processNode(value.asNode(), nodeMap, categoryNames);
                    }
                    else if (value.type().name().equals("RELATIONSHIP")) {
                        processRelationship(value.asRelationship(), edgeMap);
                    }
                    else if (value.type().name().equals("PATH")) {
                        Path path = value.asPath();
                        path.nodes().forEach(n -> processNode(n, nodeMap, categoryNames));
                        path.relationships().forEach(r -> processRelationship(r, edgeMap));
                    }
                }
            }
        }

        series.setNodes(new ArrayList<>(nodeMap.values()));
        series.setEdges(new ArrayList<>(edgeMap.values()));

        // 生成分类列表
        List<GraphCategory> categories = categoryNames.stream().map(name -> {
            GraphCategory cat = new GraphCategory();
            cat.setName(name);
            cat.setSymbol("circle");
            return cat;
        }).collect(Collectors.toList());
        series.setCategories(categories);

        return series;
    }

    private void processNode(Node node, Map<String, GraphNode> nodeMap, Set<String> categoryNames) {
        String elementId = node.elementId(); // 使用新的 elementId
        if (nodeMap.containsKey(elementId)) return;

        GraphNode gNode = new GraphNode();
        gNode.setName(elementId);

        // 获取 Label
        String label = "Unknown";
        Iterator<String> labels = node.labels().iterator();
        if (labels.hasNext()) {
            label = labels.next();
        }

        gNode.setCategoryName(label);
        categoryNames.add(label);
        gNode.setProperties(new HashMap<>(node.asMap()));

        nodeMap.put(elementId, gNode);
    }

    private void processRelationship(Relationship rel, Map<String, GraphEdge> edgeMap) {
        String elementId = rel.elementId(); // 使用新的 elementId
        if (edgeMap.containsKey(elementId)) return;

        GraphEdge gEdge = new GraphEdge();
        // 使用 elementId 保证 source/target 能正确匹配 node.name
        gEdge.setSource(rel.startNodeElementId());
        gEdge.setTarget(rel.endNodeElementId());
        gEdge.setProperties(new HashMap<>(rel.asMap()));

        // 修正：rel.get() 只接收一个参数，并检查是否为 NULL 类型
        Value weightValue = rel.get("weight");
        if (weightValue != null && !weightValue.isNull()) {
            gEdge.setValue(weightValue.asNumber());
        }

        edgeMap.put(elementId, gEdge);
    }

}