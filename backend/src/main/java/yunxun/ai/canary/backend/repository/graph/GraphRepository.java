package yunxun.ai.canary.backend.repository.graph;

import yunxun.ai.canary.backend.model.entity.graph.BaseNode;
import yunxun.ai.canary.backend.model.entity.graph.BaseRelationship;

import java.util.List;
import java.util.Map;

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

    // 获取所有节点
    List<BaseNode> getAllNodes();

    // 获取所有关系
    List<BaseRelationship> getAllRelationships();

    // 获取指定节点
    BaseNode getBaseNodeByNodeId(String nodeId);

    // 获取指定关系
    BaseRelationship getBaseRelationshipByRelationshipId(String relationshipId);

    // 运行 Cypher查询
    List<Map<String, Object>> runCypherQuery(String cypher);
}
