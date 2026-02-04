package mcp.canary.neo4j;

import org.junit.jupiter.api.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Neo4j MCP Tool 集成测试
 * <p>
 * 测试目标：
 * 1. write-neo4j-cypher
 * 2. read-neo4j-cypher
 * <p>
 * 特点：
 * - 使用真实 Neo4j
 * - 不使用 Mock
 * - 只测试 MCP 对外暴露的方法
 */
@SpringBootTest
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class Neo4jMcpToolIntegrationTest {

}
