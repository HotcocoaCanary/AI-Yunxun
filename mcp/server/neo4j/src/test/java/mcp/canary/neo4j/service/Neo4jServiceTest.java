package mcp.canary.neo4j.service;

import mcp.canary.neo4j.db.Neo4jConnection;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.neo4j.driver.Record;
import org.neo4j.driver.Result;
import org.neo4j.driver.Session;
import org.neo4j.driver.summary.ResultSummary;
import org.neo4j.driver.summary.SummaryCounters;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class Neo4jServiceTest {

    @Mock
    private Neo4jConnection neo4jConnection;

    @Mock
    private Session session;

    @Mock
    private Result result;

    @InjectMocks
    private Neo4jService neo4jService;

    @Test
    void execute_nullParams_usesEmptyMap() {
        List<Map<String, Object>> expected = List.of(Map.of("k", "v"));
        when(neo4jConnection.createSession()).thenReturn(session);
        when(session.run(anyString(), anyMap())).thenReturn(result);
        when(result.list(ArgumentMatchers.<Function<Record, Map<String, Object>>>any()))
                .thenReturn(expected);

        List<Map<String, Object>> actual = neo4jService.execute("MATCH (n) RETURN n", null);

        assertEquals(expected, actual);
        ArgumentCaptor<Map<String, Object>> paramsCaptor = ArgumentCaptor.forClass(Map.class);
        verify(session).run(anyString(), paramsCaptor.capture());
        assertTrue(paramsCaptor.getValue().isEmpty());
        verify(session).close();
    }

    @Test
    void execute_params_passthrough() {
        Map<String, Object> params = Collections.singletonMap("name", "Alice");
        when(neo4jConnection.createSession()).thenReturn(session);
        when(session.run(anyString(), anyMap())).thenReturn(result);
        when(result.list(ArgumentMatchers.<Function<Record, Map<String, Object>>>any()))
                .thenReturn(List.of());

        neo4jService.execute("MATCH (n) RETURN n", params);

        ArgumentCaptor<Map<String, Object>> paramsCaptor = ArgumentCaptor.forClass(Map.class);
        verify(session).run(anyString(), paramsCaptor.capture());
        assertSame(params, paramsCaptor.getValue());
        verify(session).close();
    }

    @Test
    void deduplicateNodesByName_executesDedupeQuery() {
        Neo4jService spyService = spy(new Neo4jService(neo4jConnection));
        doReturn(List.of()).when(spyService).execute(anyString(), isNull());

        spyService.deduplicateNodesByName();

        ArgumentCaptor<String> queryCaptor = ArgumentCaptor.forClass(String.class);
        verify(spyService).execute(queryCaptor.capture(), isNull());
        assertTrue(queryCaptor.getValue().contains("apoc.refactor.mergeNodes"));
        assertTrue(queryCaptor.getValue().contains("WHERE size(nodes) > 1"));
    }

    @Test
    void executeWriteWithSummary_returnsCounters() {
        when(neo4jConnection.createSession()).thenReturn(session);
        when(session.run(anyString(), anyMap())).thenReturn(result);
        ResultSummary summary = org.mockito.Mockito.mock(ResultSummary.class);
        SummaryCounters counters = org.mockito.Mockito.mock(SummaryCounters.class);
        when(result.consume()).thenReturn(summary);
        when(summary.counters()).thenReturn(counters);
        when(counters.nodesCreated()).thenReturn(1);
        when(counters.nodesDeleted()).thenReturn(2);
        when(counters.relationshipsCreated()).thenReturn(3);
        when(counters.propertiesSet()).thenReturn(4);

        Map<String, Object> stats = neo4jService.executeWriteWithSummary("CREATE (n)", null);

        assertEquals(1, stats.get("nodesCreated"));
        assertEquals(2, stats.get("nodesDeleted"));
        assertEquals(3, stats.get("relationshipsCreated"));
        assertEquals(4, stats.get("propertiesSet"));
        ArgumentCaptor<Map<String, Object>> paramsCaptor = ArgumentCaptor.forClass(Map.class);
        verify(session).run(anyString(), paramsCaptor.capture());
        assertTrue(paramsCaptor.getValue().isEmpty());
        verify(session).close();
    }

    @Test
    void executeWriteWithSummary_params_passthrough() {
        Map<String, Object> params = Map.of("name", "Bob");
        when(neo4jConnection.createSession()).thenReturn(session);
        when(session.run(anyString(), anyMap())).thenReturn(result);
        ResultSummary summary = org.mockito.Mockito.mock(ResultSummary.class);
        SummaryCounters counters = org.mockito.Mockito.mock(SummaryCounters.class);
        when(result.consume()).thenReturn(summary);
        when(summary.counters()).thenReturn(counters);

        neo4jService.executeWriteWithSummary("MATCH (n) RETURN n", params);

        ArgumentCaptor<Map<String, Object>> paramsCaptor = ArgumentCaptor.forClass(Map.class);
        verify(session).run(anyString(), paramsCaptor.capture());
        assertSame(params, paramsCaptor.getValue());
        verify(session).close();
    }
}
