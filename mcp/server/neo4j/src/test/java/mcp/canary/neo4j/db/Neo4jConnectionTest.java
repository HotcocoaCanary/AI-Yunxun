package mcp.canary.neo4j.db;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.neo4j.driver.AuthToken;
import org.neo4j.driver.Config;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.neo4j.driver.Session;
import org.neo4j.driver.SessionConfig;
import org.neo4j.driver.exceptions.Neo4jException;

import java.lang.reflect.Field;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class Neo4jConnectionTest {

    @Test
    void constructor_success_verifiesConnectivityAndSetsDatabase() {
        String uri = "bolt://localhost:7687";
        Driver driver = mock(Driver.class);

        try (MockedStatic<GraphDatabase> graphDb = org.mockito.Mockito.mockStatic(GraphDatabase.class)) {
            graphDb.when(() -> GraphDatabase.driver(eq(uri), any(AuthToken.class), any(Config.class)))
                    .thenReturn(driver);

            Neo4jConnection connection = new Neo4jConnection(uri, "user", "pass", "neo4j");

            verify(driver).verifyConnectivity();
            assertEquals("neo4j", connection.getDatabaseName());
        }
    }

    @Test
    void constructor_failure_wrapsException() {
        String uri = "bolt://localhost:7687";
        Driver driver = mock(Driver.class);
        doThrow(new RuntimeException("boom")).when(driver).verifyConnectivity();

        try (MockedStatic<GraphDatabase> graphDb = org.mockito.Mockito.mockStatic(GraphDatabase.class)) {
            graphDb.when(() -> GraphDatabase.driver(eq(uri), any(AuthToken.class), any(Config.class)))
                    .thenReturn(driver);

            assertThrows(Neo4jException.class,
                    () -> new Neo4jConnection(uri, "user", "pass", "neo4j"));
        }
    }

    @Test
    void createSession_usesConfiguredDatabase() {
        String uri = "bolt://localhost:7687";
        Driver driver = mock(Driver.class);
        Session session = mock(Session.class);
        when(driver.session(any(SessionConfig.class))).thenReturn(session);

        try (MockedStatic<GraphDatabase> graphDb = org.mockito.Mockito.mockStatic(GraphDatabase.class)) {
            graphDb.when(() -> GraphDatabase.driver(eq(uri), any(AuthToken.class), any(Config.class)))
                    .thenReturn(driver);

            Neo4jConnection connection = new Neo4jConnection(uri, "user", "pass", "neo4j");
            Session created = connection.createSession();

            assertEquals(session, created);
            ArgumentCaptor<SessionConfig> configCaptor = ArgumentCaptor.forClass(SessionConfig.class);
            verify(driver).session(configCaptor.capture());
            Optional<String> database = configCaptor.getValue().database();
            assertTrue(database.isPresent());
            assertEquals("neo4j", database.get());
        }
    }

    @Test
    void close_handlesDriverCloseFailure() {
        String uri = "bolt://localhost:7687";
        Driver driver = mock(Driver.class);

        try (MockedStatic<GraphDatabase> graphDb = org.mockito.Mockito.mockStatic(GraphDatabase.class)) {
            graphDb.when(() -> GraphDatabase.driver(eq(uri), any(AuthToken.class), any(Config.class)))
                    .thenReturn(driver);

            Neo4jConnection connection = new Neo4jConnection(uri, "user", "pass", "neo4j");
            doThrow(new RuntimeException("close failed")).when(driver).close();

            assertDoesNotThrow(connection::close);
        }
    }

    @Test
    void close_closesDriverSuccessfully() {
        String uri = "bolt://localhost:7687";
        Driver driver = mock(Driver.class);

        try (MockedStatic<GraphDatabase> graphDb = org.mockito.Mockito.mockStatic(GraphDatabase.class)) {
            graphDb.when(() -> GraphDatabase.driver(eq(uri), any(AuthToken.class), any(Config.class)))
                    .thenReturn(driver);

            Neo4jConnection connection = new Neo4jConnection(uri, "user", "pass", "neo4j");

            assertDoesNotThrow(connection::close);
            verify(driver).close();
        }
    }

    @Test
    void close_skipsWhenDriverNull() throws Exception {
        String uri = "bolt://localhost:7687";
        Driver driver = mock(Driver.class);

        try (MockedStatic<GraphDatabase> graphDb = org.mockito.Mockito.mockStatic(GraphDatabase.class)) {
            graphDb.when(() -> GraphDatabase.driver(eq(uri), any(AuthToken.class), any(Config.class)))
                    .thenReturn(driver);

            Neo4jConnection connection = new Neo4jConnection(uri, "user", "pass", "neo4j");
            Field driverField = Neo4jConnection.class.getDeclaredField("driver");
            driverField.setAccessible(true);
            driverField.set(connection, null);

            assertNotNull(connection.getDatabaseName());
            assertDoesNotThrow(connection::close);
        }
    }
}
