package mcp.canary.echart.model;

import lombok.Data;

/**
 * 通用数据项模型
 * 用于各种图表类型的数据输入
 */
@Data
public class DataItem {
    /**
     * 类别名称（用于 bar, pie, funnel 等图表）
     */
    private String category;

    /**
     * 数值（用于 bar, pie, funnel 等图表）
     */
    private Number value;

    /**
     * 分组名称（可选，用于多系列图表）
     */
    private String group;

    /**
     * 名称（用于 radar, gauge 等图表）
     */
    private String name;

    /**
     * X 坐标值（用于 scatter 图表）
     */
    private Number x;

    /**
     * Y 坐标值（用于 scatter 图表）
     */
    private Number y;

    /**
     * 源节点名称（用于 sankey 图表）
     */
    private String source;

    /**
     * 目标节点名称（用于 sankey 图表）
     */
    private String target;

    /**
     * 日期/时间（用于 candlestick, boxplot 等图表）
     */
    private String date;

    /**
     * 开盘价（用于 candlestick 图表）
     */
    private Number open;

    /**
     * 收盘价（用于 candlestick 图表）
     */
    private Number close;

    /**
     * 最高价（用于 candlestick 图表）
     */
    private Number high;

    /**
     * 最低价（用于 candlestick 图表）
     */
    private Number low;

    /**
     * 成交量（用于 candlestick 图表）
     */
    private Number volume;

    /**
     * 箱线图数据数组 [min, Q1, median, Q3, max]（用于 boxplot 图表）
     */
    private Number[] boxplotData;

    /**
     * 热力图数据值（用于 heatmap 图表）
     */
    private Number heatValue;

    /**
     * 平行坐标系数据值（用于 parallel 图表）
     */
    private Number[] parallelValues;
}


