package mcp.canary.echart.tool;

import com.fasterxml.jackson.databind.JsonNode;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import io.modelcontextprotocol.spec.McpSchema.LoggingLevel;
import io.modelcontextprotocol.spec.McpSchema.LoggingMessageNotification;
import mcp.canary.echart.module.graph.series.data.GraphCategory;
import mcp.canary.echart.module.graph.series.data.GraphEdge;
import mcp.canary.echart.module.graph.series.data.GraphNode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class GraphEChartMCPToolTest {

    @Mock
    private McpSyncServerExchange exchange;

    @InjectMocks
    private GraphEChartMCPTool tool;

    @Test
    void generateGraphOption_withTitleAndCategories() {
        GraphNode node = new GraphNode();
        node.setName("A");
        node.setCategoryName("Type1");

        GraphEdge edge = new GraphEdge();
        edge.setSource("A");
        edge.setTarget("B");

        GraphCategory category = new GraphCategory();
        category.setName("Type1");
        category.setSymbol("diamond");

        JsonNode result = tool.generateGraphOption(
                "Chart",
                "circular",
                List.of(node),
                List.of(edge),
                List.of(category),
                exchange
        );

        assertEquals("Chart", result.get("title").get("text").asText());
        JsonNode series = result.get("series").get(0);
        assertEquals("circular", series.get("layout").asText());
        assertEquals(1, series.get("categories").size());
        assertEquals("diamond", series.get("categories").get(0).get("symbol").asText());
        assertEquals(0, series.get("data").get(0).get("category").asInt());

        ArgumentCaptor<LoggingMessageNotification> captor =
                ArgumentCaptor.forClass(LoggingMessageNotification.class);
        verify(exchange, times(2)).loggingNotification(captor.capture());
        assertTrue(captor.getAllValues().stream()
                .allMatch(msg -> msg.level() == LoggingLevel.INFO));
    }

    @Test
    void generateGraphOption_defaultsAndAutoCategories() {
        GraphNode nodeA = new GraphNode();
        nodeA.setName("A");
        nodeA.setCategoryName("GroupA");

        GraphNode nodeB = new GraphNode();
        nodeB.setName("B");
        nodeB.setCategoryName("GroupB");

        GraphNode nodeC = new GraphNode();
        nodeC.setName("C");

        JsonNode result = tool.generateGraphOption(
                null,
                null,
                List.of(nodeA, nodeB, nodeC),
                null,
                null,
                null
        );

        assertFalse(result.has("title"));
        JsonNode series = result.get("series").get(0);
        assertEquals("force", series.get("layout").asText());
        assertEquals(3, series.get("data").size());

        Set<String> categoryNames = new HashSet<>();
        for (JsonNode category : series.get("categories")) {
            categoryNames.add(category.get("name").asText());
        }
        assertEquals(Set.of("GroupA", "GroupB"), categoryNames);
    }

    @Test
    void generateGraphOption_blankTitleAndNullNodes_skipsTitle() {
        JsonNode result = tool.generateGraphOption(
                "   ",
                null,
                null,
                null,
                List.of(),
                null
        );

        assertFalse(result.has("title"));
        JsonNode series = result.get("series").get(0);
        assertEquals(0, series.get("categories").size());
        assertEquals(0, series.get("data").size());
    }

    @Test
    void generateGraphOption_emptyNodes_returnsEmptyCategories() {
        JsonNode result = tool.generateGraphOption(
                null,
                "force",
                List.of(),
                null,
                null,
                exchange
        );

        JsonNode series = result.get("series").get(0);
        assertEquals(0, series.get("categories").size());
        assertEquals(0, series.get("data").size());
    }

    @Test
    void generateGraphOption_logsErrorOnException() {
        List<GraphNode> nodes = new java.util.ArrayList<>();
        nodes.add(null);

        ArgumentCaptor<LoggingMessageNotification> captor =
                ArgumentCaptor.forClass(LoggingMessageNotification.class);

        assertThrows(RuntimeException.class, () -> tool.generateGraphOption(
                "Chart",
                "force",
                nodes,
                null,
                null,
                exchange
        ));

        verify(exchange, times(2)).loggingNotification(captor.capture());
        assertTrue(captor.getAllValues().stream()
                .anyMatch(msg -> msg.level() == LoggingLevel.ERROR));
    }

    @Test
    void generateGraphOption_loggingFailureIsSwallowed() {
        McpSyncServerExchange failingExchange = mock(McpSyncServerExchange.class);
        doThrow(new RuntimeException("log failure"))
                .when(failingExchange)
                .loggingNotification(any());

        GraphNode node = new GraphNode();
        node.setName("A");

        JsonNode result = tool.generateGraphOption(
                "Chart",
                "force",
                List.of(node),
                null,
                null,
                failingExchange
        );

        assertNotNull(result);
    }
}
