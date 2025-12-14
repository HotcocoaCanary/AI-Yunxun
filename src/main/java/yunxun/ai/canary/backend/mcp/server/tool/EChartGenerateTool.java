package yunxun.ai.canary.backend.mcp.server.tool;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;
import yunxun.ai.canary.backend.graph.model.dto.ChartResponse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * ECharts 图表生成工具
 * 将图表生成能力暴露给前端 AI 代理，通过 MCP/SSE 接口调用
 * 负责生成 ECharts 可绘制的图表数据配置
 * 
 * 支持的图表类型：
 * - bar: 柱状图
 * - line: 折线图
 * - pie: 饼图
 * - graph: 力导向图（用于图谱可视化）
 * - scatter: 散点图
 * 
 * 工作流程：
 * 1. 用户提问（如"近10年考研人数变化趋势"）
 * 2. 大模型进行网络搜索，获取相关数据
 * 3. 将搜索到的数据整理为 JSON 数组格式
 * 4. 调用此工具生成 ECharts 图表配置
 */
@Component
public class EChartGenerateTool {

    private final ObjectMapper objectMapper;

    public EChartGenerateTool(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * 根据搜索到的数据生成图表（推荐使用）
     * 接受从网络搜索或其他数据源获取的原始数据，转换为 ECharts 可绘制的图表
     * 
     * 使用流程：
     * 1. 用户提问（如"近10年考研人数变化趋势"）
     * 2. 大模型先进行网络搜索，获取相关数据
     * 3. 将搜索到的数据整理为 JSON 数组格式
     * 4. 调用此工具生成图表
     * 
     * 数据格式要求：
     * - 时间序列数据：[{"year": "2015", "count": 150}, {"year": "2016", "count": 170}, ...]
     * - 分类数据：[{"category": "机器学习", "value": 50}, {"category": "深度学习", "value": 30}, ...]
     * - 图谱数据（graph 类型）：[{"nodes": [...], "links": [...]}]
     * - 散点数据（scatter 类型）：[{"x": 1.2, "y": 3.4}, {"x": 2.3, "y": 4.5}, ...]
     * - 支持自动识别字段名（year/date/time 作为 X 轴，count/value/amount 作为 Y 轴）
     */
    @Tool(
            name = "echart_generate",
            description = "根据原始数据生成 ECharts 图表配置。这是推荐使用的图表生成工具。可以接受从网络搜索、Neo4j、MongoDB 等数据源获取的数据，自动转换为前端可绘制的图表格式。支持 bar（柱状图）、line（折线图）、pie（饼图）、graph（力导向图）、scatter（散点图）等图表类型。数据应为 JSON 数组格式，每个元素是一个数据点。"
    )
    public ChartResponse generateChartFromData(
            @ToolParam(description = "图表标题，例如：'近10年考研人数变化趋势'")
            String title,

            @ToolParam(description = "原始数据，JSON 数组格式。示例：[{\"year\": \"2015\", \"count\": 150}, {\"year\": \"2016\", \"count\": 170}]。支持自动识别字段名，常见 X 轴字段：year/date/time/name/category，常见 Y 轴字段：count/value/amount/quantity。对于 graph 类型，数据格式应为：[{\"nodes\": [...], \"links\": [...]}]。对于 scatter 类型，数据格式应为：[{\"x\": 1.2, \"y\": 3.4}, ...]")
            String dataJson,

            @ToolParam(description = "图表类型：bar（柱状图）、line（折线图）、pie（饼图）、graph（力导向图）、scatter（散点图）。如果不指定，将根据数据自动选择（数据点>10用折线图，否则用柱状图）")
            String chartType,

            @ToolParam(description = "X 轴字段名，例如：'year'、'date'、'name'。如果不指定，将自动识别（优先：year/date/time/name/category）")
            String xField,

            @ToolParam(description = "Y 轴字段名，例如：'count'、'value'、'amount'。如果不指定，将自动识别（优先：count/value/amount/quantity）")
            String yField) {
        
        try {
            // 解析数据
            List<Map<String, Object>> dataList = objectMapper.readValue(
                    dataJson, 
                    new TypeReference<List<Map<String, Object>>>() {}
            );

            if (dataList.isEmpty()) {
                return createErrorResponse("数据为空，无法生成图表");
            }

            // 确定图表类型
            String resolvedChartType = chartType != null && !chartType.isBlank() 
                    ? chartType 
                    : (dataList.size() > 10 ? "line" : "bar");

            Map<String, Object> option;

            // 根据图表类型生成不同的配置
            switch (resolvedChartType) {
                case "graph":
                    option = generateGraphChart(dataList, title);
                    break;
                case "scatter":
                    option = generateScatterChart(dataList, title, xField, yField);
                    break;
                case "pie":
                    option = generatePieChart(dataList, title, xField, yField);
                    break;
                case "line":
                    option = generateLineChart(dataList, title, xField, yField);
                    break;
                case "bar":
                default:
                    option = generateBarChart(dataList, title, xField, yField);
                    break;
            }

            return ChartResponse.builder()
                    .chartType(resolvedChartType)
                    .engine("echarts")
                    .title(title != null ? title : "数据图表")
                    .description("根据提供的数据生成的图表")
                    .chartSpec(option)
                    .data(dataList)
                    .insightSummary("图表已生成，共 " + dataList.size() + " 个数据点。")
                    .build();

        } catch (JsonProcessingException e) {
            return createErrorResponse("数据格式错误: " + e.getMessage());
        } catch (Exception e) {
            return createErrorResponse("生成图表时出错: " + e.getMessage());
        }
    }

    /**
     * 生成柱状图配置
     */
    private Map<String, Object> generateBarChart(List<Map<String, Object>> dataList, String title, String xField, String yField) {
        Map<String, Object> firstRow = dataList.get(0);
        String resolvedXField = xField != null && !xField.isBlank() 
                ? xField 
                : autoDetectField(firstRow, "year", "date", "time", "name", "category", "label", "x");
        String resolvedYField = yField != null && !yField.isBlank() 
                ? yField 
                : autoDetectField(firstRow, "count", "value", "amount", "quantity", "number", "y");

        if (resolvedXField == null || resolvedYField == null) {
            throw new IllegalArgumentException("无法自动识别数据字段，请明确指定 xField 和 yField");
        }

        List<String> xValues = dataList.stream()
                .map(row -> String.valueOf(row.get(resolvedXField)))
                .toList();
        
        List<Number> yValues = dataList.stream()
                .map(row -> convertToNumber(row.get(resolvedYField)))
                .toList();

        Map<String, Object> option = new LinkedHashMap<>();
        Map<String, Object> titleMap = new LinkedHashMap<>();
        titleMap.put("text", title != null ? title : "数据图表");
        option.put("title", titleMap);
        
        Map<String, Object> tooltipMap = new LinkedHashMap<>();
        tooltipMap.put("trigger", "axis");
        option.put("tooltip", tooltipMap);
        
        Map<String, Object> xAxisMap = new LinkedHashMap<>();
        xAxisMap.put("type", "category");
        xAxisMap.put("data", xValues);
        option.put("xAxis", xAxisMap);
        
        Map<String, Object> yAxisMap = new LinkedHashMap<>();
        yAxisMap.put("type", "value");
        option.put("yAxis", yAxisMap);
        
        Map<String, Object> seriesItem = new LinkedHashMap<>();
        seriesItem.put("type", "bar");
        seriesItem.put("data", new ArrayList<>(yValues));
        option.put("series", List.of(seriesItem));
        
        return option;
    }

    /**
     * 生成折线图配置
     */
    private Map<String, Object> generateLineChart(List<Map<String, Object>> dataList, String title, String xField, String yField) {
        Map<String, Object> firstRow = dataList.get(0);
        String resolvedXField = xField != null && !xField.isBlank() 
                ? xField 
                : autoDetectField(firstRow, "year", "date", "time", "name", "category", "label", "x");
        String resolvedYField = yField != null && !yField.isBlank() 
                ? yField 
                : autoDetectField(firstRow, "count", "value", "amount", "quantity", "number", "y");

        if (resolvedXField == null || resolvedYField == null) {
            throw new IllegalArgumentException("无法自动识别数据字段，请明确指定 xField 和 yField");
        }

        List<String> xValues = dataList.stream()
                .map(row -> String.valueOf(row.get(resolvedXField)))
                .toList();
        
        List<Number> yValues = dataList.stream()
                .map(row -> convertToNumber(row.get(resolvedYField)))
                .toList();

        Map<String, Object> option = new LinkedHashMap<>();
        Map<String, Object> titleMap = new LinkedHashMap<>();
        titleMap.put("text", title != null ? title : "数据图表");
        option.put("title", titleMap);
        
        Map<String, Object> tooltipMap = new LinkedHashMap<>();
        tooltipMap.put("trigger", "axis");
        option.put("tooltip", tooltipMap);
        
        Map<String, Object> xAxisMap = new LinkedHashMap<>();
        xAxisMap.put("type", "category");
        xAxisMap.put("data", xValues);
        option.put("xAxis", xAxisMap);
        
        Map<String, Object> yAxisMap = new LinkedHashMap<>();
        yAxisMap.put("type", "value");
        option.put("yAxis", yAxisMap);
        
        Map<String, Object> seriesItem = new LinkedHashMap<>();
        seriesItem.put("type", "line");
        seriesItem.put("data", new ArrayList<>(yValues));
        option.put("series", List.of(seriesItem));
        
        return option;
    }

    /**
     * 生成饼图配置
     */
    private Map<String, Object> generatePieChart(List<Map<String, Object>> dataList, String title, String xField, String yField) {
        Map<String, Object> firstRow = dataList.get(0);
        String resolvedXField = xField != null && !xField.isBlank() 
                ? xField 
                : autoDetectField(firstRow, "name", "category", "label", "x");
        String resolvedYField = yField != null && !yField.isBlank() 
                ? yField 
                : autoDetectField(firstRow, "value", "count", "amount", "quantity", "number", "y");

        if (resolvedXField == null || resolvedYField == null) {
            throw new IllegalArgumentException("无法自动识别数据字段，请明确指定 xField 和 yField");
        }

        List<Map<String, Object>> pieData = new ArrayList<>();
        for (Map<String, Object> row : dataList) {
            Map<String, Object> pieItem = new LinkedHashMap<>();
            pieItem.put("name", String.valueOf(row.get(resolvedXField)));
            pieItem.put("value", convertToNumber(row.get(resolvedYField)));
            pieData.add(pieItem);
        }

        Map<String, Object> option = new LinkedHashMap<>();
        Map<String, Object> titleMap = new LinkedHashMap<>();
        titleMap.put("text", title != null ? title : "数据图表");
        option.put("title", titleMap);
        
        Map<String, Object> tooltipMap = new LinkedHashMap<>();
        tooltipMap.put("trigger", "item");
        option.put("tooltip", tooltipMap);
        
        Map<String, Object> seriesItem = new LinkedHashMap<>();
        seriesItem.put("type", "pie");
        seriesItem.put("data", pieData);
        option.put("series", List.of(seriesItem));
        
        return option;
    }

    /**
     * 生成力导向图配置（用于图谱可视化）
     */
    private Map<String, Object> generateGraphChart(List<Map<String, Object>> dataList, String title) {
        // 检查数据格式：应该是包含 nodes 和 links 的对象
        if (dataList.size() == 1 && dataList.get(0).containsKey("nodes") && dataList.get(0).containsKey("links")) {
            Map<String, Object> graphData = dataList.get(0);
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> nodes = (List<Map<String, Object>>) graphData.get("nodes");
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> links = (List<Map<String, Object>>) graphData.get("links");

            // 转换 links 为 ECharts 格式（source, target）
            List<Map<String, Object>> edges = links.stream()
                    .map(link -> {
                        Map<String, Object> edge = new HashMap<>();
                        edge.put("source", link.get("source"));
                        edge.put("target", link.get("target"));
                        if (link.containsKey("label")) {
                            edge.put("label", link.get("label"));
                        }
                        return edge;
                    })
                    .toList();

            Map<String, Object> option = new LinkedHashMap<>();
            Map<String, Object> titleMap = new LinkedHashMap<>();
            titleMap.put("text", title != null ? title : "图谱可视化");
            option.put("title", titleMap);
            option.put("tooltip", new LinkedHashMap<>());
            
            Map<String, Object> seriesItem = new LinkedHashMap<>();
            seriesItem.put("type", "graph");
            seriesItem.put("layout", "force");
            seriesItem.put("data", nodes);
            seriesItem.put("links", edges);
            seriesItem.put("roam", true);
            
            Map<String, Object> labelMap = new LinkedHashMap<>();
            labelMap.put("show", true);
            seriesItem.put("label", labelMap);
            
            Map<String, Object> edgeLabelMap = new LinkedHashMap<>();
            edgeLabelMap.put("show", true);
            seriesItem.put("edgeLabel", edgeLabelMap);
            
            Map<String, Object> forceMap = new LinkedHashMap<>();
            forceMap.put("repulsion", 100);
            forceMap.put("gravity", 0.1);
            forceMap.put("edgeLength", 50);
            seriesItem.put("force", forceMap);
            
            option.put("series", List.of(seriesItem));
            return option;
        } else {
            throw new IllegalArgumentException("graph 类型图表需要数据格式为：[{\"nodes\": [...], \"links\": [...]}]");
        }
    }

    /**
     * 生成散点图配置
     */
    private Map<String, Object> generateScatterChart(List<Map<String, Object>> dataList, String title, String xField, String yField) {
        Map<String, Object> firstRow = dataList.get(0);
        String resolvedXField = xField != null && !xField.isBlank() 
                ? xField 
                : autoDetectField(firstRow, "x", "xValue", "xAxis");
        String resolvedYField = yField != null && !yField.isBlank() 
                ? yField 
                : autoDetectField(firstRow, "y", "yValue", "yAxis");

        if (resolvedXField == null || resolvedYField == null) {
            throw new IllegalArgumentException("无法自动识别数据字段，请明确指定 xField 和 yField");
        }

        List<List<Number>> scatterData = new ArrayList<>();
        for (Map<String, Object> row : dataList) {
            List<Number> point = new ArrayList<>();
            point.add(convertToNumber(row.get(resolvedXField)).doubleValue());
            point.add(convertToNumber(row.get(resolvedYField)).doubleValue());
            scatterData.add(point);
        }

        Map<String, Object> option = new LinkedHashMap<>();
        Map<String, Object> titleMap = new LinkedHashMap<>();
        titleMap.put("text", title != null ? title : "散点图");
        option.put("title", titleMap);
        
        Map<String, Object> tooltipMap = new LinkedHashMap<>();
        tooltipMap.put("trigger", "item");
        option.put("tooltip", tooltipMap);
        
        Map<String, Object> xAxisMap = new LinkedHashMap<>();
        xAxisMap.put("type", "value");
        option.put("xAxis", xAxisMap);
        
        Map<String, Object> yAxisMap = new LinkedHashMap<>();
        yAxisMap.put("type", "value");
        option.put("yAxis", yAxisMap);
        
        Map<String, Object> seriesItem = new LinkedHashMap<>();
        seriesItem.put("type", "scatter");
        seriesItem.put("data", scatterData);
        option.put("series", List.of(seriesItem));
        
        return option;
    }

    /**
     * 自动检测字段名
     */
    private String autoDetectField(Map<String, Object> row, String... candidates) {
        for (String candidate : candidates) {
            if (row.containsKey(candidate)) {
                return candidate;
            }
        }
        // 如果候选字段都不存在，返回第一个键
        return row.keySet().isEmpty() ? null : row.keySet().iterator().next();
    }

    /**
     * 转换为数字
     */
    private Number convertToNumber(Object value) {
        if (value instanceof Number) {
            return (Number) value;
        } else if (value instanceof String) {
            try {
                return Double.parseDouble((String) value);
            } catch (NumberFormatException e) {
                return 0;
            }
        }
        return 0;
    }

    /**
     * 创建错误响应
     */
    private ChartResponse createErrorResponse(String errorMessage) {
        return ChartResponse.builder()
                .chartType("bar")
                .engine("echarts")
                .title("错误")
                .description(errorMessage)
                .chartSpec(new LinkedHashMap<>())
                .insightSummary(errorMessage)
                .build();
    }
}

