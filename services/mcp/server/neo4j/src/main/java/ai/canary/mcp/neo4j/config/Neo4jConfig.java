package ai.canary.mcp.neo4j.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "neo4j")
@Data
public class Neo4jConfig {
    private String uri;
    private String username;
    private String password;
}

