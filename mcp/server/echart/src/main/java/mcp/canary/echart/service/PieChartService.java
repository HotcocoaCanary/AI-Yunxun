package mcp.canary.echart.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import mcp.canary.echart.model.DataItem;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 饼图服务
 * 负责饼图的数据验证和配置构建
 */
@Service
public class PieChartService {
    
    private static final ObjectMapper objectMapper = new ObjectMapper();
    
    /**
     * 构建饼图配置
     */
    public ObjectNode buildChartOption(String title, List<DataItem> data, double innerRadius) {
        validateData(data);
        return buildPieChartOption(title, data, innerRadius);
    }
    
    /**
     * 验证数据
     */
    private void validateData(List<DataItem> data) {
        if (data == null || data.isEmpty()) {
            throw new IllegalArgumentException("数据不能为空");
        }
        
        for (DataItem item : data) {
            if (item.getCategory() == null || item.getValue() == null) {
                throw new IllegalArgumentException("数据项必须包含 category 和 value 字段");
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
     * 构建饼图 option
     */
    private ObjectNode buildPieChartOption(String title, List<DataItem> data, double innerRadius) {
        ObjectNode option = createBaseOption(title);
        
        // Tooltip
        ObjectNode tooltip = objectMapper.createObjectNode();
        tooltip.put("trigger", "item");
        tooltip.put("formatter", "{a} <br/>{b}: {c} ({d}%)");
        option.set("tooltip", tooltip);
        
        // Legend
        ObjectNode legend = objectMapper.createObjectNode();
        legend.put("left", "center");
        legend.put("orient", "horizontal");
        legend.put("top", "bottom");
        option.set("legend", legend);
        
        // Series
        ArrayNode seriesArray = objectMapper.createArrayNode();
        ObjectNode series = objectMapper.createObjectNode();
        series.put("type", "pie");
        
        ArrayNode pieData = objectMapper.createArrayNode();
        for (DataItem item : data) {
            ObjectNode dataItem = objectMapper.createObjectNode();
            dataItem.put("name", item.getCategory());
            dataItem.put("value", item.getValue() != null ? item.getValue().doubleValue() : 0);
            pieData.add(dataItem);
        }
        series.set("data", pieData);
        
        // Radius 配置
        if (innerRadius > 0) {
            ArrayNode radius = objectMapper.createArrayNode();
            radius.add(String.format("%.0f%%", innerRadius * 100));
            radius.add("70%");
            series.set("radius", radius);
        } else {
            series.put("radius", "70%");
        }
        
        // Emphasis
        ObjectNode emphasis = objectMapper.createObjectNode();
        ObjectNode itemStyle = objectMapper.createObjectNode();
        itemStyle.put("shadowBlur", 10);
        itemStyle.put("shadowOffsetX", 0);
        itemStyle.put("shadowColor", "rgba(0, 0, 0, 0.5)");
        emphasis.set("itemStyle", itemStyle);
        series.set("emphasis", emphasis);
        
        seriesArray.add(series);
        option.set("series", seriesArray);
        return option;
    }
}

