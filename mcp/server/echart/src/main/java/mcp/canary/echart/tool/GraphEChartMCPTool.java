package mcp.canary.echart.tool;

import mcp.canary.echart.graph.GraphOption;
import mcp.canary.echart.graph.GraphTitle;
import mcp.canary.shared.GraphSeries;
import mcp.canary.shared.data.GraphCategory;
import mcp.canary.shared.data.GraphEdge;
import mcp.canary.shared.data.GraphNode;
import org.springaicommunity.mcp.annotation.McpPrompt;
import org.springaicommunity.mcp.annotation.McpTool;
import org.springaicommunity.mcp.annotation.McpToolParam;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import io.modelcontextprotocol.spec.McpSchema.LoggingLevel;
import io.modelcontextprotocol.spec.McpSchema.LoggingMessageNotification;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;

@Component
public class GraphEChartMCPTool {

    private static final ObjectMapper mapper = new ObjectMapper();

    @McpTool(
            name = "generate-echart-graph",
            description = "根据节点、边、分类数据生成 ECharts graph 类型 option JSON，可直接返回给前端使用"
    )
    @McpPrompt(
            name = "graph-option-prompt",
            title = "生成 ECharts Graph Option JSON",
            description = """
            你是一个数据可视化助手，负责生成 ECharts graph 类型图表的 JSON 配置（option），用于前端展示关系图。
            
            工具参数说明：
            - title (String)：图表标题。
            - layout (String)：布局类型，可选 "force" 或 "circular"。
            - nodes (List<GraphNode>)：节点列表，每个节点包含：
                - name (String)：节点名称，唯一。
                - categoryName (String)：节点所属类别名称，必须对应 categories 中的 name。
                - properties (Map<String,Object>)：节点属性，可选，用于 tooltip 展示。
            - edges (List<GraphEdge>)：边列表，每条边包含：
                - source (String)：源节点名称，对应 nodes 中的 name。
                - target (String)：目标节点名称。
                - value (Number)：边的权重，可选，用于显示长度。
            - categories (List<GraphCategory>)：分类列表，每个分类包含：
                - name (String)：类别名称。
                - symbol (String)：节点标记类型，可选值包括 "circle", "rect", "roundRect", "triangle", "diamond", "pin", "arrow", "none"。
            
            要求：
            1. 返回的 JSON 必须完整且符合 ECharts graph option 结构。
            2. nodes、edges、categories 必须保证一致性。
            3. 属性 properties 可以直接用任意键值对，最终会在前端 tooltip 展示。
            4. JSON 输出应可直接作为前端 ECharts 的 option 使用。
            
            示例调用：
            @tool(generate-echart-graph)
            title: "示例关系图"
            layout: "force"
            nodes:
              - name: "节点1"
                categoryName: "类别A"
                properties: { "属性1": "值1" }
              - name: "节点2"
                categoryName: "类别B"
                properties: { "属性2": 123 }
            edges:
              - source: "节点1"
                target: "节点2"
                value: 10
            categories:
              - name: "类别A"
                symbol: "circle"
              - name: "类别B"
                symbol: "diamond"
            @endtool
            """
    )
    public String generateGraphOption(
            @McpToolParam(description = "图表标题") String title,
            @McpToolParam(description = "布局类型：force 或 circular") String layout,
            @McpToolParam(description = "节点列表，GraphNode 对象集合") List<GraphNode> nodes,
            @McpToolParam(description = "边列表，GraphEdge 对象集合") List<GraphEdge> edges,
            @McpToolParam(description = "分类列表，GraphCategory 对象集合") List<GraphCategory> categories,
            McpSyncServerExchange exchange
    ) {
        sendLog(exchange, LoggingLevel.INFO, "开始生成 ECharts graph option");

        try {
            GraphSeries series = new GraphSeries();
            series.setLayout(layout);
            series.setNodes(nodes);
            series.setEdges(edges);
            series.setCategories(categories);

            GraphOption graphOption = new GraphOption();
            graphOption.setTitle(new GraphTitle() {{
                setText(title);
            }});
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

    /**
     * 日志辅助方法
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
