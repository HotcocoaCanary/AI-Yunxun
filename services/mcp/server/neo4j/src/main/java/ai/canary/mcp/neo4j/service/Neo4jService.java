package ai.canary.mcp.neo4j.service;

import ai.canary.mcp.neo4j.config.Neo4jConfig;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.neo4j.driver.*;
import org.neo4j.driver.types.Node;
import org.neo4j.driver.types.Relationship;
import org.neo4j.driver.types.Path;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class Neo4jService {

    private final Neo4jConfig neo4jConfig;
    private Driver driver;

    @PostConstruct
    public void init() {
        driver = GraphDatabase.driver(
                neo4jConfig.getUri(),
                AuthTokens.basic(neo4jConfig.getUsername(), neo4jConfig.getPassword())
        );
    }

    @PreDestroy
    public void close() {
        if (driver != null) {
            driver.close();
        }
    }

    public List<Map<String, Object>> executeCypher(String cypher) {
        try (Session session = driver.session()) {
            Result result = session.run(cypher);
            return getMaps(result);
        }
    }

    @NonNull
    private List<Map<String, Object>> getMaps(Result result) {
        List<Map<String, Object>> records = new ArrayList<>();

        while (result.hasNext()) {
            org.neo4j.driver.Record record = result.next();
            Map<String, Object> recordMap = new HashMap<>();
            record.keys().forEach(key -> {
                Value value = record.get(key);
                recordMap.put(key, convertValue(value));
            });
            records.add(recordMap);
        }

        return records;
    }

    public List<Map<String, Object>> queryNodeWithRelationships(String nodeId) {
        String cypher = "MATCH (n) WHERE id(n) = $nodeId " +
                       "OPTIONAL MATCH (n)-[r]-(related) " +
                       "RETURN n, r, related";
        try (Session session = driver.session()) {
            Result result = session.run(cypher, Values.parameters("nodeId", Long.parseLong(nodeId)));
            return convertResultToMap(result);
        }
    }

    public List<Map<String, Object>> findPath(String startNodeId, String endNodeId) {
        String cypher = "MATCH path = shortestPath((start)-[*]-(end)) " +
                       "WHERE id(start) = $startId AND id(end) = $endId " +
                       "RETURN path";
        try (Session session = driver.session()) {
            Result result = session.run(cypher, 
                Values.parameters("startId", Long.parseLong(startNodeId), 
                                 "endId", Long.parseLong(endNodeId)));
            return convertResultToMap(result);
        }
    }

    public List<Map<String, Object>> matchPattern(String pattern) {
        String cypher = "MATCH " + pattern + " RETURN *";
        try (Session session = driver.session()) {
            Result result = session.run(cypher);
            return convertResultToMap(result);
        }
    }

    private List<Map<String, Object>> convertResultToMap(Result result) {
        return getMaps(result);
    }

    private Object convertValue(Value value) {
        if (value.isNull()) {
            return null;
        }
        return switch (value.type().name()) {
            case "NODE" -> convertNode(value.asNode());
            case "RELATIONSHIP" -> convertRelationship(value.asRelationship());
            case "PATH" -> convertPath(value.asPath());
            case "LIST" -> value.asList(this::convertValue);
            case "MAP" -> value.asMap(this::convertValue);
            default -> value.asObject();
        };
    }

    private Map<String, Object> convertNode(Node node) {
        Map<String, Object> nodeMap = new HashMap<>();
        nodeMap.put("id", node.id());
        nodeMap.put("labels", node.labels());
        nodeMap.put("properties", node.asMap());
        return nodeMap;
    }

    private Map<String, Object> convertRelationship(Relationship rel) {
        Map<String, Object> relMap = new HashMap<>();
        relMap.put("id", rel.id());
        relMap.put("type", rel.type());
        relMap.put("startNodeId", rel.startNodeId());
        relMap.put("endNodeId", rel.endNodeId());
        relMap.put("properties", rel.asMap());
        return relMap;
    }

    private List<Map<String, Object>> convertPath(Path path) {
        List<Map<String, Object>> pathList = new ArrayList<>();
        path.nodes().forEach(node -> pathList.add(convertNode(node)));
        path.relationships().forEach(rel -> pathList.add(convertRelationship(rel)));
        return pathList;
    }
}

