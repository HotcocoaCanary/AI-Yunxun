package mcp.canary.echart.graph;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Data;
import mcp.canary.shared.module.EChartModule;

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
