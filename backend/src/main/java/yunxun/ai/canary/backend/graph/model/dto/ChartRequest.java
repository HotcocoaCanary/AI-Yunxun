package yunxun.ai.canary.backend.graph.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * 通过图谱/分析服务生成图表的请求载荷
 * <p>
 * 此对象设计为对 MCP/LLM 友好：
 * - 大多数字段为可选提示，代理可以从一个问题开始，
 *   然后在需要时逐步添加结构。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChartRequest {

    /**
     * 用户的分析问题或意图，使用自然语言描述
     * 示例："最近7天每种告警类型的趋势"
     */
    private String question;

    /**
     * 首选的图表类型，例如："bar"（柱状图）、"line"（折线图）、"pie"（饼图）、"force"（力导向图）或 "auto"（自动选择）
     */
    private String chartType;

    /**
     * 可选的数据源或领域标识符，例如："alerts"（告警）、"traffic"（流量）
     */
    private String dataSource;

    /**
     * 可选的指标名称，例如："count"（计数）、"duration_avg"（平均时长）
     */
    private String metric;

    /**
     * 可选的分组维度
     * 对于简单情况，单个值就足够了
     */
    private List<String> dimensions;

    /**
     * 可选的时间范围预设，例如："last_7d"（最近7天）、"last_30d"（最近30天）、"all"（全部）
     */
    private String timeRangePreset;

    /**
     * 可选的 ISO-8601 起始时间戳，例如："2025-11-01T00:00:00Z"
     */
    private String from;

    /**
     * 可选的 ISO-8601 结束时间戳，例如："2025-11-07T23:59:59Z"
     */
    private String to;

    /**
     * 可选的简单过滤器，例如：{ "severity": ["high", "critical"], "tenantId": "xxx" }
     */
    private Map<String, Object> filters;

    /**
     * 返回数据点的最大数量，用作安全防护
     */
    private Integer limit;

    /**
     * 提示优先使用简单的图表类型（柱状图/折线图/饼图）
     */
    private Boolean preferSimpleChart;
}

