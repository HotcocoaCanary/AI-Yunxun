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
        title.put("left", "center");
        title.put("top", 8);

        ObjectNode textStyle = MAPPER.createObjectNode();
        textStyle.put("fontSize", 16);
        textStyle.put("fontWeight", "bold");
        textStyle.put("color", "#1a1b1e");
        title.set("textStyle", textStyle);

        return title;
    }
}
