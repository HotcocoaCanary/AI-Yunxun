package mcp.canary.neo4j;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.neo4j.driver.AuthToken;
import org.neo4j.driver.Config;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;

class Neo4jApplicationMainTest {

    @Test
    void main_startsAndStops() {
        String uri = "bolt://localhost:7687";
        Driver driver = mock(Driver.class);

        try (MockedStatic<GraphDatabase> graphDb = org.mockito.Mockito.mockStatic(GraphDatabase.class)) {
            graphDb.when(() -> GraphDatabase.driver(eq(uri), any(AuthToken.class), any(Config.class)))
                    .thenReturn(driver);

            assertDoesNotThrow(() -> Neo4jApplication.main(new String[]{
                    "--spring.main.web-application-type=none",
                    "--spring.main.banner-mode=off",
                    "--spring.main.register-shutdown-hook=false",
                    "--neo4j.uri=" + uri,
                    "--neo4j.username=user",
                    "--neo4j.password=pass"
            }));
        }
    }
}
