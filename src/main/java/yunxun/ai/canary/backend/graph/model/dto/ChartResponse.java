package yunxun.ai.canary.backend.graph.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * 图表 MCP 工具返回的统一图表响应对象
 * <p>
 * 前端 AI 代理可以：
 * - 直接使用 {@link #chartSpec} 进行渲染（例如 ECharts option）；
 * - 检查 {@link #data} 和 {@link #insightSummary} 来编写解释说明。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChartResponse {

    /**
     * 架构版本，用于向前兼容
     */
    @Builder.Default
    private String schemaVersion = "v1";

    /**
     * 解析后的图表类型："bar"（柱状图）| "line"（折线图）| "pie"（饼图）| "force"（力导向图）| "table"（表格）
     */
    private String chartType;

    /**
     * 渲染引擎名称，例如："echarts" 或 "vega-lite"
     */
    private String engine;

    /** 图表标题 */
    private String title;

    /** 图表描述 */
    private String description;

    /**
     * 所选引擎的图表规格对象
     * 对于 ECharts，这是完整的 option 映射
     */
    private Map<String, Object> chartSpec;

    /**
     * 可选的标准化表格数据，用于推理和/或表格渲染
     * 每个条目是一行：列名 -> 值
     */
    private List<Map<String, Object>> data;

    /**
     * 可选的简短自然语言洞察摘要
     */
    private String insightSummary;

    /**
     * 可选的要点式高亮，便于快速浏览
     */
    private List<String> insightBullets;
}

