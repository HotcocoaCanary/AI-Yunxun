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
     * 节点唯一标识（ECharts 要求不重复）
     */
    private String name;

    /**
     * 数据项所在类目的名字，转换成数据是需要填写数组下标。
     */
    private String categoryName;

    /**
     * Neo4j node properties
     * 使用value展示
     */
    private Map<String, Object> properties;


    @Override
    public JsonNode toEChartNode() {
        ObjectNode nodeJson = MAPPER.createObjectNode();

        nodeJson.put("name", name);

        // category延时注入
        nodeJson.put("category", -1);

        // 🔑 关键：properties 展平 + tooltip 字符串模板
        if (properties != null && !properties.isEmpty()) {
            ArrayNode arrayNode = MAPPER.createArrayNode();
            // 1️⃣ properties 展平
            for (Map.Entry<String, Object> entry : properties.entrySet()) {
                Object value = entry.getValue();
                if (value != null) {
                    arrayNode.add(entry.getKey() + value);
                }
            }
            nodeJson.set("value", arrayNode);
        }

        return nodeJson;
    }
}
