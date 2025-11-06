package yunxun.ai.canary.backend.service.graph;

import yunxun.ai.canary.backend.model.entity.graph.BaseNode;
import yunxun.ai.canary.backend.model.entity.graph.BaseRelationship;

import java.util.List;
import java.util.Map;

public interface GraphService {

    // ==== 模型注册管理 ====
    void registerNode(Class<? extends BaseNode> nodeClass);
    void registerRelationship(Class<? extends BaseRelationship> relClass);
    void unregisterNode(String label);
    void unregisterRelationship(String type);

    // ==== 节点操作 ====
    void addNode(BaseNode node);
    void updateNodeProperties(String nodeId, Map<String, Object> updates);
    void deleteNode(String nodeId); // 删除节点及相关关系

    // ==== 关系操作 ====
    void addRelationship(BaseRelationship relationship);
    void updateRelationshipProperties(String relationshipId, Map<String, Object> updates);
    void deleteRelationship(String relationshipId); // 只删除关系，不删除节点

    // ==== 高级操作 ====
    void connectNodes(String startNodeId, String endNodeId, String relationshipType, Map<String, Object> props);

    // ==== 查询 ====
    List<Map<String, Object>> queryByNodeLabel(String label);
    List<Map<String, Object>> queryByRelationshipType(String type);
    List<Map<String, Object>> queryByProperty(String key, String valuePattern);

    // ==== 工具 ====
    boolean isNodeRegistered(String label);
    boolean isRelationshipRegistered(String type);
}
