package yunxun.ai.canary.backend.graph.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * Unified chart response object returned by the chart MCP tool.
 * <p>
 * Frontend LLM agents can:
 * - use {@link #chartSpec} directly for rendering (e.g. ECharts option);
 * - inspect {@link #data} and {@link #insightSummary} to write explanations.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChartResponse {

    /**
     * Schema version for forward compatibility.
     */
    @Builder.Default
    private String schemaVersion = "v1";

    /**
     * Resolved chart type: "bar" | "line" | "pie" | "force" | "table".
     */
    private String chartType;

    /**
     * Rendering engine name, e.g. "echarts" or "vega-lite".
     */
    private String engine;

    private String title;

    private String description;

    /**
     * Chart specification object for the chosen engine.
     * For ECharts this is the full option map.
     */
    private Map<String, Object> chartSpec;

    /**
     * Optional normalized tabular data for reasoning and/or table rendering.
     * Each entry is a row: column name -> value.
     */
    private List<Map<String, Object>> data;

    /**
     * Optional short natural language summary for the insight.
     */
    private String insightSummary;

    /**
     * Optional bullet-style highlights for quick scanning.
     */
    private List<String> insightBullets;
}

