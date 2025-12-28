package ai.canary.mcp.echart.tool;

import ai.canary.mcp.echart.model.LineChartData;
import ai.canary.mcp.echart.service.LineChartConfigService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springaicommunity.mcp.annotation.McpTool;
import org.springaicommunity.mcp.annotation.McpToolParam;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GenerateLineChartConfigTool {

    private final LineChartConfigService lineChartConfigService;
    private final ObjectMapper objectMapper;

    @McpTool(name = "generate_line_chart_config", description = "Generate ECharts line chart configuration JSON from normalized data. Input should be JSON with title, xAxisData (list of strings), and series (array of {name, data}).")
    public String generateLineChartConfig(
            @McpToolParam(description = "JSON string containing line chart data: {title: string, xAxisData: string[], series: [{name: string, data: number[]}]}", required = true) String data) {
        try {
            LineChartData chartData = objectMapper.readValue(data, LineChartData.class);
            return lineChartConfigService.generateConfig(chartData);
        } catch (Exception e) {
            return "{\"error\": \"Failed to generate line chart config: " + e.getMessage() + "\"}";
        }
    }
}

