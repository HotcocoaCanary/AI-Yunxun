package mcp.canary.neo4j;

import mcp.canary.neo4j.service.Neo4jService;
import mcp.canary.neo4j.tool.Neo4jMCPTool;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

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

    @Autowired
    private Neo4jMCPTool neo4jMCPTool;

    /**
     * 测试 MCP 写入方法：write-neo4j-cypher
     * <p>
     * 验证点：
     * - 能成功执行 CREATE
     * - 返回写入统计信息
     */
    @Test
    @Order(1)
    void testWriteNeo4jCypher() {
        List<Map<String, Object>> result = neo4jMCPTool.neo4jWrite(
                "CREATE (n:Test_MCP_Node {name:'MCP_Test', value:123})",
                null
        );

        assertNotNull(result);
        assertEquals(1, result.size());

        Map<String, Object> counters = result.get(0);
        assertTrue((Boolean) counters.get("containsUpdates"));
        assertTrue((Integer) counters.get("nodesCreated") >= 1);
    }

    /**
     * 测试 MCP 读取方法：read-neo4j-cypher
     * <p>
     * 验证点：
     * - 能正确读取刚刚写入的数据
     * - 返回字段和值正确
     */
    @Test
    @Order(2)
    void testReadNeo4jCypher() {
        List<Map<String, Object>> result = neo4jMCPTool.neo4jRead(
                "MATCH (n:Test_MCP_Node {name:'MCP_Test'}) " +
                        "RETURN n.name AS name, n.value AS value",
                null
        );

        assertNotNull(result);
        assertEquals(1, result.size());

        Map<String, Object> row = result.get(0);
        assertEquals("MCP_Test", row.get("name"));
        assertEquals(123L, row.get("value")); // Neo4j 数值默认 Long
    }

    /**
     * 验证 MCP 层的「读写隔离」规则
     * read-neo4j-cypher 不允许写操作
     */
    @Test
    @Order(3)
    void testReadRejectWriteQuery() {
        assertThrows(IllegalArgumentException.class, () ->
                neo4jMCPTool.neo4jRead(
                        "CREATE (n:ShouldFail)",
                        null
                )
        );
    }

    /**
     * 验证 MCP 层的「读写隔离」规则
     * write-neo4j-cypher 不允许纯读操作
     */
    @Test
    @Order(4)
    void testWriteRejectReadQuery() {
        assertThrows(IllegalArgumentException.class, () ->
                neo4jMCPTool.neo4jWrite(
                        "MATCH (n) RETURN n",
                        null
                )
        );
    }

    /**
     * 清理测试数据
     */
    @AfterAll
    static void cleanup(@Autowired Neo4jService service) {
        service.executeQuery(
                "MATCH (n:Test_MCP_Node) DELETE n",
                Map.of()
        );
    }
}
