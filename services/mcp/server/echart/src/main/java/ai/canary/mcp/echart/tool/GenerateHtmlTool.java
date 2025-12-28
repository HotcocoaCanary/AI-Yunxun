package ai.canary.mcp.echart.tool;

import ai.canary.mcp.echart.service.HtmlGeneratorService;
import lombok.RequiredArgsConstructor;
import org.springaicommunity.mcp.annotation.McpTool;
import org.springaicommunity.mcp.annotation.McpToolParam;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GenerateHtmlTool {

    private final HtmlGeneratorService htmlGeneratorService;

    @McpTool(name = "generate_html", description = "Generate a complete HTML file from ECharts configuration JSON. The HTML file can be opened directly in a browser to display the chart. Input should be a valid ECharts configuration JSON string.")
    public String generateHtml(
            @McpToolParam(description = "ECharts configuration JSON string (the output from generate_line_chart_config, generate_bar_chart_config, or generate_relation_graph_config)", required = true) String echartConfigJson) {
        try {
            return htmlGeneratorService.generateHtml(echartConfigJson);
        } catch (Exception e) {
            return "<html><body><h1>Error</h1><p>Failed to generate HTML: " + e.getMessage() + "</p></body></html>";
        }
    }
}

