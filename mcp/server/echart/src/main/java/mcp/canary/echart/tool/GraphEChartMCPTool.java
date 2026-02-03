package mcp.canary.echart.tool;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import io.modelcontextprotocol.spec.McpSchema.LoggingLevel;
import io.modelcontextprotocol.spec.McpSchema.LoggingMessageNotification;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import mcp.canary.echart.graph.GraphOption;
import mcp.canary.echart.graph.GraphTitle;
import mcp.canary.shared.GraphSeries;
import mcp.canary.shared.data.GraphCategory;
import mcp.canary.shared.data.GraphEdge;
import mcp.canary.shared.data.GraphNode;
import org.springaicommunity.mcp.annotation.McpPrompt;
import org.springaicommunity.mcp.annotation.McpTool;
import org.springaicommunity.mcp.annotation.McpToolParam;
import org.springframework.stereotype.Component;

@Component
public class GraphEChartMCPTool {

    private static final ObjectMapper mapper = new ObjectMapper();

    @McpTool(
            name = "generate_graph_chart",
            description = "Generate ECharts graph option JSON from nodes/edges/categories."
    )
    @McpPrompt(
            name = "graph-option-prompt",
            title = "Generate ECharts Graph Option JSON",
            description = "Return a valid ECharts graph option JSON based on the input nodes, edges, and categories."
    )
    public String generateGraphOption(
            @McpToolParam(description = "Chart title") String title,
            @McpToolParam(description = "Layout type: force or circular") String layout,
            @McpToolParam(description = "Node list") List<GraphNode> nodes,
            @McpToolParam(description = "Edge list") List<GraphEdge> edges,
            @McpToolParam(description = "Category list") List<GraphCategory> categories,
            McpSyncServerExchange exchange
    ) {
        sendLog(exchange, LoggingLevel.INFO, "开始生成 ECharts graph option");

        try {
            GraphSeries series = new GraphSeries();
            series.setLayout(layout != null ? layout : "force");
            series.setNodes(nodes != null ? nodes : Collections.emptyList());
            series.setEdges(edges != null ? edges : Collections.emptyList());
            series.setCategories(resolveCategories(nodes, categories));

            GraphOption graphOption = new GraphOption();
            if (title != null && !title.isBlank()) {
                graphOption.setTitle(new GraphTitle() {{
                    setText(title);
                }});
            }
            graphOption.setSeries(series);

            JsonNode resultNode = graphOption.toEChartNode();
            String jsonStr = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(resultNode);

            sendLog(exchange, LoggingLevel.INFO, "ECharts option JSON 生成完成");
            return jsonStr;
        } catch (Exception e) {
            sendLog(exchange, LoggingLevel.ERROR, "生成 ECharts option 失败: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    private List<GraphCategory> resolveCategories(List<GraphNode> nodes, List<GraphCategory> categories) {
        if (categories != null && !categories.isEmpty()) {
            return categories;
        }
        if (nodes == null || nodes.isEmpty()) {
            return Collections.emptyList();
        }
        Set<String> names = new HashSet<>();
        for (GraphNode node : nodes) {
            if (node != null && node.getCategoryName() != null) {
                names.add(node.getCategoryName());
            }
        }
        List<GraphCategory> result = new ArrayList<>();
        for (String name : names) {
            GraphCategory cat = new GraphCategory();
            cat.setName(name);
            cat.setSymbol("circle");
            result.add(cat);
        }
        return result;
    }

    /**
     * Logging helper
     */
    private void sendLog(McpSyncServerExchange exchange, LoggingLevel level, String message) {
        if (exchange != null) {
            try {
                exchange.loggingNotification(
                        LoggingMessageNotification.builder()
                                .level(level)
                                .logger("echart-graph-tool")
                                .data(message)
                                .build()
                );
            } catch (Exception e) {
                System.err.println("日志发送失败: " + e.getMessage());
            }
        }
    }
}
