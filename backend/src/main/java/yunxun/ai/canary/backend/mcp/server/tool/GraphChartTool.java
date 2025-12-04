package yunxun.ai.canary.backend.mcp.server.tool;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;
import yunxun.ai.canary.backend.graph.model.dto.ChartRequest;
import yunxun.ai.canary.backend.graph.model.dto.ChartResponse;
import yunxun.ai.canary.backend.graph.service.GraphChartService;

import java.util.Arrays;
import java.util.List;

/**
 * MCP tool wrapper that exposes the chart generation capability
 * to frontend LLM agents over the MCP/SSE interface.
 */
@Component
public class GraphChartTool {

    private final GraphChartService graphChartService;

    public GraphChartTool(GraphChartService graphChartService) {
        this.graphChartService = graphChartService;
    }

    @Tool(
            name = "generate_chart",
            description = "Generate a chart specification (bar/line/pie/force) and a short insight summary for an analytic question"
    )
    public ChartResponse generateChart(
            @ToolParam(description = "User analytic question or intent in natural language")
            String question,

            @ToolParam(description = "Preferred chart type: bar, line, pie, force or auto")
            String chartType,

            @ToolParam(description = "Optional logical data source or domain, e.g. alerts, traffic")
            String dataSource,

            @ToolParam(description = "Optional metric name, e.g. count, duration_avg")
            String metric,

            @ToolParam(description = "Optional comma-separated dimensions to group by, e.g. 'date,type'")
            String dimensions,

            @ToolParam(description = "Optional time range preset, e.g. last_7d, last_30d, all")
            String timeRangePreset,

            @ToolParam(description = "Optional maximum number of data rows to return")
            Integer limit
    ) {
        ChartRequest.ChartRequestBuilder builder = ChartRequest.builder()
                .question(question)
                .chartType(chartType)
                .dataSource(dataSource)
                .metric(metric)
                .timeRangePreset(timeRangePreset)
                .limit(limit)
                .preferSimpleChart(Boolean.TRUE);

        if (dimensions != null && !dimensions.isBlank()) {
            List<String> dimensionList = Arrays.stream(dimensions.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .toList();
            builder.dimensions(dimensionList);
        }

        return graphChartService.generateChart(builder.build());
    }
}

