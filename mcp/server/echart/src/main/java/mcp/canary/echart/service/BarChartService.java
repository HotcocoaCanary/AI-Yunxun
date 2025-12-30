package mcp.canary.echart.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import mcp.canary.echart.model.DataItem;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 柱状图服务
 * 负责柱状图的数据验证和配置构建
 */
@Service
public class BarChartService {
    
    private static final ObjectMapper objectMapper = new ObjectMapper();
    
    /**
     * 构建柱状图配置
     */
    public ObjectNode buildChartOption(
            String title,
            String axisXTitle,
            String axisYTitle,
            List<DataItem> data,
            boolean group,
            boolean stack) {
        validateData(data, group, stack);
        return buildBarChartOption(title, axisXTitle, axisYTitle, data, group, stack);
    }
    
    /**
     * 验证数据
     */
    private void validateData(List<DataItem> data, boolean group, boolean stack) {
        if (data == null || data.isEmpty()) {
            throw new IllegalArgumentException("数据不能为空");
        }
        
        for (DataItem item : data) {
            if (item.getCategory() == null || item.getValue() == null) {
                throw new IllegalArgumentException("数据项必须包含 category 和 value 字段");
            }
        }
        
        if (group && stack) {
            throw new IllegalArgumentException("group 和 stack 不能同时为 true");
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
     * 构建柱状图 option
     */
    private ObjectNode buildBarChartOption(
            String title,
            String axisXTitle,
            String axisYTitle,
            List<DataItem> data,
            boolean group,
            boolean stack) {
        ObjectNode option = createBaseOption(title);
        
        // Tooltip
        ObjectNode tooltip = objectMapper.createObjectNode();
        tooltip.put("trigger", "axis");
        option.set("tooltip", tooltip);
        
        // X 轴
        Set<String> categories = data.stream()
                .map(DataItem::getCategory)
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        
        ObjectNode xAxis = objectMapper.createObjectNode();
        xAxis.put("type", "category");
        ArrayNode xAxisData = objectMapper.createArrayNode();
        categories.forEach(xAxisData::add);
        xAxis.set("data", xAxisData);
        if (axisXTitle != null && !axisXTitle.isEmpty()) {
            xAxis.put("name", axisXTitle);
        }
        option.set("xAxis", xAxis);
        
        // Y 轴
        ObjectNode yAxis = objectMapper.createObjectNode();
        yAxis.put("type", "value");
        if (axisYTitle != null && !axisYTitle.isEmpty()) {
            yAxis.put("name", axisYTitle);
        }
        option.set("yAxis", yAxis);
        
        // Series
        ArrayNode seriesArray = objectMapper.createArrayNode();
        boolean hasGroups = data.stream().anyMatch(item -> item.getGroup() != null && !item.getGroup().isEmpty());
        
        if (hasGroups && (group || stack)) {
            // 多系列处理
            Map<String, List<DataItem>> groupMap = data.stream()
                    .filter(item -> item.getGroup() != null && !item.getGroup().isEmpty())
                    .collect(Collectors.groupingBy(DataItem::getGroup));
            
            List<String> categoryList = new ArrayList<>(categories);
            
            for (Map.Entry<String, List<DataItem>> entry : groupMap.entrySet()) {
                ObjectNode series = objectMapper.createObjectNode();
                series.put("name", entry.getKey());
                series.put("type", "bar");
                
                if (stack) {
                    series.put("stack", "Total");
                }
                
                ArrayNode seriesData = objectMapper.createArrayNode();
                for (String category : categoryList) {
                    Optional<DataItem> item = entry.getValue().stream()
                            .filter(d -> category.equals(d.getCategory()))
                            .findFirst();
                    seriesData.add(item.map(DataItem::getValue).orElse(0).shortValue());
                }
                series.set("data", seriesData);
                seriesArray.add(series);
            }
            
            // 添加图例
            ObjectNode legend = objectMapper.createObjectNode();
            legend.put("left", "center");
            legend.put("orient", "horizontal");
            legend.put("bottom", 10);
            option.set("legend", legend);
        } else {
            // 单系列处理
            ObjectNode series = objectMapper.createObjectNode();
            series.put("type", "bar");
            ArrayNode seriesData = objectMapper.createArrayNode();
            for (String category : categories) {
                Optional<DataItem> item = data.stream()
                        .filter(d -> category.equals(d.getCategory()))
                        .findFirst();
                seriesData.add(item.map(DataItem::getValue).orElse(0).shortValue());
            }
            series.set("data", seriesData);
            seriesArray.add(series);
        }
        
        option.set("series", seriesArray);
        return option;
    }
}

