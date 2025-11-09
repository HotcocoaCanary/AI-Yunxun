package yunxun.ai.canary.backend.service.mcp.tools;

import jakarta.annotation.Resource;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;
import yunxun.ai.canary.backend.model.entity.graph.BaseNode;
import yunxun.ai.canary.backend.model.entity.graph.BaseRelationship;
import yunxun.ai.canary.backend.repository.graph.GraphRepository;

import java.util.*;

/**
 * ✅ GraphTool
 * 提供图数据库节点与关系的增删改查功能
 * 采用 Spring AI Tool API（方法级别 @Tool + 参数级别 @ToolParam）
 */
@Component
public class GraphTool {

    @Resource
    private GraphRepository graphRepository;

    // =============================
    // 🔹 节点操作
    // =============================

    @Tool(name = "create_or_update_node", description = "创建或更新一个节点")
    public String createOrUpdateNode(
            @ToolParam(description = "节点唯一ID") String id,
            @ToolParam(description = "节点标签，例如 User, Product") String label,
            @ToolParam(description = "节点属性Map（可为空）") Map<String, Object> properties
    ) {
        BaseNode node = new BaseNode(label) {};
        node.setId(id);
        if (properties != null) node.setProperties(properties);
        graphRepository.createOrUpdateNode(node);
        return "✅ 节点 [" + label + ":" + id + "] 创建或更新成功。";
    }

    @Tool(name = "find_node_by_id", description = "根据标签与ID查询节点")
    public Object findNodeById(
            @ToolParam(description = "节点标签") String label,
            @ToolParam(description = "节点ID") String id
    ) {
        return graphRepository.findNodeById(label, id)
                .map(BaseNode::getProperties)
                .orElse(Map.of("message", "❌ 未找到节点: " + label + ":" + id));
    }

    @Tool(name = "find_nodes_by_property", description = "根据属性查询节点")
    public Object findNodesByProperty(
            @ToolParam(description = "节点标签") String label,
            @ToolParam(description = "属性键") String key,
            @ToolParam(description = "属性值") Object value
    ) {
        List<BaseNode> nodes = graphRepository.findNodesByProperty(label, key, value);
        if (nodes.isEmpty()) {
            return Map.of("message", "❌ 未找到符合条件的节点");
        }
        List<Map<String, Object>> result = new ArrayList<>();
        for (BaseNode n : nodes) {
            Map<String, Object> map = new HashMap<>(n.getProperties());
            map.put("_id", n.getId());
            result.add(map);
        }
        return result;
    }

    @Tool(name = "delete_node_by_id", description = "删除节点（可选择是否级联删除关系）")
    public String deleteNodeById(
            @ToolParam(description = "节点标签") String label,
            @ToolParam(description = "节点ID") String id,
            @ToolParam(description = "是否级联删除关系") boolean detach
    ) {
        graphRepository.deleteNodeById(label, id, detach);
        return "🗑️ 节点 [" + label + ":" + id + "] 已删除 (detach=" + detach + ")";
    }

    // =============================
    // 🔹 关系操作
    // =============================

    @Tool(name = "create_or_update_relationship", description = "创建或更新关系")
    public String createOrUpdateRelationship(
            @ToolParam(description = "关系唯一ID") String id,
            @ToolParam(description = "关系标签，例如 USE, FRIEND_WITH") String label,
            @ToolParam(description = "起始节点标签") String startLabel,
            @ToolParam(description = "起始节点ID") String startId,
            @ToolParam(description = "终止节点标签") String endLabel,
            @ToolParam(description = "终止节点ID") String endId,
            @ToolParam(description = "关系属性Map（可为空）") Map<String, Object> properties
    ) {
        BaseNode start = new BaseNode(startLabel) {{ setId(startId); }};
        BaseNode end = new BaseNode(endLabel) {{ setId(endId); }};
        BaseRelationship rel = new BaseRelationship(label, start, end) {};
        rel.setId(id);
        if (properties != null) rel.setProperties(properties);
        graphRepository.createOrUpdateRelationship(rel);
        return "✅ 关系 [" + label + ":" + id + "] 创建或更新成功。";
    }

    @Tool(name = "find_relationship_by_id", description = "根据ID查询关系")
    public Object findRelationshipById(
            @ToolParam(description = "关系标签") String label,
            @ToolParam(description = "关系ID") String id
    ) {
        return graphRepository.findRelationshipById(label, id)
                .map(BaseRelationship::getProperties)
                .orElse(Map.of("message", "❌ 未找到关系: " + label + ":" + id));
    }

    @Tool(name = "delete_relationship_by_id", description = "根据ID删除关系")
    public String deleteRelationshipById(
            @ToolParam(description = "关系标签") String label,
            @ToolParam(description = "关系ID") String id
    ) {
        graphRepository.deleteRelationshipById(label, id);
        return "🗑️ 关系 [" + label + ":" + id + "] 已删除。";
    }

    // =============================
    // 🔹 通用查询
    // =============================

    @Tool(name = "run_cypher_query", description = "执行自定义 Cypher 查询")
    public List<Map<String, Object>> runCypherQuery(
            @ToolParam(description = "Cypher 查询语句") String query,
            @ToolParam(description = "查询参数Map（可为空）") Map<String, Object> params
    ) {
        if (params == null) params = new HashMap<>();
        return graphRepository.runCustomQuery(query, params);
    }
}
