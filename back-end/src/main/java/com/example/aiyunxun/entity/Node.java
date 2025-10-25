package com.example.aiyunxun.entity;

import lombok.*;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Node {
    // 节点的标签
    private String label;
    // 节点的属性
    private Map<String, Object> properties;
}