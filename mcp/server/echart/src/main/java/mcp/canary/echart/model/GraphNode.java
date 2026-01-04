package mcp.canary.echart.model;

import lombok.Data;

/**
 * 关系图节点模型
 * 用于 graph 图表
 */
@Data
public class GraphNode {
    /**
     * 节点唯一标识
     */
    private String id;

    /**
     * 节点显示名称
     */
    private String name;

    /**
     * 节点值（影响大小）
     */
    private Number value;

    /**
     * 节点类别（影响颜色）
     */
    private String category;
}




