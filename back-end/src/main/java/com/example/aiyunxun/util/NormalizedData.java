package com.example.aiyunxun.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.neo4j.driver.types.Node;
import org.neo4j.driver.types.Path;
import org.neo4j.driver.types.Relationship;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NormalizedData {

    public static ObjectNode normalizedCypher(List<Map<String, Object>> records) {
        List<Map<String, Object>> nodes = new ArrayList<>();
        List<Map<String, Object>> relationships = new ArrayList<>();

        for (Map<String, Object> recordMap : records) {
            processNodeOrRelationship(recordMap, nodes, relationships);
        }

        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.valueToTree(createJson(nodes, relationships));
    }

    private static void processNodeOrRelationship(Map<String, Object> recordMap, List<Map<String, Object>> nodes, List<Map<String, Object>> relationships) {
        for (Map.Entry<String, Object> entry : recordMap.entrySet()) {
            Object value = entry.getValue();
            if (value instanceof Node node) {
                if (containsNode(nodes, node.id())) {
                    nodes.add(createNodeJson(node));
                }
            } else if (value instanceof Relationship relationship) {
                if (containsRelationship(relationships, relationship.id())) {
                    relationships.add(createRelationshipJson(relationship));
                }
            } else if (value instanceof Path path) {
                for (Node node : path.nodes()) {
                    if (containsNode(nodes, node.id())) {
                        nodes.add(createNodeJson(node));
                    }
                }
                for (Relationship relationship : path.relationships()) {
                    if (containsRelationship(relationships, relationship.id())) {
                        relationships.add(createRelationshipJson(relationship));
                    }
                }
            }
        }
    }

    private static boolean containsNode(List<Map<String, Object>> nodes, Long nodeId) {
        return nodes.stream().noneMatch(node -> nodeId.equals(node.get("id")));
    }

    private static boolean containsRelationship(List<Map<String, Object>> relationships, Long relationshipId) {
        return relationships.stream().noneMatch(relationship -> relationshipId.equals(relationship.get("id")));
    }


    private static Map<String, Object> createNodeJson(Node node) {
        Map<String, Object> nodeJson = new HashMap<>();
        nodeJson.put("id", node.id());
        nodeJson.put("name", convertNeo4jValueToString(node.get("name")));
        nodeJson.put("properties", convertProperties(node.asMap()));
        nodeJson.put("category", node.labels());
        return nodeJson;
    }

    private static Map<String, Object> createRelationshipJson(Relationship relationship) {
        Map<String, Object> relationshipJson = new HashMap<>();
        relationshipJson.put("source", relationship.startNodeId());
        relationshipJson.put("name", relationship.id());
        relationshipJson.put("label", relationship.type());
        relationshipJson.put("target", relationship.endNodeId());
        return relationshipJson;
    }

    private static Map<String, Object> createJson(List<Map<String, Object>> nodes, List<Map<String, Object>> relationships) {
        Map<String, Object> json = new HashMap<>();
        json.put("nodes", nodes);
        json.put("relationships", relationships);
        return json;
    }

    private static String convertNeo4jValueToString(Object value) {
        if (value instanceof Number) {
            return value.toString();
        } else if (value != null) {
            return value.toString();
        } else {
            return null;
        }
    }

    private static Map<String, Object> convertProperties(Map<String, Object> properties) {
        Map<String, Object> convertedProperties = new HashMap<>();
        for (Map.Entry<String, Object> entry : properties.entrySet()) {
            convertedProperties.put(entry.getKey(), convertNeo4jValueToString(entry.getValue()));
        }
        return convertedProperties;
    }
}
