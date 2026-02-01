package mcp.canary.echart.tool;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import mcp.canary.echart.model.GraphData;
import mcp.canary.echart.service.GraphChartService;
import mcp.canary.echart.service.GraphGLChartService;
import org.springaicommunity.mcp.annotation.McpTool;
import org.springaicommunity.mcp.annotation.McpToolParam;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import io.modelcontextprotocol.spec.McpSchema.LoggingLevel;
import io.modelcontextprotocol.spec.McpSchema.LoggingMessageNotification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ECharts MCP 工具类。
 * 仅提供关系图与 GL 关系图两个工具：generate_graph_chart、generate_graph_gl_chart。
 * 负责 MCP 协议输出与日志通知。
 */
@Component
public class EChartMCPTool {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    private static final int DEFAULT_WIDTH = 800;
    private static final int DEFAULT_HEIGHT = 600;
    private static final String DEFAULT_THEME = "default";
    private static final String DEFAULT_LAYOUT = "force";
    private static final String DEFAULT_OUTPUT_TYPE = "option";

    private final GraphChartService graphChartService;
    private final GraphGLChartService graphGLChartService;

    @Autowired
    public EChartMCPTool(GraphChartService graphChartService, GraphGLChartService graphGLChartService) {
        this.graphChartService = graphChartService;
        this.graphGLChartService = graphGLChartService;
    }

    /**
     * 生成关系图（网络图）。MCP 工具名：generate_graph_chart。
     * 输入：title、data（nodes/edges）、layout、width、height、theme、outputType。
     * 当前仅支持 outputType=option，返回 ECharts option JSON；png/svg 为后续迭代。
     *
     * @param title     图表标题，可选
     * @param data      图数据（nodes、edges），必填
     * @param layout    布局：force / circular / none
     * @param width     画布宽度（像素）
     * @param height    画布高度（像素）
     * @param theme     主题：default / dark
     * @param outputType 输出类型：option / png / svg，当前仅支持 option
     * @param exchange  MCP 交换对象，用于发送日志
     * @return MCP 响应，content 为 text 类型的 option JSON
     */
    @McpTool(name = "generate_graph_chart", description = "生成关系图（网络图），用于显示实体（节点）之间的关系。支持 outputType：option（返回 ECharts option JSON）、png、svg（返回图像，便于嵌入或直接展示）。当前仅支持 option。")
    public Map<String, Object> generateGraphChart(
            @McpToolParam(description = "图表标题") String title,
            @McpToolParam(description = "图数据，包含 nodes 与 edges") GraphData data,
            @McpToolParam(description = "布局：force / circular / none，默认 force") String layout,
            @McpToolParam(description = "画布宽度（像素），默认 800") Integer width,
            @McpToolParam(description = "画布高度（像素），默认 600") Integer height,
            @McpToolParam(description = "主题：default / dark，默认 default") String theme,
            @McpToolParam(description = "输出类型：option / png / svg，默认 option，当前仅支持 option") String outputType,
            McpSyncServerExchange exchange) {

        sendLog(exchange, LoggingLevel.INFO, "开始生成关系图: " + (title != null ? title : "未命名图表"));

        try {
            int nodeCount = (data != null && data.getNodes() != null) ? data.getNodes().size() : 0;
            int edgeCount = (data != null && data.getEdges() != null) ? data.getEdges().size() : 0;
            sendLog(exchange, LoggingLevel.INFO,
                    String.format("正在处理关系图数据，节点数: %d, 边数: %d", nodeCount, edgeCount));

            int w = (width != null && width > 0) ? width : DEFAULT_WIDTH;
            int h = (height != null && height > 0) ? height : DEFAULT_HEIGHT;
            String t = (theme != null && !theme.isEmpty()) ? theme : DEFAULT_THEME;
            String lay = (layout != null && !layout.isEmpty()) ? layout : DEFAULT_LAYOUT;

            ObjectNode option = graphChartService.buildChartOption(title, data, lay, w, h, t);

            String effectiveOutputType = (outputType != null && !outputType.isEmpty()) ? outputType : DEFAULT_OUTPUT_TYPE;
            if ("png".equalsIgnoreCase(effectiveOutputType) || "svg".equalsIgnoreCase(effectiveOutputType)) {
                sendLog(exchange, LoggingLevel.INFO, "当前仅支持 outputType=option，png/svg 为后续迭代，返回 option");
            }

            sendLog(exchange, LoggingLevel.INFO, "关系图生成完成");
            return buildTextContentResponse(option);
        } catch (Exception e) {
            sendLog(exchange, LoggingLevel.ERROR, "生成关系图失败: " + e.getMessage());
            throw e;
        }
    }

    /**
     * 生成 GL 关系图（WebGL）。MCP 工具名：generate_graph_gl_chart。
     * 输入与 generate_graph_chart 一致；当前仅支持 outputType=option。
     *
     * @param title      图表标题，可选
     * @param data       图数据（nodes、edges），必填
     * @param layout     布局：force / circular / none
     * @param width      画布宽度（像素）
     * @param height     画布高度（像素）
     * @param theme      主题：default / dark
     * @param outputType 输出类型：option / png / svg，当前仅支持 option
     * @param exchange   MCP 交换对象
     * @return MCP 响应，content 为 text 类型的 option JSON（series[].type = "graphGL"）
     */
    @McpTool(name = "generate_graph_gl_chart", description = "生成 GL 关系图（WebGL 3D/GPU 布局），适合较大规模节点边数据。支持 outputType：option、png、svg。当前仅支持 option。")
    public Map<String, Object> generateGraphGLChart(
            @McpToolParam(description = "图表标题") String title,
            @McpToolParam(description = "图数据，包含 nodes 与 edges") GraphData data,
            @McpToolParam(description = "布局：force / circular / none，默认 force") String layout,
            @McpToolParam(description = "画布宽度（像素），默认 800") Integer width,
            @McpToolParam(description = "画布高度（像素），默认 600") Integer height,
            @McpToolParam(description = "主题：default / dark，默认 default") String theme,
            @McpToolParam(description = "输出类型：option / png / svg，默认 option，当前仅支持 option") String outputType,
            McpSyncServerExchange exchange) {

        sendLog(exchange, LoggingLevel.INFO, "开始生成 GL 关系图: " + (title != null ? title : "未命名图表"));

        try {
            int nodeCount = (data != null && data.getNodes() != null) ? data.getNodes().size() : 0;
            int edgeCount = (data != null && data.getEdges() != null) ? data.getEdges().size() : 0;
            sendLog(exchange, LoggingLevel.INFO,
                    String.format("正在处理 GL 关系图数据，节点数: %d, 边数: %d", nodeCount, edgeCount));

            int w = (width != null && width > 0) ? width : DEFAULT_WIDTH;
            int h = (height != null && height > 0) ? height : DEFAULT_HEIGHT;
            String t = (theme != null && !theme.isEmpty()) ? theme : DEFAULT_THEME;
            String lay = (layout != null && !layout.isEmpty()) ? layout : DEFAULT_LAYOUT;

            ObjectNode option = graphGLChartService.buildChartOption(title, data, lay, w, h, t);

            String effectiveOutputType = (outputType != null && !outputType.isEmpty()) ? outputType : DEFAULT_OUTPUT_TYPE;
            if ("png".equalsIgnoreCase(effectiveOutputType) || "svg".equalsIgnoreCase(effectiveOutputType)) {
                sendLog(exchange, LoggingLevel.INFO, "当前仅支持 outputType=option，png/svg 为后续迭代，返回 option");
            }

            sendLog(exchange, LoggingLevel.INFO, "GL 关系图生成完成");
            return buildTextContentResponse(option);
        } catch (Exception e) {
            sendLog(exchange, LoggingLevel.ERROR, "生成 GL 关系图失败: " + e.getMessage());
            throw e;
        }
    }

    /**
     * 发送日志通知。
     *
     * @param exchange MCP 服务器交换对象
     * @param level   日志级别
     * @param message 日志消息
     */
    private void sendLog(McpSyncServerExchange exchange, LoggingLevel level, String message) {
        if (exchange != null) {
            try {
                exchange.loggingNotification(
                        LoggingMessageNotification.builder()
                                .level(level)
                                .logger("echart-tool")
                                .data(message)
                                .build()
                );
            } catch (Exception e) {
                System.err.println("Failed to send log notification: " + e.getMessage());
            }
        }
    }

    /**
     * 将 ECharts option 包装为 MCP text 类型 content 的响应。
     *
     * @param option ECharts option
     * @return MCP 响应 map
     */
    private Map<String, Object> buildTextContentResponse(ObjectNode option) {
        try {
            String optionJson = objectMapper.writeValueAsString(option);
            Map<String, Object> response = new HashMap<>();
            Map<String, Object> content = new HashMap<>();
            content.put("type", "text");
            content.put("text", optionJson);
            response.put("content", List.of(content));
            return response;
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize option to JSON", e);
        }
    }
}
