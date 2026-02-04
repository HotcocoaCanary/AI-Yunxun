package mcp.canary.neo4j.tool;

import io.modelcontextprotocol.server.McpSyncServerExchange;
import io.modelcontextprotocol.spec.McpSchema.LoggingLevel;
import io.modelcontextprotocol.spec.McpSchema.LoggingMessageNotification;
import jakarta.annotation.Resource;
import mcp.canary.neo4j.service.Neo4jService;
import org.springaicommunity.mcp.annotation.McpTool;
import org.springaicommunity.mcp.annotation.McpToolParam;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class Neo4jMCPTool {

    @Resource
    private Neo4jService neo4jService;

    /**
     * 获取数据库架构，包含标签和属性名。
     * 提示：此工具使用 APOC 辅助生成元数据预览。
     */
    @McpTool(name = "get-neo4j-schema",
            description = "获取图数据库的 Schema。包含节点标签(Labels)、属性键和关系类型。在编写 Cypher 之前应先调用此工具。")
    public List<Map<String, Object>> getNeo4jSchema(McpSyncServerExchange exchange) {
        sendLog(exchange, "Fetching database schema...");
        String query = "CALL apoc.meta.data() YIELD label, property, type, elementType " +
                "WHERE elementType = 'node' RETURN label, collect(property) as properties";
        return neo4jService.execute(query, null);
    }

    /**
     * 读取数据并在完成后触发自动去重。
     * 注意事项：此工具仅限 MATCH/RETURN 语句。系统会自动合并 name 相同的重复节点。
     */
    @McpTool(name = "read-neo4j-cypher",
            description = "执行读取查询。注意：系统会自动触发基于 'name' 属性的去重（使用 APOC 合并节点和关系）。仅支持 MATCH 查询。")
    public List<Map<String, Object>> readNeo4jCypher(
            @McpToolParam(description = "Cypher read query (e.g. MATCH (n) RETURN n LIMIT 10)") String query,
            McpSyncServerExchange exchange) {

        if (query.toUpperCase().matches(".*\\b(CREATE|MERGE|DELETE|SET)\\b.*")) {
            throw new IllegalArgumentException("Read tool only supports read-only queries.");
        }

        sendLog(exchange, "Executing read and checking for duplicates...");
        List<Map<String, Object>> results = neo4jService.execute(query, null);

        // 执行去重逻辑
        neo4jService.deduplicateNodesByName();

        return results;
    }

    /**
     * 写入数据。
     * 注意事项：建议优先使用 MERGE 语法。如果使用 CREATE，系统会尝试应用幂等逻辑。
     */
    @McpTool(name = "write-neo4j-cypher",
            description = "执行写入查询。注意事项：请务必使用 MERGE 语法而非 CREATE，以确保 '存在即修改' 的幂等行为。若节点已存在，请在 MERGE 后面紧跟 ON MATCH SET。")
    public Map<String, Object> writeNeo4jCypher(
            @McpToolParam(description = "Cypher write query (Recommended: MERGE (n:Label {name: 'xxx'}) ON MATCH SET n.prop = 'val')") String query,
            McpSyncServerExchange exchange) {

        sendLog(exchange, "开始执行 Neo4j 读取查询");

        // 自动逻辑增强：将裸的 CREATE 转换为 MERGE 是一种高风险操作，
        // 我们通过提示词强约束 LLM 使用 MERGE，此处直接执行并返回统计。
        return neo4jService.executeWriteWithSummary(query, null);
    }

    private void sendLog(McpSyncServerExchange exchange, String message) {
        if (exchange != null) {
            exchange.loggingNotification(LoggingMessageNotification.builder()
                    .level(LoggingLevel.INFO)
                    .logger("Neo4j-Tool")
                    .data(message)
                    .build());
        }
    }
}