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

    // ==== 模型注册管理 ====

    @Override
    @Transactional
    public void registerNode(Class<? extends BaseNode> nodeClass) {
        String label = nodeClass.getSimpleName();
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
        String label = relClass.getSimpleName();
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

    // ==== 节点操作 ====

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
        BaseNode temp = new BaseNode("Temp") {};
        temp.setProperties(updates);
        graphRepository.updateNodeProperties(nodeId, temp);
    }

    @Override
    @Transactional
    public void deleteNode(String nodeId) {
        graphRepository.deleteNode(nodeId);
        // 这里不直接知道 label，可在上层业务中传入 label 来同步更新计数
    }

    // ==== 关系操作 ====

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
        BaseRelationship temp = new BaseRelationship("Temp", null, null) {};
        temp.setProperties(updates);
        graphRepository.updateRelationshipProperties(relationshipId, temp);
    }

    @Override
    @Transactional
    public void deleteRelationship(String relationshipId) {
        graphRepository.deleteRelationship(relationshipId);
    }

    // ==== 高级操作 ====

    @Override
    @Transactional
    public void connectNodes(String startNodeId, String endNodeId, String relationshipType, Map<String, Object> props) {
        BaseNode startNode = new BaseNode("TempStart") {{ setId(startNodeId); }};
        BaseNode endNode = new BaseNode("TempEnd") {{ setId(endNodeId); }};
        BaseRelationship rel = new BaseRelationship(relationshipType, startNode, endNode) {};
        rel.setProperties(props);
        addRelationship(rel);
    }

    // ==== 查询 ====
    // 这里假设未来实现，可通过 Neo4j 查询；当前先空实现。
    @Override
    public List<Map<String, Object>> queryByNodeLabel(String label) {
        return Collections.emptyList();
    }

    @Override
    public List<Map<String, Object>> queryByRelationshipType(String type) {
        return Collections.emptyList();
    }

    @Override
    public List<Map<String, Object>> queryByProperty(String key, String valuePattern) {
        return Collections.emptyList();
    }

    // ==== 工具 ====

    @Override
    public boolean isNodeRegistered(String label) {
        return nodeRegisterRepository.findByLabelAndDeletedFalse(label).isPresent();
    }

    @Override
    public boolean isRelationshipRegistered(String type) {
        return relationshipRegisterRepository.findByLabelAndDeletedFalse(type).isPresent();
    }
}
