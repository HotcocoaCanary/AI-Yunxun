package yunxun.ai.canary.project.service.mcp.server.tool;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;
import yunxun.ai.canary.project.service.mcp.server.tool.model.ToolResponse;
import yunxun.ai.canary.project.service.mcp.server.tool.model.ToolResponses;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * ECharts 图表生成工具
 * 将图表生成能力暴露给前端 AI 代理，通过 MCP/SSE 接口调用
 * 负责生成 ECharts 可绘制的图表数据配置
 * 支持的图表类型：
 * - bar: 柱状图
 * - line: 折线图
 * - pie: 饼图
 * - graph: 力导向图（用于图谱可视化）
 * - scatter: 散点图
 */
@Component
public class EChartGenerateTool {

    private final ObjectMapper objectMapper;

    public EChartGenerateTool(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Tool(name = "echart_generate", description = "根据原始数据生成 ECharts option（支持 bar/line/pie/scatter/graph）")
    public ToolResponse echartGenerate(
            @ToolParam(required = true, description = "图表类型：bar/line/pie/scatter/graph") String chartType,
            @ToolParam(required = false, description = "标题") String title,
            @ToolParam(required = false, description = "通用数据列表（用于 bar/line/pie/scatter）") List<Map<String, Object>> data,
            @ToolParam(required = false, description = "字段映射：xField/yField/seriesField 等") Map<String, Object> mapping,
            @ToolParam(required = false, description = "额外选项（unit/stack/smooth 等）") Map<String, Object> options,
            @ToolParam(required = false, description = "graph 类型输入：{nodes:[], links:[]}") Map<String, Object> graph) {
        Instant startedAt = Instant.now();
        String traceId = UUID.randomUUID().toString();
        try {
            if (chartType == null || chartType.isBlank()) {
                return ToolResponses.error("INVALID_ARGUMENT", "chartType 不能为空", null, traceId, startedAt);
            }

            ObjectNode option = buildOption(chartType.trim().toLowerCase(), title, data, mapping, options, graph);
            return ToolResponses.ok(Map.of("option", option), traceId, startedAt);
        }
        catch (Exception ex) {
            return ToolResponses.error("INVALID_ARGUMENT", ex.getMessage(), null, traceId, startedAt);
        }
    }

    private ObjectNode buildOption(
            String chartType,
            String title,
            List<Map<String, Object>> data,
            Map<String, Object> mapping,
            Map<String, Object> options,
            Map<String, Object> graph) {
        ObjectNode option = objectMapper.createObjectNode();
        if (title != null && !title.isBlank()) {
            option.set("title", objectMapper.createObjectNode().put("text", title));
        }
        option.set("tooltip", objectMapper.createObjectNode().put("trigger", "axis"));

        switch (chartType) {
            case "pie" -> buildPie(option, data, mapping);
            case "graph" -> buildGraph(option, graph);
            case "scatter" -> buildXY(option, "scatter", data, mapping);
            case "bar" -> buildXY(option, "bar", data, mapping);
            case "line" -> buildXY(option, "line", data, mapping);
            default -> throw new IllegalArgumentException("不支持的 chartType: " + chartType);
        }

        if (options != null && !options.isEmpty()) {
            option.set("aiOptions", objectMapper.valueToTree(options));
        }
        return option;
    }

    private void buildXY(ObjectNode option, String type, List<Map<String, Object>> data, Map<String, Object> mapping) {
        if (data == null || data.isEmpty()) {
            throw new IllegalArgumentException("data 不能为空");
        }

        String xField = getMapping(mapping, "xField", "x");
        String yField = getMapping(mapping, "yField", "y");
        String seriesField = getMapping(mapping, "seriesField", null);

        List<Object> xAxis = new ArrayList<>();
        Map<String, List<Object>> seriesData = new LinkedHashMap<>();

        for (Map<String, Object> row : data) {
            Object x = row.get(xField);
            Object y = row.get(yField);
            if (x == null) {
                continue;
            }
            xAxis.add(x);
            String seriesName = seriesField == null ? "series" : Objects.toString(row.get(seriesField), "series");
            seriesData.computeIfAbsent(seriesName, k -> new ArrayList<>()).add(y);
        }

        option.set("xAxis", objectMapper.createObjectNode().put("type", "category").set("data", objectMapper.valueToTree(xAxis)));
        option.set("yAxis", objectMapper.createObjectNode().put("type", "value"));

        ArrayNode series = objectMapper.createArrayNode();
        for (Map.Entry<String, List<Object>> entry : seriesData.entrySet()) {
            ObjectNode seriesItem = objectMapper.createObjectNode();
            seriesItem.put("name", entry.getKey());
            seriesItem.put("type", type);
            seriesItem.set("data", objectMapper.valueToTree(entry.getValue()));
            series.add(seriesItem);
        }
        option.set("series", series);
    }

    private void buildPie(ObjectNode option, List<Map<String, Object>> data, Map<String, Object> mapping) {
        if (data == null || data.isEmpty()) {
            throw new IllegalArgumentException("data 不能为空");
        }

        String nameField = getMapping(mapping, "nameField", getMapping(mapping, "xField", "name"));
        String valueField = getMapping(mapping, "valueField", getMapping(mapping, "yField", "value"));

        ArrayNode seriesData = objectMapper.createArrayNode();
        for (Map<String, Object> row : data) {
            ObjectNode item = objectMapper.createObjectNode();
            item.put("name", Objects.toString(row.get(nameField), ""));
            item.set("value", objectMapper.valueToTree(row.get(valueField)));
            seriesData.add(item);
        }

        ObjectNode series = objectMapper.createObjectNode();
        series.put("type", "pie");
        series.put("radius", "50%");
        series.set("data", seriesData);
        option.set("series", objectMapper.createArrayNode().add(series));

        option.set("tooltip", objectMapper.createObjectNode().put("trigger", "item"));
    }

    @SuppressWarnings("unchecked")
    private void buildGraph(ObjectNode option, Map<String, Object> graph) {
        if (graph == null || graph.isEmpty()) {
            throw new IllegalArgumentException("graph 不能为空（nodes/links）");
        }
        Object nodes = graph.get("nodes");
        Object links = graph.get("links");
        if (!(nodes instanceof List) || !(links instanceof List)) {
            throw new IllegalArgumentException("graph.nodes 与 graph.links 必须是数组");
        }

        ObjectNode series = objectMapper.createObjectNode();
        series.put("type", "graph");
        series.put("layout", "force");
        series.put("roam", true);
        series.set("data", objectMapper.valueToTree(nodes));
        series.set("links", objectMapper.valueToTree(links));

        ObjectNode label = objectMapper.createObjectNode();
        label.put("show", true);
        series.set("label", label);

        option.set("series", objectMapper.createArrayNode().add(series));
        option.set("tooltip", objectMapper.createObjectNode().put("trigger", "item"));
    }

    private static String getMapping(Map<String, Object> mapping, String key, String defaultValue) {
        if (mapping == null) {
            return defaultValue;
        }
        Object value = mapping.get(key);
        return value == null ? defaultValue : value.toString();
    }
}
