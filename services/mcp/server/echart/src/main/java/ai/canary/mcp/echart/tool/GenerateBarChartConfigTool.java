package ai.canary.mcp.echart.tool;

import ai.canary.mcp.echart.model.BarChartData;
import ai.canary.mcp.echart.service.BarChartConfigService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springaicommunity.mcp.annotation.McpTool;
import org.springaicommunity.mcp.annotation.McpToolParam;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GenerateBarChartConfigTool {

    private final BarChartConfigService barChartConfigService;
    private final ObjectMapper objectMapper;

    @McpTool(name = "generate_bar_chart_config", description = "Generate ECharts bar chart configuration JSON from normalized data. Input should be JSON with title, categories (list of strings), values (list of numbers), and optional valueName.")
    public String generateBarChartConfig(
            @McpToolParam(description = "JSON string containing bar chart data: {title: string, categories: string[], values: number[], valueName?: string}", required = true) String data) {
        try {
            BarChartData chartData = objectMapper.readValue(data, BarChartData.class);
            return barChartConfigService.generateConfig(chartData);
        } catch (Exception e) {
            return "{\"error\": \"Failed to generate bar chart config: " + e.getMessage() + "\"}";
        }
    }
}

