package mcp.canary.client.service;

import io.modelcontextprotocol.client.McpAsyncClient;
import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.transport.WebFluxSseClientTransport;
import io.modelcontextprotocol.json.McpJsonMapper;
import io.modelcontextprotocol.spec.McpSchema;
import mcp.canary.client.model.MCPServerConfig;
import mcp.canary.client.model.ToolLogEvent;
import org.springframework.ai.mcp.AsyncMcpToolCallbackProvider;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import jakarta.annotation.PostConstruct;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * MCP 客户端连接与工具回调管理。
 *
 * <p>当前实现以 SSE transport 为主，动态从 {@link MCPService} 读取服务器列表并建立连接。</p>
 */
@Service
public class MCPClientService {

    private static final String DEFAULT_SSE_ENDPOINT = "/sse";

    private final MCPService mcpService;
    private final WebClient.Builder webClientBuilder;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * key：server id（约定等于 name）；value：MCP async client
     */
    private final Map<String, McpAsyncClient> clients = new ConcurrentHashMap<>();

    public MCPClientService(MCPService mcpService,
                            WebClient.Builder webClientBuilder,
                            ApplicationEventPublisher eventPublisher) {
        this.mcpService = mcpService;
        this.webClientBuilder = webClientBuilder;
        this.eventPublisher = eventPublisher;
    }

    @PostConstruct
    public void init() {
        // 从 JSON 配置加载并创建 client
        for (MCPServerConfig server : mcpService.listServers()) {
            try {
                ensureClient(server);
            } catch (Exception e) {
                // 启动期容错：单个 server 失败不影响整体启动
                eventPublisher.publishEvent(new ToolLogEvent(
                        server.id(),
                        "ERROR",
                        "mcp-client",
                        "Failed to init MCP client: " + e.getMessage(),
                        Instant.now()
                ));
            }
        }
    }

    public List<MCPServerConfig> listServers() {
        return mcpService.listServers();
    }

    public MCPServerConfig addServer(MCPServerConfig server) {
        MCPServerConfig added = mcpService.addServer(server);
        ensureClient(added);
        return added;
    }

    public boolean deleteServer(String id) {
        boolean removed = mcpService.deleteServer(id).isPresent();
        if (removed) {
            McpAsyncClient client = clients.remove(id);
            if (client != null) {
                try {
                    client.close();
                } catch (Exception ignored) {
                }
            }
        }
        return removed;
    }

    public ToolCallback[] getToolCallbacks() {
        List<McpAsyncClient> list = new ArrayList<>(clients.values());
        return new AsyncMcpToolCallbackProvider(list).getToolCallbacks();
    }

    private void ensureClient(MCPServerConfig server) {
        if (server == null) {
            return;
        }
        String id = server.id();
        if (id == null || id.isBlank()) {
            return;
        }
        clients.computeIfAbsent(id, ignored -> createSseClient(server));
    }

    private McpAsyncClient createSseClient(MCPServerConfig server) {
        String baseUrl = server.url();
        if (baseUrl == null || baseUrl.isBlank()) {
            throw new IllegalArgumentException("server.url is blank");
        }

        WebClient.Builder builder = webClientBuilder.clone().baseUrl(baseUrl);
        WebFluxSseClientTransport transport = WebFluxSseClientTransport
                .builder(builder)
                .jsonMapper(McpJsonMapper.createDefault())
                .sseEndpoint(DEFAULT_SSE_ENDPOINT)
                .build();

        McpAsyncClient client = McpClient.async(transport)
                .requestTimeout(Duration.ofSeconds(30))
                .loggingConsumer((McpSchema.LoggingMessageNotification log) -> {
                    eventPublisher.publishEvent(new ToolLogEvent(
                            server.id(),
                            String.valueOf(log.level()),
                            log.logger(),
                            String.valueOf(log.data()),
                            Instant.now()
                    ));
                    return Mono.empty();
                })
                .build();

        // 主动初始化（异步，不阻塞启动）
        client.initialize()
                .onErrorResume(e -> {
                    eventPublisher.publishEvent(new ToolLogEvent(
                            server.id(),
                            "ERROR",
                            "mcp-client",
                            "Failed to initialize MCP client: " + e.getMessage(),
                            Instant.now()
                    ));
                    return Mono.empty();
                })
                .subscribe();

        // 默认 INFO 级别
        client.setLoggingLevel(McpSchema.LoggingLevel.INFO).subscribe();

        return client;
    }
}



