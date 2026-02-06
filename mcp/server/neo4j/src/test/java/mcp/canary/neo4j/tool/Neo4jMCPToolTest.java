package mcp.canary.neo4j.tool;

import io.modelcontextprotocol.server.McpSyncServerExchange;
import mcp.canary.neo4j.service.Neo4jService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class Neo4jMCPToolTest {

    @Mock
    private Neo4jService neo4jService;

    @Mock
    private McpSyncServerExchange exchange;

    @InjectMocks
    private Neo4jMCPTool tool;

    @Test
    void getNeo4jSchema_logsAndExecutes() {
        List<Map<String, Object>> expected = List.of(Map.of("label", "Person"));
        when(neo4jService.execute(any(), isNull())).thenReturn(expected);

        List<Map<String, Object>> result = tool.getNeo4jSchema(exchange);

        assertEquals(expected, result);
        verify(exchange).loggingNotification(any());
        verify(neo4jService).execute(
                "CALL apoc.meta.data() YIELD label, property, type, elementType " +
                        "WHERE elementType = 'node' RETURN label, collect(property) as properties",
                null);
    }

    @Test
    void getNeo4jSchema_exchangeNull_executesWithoutLogging() {
        List<Map<String, Object>> expected = List.of(Map.of("label", "Company"));
        when(neo4jService.execute(any(), isNull())).thenReturn(expected);

        List<Map<String, Object>> result = tool.getNeo4jSchema(null);

        assertEquals(expected, result);
        verify(neo4jService).execute(any(), isNull());
    }

    @Test
    void readNeo4jCypher_writeKeyword_throwsException() {
        assertThrows(IllegalArgumentException.class,
                () -> tool.readNeo4jCypher("match (n) create (m)", exchange));

        verifyNoInteractions(neo4jService);
    }

    @Test
    void readNeo4jCypher_validRead_executesAndDeduplicatesAndLogs() {
        List<Map<String, Object>> expected = List.of(Map.of("name", "Alice"));
        when(neo4jService.execute(any(), isNull())).thenReturn(expected);

        List<Map<String, Object>> result = tool.readNeo4jCypher("MATCH (n) RETURN n", exchange);

        assertEquals(expected, result);
        verify(exchange).loggingNotification(any());
        InOrder order = inOrder(neo4jService);
        order.verify(neo4jService).execute("MATCH (n) RETURN n", null);
        order.verify(neo4jService).deduplicateNodesByName();
    }

    @Test
    void readNeo4jCypher_validRead_exchangeNull_executesWithoutLogging() {
        List<Map<String, Object>> expected = List.of(Map.of("name", "Bob"));
        when(neo4jService.execute(any(), isNull())).thenReturn(expected);

        List<Map<String, Object>> result = tool.readNeo4jCypher("MATCH (n) RETURN n LIMIT 1", null);

        assertEquals(expected, result);
        InOrder order = inOrder(neo4jService);
        order.verify(neo4jService).execute("MATCH (n) RETURN n LIMIT 1", null);
        order.verify(neo4jService).deduplicateNodesByName();
    }

    @Test
    void writeNeo4jCypher_logsAndExecutes() {
        Map<String, Object> expected = Map.of("nodesCreated", 1);
        when(neo4jService.executeWriteWithSummary(any(), isNull())).thenReturn(expected);

        Map<String, Object> result = tool.writeNeo4jCypher("MERGE (n:Person {name:'A'})", exchange);

        assertEquals(expected, result);
        verify(exchange).loggingNotification(any());
        verify(neo4jService).executeWriteWithSummary("MERGE (n:Person {name:'A'})", null);
    }

    @Test
    void writeNeo4jCypher_exchangeNull_executesWithoutLogging() {
        Map<String, Object> expected = Map.of("nodesCreated", 2);
        when(neo4jService.executeWriteWithSummary(any(), isNull())).thenReturn(expected);

        Map<String, Object> result = tool.writeNeo4jCypher("CREATE (n:Person {name:'B'})", null);

        assertEquals(expected, result);
        verify(neo4jService).executeWriteWithSummary("CREATE (n:Person {name:'B'})", null);
    }
}
