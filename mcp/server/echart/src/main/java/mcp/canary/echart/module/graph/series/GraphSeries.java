package mcp.canary.echart.module.graph.series;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Data;
import mcp.canary.echart.module.graph.series.data.GraphCategory;
import mcp.canary.echart.module.graph.series.data.GraphEdge;
import mcp.canary.echart.module.graph.series.data.GraphNode;
import mcp.canary.echart.module.EChartModule;

@Data
public class GraphSeries implements EChartModule {

    private final String type = "graph";

    private String layout;

    private List<GraphNode> nodes;

    private List<GraphEdge> edges;

    private List<GraphCategory> categories;

    @Override
    public JsonNode toEChartNode() {
        ObjectNode seriesNode = MAPPER.createObjectNode();

        seriesNode.put("type", type);

        String safeLayout = "force";
        if ("circular".equalsIgnoreCase(layout)) {
            safeLayout = "circular";
        }
        seriesNode.put("layout", safeLayout);

        seriesNode.put("draggable", true);
        seriesNode.put("roam", true);

        if ("force".equals(safeLayout)) {
            ObjectNode forceNode = MAPPER.createObjectNode();
            forceNode.put("repulsion", 280);
            forceNode.put("gravity", 0.08);
            forceNode.put("edgeLength", 120);
            forceNode.put("layoutAnimation", true);
            seriesNode.set("force", forceNode);
        }

        int nodeCount = nodes != null ? nodes.size() : 0;
        int symbolSz = nodeCount > 50 ? 24 : (nodeCount > 20 ? 32 : 42);
        seriesNode.put("symbolSize", symbolSz);

        ObjectNode itemStyle = MAPPER.createObjectNode();
        itemStyle.put("borderColor", "#fff");
        itemStyle.put("borderWidth", 2);
        itemStyle.put("shadowBlur", 8);
        itemStyle.put("shadowColor", "rgba(0,0,0,0.15)");
        seriesNode.set("itemStyle", itemStyle);

        ObjectNode labelNode = MAPPER.createObjectNode();
        labelNode.put("show", true);
        labelNode.put("fontSize", 11);
        labelNode.put("color", "#333");
        labelNode.put("position", "bottom");
        labelNode.put("distance", 8);
        labelNode.put("formatter", "{b}");
        seriesNode.set("label", labelNode);

        ObjectNode lineStyle = MAPPER.createObjectNode();
        lineStyle.put("color", "source");
        lineStyle.put("curveness", 0.15);
        lineStyle.put("opacity", 0.5);
        lineStyle.put("width", 1.5);
        seriesNode.set("lineStyle", lineStyle);

        ObjectNode edgeLabel = MAPPER.createObjectNode();
        edgeLabel.put("show", true);
        edgeLabel.put("fontSize", 10);
        edgeLabel.put("color", "#999");
        edgeLabel.put("formatter", "{c}");
        seriesNode.set("edgeLabel", edgeLabel);

        ObjectNode emphasisNode = MAPPER.createObjectNode();
        ObjectNode emphasisLabel = MAPPER.createObjectNode();
        emphasisLabel.put("fontSize", 14);
        emphasisLabel.put("fontWeight", "bold");
        emphasisNode.set("label", emphasisLabel);
        ObjectNode emphasisItemStyle = MAPPER.createObjectNode();
        emphasisItemStyle.put("shadowBlur", 16);
        emphasisItemStyle.put("shadowColor", "rgba(0,0,0,0.3)");
        emphasisItemStyle.put("borderWidth", 3);
        emphasisItemStyle.put("borderColor", "#4f46e5");
        emphasisNode.set("itemStyle", emphasisItemStyle);
        ObjectNode emphasisLineStyle = MAPPER.createObjectNode();
        emphasisLineStyle.put("width", 3);
        emphasisLineStyle.put("opacity", 0.8);
        emphasisNode.set("lineStyle", emphasisLineStyle);
        emphasisNode.put("focus", "adjacency");
        seriesNode.set("emphasis", emphasisNode);

        Map<String, Integer> categoryMap = new HashMap<>();
        if (categories != null) {
            for (int i = 0; i < categories.size(); i++) {
                categoryMap.put(categories.get(i).getName(), i);
            }
        }

        ArrayNode categoriesList = MAPPER.createArrayNode();
        if (categories != null) {
            for (int i = 0; i < categories.size(); i++) {
                GraphCategory cat = categories.get(i);
                cat.setIndex(i);
                categoriesList.add(cat.toEChartNode());
            }
        }
        seriesNode.set("categories", categoriesList);

        ArrayNode dataList = MAPPER.createArrayNode();
        if (nodes != null) {
            for (GraphNode node : nodes) {
                ObjectNode nodeJson = (ObjectNode) node.toEChartNode();
                Integer idx = categoryMap.get(node.getCategoryName());
                nodeJson.put("category", idx != null ? idx : -1);
                dataList.add(nodeJson);
            }
        }
        seriesNode.set("data", dataList);

        ArrayNode linksList = MAPPER.createArrayNode();
        if (edges != null) {
            for (GraphEdge edge : edges) {
                linksList.add(edge.toEChartNode());
            }
        }
        seriesNode.set("links", linksList);

        return seriesNode;
    }
}
