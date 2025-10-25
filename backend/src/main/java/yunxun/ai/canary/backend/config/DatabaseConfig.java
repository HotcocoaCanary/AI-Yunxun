package yunxun.ai.canary.backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.data.neo4j.repository.config.EnableNeo4jRepositories;

@Configuration
@EnableJpaRepositories(basePackages = "yunxun.ai.canary.backend.repository.mysql")
@EnableMongoRepositories(basePackages = "yunxun.ai.canary.backend.repository.mongo")
@EnableNeo4jRepositories(basePackages = "yunxun.ai.canary.backend.repository.neo4j")
public class DatabaseConfig {
}
