package mcp.canary.echart.module.graph.series.data;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Data;
import mcp.canary.echart.module.EChartModule;

import java.util.Arrays;
import java.util.List;

@Data
public class GraphCategory implements EChartModule {

    private static final List<String> SYMBOL_LIST = Arrays.asList(
            "circle", "rect", "roundRect", "triangle", "diamond", "pin", "arrow", "none"
    );

    private static final List<String> CATEGORY_COLORS = Arrays.asList(
            "#4f46e5", "#06b6d4", "#10b981", "#f59e0b", "#ef4444",
            "#8b5cf6", "#ec4899", "#14b8a6", "#f97316", "#3b82f6"
    );

    /**
     * 类目名称
     */
    private String name;

    /**
     * 节点标记图形
     */
    private String symbol;

    /**
     * 类目在列表中的下标（注入时设置）
     */
    private transient int index = 0;

    @Override
    public JsonNode toEChartNode() {
        ObjectNode node = MAPPER.createObjectNode();
        node.put("name", name);

        String safeSymbol = SYMBOL_LIST.contains(symbol) ? symbol : "circle";
        node.put("symbol", safeSymbol);

        node.put("symbolSize", 32);

        ObjectNode itemStyle = MAPPER.createObjectNode();
        String color = CATEGORY_COLORS.get(index % CATEGORY_COLORS.size());
        itemStyle.put("color", color);
        node.set("itemStyle", itemStyle);

        ObjectNode label = MAPPER.createObjectNode();
        label.put("fontSize", 11);
        label.put("color", "#333");
        node.set("label", label);

        return node;
    }

    public void setIndex(int index) {
        this.index = index;
    }
}
