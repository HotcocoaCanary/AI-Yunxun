package mcp.canary.echart.module.graph;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Data;
import mcp.canary.echart.module.EChartModule;
import mcp.canary.echart.module.graph.series.GraphSeries;
import mcp.canary.echart.module.graph.title.GraphTitle;

@Data
public class GraphOption implements EChartModule {
    private GraphSeries series;

    private GraphTitle title;

    @Override
    public JsonNode toEChartNode() {
        ObjectNode option = MAPPER.createObjectNode();

        if (title != null) {
            option.set("title", title.toEChartNode());
        }

        // tooltip: show value directly
        ObjectNode tooltipNode = MAPPER.createObjectNode();
        tooltipNode.put("trigger", "item");
        tooltipNode.put("formatter", "{c}");
        option.set("tooltip", tooltipNode);

        ArrayNode seriesList = MAPPER.createArrayNode();
        if (series != null) {
            seriesList.add(series.toEChartNode());
        }
        option.set("series", seriesList);

        return option;
    }
}