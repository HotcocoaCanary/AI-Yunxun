package com.example.aiyunxun.repository;

import com.example.aiyunxun.entity.Node;
import com.example.aiyunxun.entity.Relationship;

import java.util.List;
import java.util.Map;

public interface Neo4jOperator {
    // 创建节点
    void createNode(String label, Map<String, Object> properties);
    void createNode(Map<String, List<Node>> nodes);
    // 删除节点
    void deleteNode(String label, Map<String, Object> properties);
    // 更新节点
    void updateNode(String label, Map<String, Object> properties);
    // 查询节点
    List<Node> queryNode(String label, Map<String, Object> properties);
    // 创建关系
    void createRelation(String relationType, Node startNode, Node endNode, Map<String, Object> properties);
    void createRelation(Map<String, List<Relationship>> relationships);
    // 删除关系
    void deleteRelation(String relationType, Node startNode, Node endNode, Map<String, Object> properties);
    // 更新关系
    void updateRelation(String relationType, Node startNode, Node endNode, Map<String, Object> properties);
    // 查询关系
    List<Relationship> queryRelation(String relationType, Node startNode, Node endNode, Map<String, Object> properties);
    // 执行Cypher语句
    List<Map<String, Object>> executeCypher(String cypher);
    // 关闭连接
    void close();
}
