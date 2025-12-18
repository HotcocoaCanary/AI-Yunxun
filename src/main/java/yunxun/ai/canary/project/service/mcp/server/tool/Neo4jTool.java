package yunxun.ai.canary.project.service.mcp.server.tool;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;
import yunxun.ai.canary.project.repository.neo4j.Neo4jOperation;
import yunxun.ai.canary.project.service.mcp.server.tool.model.ToolResponse;
import yunxun.ai.canary.project.service.mcp.server.tool.model.ToolResponses;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Neo4j CRUD 操作工具集
 * 将 Neo4jGraphService 的方法封装为 MCP 工具，供 AI 模型调用
 * 提供节点和关系的完整 CRUD 操作以及特殊查询功能
 */
@Component
public class Neo4jTool {

    private static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(5);

    private final Neo4jOperation neo4jOperation;

    public Neo4jTool(Neo4jOperation neo4jOperation) {
        this.neo4jOperation = neo4jOperation;
    }

    @Tool(name = "neo4j_create_node", description = "创建 Neo4j 节点（label + properties）")
    public ToolResponse neo4jCreateNode(
            @ToolParam(required = true, description = "节点标签（如 Entity/Document）") String label,
            @ToolParam(required = true, description = "节点属性（建议包含 name/type 等）") Map<String, Object> properties) {
        Instant startedAt = Instant.now();
        String traceId = UUID.randomUUID().toString();
        try {
            Map<String, Object> node = neo4jOperation.createNode(label, properties).block(DEFAULT_TIMEOUT);
            return ToolResponses.ok(node, traceId, startedAt);
        }
        catch (Exception ex) {
            return ToolResponses.error("DB_ERROR", ex.getMessage(), null, traceId, startedAt);
        }
    }

    @Tool(name = "neo4j_find_node", description = "查找 Neo4j 节点（优先按 id；否则 label+property+value）")
    public ToolResponse neo4jFindNode(
            @ToolParam(required = false, description = "节点业务 id") String id,
            @ToolParam(required = false, description = "节点标签") String label,
            @ToolParam(required = false, description = "属性名") String property,
            @ToolParam(required = false, description = "属性值") String value) {
        Instant startedAt = Instant.now();
        String traceId = UUID.randomUUID().toString();
        try {
            Map<String, Object> node;
            if (id != null && !id.isBlank()) {
                node = neo4jOperation.findNodeById(id).block(DEFAULT_TIMEOUT);
            }
            else if (label != null && !label.isBlank() && property != null && !property.isBlank() && value != null) {
                node = neo4jOperation.findNodeByProperty(label, property, value).block(DEFAULT_TIMEOUT);
            }
            else {
                return ToolResponses.error("INVALID_ARGUMENT", "需要提供 id 或 (label, property, value)", null, traceId, startedAt);
            }

            if (node == null) {
                return ToolResponses.error("NOT_FOUND", "未找到节点", null, traceId, startedAt);
            }
            return ToolResponses.ok(node, traceId, startedAt);
        }
        catch (Exception ex) {
            return ToolResponses.error("DB_ERROR", ex.getMessage(), null, traceId, startedAt);
        }
    }

    @Tool(name = "neo4j_update_node", description = "更新 Neo4j 节点（patch 方式）")
    public ToolResponse neo4jUpdateNode(
            @ToolParam(required = true, description = "节点业务 id") String id,
            @ToolParam(required = true, description = "patch 对象") Map<String, Object> patch) {
        Instant startedAt = Instant.now();
        String traceId = UUID.randomUUID().toString();
        try {
            boolean updated = Boolean.TRUE.equals(neo4jOperation.updateNode(id, patch).block(DEFAULT_TIMEOUT));
            return ToolResponses.ok(Map.of("updated", updated), traceId, startedAt);
        }
        catch (Exception ex) {
            return ToolResponses.error("DB_ERROR", ex.getMessage(), null, traceId, startedAt);
        }
    }

    @Tool(name = "neo4j_delete_node", description = "删除 Neo4j 节点")
    public ToolResponse neo4jDeleteNode(
            @ToolParam(required = true, description = "节点业务 id") String id,
            @ToolParam(required = false, description = "是否 DETACH，默认 true") Boolean detach) {
        Instant startedAt = Instant.now();
        String traceId = UUID.randomUUID().toString();
        try {
            boolean deleted = Boolean.TRUE.equals(neo4jOperation.deleteNode(id, detach == null || detach).block(DEFAULT_TIMEOUT));
            return ToolResponses.ok(Map.of("deleted", deleted), traceId, startedAt);
        }
        catch (Exception ex) {
            return ToolResponses.error("DB_ERROR", ex.getMessage(), null, traceId, startedAt);
        }
    }

    @Tool(name = "neo4j_create_relationship", description = "创建 Neo4j 关系（fromId/toId/type/properties）")
    public ToolResponse neo4jCreateRelationship(
            @ToolParam(required = true, description = "起点节点 id") String fromId,
            @ToolParam(required = true, description = "终点节点 id") String toId,
            @ToolParam(required = true, description = "关系类型（如 RELATED_TO/MENTIONS）") String type,
            @ToolParam(required = false, description = "关系属性") Map<String, Object> properties) {
        Instant startedAt = Instant.now();
        String traceId = UUID.randomUUID().toString();
        try {
            Map<String, Object> rel = neo4jOperation.createRelationship(fromId, toId, type, properties).block(DEFAULT_TIMEOUT);
            return ToolResponses.ok(rel, traceId, startedAt);
        }
        catch (Exception ex) {
            return ToolResponses.error("DB_ERROR", ex.getMessage(), null, traceId, startedAt);
        }
    }

    @Tool(name = "neo4j_find_relationship", description = "查找 Neo4j 关系（按关系 id）")
    public ToolResponse neo4jFindRelationship(@ToolParam(required = true, description = "关系 id") String id) {
        Instant startedAt = Instant.now();
        String traceId = UUID.randomUUID().toString();
        try {
            Map<String, Object> rel = neo4jOperation.findRelationship(id).block(DEFAULT_TIMEOUT);
            if (rel == null) {
                return ToolResponses.error("NOT_FOUND", "未找到关系", null, traceId, startedAt);
            }
            return ToolResponses.ok(rel, traceId, startedAt);
        }
        catch (Exception ex) {
            return ToolResponses.error("DB_ERROR", ex.getMessage(), null, traceId, startedAt);
        }
    }

    @Tool(name = "neo4j_update_relationship", description = "更新 Neo4j 关系（patch 方式）")
    public ToolResponse neo4jUpdateRelationship(
            @ToolParam(required = true, description = "关系 id") String id,
            @ToolParam(required = true, description = "patch 对象") Map<String, Object> patch) {
        Instant startedAt = Instant.now();
        String traceId = UUID.randomUUID().toString();
        try {
            boolean updated = Boolean.TRUE.equals(neo4jOperation.updateRelationship(id, patch).block(DEFAULT_TIMEOUT));
            return ToolResponses.ok(Map.of("updated", updated), traceId, startedAt);
        }
        catch (Exception ex) {
            return ToolResponses.error("DB_ERROR", ex.getMessage(), null, traceId, startedAt);
        }
    }

    @Tool(name = "neo4j_delete_relationship", description = "删除 Neo4j 关系")
    public ToolResponse neo4jDeleteRelationship(@ToolParam(required = true, description = "关系 id") String id) {
        Instant startedAt = Instant.now();
        String traceId = UUID.randomUUID().toString();
        try {
            boolean deleted = Boolean.TRUE.equals(neo4jOperation.deleteRelationship(id).block(DEFAULT_TIMEOUT));
            return ToolResponses.ok(Map.of("deleted", deleted), traceId, startedAt);
        }
        catch (Exception ex) {
            return ToolResponses.error("DB_ERROR", ex.getMessage(), null, traceId, startedAt);
        }
    }

    @Tool(name = "neo4j_find_path", description = "查找两节点间路径（shortestPath）")
    public ToolResponse neo4jFindPath(
            @ToolParam(required = true, description = "起点节点 id") String fromId,
            @ToolParam(required = true, description = "终点节点 id") String toId,
            @ToolParam(required = false, description = "最大深度，默认 4，最大 8") Integer maxDepth,
            @ToolParam(required = false, description = "关系类型过滤列表") List<String> types) {
        Instant startedAt = Instant.now();
        String traceId = UUID.randomUUID().toString();
        try {
            int depth = maxDepth == null ? 4 : maxDepth;
            Map<String, Object> path = neo4jOperation.findPath(fromId, toId, depth, types).block(DEFAULT_TIMEOUT);
            if (path == null) {
                return ToolResponses.error("NOT_FOUND", "未找到路径", null, traceId, startedAt);
            }
            return ToolResponses.ok(path, traceId, startedAt);
        }
        catch (Exception ex) {
            return ToolResponses.error("DB_ERROR", ex.getMessage(), null, traceId, startedAt);
        }
    }

    @Tool(name = "neo4j_find_neighbors", description = "查找节点邻居（指定深度与关系类型）")
    public ToolResponse neo4jFindNeighbors(
            @ToolParam(required = true, description = "节点 id") String id,
            @ToolParam(required = false, description = "深度，默认 1，最大 3") Integer depth,
            @ToolParam(required = false, description = "关系类型过滤列表") List<String> types) {
        Instant startedAt = Instant.now();
        String traceId = UUID.randomUUID().toString();
        try {
            int d = depth == null ? 1 : depth;
            Map<String, Object> neighbors = neo4jOperation.findNeighbors(id, d, types).block(DEFAULT_TIMEOUT);
            if (neighbors == null) {
                return ToolResponses.error("NOT_FOUND", "未找到邻居", null, traceId, startedAt);
            }
            return ToolResponses.ok(neighbors, traceId, startedAt);
        }
        catch (Exception ex) {
            return ToolResponses.error("DB_ERROR", ex.getMessage(), null, traceId, startedAt);
        }
    }

    @Tool(name = "neo4j_fuzzy_search", description = "模糊搜索实体（基于 name 属性 contains 匹配）")
    public ToolResponse neo4jFuzzySearch(
            @ToolParam(required = true, description = "查询词") String query,
            @ToolParam(required = false, description = "labels 过滤") List<String> labels,
            @ToolParam(required = false, description = "返回条数，默认 10，最大 20") Integer limit) {
        Instant startedAt = Instant.now();
        String traceId = UUID.randomUUID().toString();
        try {
            int lim = limit == null ? 10 : Math.min(limit, 20);
            List<Map<String, Object>> items = neo4jOperation.fuzzySearch(query, labels, lim).block(DEFAULT_TIMEOUT);
            return ToolResponses.ok(Map.of("items", items == null ? List.of() : items), traceId, startedAt);
        }
        catch (Exception ex) {
            return ToolResponses.error("DB_ERROR", ex.getMessage(), null, traceId, startedAt);
        }
    }
}
