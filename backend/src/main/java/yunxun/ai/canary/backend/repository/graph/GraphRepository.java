package yunxun.ai.canary.backend.repository.graph;

import yunxun.ai.canary.backend.model.entity.graph.BaseNode;
import yunxun.ai.canary.backend.model.entity.graph.BaseRelationship;

public interface GraphRepository {

    // 添加一个节点
    void addNode(BaseNode node);

    // 添加一个关系（节点不存在则创建）
    void addRelationship(BaseRelationship relationship);

    // 修改节点属性
    void updateNodeProperties(String nodeId, BaseNode node);

    // 修改关系属性
    void updateRelationshipProperties(String relationshipId, BaseRelationship relationship);

    // 删除节点（同时删除相关关系）
    void deleteNode(String nodeId);

    // 删除关系（仅删除此关系）
    void deleteRelationship(String relationshipId);
}
