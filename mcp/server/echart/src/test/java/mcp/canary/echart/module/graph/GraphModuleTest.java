package mcp.canary.echart.module.graph;

import com.fasterxml.jackson.databind.JsonNode;
import mcp.canary.echart.module.graph.series.GraphSeries;
import mcp.canary.echart.module.graph.series.data.GraphCategory;
import mcp.canary.echart.module.graph.series.data.GraphEdge;
import mcp.canary.echart.module.graph.series.data.GraphNode;
import mcp.canary.echart.module.graph.title.GraphTitle;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class GraphModuleTest {

    @Test
    void option_includesTitleTooltipAndSeries() {
        GraphTitle title = new GraphTitle();
        title.setText("Demo");

        GraphSeries series = new GraphSeries();

        GraphOption option = new GraphOption();
        option.setTitle(title);
        option.setSeries(series);

        JsonNode node = option.toEChartNode();

        assertEquals("Demo", node.get("title").get("text").asText());
        assertEquals("item", node.get("tooltip").get("trigger").asText());
        assertEquals("{b}", node.get("tooltip").get("formatter").asText());
        assertTrue(node.get("tooltip").get("confine").asBoolean());
        assertEquals(1, node.get("series").size());
    }

    @Test
    void option_withoutTitleOrSeriesCreatesEmptySeriesList() {
        GraphOption option = new GraphOption();

        JsonNode node = option.toEChartNode();

        assertFalse(node.has("title"));
        assertTrue(node.get("series").isArray());
        assertEquals(0, node.get("series").size());
    }

    @Test
    void series_defaultsLayoutAndMapsCategories() {
        GraphCategory catA = new GraphCategory();
        catA.setName("A");
        catA.setSymbol("circle");

        GraphCategory catB = new GraphCategory();
        catB.setName("B");
        catB.setSymbol("triangle");

        GraphNode node1 = new GraphNode();
        node1.setName("n1");
        node1.setCategoryName("B");

        GraphNode node2 = new GraphNode();
        node2.setName("n2");
        node2.setCategoryName("C");

        GraphSeries series = new GraphSeries();
        series.setLayout("grid");
        series.setCategories(List.of(catA, catB));
        series.setNodes(List.of(node1, node2));

        JsonNode node = series.toEChartNode();

        assertEquals("force", node.get("layout").asText());
        assertEquals(2, node.get("categories").size());
        assertEquals(2, node.get("data").size());
        assertEquals(1, node.get("data").get(0).get("category").asInt());
        assertEquals(-1, node.get("data").get(1).get("category").asInt());
        assertEquals(0, node.get("links").size());
    }

    @Test
    void series_hasForceConfigWhenUsingForceLayout() {
        GraphSeries series = new GraphSeries();
        series.setLayout("force");
        series.setCategories(null);
        series.setNodes(null);
        series.setEdges(null);

        JsonNode node = series.toEChartNode();

        assertEquals("force", node.get("layout").asText());
        assertTrue(node.has("force"));
        assertEquals(280, node.get("force").get("repulsion").asInt());
        assertEquals(0.08, node.get("force").get("gravity").asDouble(), 0.001);
        assertEquals(120, node.get("force").get("edgeLength").asInt());
    }

    @Test
    void series_handlesNullCollectionsAndCircularLayout() {
        GraphSeries series = new GraphSeries();
        series.setLayout("CIRCULAR");
        series.setCategories(null);
        series.setNodes(null);
        series.setEdges(null);

        JsonNode node = series.toEChartNode();

        assertEquals("circular", node.get("layout").asText());
        assertFalse(node.has("force"));
        assertEquals(0, node.get("categories").size());
        assertEquals(0, node.get("data").size());
        assertEquals(0, node.get("links").size());
    }

    @Test
    void series_hasRoamAndEmphasis() {
        GraphSeries series = new GraphSeries();

        JsonNode node = series.toEChartNode();

        assertTrue(node.get("roam").asBoolean());
        assertTrue(node.get("draggable").asBoolean());
        assertTrue(node.has("emphasis"));
        assertEquals("adjacency", node.get("emphasis").get("focus").asText());
        assertTrue(node.has("lineStyle"));
        assertTrue(node.has("edgeLabel"));
    }

    @Test
    void node_withPropertiesFlattensValues() {
        GraphNode node = new GraphNode();
        node.setName("Node1");
        node.setCategoryName("Group1");

        Map<String, Object> props = new LinkedHashMap<>();
        props.put("age", 42);
        props.put("city", null);
        node.setProperties(props);

        JsonNode json = node.toEChartNode();

        assertEquals("Node1", json.get("name").asText());
        assertEquals(-1, json.get("category").asInt());
        assertTrue(json.has("value"));
        assertTrue(json.get("value").asText().contains("age: 42"));
        assertFalse(json.get("value").asText().contains("city"));
    }

    @Test
    void node_withoutPropertiesSkipsValue() {
        GraphNode node = new GraphNode();
        node.setName("Node2");

        JsonNode json = node.toEChartNode();

        assertFalse(json.has("value"));
    }

    @Test
    void node_withEmptyPropertiesSkipsValue() {
        GraphNode node = new GraphNode();
        node.setName("Node3");
        node.setProperties(Collections.emptyMap());

        JsonNode json = node.toEChartNode();

        assertFalse(json.has("value"));
    }

    @Test
    void edge_withPropertiesFormatsValue() {
        GraphEdge edge = new GraphEdge();
        edge.setSource("A");
        edge.setTarget("B");

        Map<String, Object> props = new LinkedHashMap<>();
        props.put("weight", 5);
        props.put("label", "knows");
        edge.setProperties(props);

        JsonNode json = edge.toEChartNode();

        assertEquals("A", json.get("source").asText());
        assertEquals("B", json.get("target").asText());
        assertTrue(json.has("value"));
        String val = json.get("value").asText();
        assertTrue(val.contains("weight: 5"));
        assertTrue(val.contains("label: knows"));
    }

    @Test
    void edge_withoutPropertiesUsesNumericValue() {
        GraphEdge edge = new GraphEdge();
        edge.setSource("A");
        edge.setTarget("B");
        edge.setValue(3);

        JsonNode json = edge.toEChartNode();

        assertEquals("A", json.get("source").asText());
        assertEquals("B", json.get("target").asText());
        assertEquals("3.0", json.get("value").asText());
    }

    @Test
    void edge_skipsValueWhenNullAndNoProperties() {
        GraphEdge edge = new GraphEdge();
        edge.setSource("A");
        edge.setTarget("B");

        JsonNode json = edge.toEChartNode();

        assertFalse(json.has("value"));
    }

    @Test
    void category_withIndexHasColor() {
        GraphCategory category = new GraphCategory();
        category.setName("Group1");
        category.setSymbol("circle");
        category.setIndex(2);

        JsonNode json = category.toEChartNode();

        assertEquals("Group1", json.get("name").asText());
        assertEquals("circle", json.get("symbol").asText());
        assertTrue(json.has("itemStyle"));
        assertTrue(json.get("itemStyle").has("color"));
        assertEquals("#10b981", json.get("itemStyle").get("color").asText());
    }

    @Test
    void category_defaultsSymbolWhenInvalid() {
        GraphCategory category = new GraphCategory();
        category.setName("Group1");
        category.setSymbol("invalid");

        JsonNode json = category.toEChartNode();

        assertEquals("Group1", json.get("name").asText());
        assertEquals("circle", json.get("symbol").asText());
    }

    @Test
    void category_keepsValidSymbol() {
        GraphCategory category = new GraphCategory();
        category.setName("Group2");
        category.setSymbol("triangle");

        JsonNode json = category.toEChartNode();

        assertEquals("triangle", json.get("symbol").asText());
    }

    @Test
    void title_hasStyledText() {
        GraphTitle title = new GraphTitle();
        title.setText("Title");

        JsonNode node = title.toEChartNode();

        assertEquals("Title", node.get("text").asText());
        assertEquals("center", node.get("left").asText());
        assertTrue(node.has("textStyle"));
        assertEquals(16, node.get("textStyle").get("fontSize").asInt());
        assertEquals("bold", node.get("textStyle").get("fontWeight").asText());
    }
}
