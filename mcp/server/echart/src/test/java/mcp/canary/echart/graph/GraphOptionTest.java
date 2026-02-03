package mcp.canary.echart.graph;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonGenerator;
import mcp.canary.shared.GraphSeries;
import mcp.canary.shared.data.GraphCategory;
import mcp.canary.shared.data.GraphEdge;
import mcp.canary.shared.data.GraphNode;

import java.util.Arrays;
import java.util.HashMap;

public class GraphOptionTest {
    public static void main(String[] args) throws Exception {
        // ✅ ObjectMapper 设置为不使用引号包裹 key
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(JsonGenerator.Feature.QUOTE_FIELD_NAMES, false);

        // 1️⃣ 创建标题
        GraphTitle title = new GraphTitle();
        title.setText("示例图");

        // 2️⃣ 创建分类
        GraphCategory category1 = new GraphCategory();
        category1.setName("分类A");
        category1.setSymbol("circle");

        GraphCategory category2 = new GraphCategory();
        category2.setName("分类B");
        category2.setSymbol("diamond");

        // 3️⃣ 创建节点
        GraphNode node1 = new GraphNode();
        node1.setName("节点1");
        node1.setCategoryName("分类A");
        node1.setProperties(new HashMap<>() {{
            put("属性1", 100);
            put("属性2", "测试");
        }});

        GraphNode node2 = new GraphNode();
        node2.setName("节点2");
        node2.setCategoryName("分类B");
        node2.setProperties(new HashMap<>() {{
            put("属性3", 200);
        }});

        // 4️⃣ 创建边
        GraphEdge edge = new GraphEdge();
        edge.setSource("节点1");
        edge.setTarget("节点2");
        edge.setValue(10);

        // 5️⃣ 创建 Series
        GraphSeries series = new GraphSeries();
        series.setLayout("force");
        series.setCategories(Arrays.asList(category1, category2));
        series.setNodes(Arrays.asList(node1, node2));
        series.setEdges(Arrays.asList(edge));

        // 6️⃣ 创建 GraphOption
        GraphOption graphOption = new GraphOption();
        graphOption.setTitle(title);
        graphOption.setSeries(series);

        // 7️⃣ 打印 JSON（键不带引号）
        JsonNode resultNode = graphOption.toEChartNode();
        String jsObjectStr = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(resultNode);
        System.out.println(jsObjectStr);
    }
}
