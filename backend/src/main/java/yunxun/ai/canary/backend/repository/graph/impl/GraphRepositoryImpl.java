package yunxun.ai.canary.backend.repository.graph.impl;

import lombok.RequiredArgsConstructor;
import org.neo4j.driver.*;
import org.neo4j.driver.Record;
import org.springframework.stereotype.Repository;
import yunxun.ai.canary.backend.model.entity.graph.BaseNode;
import yunxun.ai.canary.backend.model.entity.graph.BaseRelationship;
import yunxun.ai.canary.backend.repository.graph.GraphRepository;

import java.lang.reflect.Constructor;
import java.util.*;

@Repository
@RequiredArgsConstructor
public class GraphRepositoryImpl implements GraphRepository {

    private final Driver driver;

    @Override
    public void addNode(BaseNode node) {
        String cypher = String.format(
                "MERGE (n:%s {id: $id}) SET n += $properties",
                node.getLabel()
        );
        try (Session session = driver.session()) {
            session.executeWrite(tx ->
                    tx.run(cypher, Map.of("id", node.getId(), "properties", node.getProperties()))
            );
        }
    }

    @Override
    public void addRelationship(BaseRelationship relationship) {
        String cypher = String.format(
                "MERGE (a:%s {id: $startId}) " +
                        "MERGE (b:%s {id: $endId}) " +
                        "MERGE (a)-[r:%s {id: $relId}]->(b) " +
                        "SET r += $properties",
                relationship.getStartNode().getLabel(),
                relationship.getEndNode().getLabel(),
                relationship.getLabel()
        );
        try (Session session = driver.session()) {
            session.executeWrite(tx ->
                    tx.run(cypher, Map.of(
                            "startId", relationship.getStartNode().getId(),
                            "endId", relationship.getEndNode().getId(),
                            "relId", relationship.getId(),
                            "properties", relationship.getProperties()
                    ))
            );
        }
    }

    @Override
    public void updateNodeProperties(String nodeId, BaseNode node) {
        String cypher = "MATCH (n {id: $id}) SET n += $properties";
        try (Session session = driver.session()) {
            session.executeWrite(tx ->
                    tx.run(cypher, Map.of("id", nodeId, "properties", node.getProperties()))
            );
        }
    }

    @Override
    public void updateRelationshipProperties(String relationshipId, BaseRelationship relationship) {
        String cypher = "MATCH ()-[r {id: $id}]-() SET r += $properties";
        try (Session session = driver.session()) {
            session.executeWrite(tx ->
                    tx.run(cypher, Map.of("id", relationshipId, "properties", relationship.getProperties()))
            );
        }
    }

    @Override
    public void deleteNode(String nodeId) {
        String cypher = "MATCH (n {id: $id}) DETACH DELETE n";
        try (Session session = driver.session()) {
            session.executeWrite(tx -> tx.run(cypher, Map.of("id", nodeId)));
        }
    }

    @Override
    public void deleteRelationship(String relationshipId) {
        String cypher = "MATCH ()-[r {id: $id}]-() DELETE r";
        try (Session session = driver.session()) {
            session.executeWrite(tx -> tx.run(cypher, Map.of("id", relationshipId)));
        }
    }

    @Override
    public List<BaseNode> getAllNodes() {
        String cypher = "MATCH (n) RETURN labels(n) AS labels, n.id AS id, properties(n) AS props";
        List<BaseNode> nodes = new ArrayList<>();
        try (Session session = driver.session()) {
            session.executeRead(tx -> {
                Result result = tx.run(cypher);
                while (result.hasNext()) {
                    Record record = result.next();
                    String label = record.get("labels").asList(Value::asString).get(0);
                    BaseNode node = createNodeInstance(label);
                    if (node == null) continue;
                    node.setId(record.get("id").asString());
                    node.setProperties(record.get("props").asMap());
                    nodes.add(node);
                }
                return null;
            });
        }
        return nodes;
    }

    @Override
    public List<BaseRelationship> getAllRelationships() {
        String cypher = """
                MATCH (a)-[r]->(b)
                RETURN type(r) AS type, r.id AS id, properties(r) AS props,
                       a.id AS startId, labels(a)[0] AS startLabel,
                       b.id AS endId, labels(b)[0] AS endLabel
                """;
        List<BaseRelationship> relationships = new ArrayList<>();

        try (Session session = driver.session()) {
            session.executeRead(tx -> {
                Result result = tx.run(cypher);
                while (result.hasNext()) {
                    Record record = result.next();
                    String relType = record.get("type").asString();
                    String startLabel = record.get("startLabel").asString();
                    String endLabel = record.get("endLabel").asString();

                    BaseNode start = createNodeInstance(startLabel);
                    BaseNode end = createNodeInstance(endLabel);
                    if (start == null || end == null) continue;

                    start.setId(record.get("startId").asString());
                    end.setId(record.get("endId").asString());

                    BaseRelationship rel = createRelationshipInstance(relType, start, end);
                    if (rel == null) continue;
                    rel.setId(record.get("id").asString());
                    rel.setProperties(record.get("props").asMap());
                    relationships.add(rel);
                }
                return null;
            });
        }
        return relationships;
    }

    @Override
    public BaseNode getBaseNodeByNodeId(String nodeId) {
        String cypher = "MATCH (n {id: $id}) RETURN labels(n) AS labels, properties(n) AS props";
        try (Session session = driver.session()) {
            return session.executeRead(tx -> {
                Result result = tx.run(cypher, Map.of("id", nodeId));
                if (!result.hasNext()) return null;

                Record record = result.next();
                String label = record.get("labels").asList(Value::asString).get(0);
                BaseNode node = createNodeInstance(label);
                if (node == null) return null;
                node.setId(nodeId);
                node.setProperties(record.get("props").asMap());
                return node;
            });
        }
    }

    @Override
    public BaseRelationship getBaseRelationshipByRelationshipId(String relationshipId) {
        String cypher = """
                MATCH (a)-[r {id: $id}]->(b)
                RETURN type(r) AS type, properties(r) AS props,
                       a.id AS startId, labels(a)[0] AS startLabel,
                       b.id AS endId, labels(b)[0] AS endLabel
                """;
        try (Session session = driver.session()) {
            return session.executeRead(tx -> {
                Result result = tx.run(cypher, Map.of("id", relationshipId));
                if (!result.hasNext()) return null;

                Record record = result.next();
                String relType = record.get("type").asString();
                BaseNode start = createNodeInstance(record.get("startLabel").asString());
                BaseNode end = createNodeInstance(record.get("endLabel").asString());
                if (start == null || end == null) return null;

                start.setId(record.get("startId").asString());
                end.setId(record.get("endId").asString());

                BaseRelationship rel = createRelationshipInstance(relType, start, end);
                if (rel == null) return null;

                rel.setId(relationshipId);
                rel.setProperties(record.get("props").asMap());
                return rel;
            });
        }
    }

    @Override
    public List<Map<String, Object>> runCypherQuery(String cypher) {
        List<Map<String, Object>> results = new ArrayList<>();
        try (Session session = driver.session()) {
            session.executeRead(tx -> {
                Result result = tx.run(cypher);
                while (result.hasNext()) {
                    Record record = result.next();
                    Map<String, Object> row = new HashMap<>();
                    for (String key : record.keys()) {
                        row.put(key, record.get(key).asObject());
                    }
                    results.add(row);
                }
                return null;
            });
        }
        return results;
    }

    // ------------------------
    // 🔍 反射实例化子类
    // ------------------------

    private BaseNode createNodeInstance(String label) {
        try {
            // 自动推断子类类名，例如 yunxun.ai.canary.backend.model.entity.graph.node.UserNode
            String className = "yunxun.ai.canary.backend.model.entity.graph.node." + label + "Node";
            Class<?> clazz = Class.forName(className);
            Constructor<?> ctor = clazz.getDeclaredConstructor();
            ctor.setAccessible(true);
            return (BaseNode) ctor.newInstance();
        } catch (Exception e) {
            // 若找不到对应子类，则返回 null（可换成日志输出）
            return null;
        }
    }

    private BaseRelationship createRelationshipInstance(String label, BaseNode start, BaseNode end) {
        try {
            String className = "yunxun.ai.canary.backend.model.entity.graph.relationship." + label + "Relationship";
            Class<?> clazz = Class.forName(className);
            Constructor<?> ctor = clazz.getDeclaredConstructor(BaseNode.class, BaseNode.class);
            ctor.setAccessible(true);
            return (BaseRelationship) ctor.newInstance(start, end);
        } catch (Exception e) {
            return null;
        }
    }
}
