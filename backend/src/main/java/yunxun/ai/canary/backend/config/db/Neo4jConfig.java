package yunxun.ai.canary.backend.config.db;

import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Config;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Neo4j 配置类（支持从 Spring 环境配置中读取）
 * 推荐配置位置：
 * application.yml:
 * spring:
 *   neo4j:
 *     uri: bolt://localhost:7687
 *     authentication:
 *       username: neo4j
 *       password: password
 */
@Configuration
public class Neo4jConfig {

    @Value("${spring.neo4j.uri}")
    private String uri;

    @Value("${spring.neo4j.authentication.username}")
    private String username;

    @Value("${spring.neo4j.authentication.password}")
    private String password;

    @Bean(destroyMethod = "close")
    public Driver neo4jDriver() {
        return GraphDatabase.driver(
                uri,
                AuthTokens.basic(username, password),
                Config.builder()
                        .withMaxConnectionPoolSize(50)
                        .build()
        );
    }
}
