package yunxun.ai.canary.backend.mcp.server.tool;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;
import yunxun.ai.canary.backend.db.neo4j.Neo4jGraphService;

import java.util.Map;

/**
 * Neo4j CRUD 操作工具集
 * 将 Neo4jGraphService 的方法封装为 MCP 工具，供 AI 模型调用
 * 提供节点和关系的完整 CRUD 操作以及特殊查询功能
 */
@Component
public class Neo4jCrudTool {

    private final Neo4jGraphService graphService;

    public Neo4jCrudTool(Neo4jGraphService graphService) {
        this.graphService = graphService;
    }

    // ===== 节点 CRUD 操作 =====

    /**
     * 创建节点
     */
    @Tool(name = "neo4j_create_node", description = "在 Neo4j 中创建一个节点，需要指定节点标签和属性")
    public String createNode(
            @ToolParam(description = "节点标签，如 Person、Paper、Author 等") String label,
            @ToolParam(description = "节点的属性键值对，如 {\"name\": \"张三\", \"age\": 30}") Map<String, Object> properties) {
        return graphService.createNode(label, properties);
    }

    /**
     * 删除节点
     */
    @Tool(name = "neo4j_delete_node", description = "根据节点标签和可选的属性过滤条件删除节点")
    public String deleteNode(
            @ToolParam(description = "节点标签") String label,
            @ToolParam(description = "用于过滤的属性键（可选）") String propertyKey,
            @ToolParam(description = "用于过滤的属性值（可选）") String propertyValue) {
        return graphService.deleteNode(label, propertyKey, propertyValue);
    }

    /**
     * 查询节点
     */
    @Tool(name = "neo4j_find_node", description = "根据节点标签和可选的属性过滤条件查询节点，返回 JSON 格式数据")
    public String findNode(
            @ToolParam(description = "节点标签") String label,
            @ToolParam(description = "用于过滤的属性键（可选）") String propertyKey,
            @ToolParam(description = "用于过滤的属性值（可选）") String propertyValue,
            @ToolParam(description = "返回节点的最大数量") Integer limit) {
        return graphService.findNode(label, propertyKey, propertyValue, limit);
    }

    /**
     * 更新节点
     */
    @Tool(name = "neo4j_update_node", description = "更新匹配条件的节点的属性")
    public String updateNode(
            @ToolParam(description = "节点标签") String label,
            @ToolParam(description = "用于过滤的属性键（可选）") String propertyKey,
            @ToolParam(description = "用于过滤的属性值（可选）") String propertyValue,
            @ToolParam(description = "要合并到节点的属性键值对") Map<String, Object> properties) {
        return graphService.updateNode(label, propertyKey, propertyValue, properties);
    }

    // ===== 关系 CRUD 操作 =====

    /**
     * 创建关系
     */
    @Tool(name = "neo4j_create_relationship", description = "在两个节点之间创建关系")
    public String createRelationship(
            @ToolParam(description = "起始节点的标签") String startLabel,
            @ToolParam(description = "起始节点的属性键（可选）") String startKey,
            @ToolParam(description = "起始节点的属性值（可选）") String startValue,
            @ToolParam(description = "结束节点的标签") String endLabel,
            @ToolParam(description = "结束节点的属性键（可选）") String endKey,
            @ToolParam(description = "结束节点的属性值（可选）") String endValue,
            @ToolParam(description = "关系类型，如 KNOWS、AUTHORED、CITES 等") String type,
            @ToolParam(description = "关系的属性键值对（可选）") Map<String, Object> properties) {
        return graphService.createRelationship(startLabel, startKey, startValue,
                endLabel, endKey, endValue, type, properties);
    }

    /**
     * 删除关系
     */
    @Tool(name = "neo4j_delete_relationship", description = "根据关系类型和端点过滤条件删除关系")
    public String deleteRelationship(
            @ToolParam(description = "起始节点的标签") String startLabel,
            @ToolParam(description = "起始节点的属性键（可选）") String startKey,
            @ToolParam(description = "起始节点的属性值（可选）") String startValue,
            @ToolParam(description = "结束节点的标签") String endLabel,
            @ToolParam(description = "结束节点的属性键（可选）") String endKey,
            @ToolParam(description = "结束节点的属性值（可选）") String endValue,
            @ToolParam(description = "关系类型") String type) {
        return graphService.deleteRelationship(startLabel, startKey, startValue,
                endLabel, endKey, endValue, type);
    }

    /**
     * 查询关系
     */
    @Tool(name = "neo4j_find_relationship", description = "根据关系类型和端点过滤条件查询关系，返回 JSON 格式数据。返回的数据可用于生成图谱可视化，需要转换为 {nodes: [...], edges: [...]} 格式")
    public String findRelationship(
            @ToolParam(description = "起始节点的标签") String startLabel,
            @ToolParam(description = "起始节点的属性键（可选）") String startKey,
            @ToolParam(description = "起始节点的属性值（可选）") String startValue,
            @ToolParam(description = "结束节点的标签") String endLabel,
            @ToolParam(description = "结束节点的属性键（可选）") String endKey,
            @ToolParam(description = "结束节点的属性值（可选）") String endValue,
            @ToolParam(description = "关系类型") String type,
            @ToolParam(description = "返回关系的最大数量") Integer limit) {
        return graphService.findRelationship(startLabel, startKey, startValue,
                endLabel, endKey, endValue, type, limit);
    }

    /**
     * 更新关系
     */
    @Tool(name = "neo4j_update_relationship", description = "更新匹配条件的关系的属性")
    public String updateRelationship(
            @ToolParam(description = "起始节点的标签") String startLabel,
            @ToolParam(description = "起始节点的属性键（可选）") String startKey,
            @ToolParam(description = "起始节点的属性值（可选）") String startValue,
            @ToolParam(description = "结束节点的标签") String endLabel,
            @ToolParam(description = "结束节点的属性键（可选）") String endKey,
            @ToolParam(description = "结束节点的属性值（可选）") String endValue,
            @ToolParam(description = "关系类型") String type,
            @ToolParam(description = "要合并到关系的属性键值对") Map<String, Object> properties) {
        return graphService.updateRelationship(startLabel, startKey, startValue,
                endLabel, endKey, endValue, type, properties);
    }

    // ===== 特殊查询操作 =====

    /**
     * 路径查询
     */
    @Tool(name = "neo4j_find_path", description = "查找两个节点之间的路径。支持最短路径和所有路径查询，返回 JSON 格式的路径数据")
    public String findPath(
            @ToolParam(description = "起始节点的标签") String startLabel,
            @ToolParam(description = "起始节点的属性键（可选）") String startKey,
            @ToolParam(description = "起始节点的属性值（可选）") String startValue,
            @ToolParam(description = "结束节点的标签") String endLabel,
            @ToolParam(description = "结束节点的属性键（可选）") String endKey,
            @ToolParam(description = "结束节点的属性值（可选）") String endValue,
            @ToolParam(description = "关系类型过滤（可选）") String relationshipType,
            @ToolParam(description = "最大路径深度（可选，默认 10）") Integer maxDepth,
            @ToolParam(description = "是否只返回最短路径（默认 false，返回所有路径）") Boolean shortestOnly) {
        return graphService.findPath(startLabel, startKey, startValue,
                endLabel, endKey, endValue, relationshipType, maxDepth, shortestOnly);
    }

    /**
     * 邻居查询
     */
    @Tool(name = "neo4j_find_neighbors", description = "查询指定节点的直接邻居节点和关系，返回 JSON 格式数据")
    public String findNeighbors(
            @ToolParam(description = "节点标签") String label,
            @ToolParam(description = "用于过滤的属性键（可选）") String propertyKey,
            @ToolParam(description = "用于过滤的属性值（可选）") String propertyValue,
            @ToolParam(description = "关系类型过滤（可选）") String relationshipType,
            @ToolParam(description = "返回邻居的最大数量") Integer limit) {
        return graphService.findNeighbors(label, propertyKey, propertyValue, relationshipType, limit);
    }

    /**
     * 模糊查询
     */
    @Tool(name = "neo4j_fuzzy_search", description = "基于属性值的模糊匹配查询节点。支持 CONTAINS 匹配或正则表达式匹配，返回 JSON 格式数据")
    public String fuzzySearch(
            @ToolParam(description = "节点标签") String label,
            @ToolParam(description = "要搜索的属性键") String propertyKey,
            @ToolParam(description = "搜索值") String searchValue,
            @ToolParam(description = "是否使用正则表达式（默认 false，使用 CONTAINS）") Boolean useRegex,
            @ToolParam(description = "返回节点的最大数量") Integer limit) {
        return graphService.fuzzySearch(label, propertyKey, searchValue, useRegex, limit);
    }
}

