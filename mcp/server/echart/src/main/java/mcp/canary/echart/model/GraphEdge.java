package mcp.canary.echart.model;

import lombok.Data;

/**
 * 关系图边模型
 * 用于 graph 图表
 */
@Data
public class GraphEdge {
    /**
     * 源节点 id
     */
    private String source;

    /**
     * 目标节点 id
     */
    private String target;

    /**
     * 边的权重（影响粗细）
     */
    private Number value;
}




