package com.example.aiyunxun.entity;

import lombok.*;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Relationship {
    // 关系类型
    private String type;
    // 起始节点
    private Node startNode;
    // 结束节点
    private Node endNode;
    // 属性
    private Map<String, Object> properties;

    public Relationship(String affiliation, Node startNode, Node endNode) {
        this.type = affiliation;
        this.startNode = startNode;
        this.endNode = endNode;
    }
}
