package com.example.cloudhuntchartbackend.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.neo4j.driver.types.Node;
import org.neo4j.driver.types.Path;
import org.neo4j.driver.types.Relationship;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NormalizedData {
    private final ObjectMapper objectMapper = new ObjectMapper();

    public String processRecords(List<Map<String, Object>> records) throws JsonProcessingException {
        List<Map<String, Object>> nodes = new ArrayList<>();
        List<Map<String, Object>> relationships = new ArrayList<>();

        for (Map<String, Object> recordMap : records) {
            processNodeOrRelationship(recordMap, nodes, relationships);
        }

        return objectMapper.writeValueAsString(createJson(nodes, relationships));
    }

    private void processNodeOrRelationship(Map<String, Object> recordMap, List<Map<String, Object>> nodes,
                                           List<Map<String, Object>> relationships) {
        for (Map.Entry<String, Object> entry : recordMap.entrySet()) {
            Object value = entry.getValue();
            if (value instanceof Node) {
                nodes.add(createNodeJson((Node) value));
            } else if (value instanceof Relationship) {
                relationships.add(createRelationshipJson((Relationship) value));
            } else if (value instanceof Path path) {
                for (Node node : path.nodes()) {
                    nodes.add(createNodeJson(node));
                }
                for (Relationship relationship : path.relationships()) {
                    relationships.add(createRelationshipJson(relationship));
                }
            }
        }
    }

    private Map<String, Object> createNodeJson(Node node) {
        Map<String, Object> nodeJson = new HashMap<>();
        nodeJson.put("name", convertNeo4jValueToString(node.get("name")));
        nodeJson.put("properties", convertProperties(node.asMap()));
        nodeJson.put("category", node.labels());
        return nodeJson;
    }

    private Map<String, Object> createRelationshipJson(Relationship relationship) {
        Map<String, Object> relationshipJson = new HashMap<>();
        relationshipJson.put("source", relationship.startNodeId());
        relationshipJson.put("name", relationship.id());
        relationshipJson.put("label", relationship.type());
        relationshipJson.put("target", relationship.endNodeId());
        return relationshipJson;
    }

    private Map<String, Object> createJson(List<Map<String, Object>> nodes, List<Map<String, Object>> relationships) {
        Map<String, Object> json = new HashMap<>();
        json.put("nodes", nodes);
        json.put("relationships", relationships);
        return json;
    }

    private String convertNeo4jValueToString(Object value) {
        if (value instanceof Number) {
            return value.toString();
        } else if (value != null) {
            return value.toString();
        } else {
            return null;
        }
    }

    private Map<String, Object> convertProperties(Map<String, Object> properties) {
        Map<String, Object> convertedProperties = new HashMap<>();
        for (Map.Entry<String, Object> entry : properties.entrySet()) {
            convertedProperties.put(entry.getKey(), convertNeo4jValueToString(entry.getValue()));
        }
        return convertedProperties;
    }
}
