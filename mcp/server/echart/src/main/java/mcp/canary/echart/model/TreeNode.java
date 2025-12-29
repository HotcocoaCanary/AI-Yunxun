package mcp.canary.echart.model;

import lombok.Data;
import java.util.List;

/**
 * 树节点模型（递归结构）
 * 用于 tree, treemap, sunburst 等树形图表
 */
@Data
public class TreeNode {
    /**
     * 节点名称
     */
    private String name;
    
    /**
     * 节点值（可选）
     */
    private Number value;
    
    /**
     * 子节点列表（可选，递归结构）
     */
    private List<TreeNode> children;
}

