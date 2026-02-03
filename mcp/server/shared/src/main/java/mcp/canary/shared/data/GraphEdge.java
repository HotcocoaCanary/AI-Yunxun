package mcp.canary.shared.data;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Data;
import mcp.canary.shared.module.EChartModule;

import java.util.Map;

@Data
public class GraphEdge implements EChartModule {

    /**
     * 边的源节点名称的字符串，也支持使用数字表示源节点的索引。
     */
    private String source;

    /**
     * 边的目标节点名称的字符串，也支持使用数字表示源节点的索引。
     */
    private String target;

    /**
     * 边的数值，可以在力引导布局中用于映射到边的长度。
     */
    private Number value;

    /**
     * Neo4j node properties
     * 使用value展示
     */
    private Map<String, Object> properties;

    @Override
    public JsonNode toEChartNode() {
        ObjectNode objectNode = MAPPER.createObjectNode();
        objectNode.put("source", source);
        objectNode.put("target", target);

        if (value != null) {
            objectNode.put("value", value.doubleValue());
        }
        return objectNode;
    }

}
