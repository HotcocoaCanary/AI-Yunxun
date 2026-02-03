package mcp.canary.echart.graph;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Data;
import mcp.canary.shared.GraphSeries;
import mcp.canary.shared.module.EChartModule;


@Data
public class GraphOption implements EChartModule {
    private GraphSeries series;

    private GraphTitle title;

    @Override
    public JsonNode toEChartNode() {
        ObjectNode option = MAPPER.createObjectNode();

        option.set("title", title.toEChartNode());

        // tooltip: 直接显示value
        ObjectNode tooltipNode = MAPPER.createObjectNode();
        tooltipNode.put("trigger", "item");
        tooltipNode.put("formatter", "{c}");
        option.set("tooltip", tooltipNode);

        ArrayNode seriesList = MAPPER.createArrayNode();
        seriesList.add(series.toEChartNode());
        option.set("series", seriesList);

        return option;
    }
}
