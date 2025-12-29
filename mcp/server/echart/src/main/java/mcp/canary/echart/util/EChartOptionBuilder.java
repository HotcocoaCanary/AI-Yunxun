package mcp.canary.echart.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import mcp.canary.echart.model.DataItem;
import mcp.canary.echart.model.GraphData;
import mcp.canary.echart.model.GraphEdge;
import mcp.canary.echart.model.GraphNode;
import mcp.canary.echart.model.TreeNode;

import java.util.*;
import java.util.stream.Collectors;

/**
 * ECharts Option 构建工具类
 * 提供各种图表类型的 option 构建方法
 */
public class EChartOptionBuilder {
    
    private static final ObjectMapper objectMapper = new ObjectMapper();
    
    /**
     * 构建基础 option 结构（包含 title 和 tooltip）
     */
    private static ObjectNode createBaseOption(String title) {
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
    public static ObjectNode buildBarChartOption(String title, String axisXTitle, String axisYTitle,
                                                  List<DataItem> data, boolean group, boolean stack) {
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
        
        // 检查是否有分组
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
    
    /**
     * 构建折线图 option
     */
    public static ObjectNode buildLineChartOption(String title, String axisXTitle, String axisYTitle,
                                                   List<DataItem> data, boolean smooth, boolean showArea,
                                                   boolean showSymbol, boolean stack) {
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
        
        if (hasGroups) {
            Map<String, List<DataItem>> groupMap = data.stream()
                    .filter(item -> item.getGroup() != null && !item.getGroup().isEmpty())
                    .collect(Collectors.groupingBy(DataItem::getGroup));
            
            List<String> categoryList = new ArrayList<>(categories);
            
            for (Map.Entry<String, List<DataItem>> entry : groupMap.entrySet()) {
                ObjectNode series = objectMapper.createObjectNode();
                series.put("name", entry.getKey());
                series.put("type", "line");
                series.put("smooth", smooth);
                if (showArea) {
                    series.set("areaStyle", objectMapper.createObjectNode());
                }
                series.put("showSymbol", showSymbol);
                
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
            
            ObjectNode legend = objectMapper.createObjectNode();
            legend.put("left", "center");
            legend.put("orient", "horizontal");
            legend.put("bottom", 10);
            option.set("legend", legend);
        } else {
            ObjectNode series = objectMapper.createObjectNode();
            series.put("type", "line");
            series.put("smooth", smooth);
            if (showArea) {
                series.set("areaStyle", objectMapper.createObjectNode());
            }
            series.put("showSymbol", showSymbol);
            
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
    
    /**
     * 构建饼图 option
     */
    public static ObjectNode buildPieChartOption(String title, List<DataItem> data, double innerRadius) {
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
    
    /**
     * 构建散点图 option
     */
    public static ObjectNode buildScatterChartOption(String title, String axisXTitle, String axisYTitle,
                                                      List<DataItem> data) {
        ObjectNode option = createBaseOption(title);
        
        // Tooltip
        ObjectNode tooltip = objectMapper.createObjectNode();
        tooltip.put("trigger", "item");
        option.set("tooltip", tooltip);
        
        // X 轴
        ObjectNode xAxis = objectMapper.createObjectNode();
        xAxis.put("type", "value");
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
        ObjectNode series = objectMapper.createObjectNode();
        series.put("type", "scatter");
        
        ArrayNode scatterData = objectMapper.createArrayNode();
        for (DataItem item : data) {
            ArrayNode point = objectMapper.createArrayNode();
            point.add(item.getX() != null ? item.getX().doubleValue() : 0);
            point.add(item.getY() != null ? item.getY().doubleValue() : 0);
            scatterData.add(point);
        }
        series.set("data", scatterData);
        
        seriesArray.add(series);
        option.set("series", seriesArray);
        return option;
    }
    
    /**
     * 构建雷达图 option
     */
    public static ObjectNode buildRadarChartOption(String title, List<DataItem> data) {
        ObjectNode option = createBaseOption(title);
        if (title != null && !title.isEmpty()) {
            ObjectNode titleNode = (ObjectNode) option.get("title");
            titleNode.put("top", "5%");
        }
        
        // Tooltip
        ObjectNode tooltip = objectMapper.createObjectNode();
        tooltip.put("trigger", "item");
        option.set("tooltip", tooltip);
        
        // 提取所有唯一的维度名称
        Set<String> dimensions = data.stream()
                .map(DataItem::getName)
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        
        List<String> dimensionList = new ArrayList<>(dimensions);
        Collections.sort(dimensionList);
        
        // 计算最大值（向上取整到10的倍数）
        double maxValue = data.stream()
                .filter(item -> item.getValue() != null)
                .mapToDouble(item -> item.getValue().doubleValue())
                .max()
                .orElse(100);
        maxValue = Math.ceil(maxValue / 10) * 10;
        if (maxValue == 0) maxValue = 100;
        
        // Radar 配置
        ObjectNode radar = objectMapper.createObjectNode();
        ArrayNode indicators = objectMapper.createArrayNode();
        for (String dim : dimensionList) {
            ObjectNode indicator = objectMapper.createObjectNode();
            indicator.put("name", dim);
            indicator.put("max", maxValue);
            indicators.add(indicator);
        }
        radar.set("indicator", indicators);
        radar.put("radius", "60%");
        radar.put("splitNumber", 4);
        
        ObjectNode axisName = objectMapper.createObjectNode();
        axisName.put("formatter", "{value}");
        axisName.put("color", "#666");
        radar.set("axisName", axisName);
        
        ObjectNode splitArea = objectMapper.createObjectNode();
        ObjectNode areaStyle = objectMapper.createObjectNode();
        ArrayNode colors = objectMapper.createArrayNode();
        colors.add("rgba(250, 250, 250, 0.3)");
        colors.add("rgba(200, 200, 200, 0.3)");
        areaStyle.set("color", colors);
        splitArea.set("areaStyle", areaStyle);
        radar.set("splitArea", splitArea);
        
        option.set("radar", radar);
        
        // Series
        ArrayNode seriesArray = objectMapper.createArrayNode();
        boolean hasGroups = data.stream().anyMatch(item -> item.getGroup() != null && !item.getGroup().isEmpty());
        
        if (hasGroups) {
            Map<String, List<DataItem>> groupMap = data.stream()
                    .filter(item -> item.getGroup() != null && !item.getGroup().isEmpty())
                    .collect(Collectors.groupingBy(DataItem::getGroup));
            
            for (Map.Entry<String, List<DataItem>> entry : groupMap.entrySet()) {
                ObjectNode series = objectMapper.createObjectNode();
                series.put("type", "radar");
                
                ObjectNode seriesDataItem = objectMapper.createObjectNode();
                seriesDataItem.put("name", entry.getKey());
                
                ArrayNode values = objectMapper.createArrayNode();
                for (String dim : dimensionList) {
                    Optional<DataItem> item = entry.getValue().stream()
                            .filter(d -> dim.equals(d.getName()))
                            .findFirst();
                    values.add(item.map(DataItem::getValue).orElse(0).shortValue());
                }
                seriesDataItem.set("value", values);
                
                ArrayNode seriesData = objectMapper.createArrayNode();
                seriesData.add(seriesDataItem);
                series.set("data", seriesData);
                seriesArray.add(series);
            }
            
            ObjectNode legend = objectMapper.createObjectNode();
            legend.put("left", "center");
            legend.put("orient", "horizontal");
            legend.put("bottom", "5%");
            option.set("legend", legend);
        } else {
            ObjectNode series = objectMapper.createObjectNode();
            series.put("type", "radar");
            
            ObjectNode seriesDataItem = objectMapper.createObjectNode();
            seriesDataItem.put("name", title != null && !title.isEmpty() ? title : "数据");
            
            ArrayNode values = objectMapper.createArrayNode();
            for (String dim : dimensionList) {
                Optional<DataItem> item = data.stream()
                        .filter(d -> dim.equals(d.getName()))
                        .findFirst();
                values.add(item.map(DataItem::getValue).orElse(0).shortValue());
            }
            seriesDataItem.set("value", values);
            
            ArrayNode seriesData = objectMapper.createArrayNode();
            seriesData.add(seriesDataItem);
            series.set("data", seriesData);
            seriesArray.add(series);
        }
        
        option.set("series", seriesArray);
        return option;
    }
    
    /**
     * 构建漏斗图 option
     */
    public static ObjectNode buildFunnelChartOption(String title, List<DataItem> data) {
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
        series.put("type", "funnel");
        series.put("left", "10%");
        series.put("top", "60");
        series.put("width", "80%");
        series.put("height", "80%");
        series.put("min", 0);
        series.put("max", data.stream()
                .filter(item -> item.getValue() != null)
                .mapToDouble(item -> item.getValue().doubleValue())
                .max()
                .orElse(100));
        series.put("minSize", "0%");
        series.put("maxSize", "100%");
        series.put("sort", "descending");
        series.put("gap", 2);
        ObjectNode label = objectMapper.createObjectNode();
        label.put("show", true);
        label.put("position", "inside");
        series.set("label", label);
        ObjectNode labelLine = objectMapper.createObjectNode();
        labelLine.put("length", 10);
        ObjectNode lineStyle = objectMapper.createObjectNode();
        lineStyle.put("width", 1);
        lineStyle.put("type", "solid");
        labelLine.set("lineStyle", lineStyle);
        series.set("labelLine", labelLine);
        ObjectNode itemStyle = objectMapper.createObjectNode();
        itemStyle.put("borderColor", "#fff");
        itemStyle.put("borderWidth", 1);
        series.set("itemStyle", itemStyle);
        ObjectNode emphasis = objectMapper.createObjectNode();
        ObjectNode emphasisLabel = objectMapper.createObjectNode();
        emphasisLabel.put("fontSize", 20);
        emphasis.set("label", emphasisLabel);
        series.set("emphasis", emphasis);
        
        ArrayNode funnelData = objectMapper.createArrayNode();
        for (DataItem item : data) {
            ObjectNode dataItem = objectMapper.createObjectNode();
            dataItem.put("name", item.getCategory());
            dataItem.put("value", item.getValue() != null ? item.getValue().doubleValue() : 0);
            funnelData.add(dataItem);
        }
        series.set("data", funnelData);
        
        seriesArray.add(series);
        option.set("series", seriesArray);
        return option;
    }
    
    /**
     * 构建仪表盘 option
     */
    public static ObjectNode buildGaugeChartOption(String title, List<DataItem> data, Number min, Number max) {
        ObjectNode option = createBaseOption(title);
        
        // Tooltip
        ObjectNode tooltip = objectMapper.createObjectNode();
        tooltip.put("trigger", "item");
        option.set("tooltip", tooltip);
        
        // Series
        ArrayNode seriesArray = objectMapper.createArrayNode();
        
        for (DataItem item : data) {
            ObjectNode series = objectMapper.createObjectNode();
            series.put("type", "gauge");
            series.put("name", item.getName() != null ? item.getName() : "指标");
            
            ObjectNode detail = objectMapper.createObjectNode();
            detail.put("formatter", "{value}");
            series.set("detail", detail);
            
            ObjectNode dataNode = objectMapper.createObjectNode();
            dataNode.put("value", item.getValue() != null ? item.getValue().doubleValue() : 0);
            dataNode.put("name", item.getName() != null ? item.getName() : "指标");
            
            ArrayNode seriesData = objectMapper.createArrayNode();
            seriesData.add(dataNode);
            series.set("data", seriesData);
            
            series.put("min", min != null ? min.doubleValue() : 0);
            series.put("max", max != null ? max.doubleValue() : 100);
            
            seriesArray.add(series);
        }
        
        option.set("series", seriesArray);
        return option;
    }
    
    /**
     * 构建矩形树图 option
     */
    public static ObjectNode buildTreemapChartOption(String title, List<TreeNode> data) {
        ObjectNode option = createBaseOption(title);
        
        // Tooltip
        ObjectNode tooltip = objectMapper.createObjectNode();
        tooltip.put("trigger", "item");
        option.set("tooltip", tooltip);
        
        // Series
        ArrayNode seriesArray = objectMapper.createArrayNode();
        ObjectNode series = objectMapper.createObjectNode();
        series.put("type", "treemap");
        
        ArrayNode treemapData = buildTreeNodeArray(data);
        series.set("data", treemapData);
        
        seriesArray.add(series);
        option.set("series", seriesArray);
        return option;
    }
    
    /**
     * 构建旭日图 option
     */
    public static ObjectNode buildSunburstChartOption(String title, List<TreeNode> data) {
        ObjectNode option = createBaseOption(title);
        
        // Tooltip
        ObjectNode tooltip = objectMapper.createObjectNode();
        tooltip.put("trigger", "item");
        option.set("tooltip", tooltip);
        
        // Series
        ArrayNode seriesArray = objectMapper.createArrayNode();
        ObjectNode series = objectMapper.createObjectNode();
        series.put("type", "sunburst");
        
        ArrayNode sunburstData = buildTreeNodeArray(data);
        series.set("data", sunburstData);
        
        seriesArray.add(series);
        option.set("series", seriesArray);
        return option;
    }
    
    /**
     * 构建热力图 option
     */
    public static ObjectNode buildHeatmapChartOption(String title, String axisXTitle, String axisYTitle,
                                                      List<DataItem> data) {
        ObjectNode option = createBaseOption(title);
        
        // Tooltip
        ObjectNode tooltip = objectMapper.createObjectNode();
        tooltip.put("trigger", "item");
        option.set("tooltip", tooltip);
        
        // X 轴
        Set<String> xCategories = data.stream()
                .map(DataItem::getCategory)
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        
        ObjectNode xAxis = objectMapper.createObjectNode();
        xAxis.put("type", "category");
        ArrayNode xAxisData = objectMapper.createArrayNode();
        xCategories.forEach(xAxisData::add);
        xAxis.set("data", xAxisData);
        if (axisXTitle != null && !axisXTitle.isEmpty()) {
            xAxis.put("name", axisXTitle);
        }
        xAxis.put("splitArea", true);
        option.set("xAxis", xAxis);
        
        // Y 轴
        Set<String> yCategories = data.stream()
                .map(DataItem::getName)
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        
        ObjectNode yAxis = objectMapper.createObjectNode();
        yAxis.put("type", "category");
        ArrayNode yAxisData = objectMapper.createArrayNode();
        yCategories.forEach(yAxisData::add);
        yAxis.set("data", yAxisData);
        if (axisYTitle != null && !axisYTitle.isEmpty()) {
            yAxis.put("name", axisYTitle);
        }
        yAxis.put("splitArea", true);
        option.set("yAxis", yAxis);
        
        // Visual Map
        double maxValue = data.stream()
                .filter(item -> item.getHeatValue() != null)
                .mapToDouble(item -> item.getHeatValue().doubleValue())
                .max()
                .orElse(100);
        
        ObjectNode visualMap = objectMapper.createObjectNode();
        visualMap.put("min", 0);
        visualMap.put("max", maxValue);
        visualMap.put("calculable", true);
        visualMap.put("orient", "horizontal");
        visualMap.put("left", "center");
        visualMap.put("bottom", "15%");
        option.set("visualMap", visualMap);
        
        // Series
        ArrayNode seriesArray = objectMapper.createArrayNode();
        ObjectNode series = objectMapper.createObjectNode();
        series.put("type", "heatmap");
        
        ArrayNode heatmapData = objectMapper.createArrayNode();
        List<String> xCatList = new ArrayList<>(xCategories);
        List<String> yCatList = new ArrayList<>(yCategories);
        
        for (String yCat : yCatList) {
            for (String xCat : xCatList) {
                Optional<DataItem> item = data.stream()
                        .filter(d -> xCat.equals(d.getCategory()) && yCat.equals(d.getName()))
                        .findFirst();
                if (item.isPresent() && item.get().getHeatValue() != null) {
                    ArrayNode point = objectMapper.createArrayNode();
                    point.add(xCatList.indexOf(xCat));
                    point.add(yCatList.indexOf(yCat));
                    point.add(item.get().getHeatValue().doubleValue());
                    heatmapData.add(point);
                }
            }
        }
        series.set("data", heatmapData);
        ObjectNode label2 = objectMapper.createObjectNode();
        label2.put("show", true);
        series.set("label", label2);
        ObjectNode emphasis2 = objectMapper.createObjectNode();
        ObjectNode itemStyle2 = objectMapper.createObjectNode();
        itemStyle2.put("shadowBlur", 10);
        itemStyle2.put("shadowColor", "rgba(0, 0, 0, 0.5)");
        emphasis2.set("itemStyle", itemStyle2);
        series.set("emphasis", emphasis2);
        
        seriesArray.add(series);
        option.set("series", seriesArray);
        return option;
    }
    
    /**
     * 构建 K线图 option
     */
    public static ObjectNode buildCandlestickChartOption(String title, List<DataItem> data, boolean showVolume) {
        ObjectNode option = createBaseOption(title);
        
        // Tooltip
        ObjectNode tooltip = objectMapper.createObjectNode();
        tooltip.put("trigger", "axis");
        ObjectNode axisPointer = objectMapper.createObjectNode();
        axisPointer.put("type", "cross");
        tooltip.set("axisPointer", axisPointer);
        option.set("tooltip", tooltip);
        
        // X 轴（日期）
        Set<String> dates = data.stream()
                .map(DataItem::getDate)
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        
        ObjectNode xAxis = objectMapper.createObjectNode();
        xAxis.put("type", "category");
        ArrayNode xAxisData = objectMapper.createArrayNode();
        dates.forEach(xAxisData::add);
        xAxis.set("data", xAxisData);
        xAxis.put("scale", true);
        xAxis.put("boundaryGap", false);
        ObjectNode axisLine = objectMapper.createObjectNode();
        axisLine.put("onZero", false);
        xAxis.set("axisLine", axisLine);
        ObjectNode splitLine = objectMapper.createObjectNode();
        splitLine.put("show", false);
        xAxis.set("splitLine", splitLine);
        xAxis.put("min", "dataMin");
        xAxis.put("max", "dataMax");
        option.set("xAxis", xAxis);
        
        // Y 轴
        ArrayNode yAxisArray = objectMapper.createArrayNode();
        
        ObjectNode yAxis1 = objectMapper.createObjectNode();
        yAxis1.put("scale", true);
        yAxis1.set("splitArea", objectMapper.createObjectNode().put("show", true));
        yAxisArray.add(yAxis1);
        
        if (showVolume) {
            ObjectNode yAxis2 = objectMapper.createObjectNode();
            yAxis2.put("scale", true);
            yAxis2.put("gridIndex", 1);
            yAxis2.put("splitNumber", 2);
            ObjectNode axisLabel = objectMapper.createObjectNode();
            axisLabel.put("show", false);
            yAxis2.set("axisLabel", axisLabel);
            ObjectNode axisLine2 = objectMapper.createObjectNode();
            axisLine2.put("show", false);
            yAxis2.set("axisLine", axisLine2);
            ObjectNode axisTick = objectMapper.createObjectNode();
            axisTick.put("show", false);
            yAxis2.set("axisTick", axisTick);
            ObjectNode splitLine2 = objectMapper.createObjectNode();
            splitLine2.put("show", false);
            yAxis2.set("splitLine", splitLine2);
            yAxisArray.add(yAxis2);
        }
        
        option.set("yAxis", yAxisArray);
        
        // Grid（如果需要显示成交量）
        if (showVolume) {
            ArrayNode gridArray = objectMapper.createArrayNode();
            ObjectNode grid1 = objectMapper.createObjectNode();
            grid1.put("left", "10%");
            grid1.put("right", "8%");
            grid1.put("height", "50%");
            gridArray.add(grid1);
            
            ObjectNode grid2 = objectMapper.createObjectNode();
            grid2.put("left", "10%");
            grid2.put("right", "8%");
            grid2.put("top", "63%");
            grid2.put("height", "16%");
            gridArray.add(grid2);
            option.set("grid", gridArray);
        }
        
        // Series
        ArrayNode seriesArray = objectMapper.createArrayNode();
        
        // K线图
        ObjectNode candlestickSeries = objectMapper.createObjectNode();
        candlestickSeries.put("name", "K线图");
        candlestickSeries.put("type", "candlestick");
        if (showVolume) {
            candlestickSeries.put("xAxisIndex", 0);
            candlestickSeries.put("yAxisIndex", 0);
        }
        
        ArrayNode candlestickData = objectMapper.createArrayNode();
        List<String> dateList = new ArrayList<>(dates);
        for (String date : dateList) {
            Optional<DataItem> item = data.stream()
                    .filter(d -> date.equals(d.getDate()))
                    .findFirst();
            if (item.isPresent()) {
                ArrayNode ohlc = objectMapper.createArrayNode();
                ohlc.add(item.get().getOpen() != null ? item.get().getOpen().doubleValue() : 0);
                ohlc.add(item.get().getClose() != null ? item.get().getClose().doubleValue() : 0);
                ohlc.add(item.get().getLow() != null ? item.get().getLow().doubleValue() : 0);
                ohlc.add(item.get().getHigh() != null ? item.get().getHigh().doubleValue() : 0);
                candlestickData.add(ohlc);
            }
        }
        candlestickSeries.set("data", candlestickData);
        seriesArray.add(candlestickSeries);
        
        // 成交量（如果显示）
        if (showVolume) {
            ObjectNode volumeSeries = objectMapper.createObjectNode();
            volumeSeries.put("name", "成交量");
            volumeSeries.put("type", "bar");
            volumeSeries.put("xAxisIndex", 1);
            volumeSeries.put("yAxisIndex", 1);
            
            ArrayNode volumeData = objectMapper.createArrayNode();
            for (String date : dateList) {
                Optional<DataItem> item = data.stream()
                        .filter(d -> date.equals(d.getDate()))
                        .findFirst();
                volumeData.add(item.map(d -> d.getVolume() != null ? d.getVolume().doubleValue() : 0.0).orElse(0.0));
            }
            volumeSeries.set("data", volumeData);
            seriesArray.add(volumeSeries);
        }
        
        option.set("series", seriesArray);
        return option;
    }
    
    /**
     * 构建箱线图 option
     */
    public static ObjectNode buildBoxplotChartOption(String title, String axisXTitle, String axisYTitle,
                                                      List<DataItem> data) {
        ObjectNode option = createBaseOption(title);
        
        // Tooltip
        ObjectNode tooltip = objectMapper.createObjectNode();
        tooltip.put("trigger", "item");
        ObjectNode axisPointer2 = objectMapper.createObjectNode();
        axisPointer2.put("type", "shadow");
        tooltip.set("axisPointer", axisPointer2);
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
        xAxis.put("boundaryGap", true);
        xAxis.put("nameGap", 10);
        ObjectNode splitArea = objectMapper.createObjectNode();
        splitArea.put("show", false);
        xAxis.set("splitArea", splitArea);
        ObjectNode splitLine = objectMapper.createObjectNode();
        splitLine.put("show", false);
        xAxis.set("splitLine", splitLine);
        option.set("xAxis", xAxis);
        
        // Y 轴
        ObjectNode yAxis = objectMapper.createObjectNode();
        yAxis.put("type", "value");
        if (axisYTitle != null && !axisYTitle.isEmpty()) {
            yAxis.put("name", axisYTitle);
        }
        ObjectNode splitArea2 = objectMapper.createObjectNode();
        splitArea2.put("show", true);
        yAxis.set("splitArea", splitArea2);
        option.set("yAxis", yAxis);
        
        // Series
        ArrayNode seriesArray = objectMapper.createArrayNode();
        ObjectNode series = objectMapper.createObjectNode();
        series.put("name", "boxplot");
        series.put("type", "boxplot");
        
        ArrayNode boxplotData = objectMapper.createArrayNode();
        List<String> categoryList = new ArrayList<>(categories);
        for (String category : categoryList) {
            Optional<DataItem> item = data.stream()
                    .filter(d -> category.equals(d.getCategory()))
                    .findFirst();
            if (item.isPresent() && item.get().getBoxplotData() != null) {
                ArrayNode boxData = objectMapper.createArrayNode();
                for (Number num : item.get().getBoxplotData()) {
                    boxData.add(num.doubleValue());
                }
                boxplotData.add(boxData);
            } else {
                boxplotData.add(objectMapper.createArrayNode().add(0).add(0).add(0).add(0).add(0));
            }
        }
        series.set("data", boxplotData);
        
        seriesArray.add(series);
        option.set("series", seriesArray);
        return option;
    }
    
    /**
     * 构建关系图 option
     */
    public static ObjectNode buildGraphChartOption(String title, GraphData data, String layout) {
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
    
    /**
     * 构建平行坐标系 option
     */
    public static ObjectNode buildParallelChartOption(String title, List<DataItem> data, List<String> dimensions) {
        ObjectNode option = createBaseOption(title);
        
        // Tooltip
        ObjectNode tooltip = objectMapper.createObjectNode();
        tooltip.put("trigger", "item");
        option.set("tooltip", tooltip);
        
        // Parallel
        ObjectNode parallel = objectMapper.createObjectNode();
        ArrayNode parallelAxisArray = objectMapper.createArrayNode();
        for (String dim : dimensions) {
            ObjectNode axis = objectMapper.createObjectNode();
            axis.put("dim", dimensions.indexOf(dim));
            axis.put("name", dim);
            parallelAxisArray.add(axis);
        }
        parallel.set("parallelAxis", parallelAxisArray);
        option.set("parallel", parallel);
        
        // Series
        ArrayNode seriesArray = objectMapper.createArrayNode();
        ObjectNode series = objectMapper.createObjectNode();
        series.put("type", "parallel");
        
        ArrayNode parallelData = objectMapper.createArrayNode();
        for (DataItem item : data) {
            if (item.getParallelValues() != null && item.getParallelValues().length == dimensions.size()) {
                ArrayNode values = objectMapper.createArrayNode();
                for (Number num : item.getParallelValues()) {
                    values.add(num.doubleValue());
                }
                parallelData.add(values);
            }
        }
        series.set("data", parallelData);
        
        seriesArray.add(series);
        option.set("series", seriesArray);
        return option;
    }
    
    /**
     * 构建桑基图 option
     */
    public static ObjectNode buildSankeyChartOption(String title, List<DataItem> data, String nodeAlign) {
        ObjectNode option = createBaseOption(title);
        
        // Tooltip
        ObjectNode tooltip = objectMapper.createObjectNode();
        tooltip.put("trigger", "item");
        tooltip.put("triggerOn", "mousemove");
        option.set("tooltip", tooltip);
        
        // Series
        ArrayNode seriesArray = objectMapper.createArrayNode();
        ObjectNode series = objectMapper.createObjectNode();
        series.put("type", "sankey");
        
        // 提取所有节点
        Set<String> nodes = new LinkedHashSet<>();
        for (DataItem item : data) {
            if (item.getSource() != null) {
                nodes.add(item.getSource());
            }
            if (item.getTarget() != null) {
                nodes.add(item.getTarget());
            }
        }
        
        ArrayNode nodesArray = objectMapper.createArrayNode();
        for (String nodeName : nodes) {
            ObjectNode node = objectMapper.createObjectNode();
            node.put("name", nodeName);
            nodesArray.add(node);
        }
        series.set("data", nodesArray);
        
        // Links
        ArrayNode linksArray = objectMapper.createArrayNode();
        for (DataItem item : data) {
            if (item.getSource() != null && item.getTarget() != null && nodes.contains(item.getSource()) && nodes.contains(item.getTarget())) {
                ObjectNode link = objectMapper.createObjectNode();
                link.put("source", item.getSource());
                link.put("target", item.getTarget());
                link.put("value", item.getValue() != null ? item.getValue().doubleValue() : 0);
                linksArray.add(link);
            }
        }
        series.set("links", linksArray);
        
        ObjectNode emphasis = objectMapper.createObjectNode();
        emphasis.put("focus", "adjacency");
        series.set("emphasis", emphasis);
        
        ObjectNode lineStyle = objectMapper.createObjectNode();
        lineStyle.put("color", "gradient");
        lineStyle.put("curveness", 0.5);
        series.set("lineStyle", lineStyle);
        
        ObjectNode label = objectMapper.createObjectNode();
        label.put("fontSize", 12);
        series.set("label", label);
        
        if (nodeAlign != null) {
            series.put("nodeAlign", nodeAlign);
        } else {
            series.put("nodeAlign", "justify");
        }
        
        seriesArray.add(series);
        option.set("series", seriesArray);
        return option;
    }
    
    /**
     * 构建树图 option
     */
    public static ObjectNode buildTreeChartOption(String title, TreeNode data, String layout, String orient) {
        ObjectNode option = createBaseOption(title);
        
        // Tooltip
        ObjectNode tooltip = objectMapper.createObjectNode();
        tooltip.put("trigger", "item");
        tooltip.put("triggerOn", "mousemove");
        option.set("tooltip", tooltip);
        
        // Series
        ArrayNode seriesArray = objectMapper.createArrayNode();
        ObjectNode series = objectMapper.createObjectNode();
        series.put("type", "tree");
        series.put("layout", layout != null ? layout : "orthogonal");
        if (layout != null && layout.equals("orthogonal") && orient != null) {
            series.put("orient", orient);
        }
        series.put("symbol", "emptyCircle");
        series.put("symbolSize", 7);
        series.put("initialTreeDepth", -1);
        
        ObjectNode itemStyle = objectMapper.createObjectNode();
        itemStyle.put("color", "#4154f3");
        itemStyle.put("borderWidth", 2);
        series.set("itemStyle", itemStyle);
        
        ObjectNode lineStyle = objectMapper.createObjectNode();
        lineStyle.put("color", "#ccc");
        lineStyle.put("width", 1.5);
        lineStyle.put("curveness", 0.5);
        series.set("lineStyle", lineStyle);
        
        // 根据 layout 和 orient 设置标签位置
        ObjectNode label = objectMapper.createObjectNode();
        if (layout != null && layout.equals("radial")) {
            label.put("position", "top");
            label.put("align", "center");
        } else if (orient != null) {
            switch (orient) {
                case "LR":
                    label.put("position", "right");
                    label.put("verticalAlign", "middle");
                    label.put("align", "left");
                    break;
                case "RL":
                    label.put("position", "left");
                    label.put("verticalAlign", "middle");
                    label.put("align", "right");
                    break;
                case "TB":
                    label.put("position", "bottom");
                    label.put("verticalAlign", "top");
                    label.put("align", "center");
                    break;
                case "BT":
                    label.put("position", "top");
                    label.put("verticalAlign", "bottom");
                    label.put("align", "center");
                    break;
                default:
                    label.put("position", "right");
                    label.put("verticalAlign", "middle");
                    label.put("align", "left");
            }
        } else {
            label.put("position", "right");
            label.put("verticalAlign", "middle");
            label.put("align", "left");
        }
        label.put("fontSize", 12);
        series.set("label", label);
        
        ObjectNode leaves = objectMapper.createObjectNode();
        ObjectNode leavesLabel = objectMapper.createObjectNode();
        if (layout != null && layout.equals("radial")) {
            leavesLabel.put("position", "top");
            leavesLabel.put("align", "center");
        } else if (orient != null && orient.equals("LR")) {
            leavesLabel.put("position", "right");
            leavesLabel.put("verticalAlign", "middle");
            leavesLabel.put("align", "left");
        } else if (orient != null && orient.equals("RL")) {
            leavesLabel.put("position", "left");
            leavesLabel.put("verticalAlign", "middle");
            leavesLabel.put("align", "right");
        } else if (orient != null && orient.equals("TB")) {
            leavesLabel.put("position", "bottom");
            leavesLabel.put("align", "center");
        } else if (orient != null && orient.equals("BT")) {
            leavesLabel.put("position", "top");
            leavesLabel.put("align", "center");
        } else {
            leavesLabel.put("position", "right");
            leavesLabel.put("verticalAlign", "middle");
            leavesLabel.put("align", "left");
        }
        leaves.set("label", leavesLabel);
        series.set("leaves", leaves);
        
        ObjectNode emphasis = objectMapper.createObjectNode();
        emphasis.put("focus", "descendant");
        series.set("emphasis", emphasis);
        
        series.put("expandAndCollapse", true);
        series.put("animationDuration", 550);
        series.put("animationDurationUpdate", 750);
        
        // 构建树数据（包装成数组）
        ArrayNode treeData = objectMapper.createArrayNode();
        treeData.add(buildTreeNode(data));
        series.set("data", treeData);
        
        seriesArray.add(series);
        option.set("series", seriesArray);
        return option;
    }
    
    /**
     * 递归构建树节点
     */
    private static ObjectNode buildTreeNode(TreeNode node) {
        ObjectNode nodeObj = objectMapper.createObjectNode();
        nodeObj.put("name", node.getName());
        if (node.getValue() != null) {
            nodeObj.put("value", node.getValue().doubleValue());
        }
        if (node.getChildren() != null && !node.getChildren().isEmpty()) {
            ArrayNode childrenArray = objectMapper.createArrayNode();
            for (TreeNode child : node.getChildren()) {
                childrenArray.add(buildTreeNode(child));
            }
            nodeObj.set("children", childrenArray);
        }
        return nodeObj;
    }
    
    /**
     * 构建树节点数组（用于 treemap 和 sunburst）
     */
    private static ArrayNode buildTreeNodeArray(List<TreeNode> nodes) {
        ArrayNode array = objectMapper.createArrayNode();
        for (TreeNode node : nodes) {
            array.add(buildTreeNode(node));
        }
        return array;
    }
}

