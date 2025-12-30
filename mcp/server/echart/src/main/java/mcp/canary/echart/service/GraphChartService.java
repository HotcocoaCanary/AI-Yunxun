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
 * 关系图服务
 * 负责关系图的数据验证和配置构建
 */
@Service
public class GraphChartService {
    
    private static final ObjectMapper objectMapper = new ObjectMapper();
    
    /**
     * 构建关系图配置
     */
    public ObjectNode buildChartOption(String title, GraphData data, String layout) {
        validateData(data);
        return buildGraphChartOption(title, data, layout);
    }
    
    /**
     * 验证数据
     */
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
    
    /**
     * 创建基础 option 结构
     */
    private ObjectNode createBaseOption(String title) {
        ObjectNode option = objectMapper.createObjectNode();
        if (title != null && !title.isEmpty()) {
            ObjectNode titleNode = objectMapper.createObjectNode();
            titleNode.put("text", title);
            titleNode.put("left", "center");
            option.set("title", titleNode);
        }
        return option;
    }
    
    /**
     * 构建关系图 option
     */
    private ObjectNode buildGraphChartOption(String title, GraphData data, String layout) {
        ObjectNode option = createBaseOption(title);
        
        // Tooltip
        ObjectNode tooltip = objectMapper.createObjectNode();
        tooltip.put("trigger", "item");
        option.set("tooltip", tooltip);
        
        // 提取类别
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
        
        // 构建类别索引映射
        Map<String, Integer> categoryIndexMap = new HashMap<>();
        int index = 0;
        for (String cat : categories) {
            categoryIndexMap.put(cat, index++);
        }
        
        // Series
        ArrayNode seriesArray = objectMapper.createArrayNode();
        ObjectNode series = objectMapper.createObjectNode();
        series.put("type", "graph");
        series.put("layout", layout != null ? layout : "force");
        series.put("roam", true);
        
        ObjectNode label = objectMapper.createObjectNode();
        label.put("show", true);
        label.put("position", "right");
        series.set("label", label);
        
        ObjectNode labelLayout = objectMapper.createObjectNode();
        labelLayout.put("hideOverlap", true);
        series.set("labelLayout", labelLayout);
        
        ObjectNode scaleLimit = objectMapper.createObjectNode();
        scaleLimit.put("min", 0.4);
        scaleLimit.put("max", 2);
        series.set("scaleLimit", scaleLimit);
        
        ObjectNode lineStyle = objectMapper.createObjectNode();
        lineStyle.put("color", "source");
        lineStyle.put("curveness", 0.3);
        series.set("lineStyle", lineStyle);
        
        // Nodes
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
        
        // Links
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
        
        // Categories
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

