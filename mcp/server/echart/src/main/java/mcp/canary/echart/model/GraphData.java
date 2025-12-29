package mcp.canary.echart.model;

import lombok.Data;
import java.util.List;

/**
 * 关系图数据模型
 * 用于 graph 图表
 */
@Data
public class GraphData {
    /**
     * 节点列表
     */
    private List<GraphNode> nodes;
    
    /**
     * 边列表
     */
    private List<GraphEdge> edges;
}

