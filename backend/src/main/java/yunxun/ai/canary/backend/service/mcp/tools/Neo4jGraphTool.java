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
 * 将 GraphRepository 的每个方法暴露为可调用的 AI 工具。
 */
@Component
public class Neo4jGraphTool {

    @Resource
    private GraphService repository;

    // ================= 注册管理 =================

    @Tool(
            name = "register_node",
            description = "注册一个新的节点类型到模型注册表中。"
    )
    public void registerNode(
            @ToolParam(description = "节点类的全限定类名，例如 yunxun.ai.canary.backend.repository.graph.entity.UserNode")
            String nodeClassName
    ) throws Exception {
        Class<?> clazz = Class.forName(nodeClassName);
        Class<? extends BaseNode> nodeClass = clazz.asSubclass(BaseNode.class);
        repository.registerNode(nodeClass);
    }

    @Tool(
            name = "register_relationship",
            description = "注册一个新的关系类型到模型注册表中。"
    )
    public void registerRelationship(
            @ToolParam(description = "关系类的全限定类名，例如 yunxun.ai.canary.backend.repository.graph.entity.FriendshipRelationship")
            String relClassName
    ) throws Exception {
        Class<?> clazz = Class.forName(relClassName);
        Class<? extends BaseRelationship> relClass = clazz.asSubclass(BaseRelationship.class);
        repository.registerRelationship(relClass);
    }


    @Tool(
            name = "unregister_node",
            description = "根据标签移除已注册的节点类型并删除所有该类型节点。"
    )
    public void unregisterNode(@ToolParam(description = "节点标签，例如 User") String label) {
        repository.unregisterNode(label);
    }

    @Tool(
            name = "unregister_relationship",
            description = "根据类型移除已注册的关系类型并删除所有该类型关系。"
    )
    public void unregisterRelationship(@ToolParam(description = "关系类型，例如 FRIEND_OF") String type) {
        repository.unregisterRelationship(type);
    }

    // ================= 节点操作 =================

    @Tool(
            name = "add_node",
            description = "添加一个新的节点到图数据库中。"
    )
    public void addNode(@ToolParam(description = "节点对象，包含 label 与属性映射") BaseNode node) {
        repository.addNode(node);
    }

    @Tool(
            name = "update_node_properties",
            description = "更新指定节点的属性。"
    )
    public void updateNodeProperties(
            @ToolParam(description = "节点 ID") String nodeId,
            @ToolParam(description = "属性更新 Map，例如 {\"name\":\"Alice\"}") Map<String, Object> updates
    ) {
        repository.updateNodeProperties(nodeId, updates);
    }

    @Tool(
            name = "delete_node",
            description = "根据节点ID删除节点及相关关系。"
    )
    public void deleteNode(@ToolParam(description = "节点ID") String nodeId) {
        repository.deleteNode(nodeId);
    }

    // ================= 关系操作 =================

    @Tool(
            name = "add_relationship",
            description = "添加一个新的关系到图数据库中。"
    )
    public void addRelationship(@ToolParam(description = "关系对象，包含类型、起始节点、终止节点、属性等") BaseRelationship rel) {
        repository.addRelationship(rel);
    }

    @Tool(
            name = "update_relationship_properties",
            description = "更新指定关系的属性。"
    )
    public void updateRelationshipProperties(
            @ToolParam(description = "关系ID") String relId,
            @ToolParam(description = "属性更新 Map，例如 {\"weight\":0.85}") Map<String, Object> updates
    ) {
        repository.updateRelationshipProperties(relId, updates);
    }

    @Tool(
            name = "delete_relationship",
            description = "根据关系ID删除该关系，不影响节点。"
    )
    public void deleteRelationship(@ToolParam(description = "关系ID") String relId) {
        repository.deleteRelationship(relId);
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
        repository.connectNodes(startNodeId, endNodeId, relationshipType, props);
    }

    // ================= 查询 =================

    @Tool(
            name = "query_by_node_label",
            description = "根据节点标签查询所有节点及其关系。"
    )
    public List<Map<String, Object>> queryByNodeLabel(
            @ToolParam(description = "节点标签，例如 User") String label
    ) {
        return repository.queryByNodeLabel(label);
    }

    @Tool(
            name = "query_by_relationship_type",
            description = "根据关系类型查询所有匹配的关系及其节点。"
    )
    public List<Map<String, Object>> queryByRelationshipType(
            @ToolParam(description = "关系类型，例如 FRIEND_OF") String type
    ) {
        return repository.queryByRelationshipType(type);
    }

    @Tool(
            name = "query_by_property",
            description = "根据属性值模糊匹配查询节点和关系。"
    )
    public List<Map<String, Object>> queryByProperty(
            @ToolParam(description = "属性名，例如 name") String key,
            @ToolParam(description = "模糊匹配值，例如 'Ali'") String valuePattern
    ) {
        return repository.queryByProperty(key, valuePattern);
    }
}
