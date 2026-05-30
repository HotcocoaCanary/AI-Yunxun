package mcp.canary.echart.module.graph.series.data;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Data;
import mcp.canary.echart.module.EChartModule;

import java.util.Map;

@Data
public class GraphNode implements EChartModule {

    /**
     * 节点唯一标识
     */
    private String name;

    /**
     * 数据项所在类目的名字
     */
    private String categoryName;

    /**
     * Neo4j node properties
     */
    private Map<String, Object> properties;

    @Override
    public JsonNode toEChartNode() {
        ObjectNode nodeJson = MAPPER.createObjectNode();

        nodeJson.put("name", name);
        nodeJson.put("category", -1);

        if (properties != null && !properties.isEmpty()) {
            StringBuilder displayValue = new StringBuilder();
            for (Map.Entry<String, Object> entry : properties.entrySet()) {
                if (entry.getValue() != null) {
                    if (displayValue.length() > 0) {
                        displayValue.append("\n");
                    }
                    displayValue.append(entry.getKey()).append(": ").append(entry.getValue());
                }
            }
            nodeJson.put("value", displayValue.toString());
        }

        return nodeJson;
    }
}
