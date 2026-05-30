package mcp.canary.echart.module.graph.series.data;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Data;
import mcp.canary.echart.module.EChartModule;

import java.util.Map;

@Data
public class GraphEdge implements EChartModule {

    /**
     * 边的源节点名称
     */
    private String source;

    /**
     * 边的目标节点名称
     */
    private String target;

    /**
     * 边的数值，用于力引导布局中的距离映射
     */
    private Number value;

    /**
     * Neo4j edge properties
     */
    private Map<String, Object> properties;

    @Override
    public JsonNode toEChartNode() {
        ObjectNode objectNode = MAPPER.createObjectNode();
        objectNode.put("source", source);
        objectNode.put("target", target);

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
            objectNode.put("value", displayValue.toString());
        } else if (value != null) {
            objectNode.put("value", String.valueOf(value.doubleValue()));
        }

        return objectNode;
    }
}
