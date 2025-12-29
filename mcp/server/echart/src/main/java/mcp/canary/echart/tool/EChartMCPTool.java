package mcp.canary.echart.tool;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import mcp.canary.echart.model.*;
import mcp.canary.echart.util.EChartOptionBuilder;
import org.springaicommunity.mcp.annotation.McpTool;
import org.springaicommunity.mcp.annotation.McpToolParam;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * ECharts MCP 工具类
 * 提供 17 个图表生成工具
 */
@Component
public class EChartMCPTool {
    
    private static final ObjectMapper objectMapper = new ObjectMapper();
    
    /**
     * 构建 MCP 标准响应格式
     */
    private Map<String, Object> buildMCPResponse(String optionJson) {
        Map<String, Object> response = new HashMap<>();
        Map<String, Object> content = new HashMap<>();
        content.put("type", "text");
        content.put("text", optionJson);
        response.put("content", List.of(content));
        return response;
    }
    
    /**
     * 将 ObjectNode 序列化为 JSON 字符串并构建 MCP 响应
     */
    private Map<String, Object> buildMCPResponseFromOption(com.fasterxml.jackson.databind.node.ObjectNode option) {
        try {
            return buildMCPResponse(objectMapper.writeValueAsString(option));
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize option to JSON", e);
        }
    }
    
    /**
     * 验证 outputType
     */
    private void validateOutputType(String outputType) {
        if (outputType != null && !"option".equals(outputType)) {
            throw new IllegalArgumentException("当前版本仅支持 outputType = 'option'");
        }
    }
    
    /**
     * 1. 通用 ECharts 工具
     */
    @McpTool(name = "generate_echarts", description = "使用完整的 ECharts 配置动态生成图表")
    public Map<String, Object> generateECharts(
            @McpToolParam(description = "ECharts 配置的 JSON 字符串") String echartsOption,
            @McpToolParam(description = "输出类型，当前仅支持 option") String outputType) {
        
        validateOutputType(outputType);
        
        // 验证 echartsOption 是有效的 JSON
        try {
            JsonNode optionNode = objectMapper.readTree(echartsOption);
            if (!optionNode.isObject()) {
                throw new IllegalArgumentException("ECharts option 必须是对象类型");
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid ECharts option JSON: " + e.getMessage());
        }
        
        return buildMCPResponse(echartsOption);
    }
    
    /**
     * 2. 柱状图
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
        
        validateOutputType(outputType);
        
        if (data == null || data.isEmpty()) {
            throw new IllegalArgumentException("数据不能为空");
        }
        
        for (DataItem item : data) {
            if (item.getCategory() == null || item.getValue() == null) {
                throw new IllegalArgumentException("数据项必须包含 category 和 value 字段");
            }
        }
        
        if (group != null && stack != null && group && stack) {
            throw new IllegalArgumentException("group 和 stack 不能同时为 true");
        }
        
        var option = EChartOptionBuilder.buildBarChartOption(
                title, axisXTitle, axisYTitle, data,
                group != null && group, stack != null && stack);
        
        return buildMCPResponseFromOption(option);
    }
    
    /**
     * 3. 折线图
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
        
        validateOutputType(outputType);
        
        if (data == null || data.isEmpty()) {
            throw new IllegalArgumentException("数据不能为空");
        }
        
        for (DataItem item : data) {
            if (item.getCategory() == null || item.getValue() == null) {
                throw new IllegalArgumentException("数据项必须包含 category 和 value 字段");
            }
        }
        
        var option = EChartOptionBuilder.buildLineChartOption(
                title, axisXTitle, axisYTitle, data,
                smooth != null && smooth,
                showArea != null && showArea,
                showSymbol == null || showSymbol,
                stack != null && stack);
        
        return buildMCPResponseFromOption(option);
    }
    
    /**
     * 4. 饼图
     */
    @McpTool(name = "generate_pie_chart", description = "生成饼图，用于显示各部分占整体的比例关系")
    public Map<String, Object> generatePieChart(
            @McpToolParam(description = "图表标题") String title,
            @McpToolParam(description = "图表数据") List<DataItem> data,
            @McpToolParam(description = "内半径（0-1），0为饼图，>0为环形图") Double innerRadius,
            @McpToolParam(description = "输出类型，当前仅支持 option") String outputType) {
        
        validateOutputType(outputType);
        
        if (data == null || data.isEmpty()) {
            throw new IllegalArgumentException("数据不能为空");
        }
        
        for (DataItem item : data) {
            if (item.getCategory() == null || item.getValue() == null) {
                throw new IllegalArgumentException("数据项必须包含 category 和 value 字段");
            }
        }
        
        var option = EChartOptionBuilder.buildPieChartOption(
                title, data, innerRadius != null ? innerRadius : 0.0);
        
        return buildMCPResponseFromOption(option);
    }
    
    /**
     * 5. 散点图
     */
    @McpTool(name = "generate_scatter_chart", description = "生成散点图，用于显示两个变量之间的关系")
    public Map<String, Object> generateScatterChart(
            @McpToolParam(description = "图表标题") String title,
            @McpToolParam(description = "X 轴标题") String axisXTitle,
            @McpToolParam(description = "Y 轴标题") String axisYTitle,
            @McpToolParam(description = "图表数据") List<DataItem> data,
            @McpToolParam(description = "输出类型，当前仅支持 option") String outputType) {
        
        validateOutputType(outputType);
        
        if (data == null || data.isEmpty()) {
            throw new IllegalArgumentException("数据不能为空");
        }
        
        for (DataItem item : data) {
            if (item.getX() == null || item.getY() == null) {
                throw new IllegalArgumentException("数据项必须包含 x 和 y 字段");
            }
        }
        
        var option = EChartOptionBuilder.buildScatterChartOption(
                title, axisXTitle, axisYTitle, data);
        
        return buildMCPResponseFromOption(option);
    }
    
    /**
     * 6. 雷达图
     */
    @McpTool(name = "generate_radar_chart", description = "生成雷达图，用于显示多维数据")
    public Map<String, Object> generateRadarChart(
            @McpToolParam(description = "图表标题") String title,
            @McpToolParam(description = "图表数据") List<DataItem> data,
            @McpToolParam(description = "输出类型，当前仅支持 option") String outputType) {
        
        validateOutputType(outputType);
        
        if (data == null || data.isEmpty()) {
            throw new IllegalArgumentException("数据不能为空");
        }
        
        for (DataItem item : data) {
            if (item.getName() == null || item.getValue() == null) {
                throw new IllegalArgumentException("数据项必须包含 name 和 value 字段");
            }
        }
        
        var option = EChartOptionBuilder.buildRadarChartOption(title, data);
        
        return buildMCPResponseFromOption(option);
    }
    
    /**
     * 7. 漏斗图
     */
    @McpTool(name = "generate_funnel_chart", description = "生成漏斗图，用于可视化数据在通过各个阶段时的逐步减少过程")
    public Map<String, Object> generateFunnelChart(
            @McpToolParam(description = "图表标题") String title,
            @McpToolParam(description = "图表数据") List<DataItem> data,
            @McpToolParam(description = "输出类型，当前仅支持 option") String outputType) {
        
        validateOutputType(outputType);
        
        if (data == null || data.isEmpty()) {
            throw new IllegalArgumentException("数据不能为空");
        }
        
        for (DataItem item : data) {
            if (item.getCategory() == null || item.getValue() == null) {
                throw new IllegalArgumentException("数据项必须包含 category 和 value 字段");
            }
        }
        
        var option = EChartOptionBuilder.buildFunnelChartOption(title, data);
        
        return buildMCPResponseFromOption(option);
    }
    
    /**
     * 8. 仪表盘
     */
    @McpTool(name = "generate_gauge_chart", description = "生成仪表盘图表，用于显示单个指标的当前状态")
    public Map<String, Object> generateGaugeChart(
            @McpToolParam(description = "图表标题") String title,
            @McpToolParam(description = "仪表盘数据") List<DataItem> data,
            @McpToolParam(description = "最小值") Number min,
            @McpToolParam(description = "最大值") Number max,
            @McpToolParam(description = "输出类型，当前仅支持 option") String outputType) {
        
        validateOutputType(outputType);
        
        if (data == null || data.isEmpty()) {
            throw new IllegalArgumentException("数据不能为空");
        }
        
        for (DataItem item : data) {
            if (item.getName() == null || item.getValue() == null) {
                throw new IllegalArgumentException("数据项必须包含 name 和 value 字段");
            }
        }
        
        var option = EChartOptionBuilder.buildGaugeChartOption(title, data, min, max);
        
        return buildMCPResponseFromOption(option);
    }
    
    /**
     * 9. 矩形树图
     */
    @McpTool(name = "generate_treemap_chart", description = "生成矩形树图，用于显示层次化数据")
    public Map<String, Object> generateTreemapChart(
            @McpToolParam(description = "图表标题") String title,
            @McpToolParam(description = "树形数据（数组，支持多个根节点）") List<TreeNode> data,
            @McpToolParam(description = "输出类型，当前仅支持 option") String outputType) {
        
        validateOutputType(outputType);
        
        if (data == null || data.isEmpty()) {
            throw new IllegalArgumentException("数据不能为空");
        }
        
        for (TreeNode node : data) {
            if (node.getName() == null) {
                throw new IllegalArgumentException("树节点必须包含 name 字段");
            }
        }
        
        var option = EChartOptionBuilder.buildTreemapChartOption(title, data);
        
        return buildMCPResponseFromOption(option);
    }
    
    /**
     * 10. 旭日图
     */
    @McpTool(name = "generate_sunburst_chart", description = "生成旭日图，用于显示多级层次化数据")
    public Map<String, Object> generateSunburstChart(
            @McpToolParam(description = "图表标题") String title,
            @McpToolParam(description = "树形数据（数组，支持多个根节点）") List<TreeNode> data,
            @McpToolParam(description = "输出类型，当前仅支持 option") String outputType) {
        
        validateOutputType(outputType);
        
        if (data == null || data.isEmpty()) {
            throw new IllegalArgumentException("数据不能为空");
        }
        
        for (TreeNode node : data) {
            if (node.getName() == null) {
                throw new IllegalArgumentException("树节点必须包含 name 字段");
            }
        }
        
        var option = EChartOptionBuilder.buildSunburstChartOption(title, data);
        
        return buildMCPResponseFromOption(option);
    }
    
    /**
     * 11. 热力图
     */
    @McpTool(name = "generate_heatmap_chart", description = "生成热力图，用于显示数据密度或强度分布")
    public Map<String, Object> generateHeatmapChart(
            @McpToolParam(description = "图表标题") String title,
            @McpToolParam(description = "X 轴标题") String axisXTitle,
            @McpToolParam(description = "Y 轴标题") String axisYTitle,
            @McpToolParam(description = "图表数据") List<DataItem> data,
            @McpToolParam(description = "输出类型，当前仅支持 option") String outputType) {
        
        validateOutputType(outputType);
        
        if (data == null || data.isEmpty()) {
            throw new IllegalArgumentException("数据不能为空");
        }
        
        for (DataItem item : data) {
            if (item.getCategory() == null || item.getName() == null || item.getHeatValue() == null) {
                throw new IllegalArgumentException("数据项必须包含 category, name 和 heatValue 字段");
            }
        }
        
        var option = EChartOptionBuilder.buildHeatmapChartOption(title, axisXTitle, axisYTitle, data);
        
        return buildMCPResponseFromOption(option);
    }
    
    /**
     * 12. K线图
     */
    @McpTool(name = "generate_candlestick_chart", description = "生成 K 线图（蜡烛图），用于金融数据可视化")
    public Map<String, Object> generateCandlestickChart(
            @McpToolParam(description = "图表标题") String title,
            @McpToolParam(description = "K线数据") List<DataItem> data,
            @McpToolParam(description = "是否显示成交量图") Boolean showVolume,
            @McpToolParam(description = "输出类型，当前仅支持 option") String outputType) {
        
        validateOutputType(outputType);
        
        if (data == null || data.isEmpty()) {
            throw new IllegalArgumentException("数据不能为空");
        }
        
        for (DataItem item : data) {
            if (item.getDate() == null || item.getOpen() == null || item.getClose() == null
                    || item.getHigh() == null || item.getLow() == null) {
                throw new IllegalArgumentException("数据项必须包含 date, open, close, high, low 字段");
            }
            if (showVolume != null && showVolume && item.getVolume() == null) {
                throw new IllegalArgumentException("显示成交量时，数据项必须包含 volume 字段");
            }
        }
        
        var option = EChartOptionBuilder.buildCandlestickChartOption(
                title, data, showVolume != null && showVolume);
        
        return buildMCPResponseFromOption(option);
    }
    
    /**
     * 13. 箱线图
     */
    @McpTool(name = "generate_boxplot_chart", description = "生成箱线图，用于显示不同类别之间的数据统计摘要")
    public Map<String, Object> generateBoxplotChart(
            @McpToolParam(description = "图表标题") String title,
            @McpToolParam(description = "X 轴标题") String axisXTitle,
            @McpToolParam(description = "Y 轴标题") String axisYTitle,
            @McpToolParam(description = "图表数据") List<DataItem> data,
            @McpToolParam(description = "输出类型，当前仅支持 option") String outputType) {
        
        validateOutputType(outputType);
        
        if (data == null || data.isEmpty()) {
            throw new IllegalArgumentException("数据不能为空");
        }
        
        for (DataItem item : data) {
            if (item.getCategory() == null || item.getBoxplotData() == null || item.getBoxplotData().length != 5) {
                throw new IllegalArgumentException("数据项必须包含 category 和 boxplotData 字段，且 boxplotData 必须是长度为 5 的数组 [min, Q1, median, Q3, max]");
            }
        }
        
        var option = EChartOptionBuilder.buildBoxplotChartOption(title, axisXTitle, axisYTitle, data);
        
        return buildMCPResponseFromOption(option);
    }
    
    /**
     * 14. 关系图
     */
    @McpTool(name = "generate_graph_chart", description = "生成关系图（网络图），用于显示实体（节点）之间的关系")
    public Map<String, Object> generateGraphChart(
            @McpToolParam(description = "图表标题") String title,
            @McpToolParam(description = "图和边数据") GraphData data,
            @McpToolParam(description = "布局算法") String layout,
            @McpToolParam(description = "输出类型，当前仅支持 option") String outputType) {
        
        validateOutputType(outputType);
        
        if (data == null) {
            throw new IllegalArgumentException("数据不能为空");
        }
        
        if (data.getNodes() == null || data.getNodes().isEmpty()) {
            throw new IllegalArgumentException("节点数据不能为空");
        }
        
        if (data.getEdges() == null) {
            data.setEdges(new ArrayList<>());
        }
        
        for (GraphNode node : data.getNodes()) {
            if (node.getId() == null || node.getName() == null) {
                throw new IllegalArgumentException("节点必须包含 id 和 name 字段");
            }
        }
        
        Set<String> nodeIds = new HashSet<>();
        for (GraphNode node : data.getNodes()) {
            if (nodeIds.contains(node.getId())) {
                throw new IllegalArgumentException("节点 id 必须唯一: " + node.getId());
            }
            nodeIds.add(node.getId());
        }
        
        for (GraphEdge edge : data.getEdges()) {
            if (edge.getSource() == null || edge.getTarget() == null) {
                throw new IllegalArgumentException("边必须包含 source 和 target 字段");
            }
            if (!nodeIds.contains(edge.getSource()) || !nodeIds.contains(edge.getTarget())) {
                throw new IllegalArgumentException("边的 source 和 target 必须在节点中存在");
            }
        }
        
        var option = EChartOptionBuilder.buildGraphChartOption(title, data, layout);
        
        return buildMCPResponseFromOption(option);
    }
    
    /**
     * 15. 平行坐标系
     */
    @McpTool(name = "generate_parallel_chart", description = "生成平行坐标系图表，用于显示多维数据")
    public Map<String, Object> generateParallelChart(
            @McpToolParam(description = "图表标题") String title,
            @McpToolParam(description = "数据项") List<DataItem> data,
            @McpToolParam(description = "维度名称数组") List<String> dimensions,
            @McpToolParam(description = "输出类型，当前仅支持 option") String outputType) {
        
        validateOutputType(outputType);
        
        if (data == null || data.isEmpty()) {
            throw new IllegalArgumentException("数据不能为空");
        }
        
        if (dimensions == null || dimensions.isEmpty()) {
            throw new IllegalArgumentException("维度数组不能为空");
        }
        
        for (DataItem item : data) {
            if (item.getParallelValues() == null || item.getParallelValues().length != dimensions.size()) {
                throw new IllegalArgumentException("数据项的 parallelValues 数组长度必须与 dimensions 数组长度相同");
            }
        }
        
        var option = EChartOptionBuilder.buildParallelChartOption(title, data, dimensions);
        
        return buildMCPResponseFromOption(option);
    }
    
    /**
     * 16. 桑基图
     */
    @McpTool(name = "generate_sankey_chart", description = "生成桑基图，用于可视化数据在不同阶段或类别之间的流动")
    public Map<String, Object> generateSankeyChart(
            @McpToolParam(description = "图表标题") String title,
            @McpToolParam(description = "流动数据") List<DataItem> data,
            @McpToolParam(description = "节点对齐方式") String nodeAlign,
            @McpToolParam(description = "输出类型，当前仅支持 option") String outputType) {
        
        validateOutputType(outputType);
        
        if (data == null || data.isEmpty()) {
            throw new IllegalArgumentException("数据不能为空");
        }
        
        for (DataItem item : data) {
            if (item.getSource() == null || item.getTarget() == null || item.getValue() == null) {
                throw new IllegalArgumentException("数据项必须包含 source, target 和 value 字段");
            }
        }
        
        var option = EChartOptionBuilder.buildSankeyChartOption(title, data, nodeAlign);
        
        return buildMCPResponseFromOption(option);
    }
    
    /**
     * 17. 树图
     */
    @McpTool(name = "generate_tree_chart", description = "生成树图，用于显示层次化数据结构")
    public Map<String, Object> generateTreeChart(
            @McpToolParam(description = "图表标题") String title,
            @McpToolParam(description = "树形数据（单个根节点）") TreeNode data,
            @McpToolParam(description = "布局类型") String layout,
            @McpToolParam(description = "方向（仅 orthogonal 有效）") String orient,
            @McpToolParam(description = "输出类型，当前仅支持 option") String outputType) {
        
        validateOutputType(outputType);
        
        if (data == null) {
            throw new IllegalArgumentException("数据不能为空");
        }
        
        if (data.getName() == null) {
            throw new IllegalArgumentException("树节点必须包含 name 字段");
        }
        
        var option = EChartOptionBuilder.buildTreeChartOption(title, data, layout, orient);
        
        return buildMCPResponseFromOption(option);
    }
}

