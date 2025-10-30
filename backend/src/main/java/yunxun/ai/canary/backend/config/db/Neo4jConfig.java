package yunxun.ai.canary.backend.config.db;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.neo4j.repository.config.EnableNeo4jRepositories;

@Configuration
@EnableNeo4jRepositories(basePackages = "yunxun.ai.canary.backend.repository")
public class Neo4jConfig {
    // Spring Boot 3+ 已自动管理驱动，一般无需手动配置
}
