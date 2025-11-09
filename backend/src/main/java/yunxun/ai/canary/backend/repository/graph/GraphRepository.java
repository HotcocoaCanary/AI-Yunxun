package yunxun.ai.canary.backend.repository.graph;

import org.neo4j.driver.Driver;
import org.neo4j.driver.Session;
import org.neo4j.driver.Result;
import org.neo4j.driver.Record;
import org.neo4j.driver.Value;
import org.neo4j.driver.Values;
import org.springframework.stereotype.Repository;
import yunxun.ai.canary.backend.model.entity.graph.BaseNode;
import yunxun.ai.canary.backend.model.entity.graph.BaseRelationship;

import java.util.*;

/**
 * 通用图数据库操作接口：
 * 提供节点和关系的增删改查（CRUD）能力。
 * 支持动态属性、灵活的条件查询、事务控制。
 */
@Repository
public class GraphRepository {

    private final Driver driver;

    public GraphRepository(Driver driver) {
        this.driver = driver;
    }

    // =============================
    // 🔹 节点操作部分
    // =============================

    /**
     * 创建节点（若ID存在则更新）
     */
    public void createOrUpdateNode(BaseNode node) {
        try (Session session = driver.session()) {
            session.executeWrite(tx -> {
                String cypher = "MERGE (n:" + node.getLabel() + " {id: $id}) " +
                        "SET n += $properties";
                Map<String, Object> params = new HashMap<>();
                params.put("id", node.getId());
                params.put("properties", node.getProperties());
                tx.run(cypher, params);
                return null;
            });
        }
    }

    /**
     * 查询节点（根据标签 + ID）
     */
    public Optional<BaseNode> findNodeById(String label, String id) {
        try (Session session = driver.session()) {
            return session.executeRead(tx -> {
                String cypher = "MATCH (n:" + label + " {id: $id}) RETURN n";
                Result result = tx.run(cypher, Values.parameters("id", id));
                if (result.hasNext()) {
                    Record record = result.next();
                    Value nodeValue = record.get("n");
                    BaseNode node = new BaseNode(label) {};
                    node.setId(nodeValue.get("id").asString());
                    node.setProperties(nodeValue.asMap());
                    return Optional.of(node);
                }
                return Optional.empty();
            });
        }
    }

    /**
     * 根据属性条件查询节点
     */
    public List<BaseNode> findNodesByProperty(String label, String key, Object value) {
        try (Session session = driver.session()) {
            return session.executeRead(tx -> {
                String cypher = "MATCH (n:" + label + ") WHERE n." + key + " = $value RETURN n";
                Result result = tx.run(cypher, Values.parameters("value", value));
                List<BaseNode> nodes = new ArrayList<>();
                while (result.hasNext()) {
                    Record record = result.next();
                    Value nodeValue = record.get("n");
                    BaseNode node = new BaseNode(label) {};
                    node.setId(nodeValue.get("id").asString());
                    node.setProperties(nodeValue.asMap());
                    nodes.add(node);
                }
                return nodes;
            });
        }
    }

    /**
     * 删除节点（可选是否级联删除关系）
     */
    public void deleteNodeById(String label, String id, boolean detach) {
        try (Session session = driver.session()) {
            session.executeWrite(tx -> {
                String cypher = (detach ? "MATCH (n:" + label + " {id: $id}) DETACH DELETE n"
                        : "MATCH (n:" + label + " {id: $id}) DELETE n");
                tx.run(cypher, Values.parameters("id", id));
                return null;
            });
        }
    }

    // =============================
    // 🔹 关系操作部分
    // =============================

    /**
     * 创建关系（若已存在则更新属性）
     */
    public void createOrUpdateRelationship(BaseRelationship rel) {
        try (Session session = driver.session()) {
            session.executeWrite(tx -> {
                String cypher = String.format(
                        "MATCH (a:%s {id: $startId}), (b:%s {id: $endId}) " +
                                "MERGE (a)-[r:%s {id: $id}]->(b) " +
                                "SET r += $properties",
                        rel.getStartNode().getLabel(),
                        rel.getEndNode().getLabel(),
                        rel.getLabel()
                );
                Map<String, Object> params = new HashMap<>();
                params.put("id", rel.getId());
                params.put("startId", rel.getStartNode().getId());
                params.put("endId", rel.getEndNode().getId());
                params.put("properties", rel.getProperties());
                tx.run(cypher, params);
                return null;
            });
        }
    }

    /**
     * 查询关系（根据ID）
     */
    public Optional<BaseRelationship> findRelationshipById(String label, String id) {
        try (Session session = driver.session()) {
            return session.executeRead(tx -> {
                String cypher = "MATCH ()-[r:" + label + " {id: $id}]->() RETURN r";
                Result result = tx.run(cypher, Values.parameters("id", id));
                if (result.hasNext()) {
                    Record record = result.next();
                    Value relValue = record.get("r");
                    BaseRelationship rel = new BaseRelationship(label, null, null) {};
                    rel.setId(relValue.get("id").asString());
                    rel.setProperties(relValue.asMap());
                    return Optional.of(rel);
                }
                return Optional.empty();
            });
        }
    }

    /**
     * 删除关系（根据ID）
     */
    public void deleteRelationshipById(String label, String id) {
        try (Session session = driver.session()) {
            session.executeWrite(tx -> {
                String cypher = "MATCH ()-[r:" + label + " {id: $id}]->() DELETE r";
                tx.run(cypher, Values.parameters("id", id));
                return null;
            });
        }
    }

    // =============================
    // 🔹 通用查询方法
    // =============================

    /**
     * 执行自定义 Cypher 查询
     * 返回 List<Map> 形式结果，适合动态查询
     */
    public List<Map<String, Object>> runCustomQuery(String cypher, Map<String, Object> params) {
        try (Session session = driver.session()) {
            return session.executeRead(tx -> {
                Result result = tx.run(cypher, params);
                List<Map<String, Object>> list = new ArrayList<>();
                while (result.hasNext()) {
                    Record record = result.next();
                    list.add(record.asMap());
                }
                return list;
            });
        }
    }
}
