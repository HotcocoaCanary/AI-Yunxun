package yunxun.ai.canary.backend.graph.service;

import org.springframework.stereotype.Service;
import yunxun.ai.canary.backend.graph.model.dto.ChartRequest;
import yunxun.ai.canary.backend.graph.model.dto.ChartResponse;

import java.util.*;

/**
 * 核心图表生成服务
 * <p>
 * 当前版本为简化实现，尚未连接到具体数据库，只返回结构化的图表配置，
 * 用于验证 MCP 工具架构和前端集成。
 * <p>
 * 后续可以接入真实数据源（如 Neo4j、MongoDB、MySQL），
 * 并在此处构建特定引擎的图表规格。
 */
@Service
public class GraphChartService {

    /**
     * 根据请求生成图表
     * 当前实现为演示版本，生成模拟数据
     */
    public ChartResponse generateChart(ChartRequest request) {
        String resolvedChartType = (request.getChartType() == null || request.getChartType().isBlank())
                ? "bar"
                : request.getChartType();

        String title = request.getQuestion() != null && !request.getQuestion().isBlank()
                ? request.getQuestion()
                : "Generated chart";

        // 当前未连接真实数据源
        // 为了提供更好的演示效果，当问题中提到"近10年"时，
        // 我们生成一个简单的单调递增时间序列数据，以便前端显示有意义的柱状图/折线图
        List<String> xValues;
        List<Number> yValues;

        if (title.contains("近10年") || title.contains("近十年")) {
            xValues = List.of("2015", "2016", "2017", "2018", "2019",
                    "2020", "2021", "2022", "2023", "2024");
            yValues = List.of(150, 170, 190, 210, 230, 260, 290, 320, 350, 380);
        } else {
            xValues = List.of("A", "B", "C", "D");
            yValues = List.of(10, 20, 15, 30);
        }

        // 使用 LinkedHashMap 和 ArrayList 避免类型推断问题
        Map<String, Object> titleMap = new LinkedHashMap<>();
        titleMap.put("text", title);
        
        Map<String, Object> tooltipMap = new LinkedHashMap<>();
        tooltipMap.put("trigger", "axis");
        
        Map<String, Object> xAxisMap = new LinkedHashMap<>();
        xAxisMap.put("type", "category");
        xAxisMap.put("data", xValues);
        
        Map<String, Object> yAxisMap = new LinkedHashMap<>();
        yAxisMap.put("type", "value");
        
        Map<String, Object> seriesItem = new LinkedHashMap<>();
        seriesItem.put("type", resolvedChartType.equals("line") ? "line" : "bar");
        seriesItem.put("data", new ArrayList<>(yValues)); // 转换为 ArrayList<Number>
        
        List<Map<String, Object>> seriesList = new ArrayList<>();
        seriesList.add(seriesItem);
        
        Map<String, Object> option = new LinkedHashMap<>();
        option.put("title", titleMap);
        option.put("tooltip", tooltipMap);
        option.put("xAxis", xAxisMap);
        option.put("yAxis", yAxisMap);
        option.put("series", seriesList);

        List<Map<String, Object>> dataRows = new ArrayList<>();
        for (int i = 0; i < xValues.size(); i++) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("x", xValues.get(i));
            row.put("value", yValues.get(i));
            dataRows.add(row);
        }

        return ChartResponse.builder()
                .chartType(resolvedChartType)
                .engine("echarts")
                .title(title)
                .description("由 GraphChartService 演示流程生成的模拟图表数据。")
                .chartSpec(option)
                .data(dataRows)
                .insightSummary("数值整体呈现上升趋势。")
                .insightBullets(List.of(
                        "横轴为类别或年份，纵轴为对应数值。",
                        "此数据为示例数据，用于演示图表生成与展示链路。"
                ))
                .build();
    }
}

