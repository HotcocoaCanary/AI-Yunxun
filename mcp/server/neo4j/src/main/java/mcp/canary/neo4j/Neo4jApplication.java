package mcp.canary.neo4j;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.neo4j.Neo4jDataAutoConfiguration;
import org.springframework.boot.autoconfigure.neo4j.Neo4jAutoConfiguration;

/**
 * Neo4j MCP Server 应用主类
 * 禁用 Spring Boot 的 Neo4j 自动配置，使用自定义的 Neo4jConnection
 *
 * @author dsimile
 * @date 2025-5-7
 */
@SpringBootApplication(exclude = {
        Neo4jAutoConfiguration.class,
        Neo4jDataAutoConfiguration.class
})
public class Neo4jApplication {

    public static void main(String[] args) {
        SpringApplication.run(Neo4jApplication.class, args);
    }

}
