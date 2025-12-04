package yunxun.ai.canary.backend.graph.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * Request payload for generating charts via the graph/analytics service.
 * <p>
 * This object is designed to be MCP/LLM friendly:
 * - Most fields are optional hints so that agents can start with only a question
 *   and gradually add structure when needed.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChartRequest {

    /**
     * User analytic question or intent in natural language.
     * Example: "最近7天每种告警类型的趋势".
     */
    private String question;

    /**
     * Preferred chart type, e.g. "bar", "line", "pie", "force" or "auto".
     */
    private String chartType;

    /**
     * Optional logical data source or domain identifier, e.g. "alerts", "traffic".
     */
    private String dataSource;

    /**
     * Optional metric name, e.g. "count", "duration_avg".
     */
    private String metric;

    /**
     * Optional dimension(s) to group by.
     * For simple cases, a single value is enough.
     */
    private List<String> dimensions;

    /**
     * Optional time range preset, e.g. "last_7d", "last_30d", "all".
     */
    private String timeRangePreset;

    /**
     * Optional ISO-8601 from timestamp, e.g. "2025-11-01T00:00:00Z".
     */
    private String from;

    /**
     * Optional ISO-8601 to timestamp, e.g. "2025-11-07T23:59:59Z".
     */
    private String to;

    /**
     * Optional simple filters, e.g. { "severity": ["high", "critical"], "tenantId": "xxx" }.
     */
    private Map<String, Object> filters;

    /**
     * Maximum number of data points to return, used as a safety guard.
     */
    private Integer limit;

    /**
     * Hint to prefer simple chart types (bar/line/pie) when possible.
     */
    private Boolean preferSimpleChart;
}

