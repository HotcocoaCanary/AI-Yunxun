package yunxun.ai.canary.project.service.mcp.server.tool;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.spec.McpSchema;
import io.modelcontextprotocol.spec.McpSchema.TextContent;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import yunxun.ai.canary.project.service.mcp.server.tool.model.ToolResponse;
import yunxun.ai.canary.project.service.mcp.server.tool.model.ToolResponses;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * 网络搜索工具
 *
 * <p>说明：本工具仅在 {@code web.search.enabled=true} 时注册到本地 MCP Server。
 * 默认实现优先通过外部 MCP（如 mcp-searxng）进行搜索。</p>
 */
@Component
@ConditionalOnProperty(name = "web.search.enabled", havingValue = "true", matchIfMissing = false)
public class WebSearchTool {

    private final ObjectMapper objectMapper;
    private final Optional<McpSyncClient> webSearchMcpClient;
    private final String toolName;

    public WebSearchTool(
            ObjectMapper objectMapper,
            @Qualifier("webSearchMcpClient") Optional<McpSyncClient> webSearchMcpClient,
            @Value("${web.search.mcp.tool-name:web_search}") String toolName) {
        this.objectMapper = objectMapper;
        this.webSearchMcpClient = webSearchMcpClient;
        this.toolName = toolName;
    }

    @Tool(name = "web_search", description = "执行网络搜索，返回 title/url/snippet 等结构化结果")
    public ToolResponse webSearch(
            @ToolParam(required = true, description = "搜索关键词") String query,
            @ToolParam(required = false, description = "最大返回条数，默认 5，最大 20") Integer maxResults,
            @ToolParam(required = false, description = "语言，如 zh-CN") String language,
            @ToolParam(required = false, description = "时间范围（天），如 30") Integer recencyDays) {
        Instant startedAt = Instant.now();
        String traceId = UUID.randomUUID().toString();

        if (query == null || query.isBlank()) {
            return ToolResponses.error("INVALID_ARGUMENT", "query 不能为空", null, traceId, startedAt);
        }

        int lim = maxResults == null ? 5 : Math.min(Math.max(maxResults, 1), 20);
        String lang = language == null || language.isBlank() ? "zh-CN" : language;
        int recency = recencyDays == null ? 30 : Math.min(Math.max(recencyDays, 1), 365);

        if (webSearchMcpClient.isEmpty()) {
            return ToolResponses.error("UPSTREAM_ERROR", "未配置外部 MCP 搜索客户端（web.search.mcp.url）", null, traceId, startedAt);
        }

        try {
            McpSyncClient client = webSearchMcpClient.get();
            if (!client.isInitialized()) {
                client.initialize();
            }

            Map<String, Object> args = new HashMap<>();
            args.put("query", query);
            args.put("maxResults", lim);
            args.put("language", lang);
            args.put("recencyDays", recency);

            McpSchema.CallToolResult result = client.callTool(new McpSchema.CallToolRequest(toolName, args));
            Object data = parseToolResultContent(result);
            return ToolResponses.ok(data, traceId, startedAt);
        }
        catch (Exception ex) {
            return ToolResponses.error("UPSTREAM_ERROR", ex.getMessage(), null, traceId, startedAt);
        }
    }

    private Object parseToolResultContent(McpSchema.CallToolResult result) throws JsonProcessingException {
        if (result == null || result.content() == null || result.content().isEmpty()) {
            return Map.of("items", List.of());
        }

        for (McpSchema.Content content : result.content()) {
            if (content instanceof TextContent textContent) {
                String text = textContent.text();
                if (text == null || text.isBlank()) {
                    continue;
                }
                try {
                    return objectMapper.readValue(text, Object.class);
                }
                catch (JsonProcessingException ignored) {
                    return Map.of("text", text);
                }
            }
        }

        return Map.of("items", List.of());
    }
}
