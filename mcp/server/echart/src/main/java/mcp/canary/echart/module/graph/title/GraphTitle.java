package mcp.canary.echart.module.graph.title;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Data;
import mcp.canary.echart.module.EChartModule;

@Data
public class GraphTitle implements EChartModule {
    private String text;

    @Override
    public JsonNode toEChartNode() {
        ObjectNode title = MAPPER.createObjectNode();
        title.put("text", text);
        return title;
    }
}
