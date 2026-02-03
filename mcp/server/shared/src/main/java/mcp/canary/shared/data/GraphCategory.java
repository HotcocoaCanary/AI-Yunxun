package mcp.canary.shared.data;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Data;
import mcp.canary.shared.module.EChartModule;

import java.util.Arrays;
import java.util.List;

@Data
public class GraphCategory implements EChartModule {

    /**
     * 类目名称，用于和 legend 对应以及格式化 tooltip 的内容。
     */
    private String name;

    /**
     * 该类目节点标记的图形。
     * 使用自定义的 Symbol 枚举
     */
    private String symbol;

    /**
     * ECharts 提供的标准标记类型列表
     */
    private static final List<String> SYMBOL_LIST = Arrays.asList(
            "circle", "rect", "roundRect", "triangle", "diamond", "pin", "arrow", "none"
    );

    @Override
    public JsonNode toEChartNode() {
        ObjectNode node = MAPPER.createObjectNode();
        node.put("name", name);
        node.put("symbol", SYMBOL_LIST.contains(symbol) ? symbol : "circle");
        return node;
    }
}