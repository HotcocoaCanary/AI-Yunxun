package mcp.canary.neo4j.service;

import mcp.canary.neo4j.db.Neo4jConnection;
import org.neo4j.driver.Result;
import org.neo4j.driver.summary.SummaryCounters;
import org.neo4j.driver.types.MapAccessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class Neo4jService {
    private static final Logger logger = LoggerFactory.getLogger(Neo4jService.class);
    private final Neo4jConnection neo4jConnection;

    public Neo4jService(Neo4jConnection neo4jConnection) {
        this.neo4jConnection = neo4jConnection;
    }

    /**
     * 执行原生 Cypher 并返回结果列表
     */
    public List<Map<String, Object>> execute(String query, Map<String, Object> params) {
        try (var session = neo4jConnection.createSession()) {
            return session.run(query, params != null ? params : Collections.emptyMap())
                    .list(MapAccessor::asMap);
        }
    }

    /**
     * 自动去重逻辑：查找 name 属性重复的节点，并调用 APOC 进行深度合并。
     * 合并规则：属性结合、关系重定向到新节点。
     */
    public void deduplicateNodesByName() {
        String dedupeCypher = """
            MATCH (n)
            WHERE n.name IS NOT NULL
            WITH n.name AS name, collect(n) AS nodes
            WHERE size(nodes) > 1
            CALL apoc.refactor.mergeNodes(nodes, {properties:"combine", mergeRels:true})
            YIELD node RETURN count(node)
            """;
        execute(dedupeCypher, null);
        logger.info("Executed APOC node deduplication based on 'name' property.");
    }

    /**
     * 执行写入操作并获取统计摘要
     */
    public Map<String, Object> executeWriteWithSummary(String query, Map<String, Object> params) {
        try (var session = neo4jConnection.createSession()) {
            Result result = session.run(query, params != null ? params : Collections.emptyMap());
            SummaryCounters c = result.consume().counters();

            Map<String, Object> stats = new HashMap<>();
            stats.put("nodesCreated", c.nodesCreated());
            stats.put("nodesDeleted", c.nodesDeleted());
            stats.put("relationshipsCreated", c.relationshipsCreated());
            stats.put("propertiesSet", c.propertiesSet());
            return stats;
        }
    }
}