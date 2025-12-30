package mcp.canary.echart.tool;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import mcp.canary.echart.model.*;
import mcp.canary.echart.service.BarChartService;
import mcp.canary.echart.service.GraphChartService;
import mcp.canary.echart.service.LineChartService;
import mcp.canary.echart.service.PieChartService;
import org.springaicommunity.mcp.annotation.McpTool;
import org.springaicommunity.mcp.annotation.McpToolParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ECharts MCP 工具类
 * 提供 4 个图表生成工具：柱状图、折线图、饼图、关系图
 * 负责 MCP 协议相关的输出处理
 */
@Component
public class EChartMCPTool {
    
    private static final ObjectMapper objectMapper = new ObjectMapper();
    
    private final BarChartService barChartService;
    private final LineChartService lineChartService;
    private final PieChartService pieChartService;
    private final GraphChartService graphChartService;
    
    @Autowired
    public EChartMCPTool(
            BarChartService barChartService,
            LineChartService lineChartService,
            PieChartService pieChartService,
            GraphChartService graphChartService) {
        this.barChartService = barChartService;
        this.lineChartService = lineChartService;
        this.pieChartService = pieChartService;
        this.graphChartService = graphChartService;
    }
    
    /**
     * 1. 柱状图
     */
    @McpTool(name = "generate_bar_chart", description = "生成柱状图，用于显示不同类别之间的数值比较")
    public Map<String, Object> generateBarChart(
            @McpToolParam(description = "图表标题") String title,
            @McpToolParam(description = "X 轴标题") String axisXTitle,
            @McpToolParam(description = "Y 轴标题") String axisYTitle,
            @McpToolParam(description = "图表数据") List<DataItem> data,
            @McpToolParam(description = "是否启用分组（多系列时）") Boolean group,
            @McpToolParam(description = "是否启用堆叠（多系列时）") Boolean stack,
            @McpToolParam(description = "输出类型，当前仅支持 option") String outputType) {
        
        ObjectNode option = barChartService.buildChartOption(
                title, axisXTitle, axisYTitle, data,
                group != null && group, stack != null && stack);
        return buildMCPResponse(option);
    }
    
    /**
     * 2. 折线图
     */
    @McpTool(name = "generate_line_chart", description = "生成折线图，用于显示数据随时间或其他连续变量的趋势变化")
    public Map<String, Object> generateLineChart(
            @McpToolParam(description = "图表标题") String title,
            @McpToolParam(description = "X 轴标题（通常是时间）") String axisXTitle,
            @McpToolParam(description = "Y 轴标题") String axisYTitle,
            @McpToolParam(description = "图表数据") List<DataItem> data,
            @McpToolParam(description = "是否使用平滑曲线") Boolean smooth,
            @McpToolParam(description = "是否填充区域") Boolean showArea,
            @McpToolParam(description = "是否显示数据点标记") Boolean showSymbol,
            @McpToolParam(description = "是否堆叠（多系列时）") Boolean stack,
            @McpToolParam(description = "输出类型，当前仅支持 option") String outputType) {
        
        ObjectNode option = lineChartService.buildChartOption(
                title, axisXTitle, axisYTitle, data,
                smooth != null && smooth,
                showArea != null && showArea,
                showSymbol == null || showSymbol,
                stack != null && stack);
        return buildMCPResponse(option);
    }
    
    /**
     * 3. 饼图
     */
    @McpTool(name = "generate_pie_chart", description = "生成饼图，用于显示各部分占整体的比例关系")
    public Map<String, Object> generatePieChart(
            @McpToolParam(description = "图表标题") String title,
            @McpToolParam(description = "图表数据") List<DataItem> data,
            @McpToolParam(description = "内半径（0-1），0为饼图，>0为环形图") Double innerRadius,
            @McpToolParam(description = "输出类型，当前仅支持 option") String outputType) {
        
        ObjectNode option = pieChartService.buildChartOption(
                title, data, innerRadius != null ? innerRadius : 0.0);
        return buildMCPResponse(option);
    }
    
    /**
     * 4. 关系图
     */
    @McpTool(name = "generate_graph_chart", description = "生成关系图（网络图），用于显示实体（节点）之间的关系")
    public Map<String, Object> generateGraphChart(
            @McpToolParam(description = "图表标题") String title,
            @McpToolParam(description = "图和边数据") GraphData data,
            @McpToolParam(description = "布局算法") String layout,
            @McpToolParam(description = "输出类型，当前仅支持 option") String outputType) {
        
        ObjectNode option = graphChartService.buildChartOption(title, data, layout);
        return buildMCPResponse(option);
    }
    
    /**
     * 构建 MCP 标准响应格式
     * 将 ECharts option 配置转换为 JSON 字符串并包装为 MCP 响应
     * 
     * @param option ECharts option 配置
     * @return MCP 标准响应格式
     */
    private Map<String, Object> buildMCPResponse(ObjectNode option) {
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
