package yunxun.ai.canary.backend.repository.graph.impl;

import lombok.RequiredArgsConstructor;
import org.neo4j.driver.*;
import org.springframework.stereotype.Repository;
import yunxun.ai.canary.backend.model.entity.graph.BaseNode;
import yunxun.ai.canary.backend.model.entity.graph.BaseRelationship;
import yunxun.ai.canary.backend.repository.graph.GraphRepository;

import java.util.Map;

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
            session.executeWriteWithoutResult(tx ->
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
            session.executeWriteWithoutResult(tx ->
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
            session.executeWriteWithoutResult(tx ->
                    tx.run(cypher, Map.of("id", nodeId, "properties", node.getProperties()))
            );
        }
    }

    @Override
    public void updateRelationshipProperties(String relationshipId, BaseRelationship relationship) {
        String cypher = "MATCH ()-[r {id: $id}]-() SET r += $properties";
        try (Session session = driver.session()) {
            session.executeWriteWithoutResult(tx ->
                    tx.run(cypher, Map.of("id", relationshipId, "properties", relationship.getProperties()))
            );
        }
    }

    @Override
    public void deleteNode(String nodeId) {
        String cypher = "MATCH (n {id: $id}) DETACH DELETE n";
        try (Session session = driver.session()) {
            session.executeWriteWithoutResult(tx ->
                    tx.run(cypher, Map.of("id", nodeId))
            );
        }
    }

    @Override
    public void deleteRelationship(String relationshipId) {
        String cypher = "MATCH ()-[r {id: $id}]-() DELETE r";
        try (Session session = driver.session()) {
            session.executeWriteWithoutResult(tx ->
                    tx.run(cypher, Map.of("id", relationshipId))
            );
        }
    }
}
