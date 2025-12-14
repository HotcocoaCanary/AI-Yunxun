package yunxun.ai.canary.backend.mcp.server.tool;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;
import yunxun.ai.canary.backend.graph.model.dto.ChartRequest;
import yunxun.ai.canary.backend.graph.model.dto.ChartResponse;
import yunxun.ai.canary.backend.graph.service.GraphChartService;

import java.util.List;
import java.util.Map;

/**
 * 图表生成工具
 * 将图表生成能力暴露给前端 AI 代理，通过 MCP/SSE 接口调用
 * 负责生成 ECharts 可绘制的图表数据配置
 * 
 * 工作流程：
 * 1. 用户提问（如"近10年考研人数变化趋势"）
 * 2. 大模型进行网络搜索，获取相关数据
 * 3. 将搜索到的数据整理为 JSON 数组格式
 * 4. 调用此工具生成 ECharts 图表配置
 */
@Component
public class GraphChartTool {

    private final GraphChartService graphChartService;
    private final ObjectMapper objectMapper;

    public GraphChartTool(GraphChartService graphChartService, ObjectMapper objectMapper) {
        this.graphChartService = graphChartService;
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
     * - 支持自动识别字段名（year/date/time 作为 X 轴，count/value/amount 作为 Y 轴）
     */
    @Tool(
            name = "generate_chart_from_data",
            description = "根据原始数据生成 ECharts 图表配置。这是推荐使用的图表生成工具。可以接受从网络搜索、Neo4j、MongoDB 等数据源获取的数据，自动转换为前端可绘制的图表格式。数据应为 JSON 数组格式，每个元素是一个数据点。"
    )
    public ChartResponse generateChartFromData(
            @ToolParam(description = "图表标题，例如：'近10年考研人数变化趋势'")
            String title,

            @ToolParam(description = "原始数据，JSON 数组格式。示例：[{\"year\": \"2015\", \"count\": 150}, {\"year\": \"2016\", \"count\": 170}]。支持自动识别字段名，常见 X 轴字段：year/date/time/name/category，常见 Y 轴字段：count/value/amount/quantity")
            String dataJson,

            @ToolParam(description = "图表类型：bar（柱状图）、line（折线图）、pie（饼图）。如果不指定，将根据数据自动选择（数据点>10用折线图，否则用柱状图）")
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

            // 自动识别字段名
            Map<String, Object> firstRow = dataList.get(0);
            String resolvedXField = xField != null && !xField.isBlank() 
                    ? xField 
                    : autoDetectField(firstRow, "year", "date", "time", "name", "category", "label", "x");
            String resolvedYField = yField != null && !yField.isBlank() 
                    ? yField 
                    : autoDetectField(firstRow, "count", "value", "amount", "quantity", "number", "y");

            if (resolvedXField == null || resolvedYField == null) {
                return createErrorResponse("无法自动识别数据字段，请明确指定 xField 和 yField");
            }

            // 提取数据值
            List<String> xValues = dataList.stream()
                    .map(row -> String.valueOf(row.get(resolvedXField)))
                    .toList();
            
            List<Number> yValues = dataList.stream()
                    .map(row -> {
                        Object val = row.get(resolvedYField);
                        if (val instanceof Number) {
                            return (Number) val;
                        } else if (val instanceof String) {
                            try {
                                return Double.parseDouble((String) val);
                            } catch (NumberFormatException e) {
                                return 0;
                            }
                        }
                        return 0;
                    })
                    .toList();

            // 确定图表类型
            String resolvedChartType = chartType != null && !chartType.isBlank() 
                    ? chartType 
                    : (dataList.size() > 10 ? "line" : "bar");

            // 生成 ECharts 配置
            Map<String, Object> option = Map.of(
                    "title", Map.of("text", title != null ? title : "数据图表"),
                    "tooltip", Map.of("trigger", "axis"),
                    "xAxis", Map.of("type", "category", "data", xValues),
                    "yAxis", Map.of("type", "value"),
                    "series", List.of(
                            Map.of(
                                    "type", resolvedChartType.equals("line") ? "line" : 
                                            resolvedChartType.equals("pie") ? "pie" : "bar",
                                    "data", resolvedChartType.equals("pie") 
                                            ? dataList.stream()
                                                    .map(row -> Map.of(
                                                            "name", String.valueOf(row.get(resolvedXField)),
                                                            "value", row.get(resolvedYField)
                                                    ))
                                                    .toList()
                                            : yValues
                            )
                    )
            );

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
     * 创建错误响应
     */
    private ChartResponse createErrorResponse(String errorMessage) {
        return ChartResponse.builder()
                .chartType("bar")
                .engine("echarts")
                .title("错误")
                .description(errorMessage)
                .chartSpec(Map.of())
                .insightSummary(errorMessage)
                .build();
    }
}

