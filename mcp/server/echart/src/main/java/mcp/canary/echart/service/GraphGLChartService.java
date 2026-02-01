package mcp.canary.echart.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import mcp.canary.echart.model.GraphData;
import mcp.canary.echart.model.GraphEdge;
import mcp.canary.echart.model.GraphNode;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * GL 关系图服务。
 * 构建 ECharts GL 关系图 option（series[].type = "graphGL"），输入与 GraphChartService 一致：
 * title、data（nodes/edges）、layout、width、height、theme。
 */
@Service
public class GraphGLChartService {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 构建 GL 关系图 ECharts option。
     *
     * @param title  图表标题，可为 null
     * @param data   图数据（nodes、edges），不能为空
     * @param layout 布局：force / circular / none，null 时默认 force
     * @param width  画布宽度（像素），null 时默认 800
     * @param height 画布高度（像素），null 时默认 600
     * @param theme  主题：default / dark，null 时默认 default
     * @return ECharts option，series[].type = "graphGL"，含 data 与 links
     */
    public ObjectNode buildChartOption(String title, GraphData data, String layout,
                                       Integer width, Integer height, String theme) {
        validateData(data);
        return buildGraphGLOption(title, data, layout, width, height, theme);
    }

    private void validateData(GraphData data) {
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
    }

    private ObjectNode createBaseOption(String title, Integer width, Integer height, String theme) {
        ObjectNode option = objectMapper.createObjectNode();
        if (title != null && !title.isEmpty()) {
            ObjectNode titleNode = objectMapper.createObjectNode();
            titleNode.put("text", title);
            titleNode.put("left", "center");
            option.set("title", titleNode);
        }
        int w = (width != null && width > 0) ? width : 800;
        int h = (height != null && height > 0) ? height : 600;
        option.put("width", w);
        option.put("height", h);
        if ("dark".equalsIgnoreCase(theme)) {
            option.put("backgroundColor", "#1a1a1a");
        }
        return option;
    }

    private ObjectNode buildGraphGLOption(String title, GraphData data, String layout,
                                          Integer width, Integer height, String theme) {
        ObjectNode option = createBaseOption(title, width, height, theme);

        ObjectNode tooltip = objectMapper.createObjectNode();
        tooltip.put("trigger", "item");
        option.set("tooltip", tooltip);

        Set<String> categories = data.getNodes().stream()
                .map(GraphNode::getCategory)
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        if (!categories.isEmpty()) {
            ObjectNode legend = objectMapper.createObjectNode();
            ArrayNode legendData = objectMapper.createArrayNode();
            categories.forEach(legendData::add);
            legend.set("data", legendData);
            legend.put("left", "center");
            legend.put("bottom", 10);
            option.set("legend", legend);
        }

        Map<String, Integer> categoryIndexMap = new HashMap<>();
        int index = 0;
        for (String cat : categories) {
            categoryIndexMap.put(cat, index++);
        }

        ArrayNode seriesArray = objectMapper.createArrayNode();
        ObjectNode series = objectMapper.createObjectNode();
        series.put("type", "graphGL");
        series.put("layout", layout != null ? layout : "force");
        series.put("roam", true);

        ObjectNode label = objectMapper.createObjectNode();
        label.put("show", true);
        series.set("label", label);

        ArrayNode nodesArray = objectMapper.createArrayNode();
        for (GraphNode node : data.getNodes()) {
            ObjectNode nodeObj = objectMapper.createObjectNode();
            nodeObj.put("id", node.getId());
            nodeObj.put("name", node.getName());
            if (node.getValue() != null) {
                nodeObj.put("value", node.getValue().doubleValue());
            }
            if (node.getCategory() != null) {
                nodeObj.put("category", categoryIndexMap.getOrDefault(node.getCategory(), 0));
            }
            nodesArray.add(nodeObj);
        }
        series.set("data", nodesArray);

        ArrayNode linksArray = objectMapper.createArrayNode();
        Set<String> nodeIds = data.getNodes().stream()
                .map(GraphNode::getId)
                .collect(Collectors.toSet());

        for (GraphEdge edge : data.getEdges()) {
            if (nodeIds.contains(edge.getSource()) && nodeIds.contains(edge.getTarget())) {
                ObjectNode link = objectMapper.createObjectNode();
                link.put("source", edge.getSource());
                link.put("target", edge.getTarget());
                if (edge.getValue() != null) {
                    link.put("value", edge.getValue().doubleValue());
                }
                linksArray.add(link);
            }
        }
        series.set("links", linksArray);

        if (!categories.isEmpty()) {
            ArrayNode categoriesArray = objectMapper.createArrayNode();
            for (String cat : categories) {
                ObjectNode catObj = objectMapper.createObjectNode();
                catObj.put("name", cat);
                categoriesArray.add(catObj);
            }
            series.set("categories", categoriesArray);
        }

        seriesArray.add(series);
        option.set("series", seriesArray);
        return option;
    }
}
