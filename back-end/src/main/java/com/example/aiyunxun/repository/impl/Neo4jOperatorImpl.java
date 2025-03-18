package com.example.aiyunxun.repository.impl;

import com.example.aiyunxun.db.Neo4jConnector;
import com.example.aiyunxun.repository.Neo4jOperator;
import com.example.aiyunxun.entity.Node;
import com.example.aiyunxun.entity.Relationship;
import org.neo4j.driver.*;
import org.neo4j.driver.Record;
import org.neo4j.driver.summary.ResultSummary;
import org.neo4j.driver.types.MapAccessor;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.stream.Collectors;

@Repository
public class Neo4jOperatorImpl implements Neo4jOperator {

    @Override
    public void createNode(String label, Map<String, Object> properties) {
        // 检查属性中是否包含id
        if (!properties.containsKey("id")) {
            throw new IllegalArgumentException("Node properties must contain 'id'");
        }
        // 构造CQL语句
        String cql = "CREATE (n:" + label + " $props)";
        try (Session session = Neo4jConnector.getDriver().session()) {
            // 执行CQL语句
            session.run(cql, Values.parameters("props", properties));
        } catch (Exception e) {
            throw new RuntimeException("创建节点失败", e);
        }
    }

    @Override
    public void createNode(Map<String, List<Node>> nodes) {
        for(Map.Entry<String, List<Node>> entry : nodes.entrySet()) {
            String label = entry.getKey();
            List<Node> nodeList = entry.getValue();
            for (Node node : nodeList) {
                createNode(label, node.getProperties());
            }
        }
    }

    @Override
    public void deleteNode(String label, Map<String, Object> properties) {
        String cql;
        // 如果属性为空，删除所有该label的节点
        if (properties == null || properties.isEmpty()) {
            // 如果属性为空，删除所有该label的节点
            cql = String.format("MATCH (n:%s) DELETE n", label);
        } else {
            // 如果属性不为空，根据属性删除节点
            String conditions = properties.keySet().stream()
                    .map(k -> "n." + k + " = $" + k)
                    .collect(Collectors.joining(" AND "));
            cql = String.format("MATCH (n:%s) WHERE %s DELETE n", label, conditions);
        }
        try (Session session = Neo4jConnector.getDriver().session()) {
            ResultSummary result = session.run(cql, properties).consume();
        } catch (Exception e) {
            throw new RuntimeException("删除节点失败", e);
        }
    }

    @Override
    public void updateNode(String label, Map<String, Object> properties) {
        if (!properties.containsKey("id")) {
            throw new IllegalArgumentException("Node properties must contain 'id'");
        }

        String setClause = properties.keySet().stream()
                .filter(k -> !k.equals("id"))
                .map(k -> "n." + k + " = $" + k)
                .collect(Collectors.joining(", "));

        if (setClause.isEmpty()) {
            return;
        }
        String cql = String.format("MATCH (n:%s {id: $id}) SET %s", label, setClause);
        try (Session session = Neo4jConnector.getDriver().session()) {
            session.run(cql, properties);
        } catch (Exception e) {
            throw new RuntimeException("更新节点失败", e);
        }
    }

    @Override
    public List<Node> queryNode(String label, Map<String, Object> properties) {
        String cql;
        if (properties == null || properties.isEmpty()) {
            // 如果属性为空，查询所有该label的节点
            cql = String.format("MATCH (n:%s) RETURN n", label);
        } else {
            // 如果属性不为空，根据属性查询节点
            String conditions = properties.keySet().stream()
                    .map(k -> "n." + k + " = $" + k)
                    .collect(Collectors.joining(" AND "));
            cql = String.format("MATCH (n:%s) WHERE %s RETURN n", label, conditions);
        }
        try (Session session = Neo4jConnector.getDriver().session()) {
            Result result = session.run(cql, properties);
            List<Node> nodes = new ArrayList<>();
            while (result.hasNext()) {
                Record record = result.next();
                org.neo4j.driver.types.Node neo4jNode = record.get("n").asNode();
                Node node = convertDriverNode(neo4jNode);
                nodes.add(node);
            }
            return nodes;
        } catch (Exception e) {
            throw new RuntimeException("查询节点失败", e);
        }
    }

    @Override
    public void createRelation(String relationType, Node startNode, Node endNode, Map<String, Object> properties) {
        String cql = String.format(
                "MATCH (a:%s {id: $startId}), (b:%s {id: $endId}) " +
                        "CREATE (a)-[r:%s $props]->(b)",
                startNode.getLabel(), endNode.getLabel(), relationType
        );
        Map<String, Object> params = new HashMap<>();
        params.put("startId", startNode.getProperties().get("id"));
        params.put("endId", endNode.getProperties().get("id"));
        params.put("props", properties != null ? properties : Collections.emptyMap());

        try (Session session = Neo4jConnector.getDriver().session()) {
            session.run(cql, params);
        } catch (Exception e) {
            throw new RuntimeException("创建关系失败", e);
        }
    }

    @Override
    public void createRelation(Map<String, List<Relationship>> relationships) {
        for (Map.Entry<String, List<Relationship>> entry : relationships.entrySet()) {
            String relationType = entry.getKey();
            List<Relationship> relations = entry.getValue();
            for (Relationship relation : relations) {
                createRelation(relationType, relation.getStartNode(), relation.getEndNode(), relation.getProperties());
            }
        }
    }

    @Override
    public void deleteRelation(String relationType, Node startNode, Node endNode, Map<String, Object> properties) {
        StringBuilder cql = new StringBuilder();
        cql.append(String.format(
                "MATCH (a:%s {id: $startId})-[r:%s]->(b:%s {id: $endId})",
                startNode.getLabel(), relationType, endNode.getLabel()
        ));

        if (properties != null && !properties.isEmpty()) {
            String where = properties.keySet().stream()
                    .map(k -> "r." + k + " = $" + k)
                    .collect(Collectors.joining(" AND "));
            cql.append(" WHERE ").append(where);
        }
        cql.append(" DELETE r");

        Map<String, Object> params = new HashMap<>();
        params.put("startId", startNode.getProperties().get("id"));
        params.put("endId", endNode.getProperties().get("id"));
        if (properties != null) params.putAll(properties);

        try (Session session = Neo4jConnector.getDriver().session()) {
            ResultSummary result = session.run(cql.toString(), params).consume();
        } catch (Exception e) {
            throw new RuntimeException("删除关系失败", e);
        }
    }

    @Override
    public void updateRelation(String relationType, Node startNode, Node endNode, Map<String, Object> properties) {
        if (properties == null || properties.isEmpty()) {
            return;
        }

        String setClause = properties.keySet().stream()
                .map(k -> "r." + k + " = $" + k)
                .collect(Collectors.joining(", "));

        String cql = String.format(
                "MATCH (a:%s {id: $startId})-[r:%s]->(b:%s {id: $endId}) SET %s",
                startNode.getLabel(), relationType, endNode.getLabel(), setClause
        );

        Map<String, Object> params = new HashMap<>();
        params.put("startId", startNode.getProperties().get("id"));
        params.put("endId", endNode.getProperties().get("id"));
        params.putAll(properties);

        try (Session session = Neo4jConnector.getDriver().session()) {
            session.run(cql, params);
        } catch (Exception e) {
            throw new RuntimeException("更新关系失败", e);
        }
    }

    @Override
    public List<Relationship> queryRelation(String relationType, Node startNode, Node endNode, Map<String, Object> properties) {
    StringBuilder cql = new StringBuilder();
    cql.append(String.format(
            "MATCH (a:%s {id: $startId})-[r:%s]->(b:%s {id: $endId})",
            startNode.getLabel(), relationType, endNode.getLabel()
    ));

    if (properties != null && !properties.isEmpty()) {
        String where = properties.keySet().stream()
                .map(k -> "r." + k + " = $" + k)
                .collect(Collectors.joining(" AND "));
        cql.append(" WHERE ").append(where);
    }
    cql.append(" RETURN r");

    Map<String, Object> params = new HashMap<>();
    params.put("startId", startNode.getProperties().get("id"));
    params.put("endId", endNode.getProperties().get("id"));
    if (properties != null) params.putAll(properties);

    try (Session session = Neo4jConnector.getDriver().session()) {
        Result result = session.run(cql.toString(), params);
        List<Relationship> relationships = new ArrayList<>();
        while (result.hasNext()) {
            Record record = result.next();
            Relationship relationship = new Relationship();
            relationship.setType(relationType);
            relationship.setStartNode(startNode);
            relationship.setEndNode(endNode);
            relationship.setProperties(record.get("r").asMap());
            relationships.add(relationship);
        }
        return relationships;
    } catch (Exception e) {
        throw new RuntimeException("查询关系失败", e);
    }
}

    @Override
    public List<Map<String, Object>> executeCypher(String cypher) {
        try(Session session = Neo4jConnector.getDriver().session()){
            Result result = session.run(cypher);
            return result.list(MapAccessor::asMap);
        }
    }

    @Override
    public void close() {
        Neo4jConnector.close();
    }

    private Node convertDriverNode(org.neo4j.driver.types.Node neo4jNode) {
        Node node = new Node();
        node.setLabel(neo4jNode.labels().iterator().next());
        Map<String, Object> properties = neo4jNode.asMap();
        properties.put("id", neo4jNode.get("id"));
        node.setProperties(neo4jNode.asMap());
        return node;
    }
}