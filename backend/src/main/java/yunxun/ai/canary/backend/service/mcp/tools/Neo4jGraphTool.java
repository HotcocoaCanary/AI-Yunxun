package yunxun.ai.canary.backend.service.mcp.tools;

import jakarta.annotation.Resource;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;
import yunxun.ai.canary.backend.model.entity.graph.BaseNode;
import yunxun.ai.canary.backend.model.entity.graph.BaseRelationship;
import yunxun.ai.canary.backend.service.graph.GraphService;

import java.util.List;
import java.util.Map;

/**
 * Neo4j 数据库操作工具（MCP Tool）
 * 将 GraphService 的每个方法暴露为可调用的 AI 工具。
 */
@Component
public class Neo4jGraphTool {

    @Resource
    private GraphService graphService;

    // ================= 注册管理 =================

    @Tool(
            name = "register_node",
            description = "注册一个新的节点类型到模型注册表中。"
    )
    public void registerNode(
            @ToolParam(description = "节点类的全限定类名，例如 yunxun.ai.canary.backend.model.entity.graph.UserNode")
            String nodeClassName
    ) throws Exception {
        Class<?> clazz = Class.forName(nodeClassName);
        Class<? extends BaseNode> nodeClass = clazz.asSubclass(BaseNode.class);
        graphService.registerNode(nodeClass);
    }

    @Tool(
            name = "register_relationship",
            description = "注册一个新的关系类型到模型注册表中。"
    )
    public void registerRelationship(
            @ToolParam(description = "关系类的全限定类名，例如 yunxun.ai.canary.backend.model.entity.graph.FriendshipRelationship")
            String relClassName
    ) throws Exception {
        Class<?> clazz = Class.forName(relClassName);
        Class<? extends BaseRelationship> relClass = clazz.asSubclass(BaseRelationship.class);
        graphService.registerRelationship(relClass);
    }

    @Tool(
            name = "unregister_node",
            description = "根据标签移除已注册的节点类型并删除所有该类型节点。"
    )
    public void unregisterNode(@ToolParam(description = "节点标签，例如 User") String label) {
        graphService.unregisterNode(label);
    }

    @Tool(
            name = "unregister_relationship",
            description = "根据类型移除已注册的关系类型并删除所有该类型关系。"
    )
    public void unregisterRelationship(@ToolParam(description = "关系类型，例如 FRIEND_OF") String type) {
        graphService.unregisterRelationship(type);
    }

    // ================= 节点操作 =================

    @Tool(
            name = "add_node",
            description = "添加一个新的节点到图数据库中。"
    )
    public void addNode(
            @ToolParam(description = "节点标签，例如 'User'") String label,
            @ToolParam(description = "节点属性，例如 {\"name\":\"Alice\", \"age\":25}") Map<String, Object> properties
    ) {
        BaseNode node = new BaseNode(label) {};
        node.setProperties(properties);
        graphService.addNode(node);
    }

    @Tool(
            name = "update_node_properties",
            description = "更新指定节点的属性。"
    )
    public void updateNodeProperties(
            @ToolParam(description = "节点 ID") String nodeId,
            @ToolParam(description = "属性更新 Map，例如 {\"name\":\"Alice\"}") Map<String, Object> updates
    ) {
        graphService.updateNodeProperties(nodeId, updates);
    }

    @Tool(
            name = "delete_node",
            description = "根据节点ID删除节点及相关关系。"
    )
    public void deleteNode(@ToolParam(description = "节点ID") String nodeId) {
        graphService.deleteNode(nodeId);
    }

    // ================= 关系操作 =================

    @Tool(
            name = "add_relationship",
            description = "添加一个新的关系到图数据库中。"
    )
    public void addRelationship(
            @ToolParam(description = "关系类型，例如 FRIEND_OF") String label,
            @ToolParam(description = "起始节点ID") String startNodeId,
            @ToolParam(description = "目标节点ID") String endNodeId,
            @ToolParam(description = "关系属性 Map") Map<String, Object> properties
    ) {
        BaseNode startNode = new BaseNode("TempStart") {{ setId(startNodeId); }};
        BaseNode endNode = new BaseNode("TempEnd") {{ setId(endNodeId); }};
        BaseRelationship rel = new BaseRelationship(label, startNode, endNode) {};
        rel.setProperties(properties);
        graphService.addRelationship(rel);
    }

    @Tool(
            name = "update_relationship_properties",
            description = "更新指定关系的属性。"
    )
    public void updateRelationshipProperties(
            @ToolParam(description = "关系ID") String relId,
            @ToolParam(description = "属性更新 Map，例如 {\"weight\":0.85}") Map<String, Object> updates
    ) {
        graphService.updateRelationshipProperties(relId, updates);
    }

    @Tool(
            name = "delete_relationship",
            description = "根据关系ID删除该关系，不影响节点。"
    )
    public void deleteRelationship(@ToolParam(description = "关系ID") String relId) {
        graphService.deleteRelationship(relId);
    }

    // ================= 节点连接 =================

    @Tool(
            name = "connect_nodes",
            description = "在两个节点之间创建或合并一个关系（若节点不存在则自动创建）。"
    )
    public void connectNodes(
            @ToolParam(description = "起始节点ID") String startNodeId,
            @ToolParam(description = "目标节点ID") String endNodeId,
            @ToolParam(description = "关系类型，例如 FRIEND_OF") String relationshipType,
            @ToolParam(description = "关系属性 Map，例如 {\"since\":2020}") Map<String, Object> props
    ) {
        graphService.connectNodes(startNodeId, endNodeId, relationshipType, props);
    }

    // ================= 查询 =================

    @Tool(
            name = "query_by_node_label",
            description = "根据节点标签查询所有节点及其关系。"
    )
    public List<Map<String, Object>> queryByNodeLabel(
            @ToolParam(description = "节点标签，例如 User") String label
    ) {
        return graphService.queryByNodeLabel(label);
    }

    @Tool(
            name = "query_by_relationship_label",
            description = "根据关系类型查询所有匹配的关系及其节点。"
    )
    public List<Map<String, Object>> queryByRelationshipLabel(
            @ToolParam(description = "关系类型，例如 FRIEND_OF") String label
    ) {
        return graphService.queryByRelationshipLabel(label);
    }

    @Tool(
            name = "query_by_property",
            description = "根据属性键值对匹配节点或关系。"
    )
    public List<Map<String, Object>> queryByProperty(
            @ToolParam(description = "属性键值对，例如 {\"name\":\"Alice\"}") Map<String, Object> keyValue
    ) {
        return graphService.queryByProperty(keyValue);
    }

    // ================= 工具检查 =================

    @Tool(
            name = "is_node_registered",
            description = "检查某个节点类型是否已注册。"
    )
    public boolean isNodeRegistered(@ToolParam(description = "节点标签，例如 User") String label) {
        return graphService.isNodeRegistered(label);
    }

    @Tool(
            name = "is_relationship_registered",
            description = "检查某个关系类型是否已注册。"
    )
    public boolean isRelationshipRegistered(@ToolParam(description = "关系类型，例如 FRIEND_OF") String type) {
        return graphService.isRelationshipRegistered(type);
    }
}
