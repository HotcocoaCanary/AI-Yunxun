package mcp.canary.shared;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Data;
import mcp.canary.shared.data.GraphCategory;
import mcp.canary.shared.data.GraphEdge;
import mcp.canary.shared.data.GraphNode;
import mcp.canary.shared.module.EChartModule;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class GraphSeries implements EChartModule {

    /**
     * series 类型，固定为 graph
     */
    private final String type = "graph";

    /**
     * 布局（force / circular）
     */
    private String layout;

    /**
     * 节点
     */
    private List<GraphNode> nodes;

    /**
     * 边
     */
    private List<GraphEdge> edges;

    /**
     * 分类
     */
    private List<GraphCategory> categories;

    @Override
    public JsonNode toEChartNode() {

        ObjectNode seriesNode = MAPPER.createObjectNode();

        // type: graph
        seriesNode.put("type", type);
        // layout: 布局
        seriesNode.put("layout", layout.equals("circular") ? "circular" : "force");
        //
        seriesNode.put("draggable", true);
        seriesNode.put("symbolSize", 35);

        // label：直接显示 node.label
        ObjectNode labelNode = MAPPER.createObjectNode();
        labelNode.put("show", true);
        labelNode.put("formatter", "{b}");
        seriesNode.set("label", labelNode);

        // 添加category

        // 生成 categoryMap
        Map<String, Integer> categoryMap = new HashMap<>();
        if (categories != null) {
            for (int i = 0; i < categories.size(); i++) {
                categoryMap.put(categories.get(i).getName(), i);
            }
        }

        // categories：
        ArrayNode categoriesList = MAPPER.createArrayNode();
        for (GraphCategory index : categories) {
            JsonNode res = index.toEChartNode();
            categoriesList.add(res);
        }
        seriesNode.set("categories", categoriesList);

        // data：nodes
        ArrayNode dataList = MAPPER.createArrayNode();
        for (GraphNode index : nodes) {
            ObjectNode nodeJson = (ObjectNode) index.toEChartNode();
            Integer idx = categoryMap.get(index.getCategoryName());
            nodeJson.put("category", idx != null ? idx : -1);
            dataList.add(nodeJson);
        }
        seriesNode.set("data", dataList);

        // like: edges
        ArrayNode likeList = MAPPER.createArrayNode();
        for (GraphEdge index : edges) {
            JsonNode res = index.toEChartNode();
            likeList.add(res);
        }
        seriesNode.set("links", likeList);

        return seriesNode;
    }
}
