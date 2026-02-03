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
import mcp.canary.echart.module.graph.GraphOption;
import mcp.canary.echart.module.graph.series.GraphSeries;
import mcp.canary.echart.module.graph.title.GraphTitle;
import mcp.canary.echart.module.graph.series.data.GraphCategory;
import mcp.canary.echart.module.graph.series.data.GraphEdge;
import mcp.canary.echart.module.graph.series.data.GraphNode;
import mcp.canary.echart.prompt.GraphEChartMCPPrompt;
import org.springaicommunity.mcp.annotation.McpTool;
import org.springaicommunity.mcp.annotation.McpToolParam;
import org.springaicommunity.mcp.annotation.McpPrompt;
import org.springframework.stereotype.Component;

@Component
public class GraphEChartMCPTool {

    private static final ObjectMapper mapper = new ObjectMapper();

    @McpTool(
            name = "generate_graph_chart",
            description = "生成 ECharts Graph 图的 option JSON。"
    )
    @McpPrompt(
            name = "graph-option-prompt",
            title = "生成 ECharts Graph Option JSON",
            description = GraphEChartMCPPrompt.GRAPH_OPTION_PROMPT
    )
    public String generateGraphOption(
            @McpToolParam(description = "图表标题，可为空") String title,
            @McpToolParam(description = "布局类型：force 或 circular，缺省为 force") String layout,
            @McpToolParam(description = "节点列表，name 必须唯一") List<GraphNode> nodes,
            @McpToolParam(description = "边列表，source/target 必须存在于节点 name 中") List<GraphEdge> edges,
            @McpToolParam(description = "分类列表，可为空；为空时由节点 categoryName 自动生成") List<GraphCategory> categories,
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
