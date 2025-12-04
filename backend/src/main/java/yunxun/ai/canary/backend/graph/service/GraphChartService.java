package yunxun.ai.canary.backend.graph.service;

import org.springframework.stereotype.Service;
import yunxun.ai.canary.backend.graph.model.dto.ChartRequest;
import yunxun.ai.canary.backend.graph.model.dto.ChartResponse;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Core chart generation service.
 * <p>
 * In the current minimal version this service does not yet connect to
 * specific databases; it only echoes back a safe, well-structured
 * {@link ChartResponse} so that the MCP tool schema and frontend
 * integration can be validated first.
 * <p>
 * Later we can plug in real data sources (e.g. Neo4j, Mongo, MySQL)
 * and build engine-specific chart specifications here.
 */
@Service
public class GraphChartService {

    public ChartResponse generateChart(ChartRequest request) {
        String resolvedChartType = (request.getChartType() == null || request.getChartType().isBlank())
                ? "auto"
                : request.getChartType();

        String title = request.getQuestion() != null && !request.getQuestion().isBlank()
                ? request.getQuestion()
                : "Generated chart";

        // Minimal placeholder ECharts-like option so that the frontend
        // can render something even before the real data pipeline is ready.
        Map<String, Object> option = Map.of(
                "title", Map.of("text", title),
                "tooltip", Map.of("trigger", "axis"),
                "xAxis", Map.of("type", "category", "data", List.of()),
                "yAxis", Map.of("type", "value"),
                "series", List.of(
                        Map.of(
                                "type", resolvedChartType.equals("pie") ? "pie" : "bar",
                                "data", List.of()
                        )
                )
        );

        return ChartResponse.builder()
                .chartType(resolvedChartType)
                .engine("echarts")
                .title(title)
                .description("Placeholder chart response; data pipeline not implemented yet.")
                .chartSpec(option)
                .data(Collections.emptyList())
                .insightSummary("Chart generation service is wired, but data-driven analytics are not implemented yet.")
                .insightBullets(List.of(
                        "You can already inspect chartSpec to render an empty chart.",
                        "Backend can later attach real data based on question/dataSource/metric."
                ))
                .build();
    }
}

