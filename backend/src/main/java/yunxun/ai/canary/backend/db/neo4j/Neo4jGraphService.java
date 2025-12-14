package yunxun.ai.canary.backend.db.neo4j;

import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Neo4j graph operations service that implements constrained CRUD-like
 * operations on nodes and relationships.
 *
 * MCP tools should delegate to this service instead of embedding Cypher logic.
 */
@Service
public class Neo4jGraphService {

    private final Neo4jClient neo4jClient;
    private final Neo4jQueryService neo4jQueryService;

    public Neo4jGraphService(Neo4jClient neo4jClient, Neo4jQueryService neo4jQueryService) {
        this.neo4jClient = neo4jClient;
        this.neo4jQueryService = neo4jQueryService;
    }

    // ===== Node CRUD =====

    public String createNode(String label, Map<String, Object> properties) {
        if (label == null || label.isBlank()) {
            return "Label is required.";
        }
        Map<String, Object> props = properties != null ? new HashMap<>(properties) : new HashMap<>();
        // Ensure every node has an 'id' property
        props.putIfAbsent("id", UUID.randomUUID().toString());

        neo4jClient.query("CREATE (n:`" + label + "`) SET n += $props")
                .bind(props).to("props")
                .run();
        return "Created node with label " + label + " and id " + props.get("id") + ".";
    }

    public String deleteNode(String label, String propertyKey, String propertyValue) {
        if (label == null || label.isBlank()) {
            return "Label is required.";
        }
        StringBuilder cypher = new StringBuilder("MATCH (n:`").append(label).append("`)");
        boolean hasFilter = propertyKey != null && !propertyKey.isBlank();
        if (hasFilter) {
            cypher.append(" WHERE n.").append(propertyKey).append(" = $value");
        }
        cypher.append(" DETACH DELETE n");
        Neo4jClient.RunnableSpec runnable = neo4jClient.query(cypher.toString());
        if (hasFilter) {
            runnable = ((Neo4jClient.UnboundRunnableSpec) runnable).bind(propertyValue).to("value");
        }
        long deleted = runnable.run().counters().nodesDeleted();
        return "Deleted " + deleted + " nodes.";
    }

    public String findNode(String label, String propertyKey, String propertyValue, Integer limit) {
        if (label == null || label.isBlank()) {
            return "Label is required.";
        }
        StringBuilder cypher = new StringBuilder("MATCH (n:`").append(label).append("`)");
        if (propertyKey != null && !propertyKey.isBlank()) {
            cypher.append(" WHERE n.").append(propertyKey).append(" = $value");
        }
        cypher.append(" RETURN n");
        if (limit != null && limit > 0) {
            cypher.append(" LIMIT ").append(limit);
        }
        // For simplicity we do not bind the value here; agents should avoid unsafe values.
        return neo4jQueryService.runQueryAsJson(cypher.toString());
    }

    public String updateNode(String label,
                             String propertyKey,
                             String propertyValue,
                             Map<String, Object> properties) {
        if (label == null || label.isBlank()) {
            return "Label is required.";
        }
        StringBuilder cypher = new StringBuilder("MATCH (n:`").append(label).append("`)");
        boolean hasFilter = propertyKey != null && !propertyKey.isBlank();
        if (hasFilter) {
            cypher.append(" WHERE n.").append(propertyKey).append(" = $value");
        }
        cypher.append(" SET n += $props");
        Neo4jClient.RunnableSpec runnable = neo4jClient.query(cypher.toString());
        if (hasFilter) {
            runnable = ((Neo4jClient.UnboundRunnableSpec) runnable).bind(propertyValue).to("value");
        }
        runnable = runnable.bind(properties != null ? properties : Map.of()).to("props");
        long updated = runnable.run().counters().propertiesSet();
        return "Updated node properties, total properties set: " + updated + ".";
    }

    // ===== Relationship CRUD =====

    public String createRelationship(String startLabel,
                                     String startKey,
                                     String startValue,
                                     String endLabel,
                                     String endKey,
                                     String endValue,
                                     String type,
                                     Map<String, Object> properties) {
        if (startLabel == null || endLabel == null || type == null
                || startLabel.isBlank() || endLabel.isBlank() || type.isBlank()) {
            return "startLabel, endLabel and type are required.";
        }
        StringBuilder cypher = new StringBuilder(
                "MATCH (s:`" + startLabel + "`), (e:`" + endLabel + "`)");
        boolean hasStart = startKey != null && !startKey.isBlank();
        boolean hasEnd = endKey != null && !endKey.isBlank();
        if (hasStart) {
            cypher.append(" WHERE s.").append(startKey).append(" = $s_value");
        }
        if (hasEnd) {
            cypher.append(hasStart ? " AND" : " WHERE");
            cypher.append(" e.").append(endKey).append(" = $e_value");
        }
        cypher.append(" CREATE (s)-[r:`").append(type).append("`]->(e) SET r += $props");

        Map<String, Object> relProps = properties != null ? new HashMap<>(properties) : new HashMap<>();
        relProps.putIfAbsent("id", UUID.randomUUID().toString());

        Neo4jClient.RunnableSpec runnable = neo4jClient.query(cypher.toString())
                .bind(relProps).to("props");
        if (hasStart) {
            runnable = ((Neo4jClient.UnboundRunnableSpec) runnable).bind(startValue).to("s_value");
        }
        if (hasEnd) {
            runnable = ((Neo4jClient.UnboundRunnableSpec) runnable).bind(endValue).to("e_value");
        }
        runnable.run();
        return "Created relationship of type " + type + " with id " + relProps.get("id") + ".";
    }

    public String deleteRelationship(String startLabel,
                                     String startKey,
                                     String startValue,
                                     String endLabel,
                                     String endKey,
                                     String endValue,
                                     String type) {
        if (startLabel == null || endLabel == null || type == null
                || startLabel.isBlank() || endLabel.isBlank() || type.isBlank()) {
            return "startLabel, endLabel and type are required.";
        }
        StringBuilder cypher = new StringBuilder(
                "MATCH (s:`" + startLabel + "`)-[r:`" + type + "`]->(e:`" + endLabel + "`)");
        boolean hasStart = startKey != null && !startKey.isBlank();
        boolean hasEnd = endKey != null && !endKey.isBlank();
        if (hasStart) {
            cypher.append(" WHERE s.").append(startKey).append(" = $s_value");
        }
        if (hasEnd) {
            cypher.append(hasStart ? " AND" : " WHERE");
            cypher.append(" e.").append(endKey).append(" = $e_value");
        }
        cypher.append(" DELETE r");
        Neo4jClient.RunnableSpec runnable = neo4jClient.query(cypher.toString());
        if (hasStart) {
            runnable = ((Neo4jClient.UnboundRunnableSpec) runnable).bind(startValue).to("s_value");
        }
        if (hasEnd) {
            runnable = ((Neo4jClient.UnboundRunnableSpec) runnable).bind(endValue).to("e_value");
        }
        long deleted = runnable.run().counters().relationshipsDeleted();
        return "Deleted " + deleted + " relationships.";
    }

    public String findRelationship(String startLabel,
                                   String startKey,
                                   String startValue,
                                   String endLabel,
                                   String endKey,
                                   String endValue,
                                   String type,
                                   Integer limit) {
        if (startLabel == null || endLabel == null || type == null
                || startLabel.isBlank() || endLabel.isBlank() || type.isBlank()) {
            return "startLabel, endLabel and type are required.";
        }
        StringBuilder cypher = new StringBuilder(
                "MATCH (s:`" + startLabel + "`)-[r:`" + type + "`]->(e:`" + endLabel + "`)");
        boolean hasStart = startKey != null && !startKey.isBlank();
        boolean hasEnd = endKey != null && !endKey.isBlank();
        if (hasStart) {
            cypher.append(" WHERE s.").append(startKey).append(" = $s_value");
        }
        if (hasEnd) {
            cypher.append(hasStart ? " AND" : " WHERE");
            cypher.append(" e.").append(endKey).append(" = $e_value");
        }
        cypher.append(" RETURN s, r, e");
        if (limit != null && limit > 0) {
            cypher.append(" LIMIT ").append(limit);
        }
        return neo4jQueryService.runQueryAsJson(cypher.toString());
    }

    public String updateRelationship(String startLabel,
                                     String startKey,
                                     String startValue,
                                     String endLabel,
                                     String endKey,
                                     String endValue,
                                     String type,
                                     Map<String, Object> properties) {
        if (startLabel == null || endLabel == null || type == null
                || startLabel.isBlank() || endLabel.isBlank() || type.isBlank()) {
            return "startLabel, endLabel and type are required.";
        }
        StringBuilder cypher = new StringBuilder(
                "MATCH (s:`" + startLabel + "`)-[r:`" + type + "`]->(e:`" + endLabel + "`)");
        boolean hasStart = startKey != null && !startKey.isBlank();
        boolean hasEnd = endKey != null && !endKey.isBlank();
        if (hasStart) {
            cypher.append(" WHERE s.").append(startKey).append(" = $s_value");
        }
        if (hasEnd) {
            cypher.append(hasStart ? " AND" : " WHERE");
            cypher.append(" e.").append(endKey).append(" = $e_value");
        }
        cypher.append(" SET r += $props");
        Neo4jClient.RunnableSpec runnable = neo4jClient.query(cypher.toString())
                .bind(properties != null ? properties : Map.of()).to("props");
        if (hasStart) {
            runnable = ((Neo4jClient.UnboundRunnableSpec) runnable).bind(startValue).to("s_value");
        }
        if (hasEnd) {
            runnable = ((Neo4jClient.UnboundRunnableSpec) runnable).bind(endValue).to("e_value");
        }
        long updated = runnable.run().counters().propertiesSet();
        return "Updated relationship properties, total properties set: " + updated + ".";
    }

    // ===== Special Query Operations =====

    /**
     * 查找两个节点之间的路径
     * 支持最短路径和所有路径查询
     */
    public String findPath(String startLabel,
                          String startKey,
                          String startValue,
                          String endLabel,
                          String endKey,
                          String endValue,
                          String relationshipType,
                          Integer maxDepth,
                          Boolean shortestOnly) {
        if (startLabel == null || endLabel == null
                || startLabel.isBlank() || endLabel.isBlank()) {
            return "startLabel and endLabel are required.";
        }

        StringBuilder cypher = new StringBuilder();
        if (Boolean.TRUE.equals(shortestOnly)) {
            // 最短路径查询
            cypher.append("MATCH path = shortestPath((s:`").append(startLabel)
                    .append("`)-[*");
            if (maxDepth != null && maxDepth > 0) {
                cypher.append("..").append(maxDepth);
            }
            if (relationshipType != null && !relationshipType.isBlank()) {
                cypher.append(":`").append(relationshipType).append("`");
            }
            cypher.append("]-(e:`").append(endLabel).append("`))");
        } else {
            // 所有路径查询
            cypher.append("MATCH path = (s:`").append(startLabel).append("`)-[*");
            if (maxDepth != null && maxDepth > 0) {
                cypher.append("..").append(maxDepth);
            } else {
                cypher.append("1..10"); // 默认最大深度 10
            }
            if (relationshipType != null && !relationshipType.isBlank()) {
                cypher.append(":`").append(relationshipType).append("`");
            }
            cypher.append("]-(e:`").append(endLabel).append("`))");
        }

        boolean hasStart = startKey != null && !startKey.isBlank();
        boolean hasEnd = endKey != null && !endKey.isBlank();
        if (hasStart || hasEnd) {
            cypher.append(" WHERE");
            if (hasStart) {
                cypher.append(" s.").append(startKey).append(" = $s_value");
            }
            if (hasEnd) {
                if (hasStart) {
                    cypher.append(" AND");
                }
                cypher.append(" e.").append(endKey).append(" = $e_value");
            }
        }

        cypher.append(" RETURN path");
        if (maxDepth != null && maxDepth > 0 && !Boolean.TRUE.equals(shortestOnly)) {
            cypher.append(" LIMIT 50"); // 限制返回路径数量
        }

        Neo4jClient.RunnableSpec runnable = neo4jClient.query(cypher.toString());
        if (hasStart) {
            runnable = ((Neo4jClient.UnboundRunnableSpec) runnable).bind(startValue).to("s_value");
        }
        if (hasEnd) {
            runnable = ((Neo4jClient.UnboundRunnableSpec) runnable).bind(endValue).to("e_value");
        }

        return neo4jQueryService.runQueryAsJson(cypher.toString());
    }

    /**
     * 查找节点的邻居节点和关系
     */
    public String findNeighbors(String label,
                               String propertyKey,
                               String propertyValue,
                               String relationshipType,
                               Integer limit) {
        if (label == null || label.isBlank()) {
            return "Label is required.";
        }

        StringBuilder cypher = new StringBuilder("MATCH (n:`").append(label).append("`)");
        if (propertyKey != null && !propertyKey.isBlank()) {
            cypher.append(" WHERE n.").append(propertyKey).append(" = $value");
        }
        cypher.append(" MATCH (n)-[r");
        if (relationshipType != null && !relationshipType.isBlank()) {
            cypher.append(":`").append(relationshipType).append("`");
        }
        cypher.append("]-(neighbor) RETURN n, r, neighbor");
        if (limit != null && limit > 0) {
            cypher.append(" LIMIT ").append(limit);
        }

        Neo4jClient.RunnableSpec runnable = neo4jClient.query(cypher.toString());
        if (propertyKey != null && !propertyKey.isBlank()) {
            runnable = ((Neo4jClient.UnboundRunnableSpec) runnable).bind(propertyValue).to("value");
        }

        return neo4jQueryService.runQueryAsJson(cypher.toString());
    }

    /**
     * 模糊查询节点
     * 支持基于属性值的模糊匹配（使用 CONTAINS 或正则表达式）
     */
    public String fuzzySearch(String label,
                             String propertyKey,
                             String searchValue,
                             Boolean useRegex,
                             Integer limit) {
        if (label == null || label.isBlank()) {
            return "Label is required.";
        }
        if (propertyKey == null || propertyKey.isBlank()) {
            return "Property key is required for fuzzy search.";
        }
        if (searchValue == null || searchValue.isBlank()) {
            return "Search value is required.";
        }

        StringBuilder cypher = new StringBuilder("MATCH (n:`").append(label).append("`) WHERE");
        if (Boolean.TRUE.equals(useRegex)) {
            // 使用正则表达式匹配
            cypher.append(" n.").append(propertyKey).append(" =~ $pattern");
        } else {
            // 使用 CONTAINS 进行模糊匹配
            cypher.append(" n.").append(propertyKey).append(" CONTAINS $search");
        }
        cypher.append(" RETURN n");
        if (limit != null && limit > 0) {
            cypher.append(" LIMIT ").append(limit);
        }

        Neo4jClient.RunnableSpec runnable = neo4jClient.query(cypher.toString());
        if (Boolean.TRUE.equals(useRegex)) {
            // 为正则表达式添加通配符
            String pattern = ".*" + searchValue.replaceAll("([\\\\+*?\\[\\](){}|.^$])", "\\\\$1") + ".*";
            runnable = ((Neo4jClient.UnboundRunnableSpec) runnable).bind(pattern).to("pattern");
        } else {
            runnable = ((Neo4jClient.UnboundRunnableSpec) runnable).bind(searchValue).to("search");
        }

        return neo4jQueryService.runQueryAsJson(cypher.toString());
    }
}

