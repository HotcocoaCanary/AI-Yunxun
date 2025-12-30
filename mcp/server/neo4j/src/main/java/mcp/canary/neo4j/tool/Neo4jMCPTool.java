package mcp.canary.neo4j.tool;

import jakarta.annotation.Resource;
import mcp.canary.neo4j.service.Neo4jService;
import org.springaicommunity.mcp.annotation.McpTool;
import org.springaicommunity.mcp.annotation.McpToolParam;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import io.modelcontextprotocol.spec.McpSchema.LoggingLevel;
import io.modelcontextprotocol.spec.McpSchema.LoggingMessageNotification;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Component
public class Neo4jMCPTool {

    @Resource
    public Neo4jService neo4jService;

    private static final String SCHEMA = """
            call apoc.meta.data() yield label, property, type, other, unique, index, elementType
            where elementType = 'node' and not label starts with '_'
            with label,
                collect(case when type <> 'RELATIONSHIP' then [property, type + case when unique then " unique" else "" end + case when index then " indexed" else "" end] end) as attributes,
                collect(case when type = 'RELATIONSHIP' then [property, head(other)] end) as relationships
            RETURN label, apoc.map.fromPairs(attributes) as attributes, apoc.map.fromPairs(relationships) as relationships
            """;

    @McpTool(name = "get-neo4j-schema", description = "列出 Neo4j 数据库中的所有节点类型、它们的属性以及它们与其他节点类型之间的关系")
    public List<Map<String, Object>> neo4jSchema(McpSyncServerExchange exchange) {
        sendLog(exchange, LoggingLevel.INFO, "开始获取 Neo4j 数据库架构信息");
        
        try {
            List<Map<String, Object>> result = neo4jService.executeQuery(SCHEMA, Collections.emptyMap());
            sendLog(exchange, LoggingLevel.INFO, "成功获取架构信息，共 " + result.size() + " 个节点类型");
            return result;
        } catch (Exception e) {
            sendLog(exchange, LoggingLevel.ERROR, "获取架构信息失败: " + e.getMessage());
            throw e;
        }
    }

    @McpTool(name = "read-neo4j-cypher", description = "在 neo4j 数据库上执行 Cypher 查询")
    public List<Map<String, Object>> neo4jRead(
            @McpToolParam(description = "Cypher 读取要执行的查询") String query,
            McpSyncServerExchange exchange) {
        
        sendLog(exchange, LoggingLevel.INFO, "开始执行 Neo4j 读取查询");
        sendLog(exchange, LoggingLevel.INFO, "查询语句: " + (query != null ? query.substring(0, Math.min(100, query.length())) + (query.length() > 100 ? "..." : "") : "null"));
        
        try {
            if (neo4jService.isWriteQuery(query)) {
                sendLog(exchange, LoggingLevel.ERROR, "读取查询只允许使用 MATCH 查询");
                throw new IllegalArgumentException("读取查询只允许使用 MATCH 查询。");
            }
            
            List<Map<String, Object>> result = neo4jService.executeQuery(query, Collections.emptyMap());
            sendLog(exchange, LoggingLevel.INFO, "读取查询执行完成，返回 " + result.size() + " 条记录");
            return result;
        } catch (Exception e) {
            sendLog(exchange, LoggingLevel.ERROR, "执行读取查询失败: " + e.getMessage());
            throw e;
        }
    }

    @McpTool(name = "write-neo4j-cypher", description = "在 Neo4j 数据库上执行写入 Cypher 查询")
    public List<Map<String, Object>> neo4jWrite(
            @McpToolParam(description = "编写 Cypher 查询语句并执行") String query,
            McpSyncServerExchange exchange) {
        
        sendLog(exchange, LoggingLevel.INFO, "开始执行 Neo4j 写入查询");
        sendLog(exchange, LoggingLevel.INFO, "查询语句: " + (query != null ? query.substring(0, Math.min(100, query.length())) + (query.length() > 100 ? "..." : "") : "null"));
        
        try {
            if (!neo4jService.isWriteQuery(query)) {
                sendLog(exchange, LoggingLevel.ERROR, "仅允许对写入查询执行写入操作");
                throw new IllegalArgumentException("仅允许对写入查询执行写入操作。");
            }
            
            List<Map<String, Object>> result = neo4jService.executeQuery(query, Collections.emptyMap());
            
            // 从结果中提取写入统计信息
            if (!result.isEmpty() && result.get(0).containsKey("nodesCreated")) {
                Map<String, Object> counters = result.get(0);
                sendLog(exchange, LoggingLevel.INFO, 
                    String.format("写入操作完成 - 创建节点: %d, 创建关系: %d, 设置属性: %d",
                        (Integer) counters.getOrDefault("nodesCreated", 0),
                        (Integer) counters.getOrDefault("relationshipsCreated", 0),
                        (Integer) counters.getOrDefault("propertiesSet", 0)));
            } else {
                sendLog(exchange, LoggingLevel.INFO, "写入查询执行完成");
            }
            
            return result;
        } catch (Exception e) {
            sendLog(exchange, LoggingLevel.ERROR, "执行写入查询失败: " + e.getMessage());
            throw e;
        }
    }
    
    /**
     * 发送日志通知的辅助方法
     * 
     * @param exchange MCP服务器交换对象，用于发送日志通知
     * @param level 日志级别
     * @param message 日志消息
     */
    private void sendLog(McpSyncServerExchange exchange, LoggingLevel level, String message) {
        if (exchange != null) {
            try {
                exchange.loggingNotification(
                    LoggingMessageNotification.builder()
                        .level(level)
                        .logger("neo4j-tool")
                        .data(message)
                        .build()
                );
            } catch (Exception e) {
                // 如果日志发送失败，不影响主流程，只记录到控制台
                System.err.println("Failed to send log notification: " + e.getMessage());
            }
        }
    }

}
