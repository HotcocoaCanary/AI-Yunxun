package yunxun.ai.canary.backend.service.graph.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import yunxun.ai.canary.backend.model.entity.graph.BaseNode;
import yunxun.ai.canary.backend.model.entity.graph.BaseRelationship;
import yunxun.ai.canary.backend.model.entity.mysql.NodeRegister;
import yunxun.ai.canary.backend.model.entity.mysql.RelationshipRegister;
import yunxun.ai.canary.backend.repository.graph.GraphRepository;
import yunxun.ai.canary.backend.repository.mysql.NodeRegisterRepository;
import yunxun.ai.canary.backend.repository.mysql.RelationshipRegisterRepository;
import yunxun.ai.canary.backend.service.graph.GraphService;

import java.util.*;

@Service
@RequiredArgsConstructor
public class GraphServiceImpl implements GraphService {

    private final GraphRepository graphRepository;
    private final NodeRegisterRepository nodeRegisterRepository;
    private final RelationshipRegisterRepository relationshipRegisterRepository;


    // ========================
    // 工具
    // ========================

    @Override
    public boolean isNodeRegistered(String label) {
        return nodeRegisterRepository.findByLabelAndDeletedFalse(label).isPresent();
    }

    @Override
    public boolean isRelationshipRegistered(String type) {
        return relationshipRegisterRepository.findByLabelAndDeletedFalse(type).isPresent();
    }

    @Override
    public BaseNode getNodeById(String nodeId) {
        return graphRepository.getBaseNodeByNodeId(nodeId);
    }

    @Override
    public BaseRelationship getRelationshipById(String relationshipId) {
        return graphRepository.getBaseRelationshipByRelationshipId(relationshipId);
    }

    // ========================
    // 模型注册管理
    // ========================

    @Override
    @Transactional
    public void registerNode(Class<? extends BaseNode> nodeClass) {
        String label = nodeClass.getSimpleName().replace("Node", "");
        nodeRegisterRepository.findByLabelAndDeletedFalse(label)
                .orElseGet(() -> nodeRegisterRepository.save(
                        NodeRegister.builder()
                                .label(label)
                                .count(0L)
                                .deleted(false)
                                .build()
                ));
    }

    @Override
    @Transactional
    public void registerRelationship(Class<? extends BaseRelationship> relClass) {
        String label = relClass.getSimpleName().replace("Relationship", "");
        relationshipRegisterRepository.findByLabelAndDeletedFalse(label)
                .orElseGet(() -> relationshipRegisterRepository.save(
                        RelationshipRegister.builder()
                                .label(label)
                                .count(0L)
                                .deleted(false)
                                .build()
                ));
    }

    @Override
    @Transactional
    public void unregisterNode(String label) {
        nodeRegisterRepository.softDeleteByLabel(label);
    }

    @Override
    @Transactional
    public void unregisterRelationship(String label) {
        relationshipRegisterRepository.softDeleteByLabel(label);
    }

    // ========================
    // 节点操作
    // ========================

    @Override
    @Transactional
    public void addNode(BaseNode node) {
        graphRepository.addNode(node);

        String label = node.getLabel();
        NodeRegister reg = nodeRegisterRepository.findByLabelAndDeletedFalse(label)
                .orElseGet(() -> nodeRegisterRepository.save(
                        NodeRegister.builder()
                                .label(label)
                                .count(0L)
                                .deleted(false)
                                .build()
                ));

        nodeRegisterRepository.updateCountByLabel(label, reg.getCount() + 1);
    }

    @Override
    @Transactional
    public void updateNodeProperties(String nodeId, Map<String, Object> updates) {
        if (updates == null || updates.isEmpty()) return;
        BaseNode temp = new BaseNode("Temp") {};
        temp.setProperties(updates);
        graphRepository.updateNodeProperties(nodeId, temp);
    }

    @Override
    @Transactional
    public void deleteNode(String nodeId) {
        graphRepository.deleteNode(nodeId);
        // 可扩展：通过 getNodeById(nodeId) 获取 label 并更新注册计数
        BaseNode node = getNodeById(nodeId);
        if (node != null) {
            nodeRegisterRepository.updateCountByLabel(node.getLabel(), nodeRegisterRepository.findByLabelAndDeletedFalse(node.getLabel()).get().getCount() - 1);
        }
    }

    // ========================
    // 关系操作
    // ========================

    @Override
    @Transactional
    public void addRelationship(BaseRelationship relationship) {
        graphRepository.addRelationship(relationship);

        String label = relationship.getLabel();
        RelationshipRegister reg = relationshipRegisterRepository.findByLabelAndDeletedFalse(label)
                .orElseGet(() -> relationshipRegisterRepository.save(
                        RelationshipRegister.builder()
                                .label(label)
                                .count(0L)
                                .deleted(false)
                                .build()
                ));

        relationshipRegisterRepository.updateCountByLabel(label, reg.getCount() + 1);
    }

    @Override
    @Transactional
    public void updateRelationshipProperties(String relationshipId, Map<String, Object> updates) {
        if (updates == null || updates.isEmpty()) return;
        BaseRelationship temp = new BaseRelationship("Temp", null, null) {};
        temp.setProperties(updates);
        graphRepository.updateRelationshipProperties(relationshipId, temp);
    }

    @Override
    @Transactional
    public void deleteRelationship(String relationshipId) {
        graphRepository.deleteRelationship(relationshipId);
    }

    // ========================
    // 高级操作
    // ========================

    @Override
    @Transactional
    public void connectNodes(String startNodeId, String endNodeId, String relationshipType, Map<String, Object> props) {
        BaseNode startNode = new BaseNode("TempStart") {{ setId(startNodeId); }};
        BaseNode endNode = new BaseNode("TempEnd") {{ setId(endNodeId); }};
        BaseRelationship rel = new BaseRelationship(relationshipType, startNode, endNode) {};
        if (props != null) rel.setProperties(props);
        addRelationship(rel);
    }

    // ========================
    // 查询
    // ========================

    @Override
    public List<Map<String, Object>> queryByNodeLabel(String label) {
        String cypher = String.format("MATCH (n:%s) RETURN n", label);
        return graphRepository.runCypherQuery(cypher);
    }

    @Override
    public List<Map<String, Object>> queryByRelationshipLabel(String label) {
        String cypher = String.format("MATCH ()-[r:%s]->() RETURN r", label);
        return graphRepository.runCypherQuery(cypher);
    }

    @Override
    public List<Map<String, Object>> queryByProperty(Map<String, Object> keyValue) {
        if (keyValue == null || keyValue.isEmpty()) return Collections.emptyList();

        // 拼接 WHERE 条件
        StringBuilder whereClause = new StringBuilder();
        List<String> keys = new ArrayList<>(keyValue.keySet());
        for (int i = 0; i < keys.size(); i++) {
            whereClause.append("n.").append(keys.get(i)).append(" = $").append(keys.get(i));
            if (i < keys.size() - 1) whereClause.append(" AND ");
        }

        String cypher = String.format("MATCH (n) WHERE %s RETURN n", whereClause);
        return graphRepository.runCypherQuery(cypher);
    }
}
