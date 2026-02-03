package mcp.canary.neo4j.db;

import lombok.Getter;
import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Config;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.neo4j.driver.Session;
import org.neo4j.driver.SessionConfig;
import org.neo4j.driver.exceptions.Neo4jException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * Neo4j 数据库连接管理类
 * 负责创建和管理 Neo4j 驱动程序的连接
 */
@Getter
@Component
public class Neo4jConnection implements AutoCloseable {

    private static final Logger logger = LoggerFactory.getLogger(Neo4jConnection.class);
    private final Driver driver;
    private final String databaseName;

    public Neo4jConnection(
            @Value("${neo4j.uri}") String uri,
            @Value("${neo4j.username}") String username,
            @Value("${neo4j.password}") String password,
            @Value("${neo4j.database:neo4j}") String databaseName) {
        logger.debug("正在初始化与 {} 数据库的连接，数据库名称为 {}", uri, databaseName);
        this.driver = GraphDatabase.driver(uri, AuthTokens.basic(username, password), config());
        try {
            driver.verifyConnectivity();
            logger.info("已成功连接到位于 {} 的 Neo4j 数据库 {}", uri, databaseName);
        } catch (Exception e) {
            logger.error("无法验证与 Neo4j 的连接：{}", e.getMessage());
            throw new Neo4jException("Neo4j 连接失败: ", e);
        }
        this.databaseName = databaseName;
    }

    private Config config() {
        return Config.builder()
                .withConnectionTimeout(300, TimeUnit.SECONDS)
                .withMaxConnectionPoolSize(100)
                .withMaxConnectionLifetime(1, TimeUnit.HOURS)
                .withConnectionAcquisitionTimeout(600, TimeUnit.SECONDS)
                .withMaxTransactionRetryTime(300, TimeUnit.SECONDS)
                .build();
    }

    public Session createSession() {
        return driver.session(SessionConfig.forDatabase(databaseName));
    }

    @Override
    public void close() {
        if (driver != null) {
            try {
                driver.close();
                logger.info("Neo4j 驱动程序已成功关闭。");
            } catch (Exception e) {
                logger.error("关闭 Neo4j 驱动程序时出错：{}", e.getMessage(), e);
            }
        }
    }
}