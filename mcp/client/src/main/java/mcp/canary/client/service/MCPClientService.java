package mcp.canary.client.service;

import io.modelcontextprotocol.client.McpAsyncClient;
import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.transport.ServerParameters;
import io.modelcontextprotocol.client.transport.StdioClientTransport;
import io.modelcontextprotocol.json.McpJsonMapper;
import io.modelcontextprotocol.spec.McpSchema.LoggingMessageNotification;
import mcp.canary.client.model.McpServerDefinition;
import mcp.canary.client.model.McpServersConfig;
import org.springframework.ai.mcp.AsyncMcpToolCallbackProvider;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static mcp.canary.client.config.McpLoggingCustomizer.getVoidMono;

@Service
public class MCPClientService {

    private static final Duration INIT_TIMEOUT = Duration.ofSeconds(10);
    private static final McpJsonMapper JSON_MAPPER = McpJsonMapper.createDefault();
    private static final Set<String> WINDOWS_WRAPPED_COMMANDS = Set.of(
            "npx", "npm", "node", "pnpm", "yarn", "python", "pip", "mvn", "gradle"
    );

    private final MCPService mcpService;
    private final ToolCallbackProvider toolCallbackProvider;
    private final ApplicationEventPublisher eventPublisher;
    private final Map<String, McpAsyncClient> stdioClients = new ConcurrentHashMap<>();

    public MCPClientService(MCPService mcpService,
                            ObjectProvider<ToolCallbackProvider> toolCallbackProvider,
                            ApplicationEventPublisher eventPublisher) {
        this.mcpService = mcpService;
        this.toolCallbackProvider = toolCallbackProvider.getIfAvailable();
        this.eventPublisher = eventPublisher;
    }

    @PostConstruct
    public void initialize() {
        reloadFromConfig();
    }

    public Map<String, McpServerDefinition> listServers() {
        return mcpService.listServers();
    }

    public McpServersConfig replaceServers(McpServersConfig config) {
        McpServersConfig saved = mcpService.replaceConfig(config);
        reloadFromConfig();
        return saved;
    }

    public McpServerDefinition addServer(String name, McpServerDefinition definition) {
        McpServerDefinition saved = mcpService.upsertServer(name, definition);
        connectServer(name, saved);
        return saved;
    }

    public boolean deleteServer(String name) {
        return mcpService.deleteServer(name)
                .map(removed -> {
                    disconnectServer(name);
                    return true;
                })
                .orElse(false);
    }

    public ToolCallback[] getToolCallbacks() {
        List<ToolCallback> callbacks = new ArrayList<>();
        if (toolCallbackProvider != null) {
            callbacks.addAll(Arrays.asList(toolCallbackProvider.getToolCallbacks()));
        }
        if (!stdioClients.isEmpty()) {
            AsyncMcpToolCallbackProvider dynamicProvider = AsyncMcpToolCallbackProvider.builder()
                    .mcpClients(new ArrayList<>(stdioClients.values()))
                    .build();
            callbacks.addAll(Arrays.asList(dynamicProvider.getToolCallbacks()));
        }
        return callbacks.toArray(new ToolCallback[0]);
    }

    private void reloadFromConfig() {
        stdioClients.keySet().forEach(this::disconnectServer);
        McpServersConfig config = mcpService.readConfig();
        for (Map.Entry<String, McpServerDefinition> entry : config.mcpServers().entrySet()) {
            connectServer(entry.getKey(), entry.getValue());
        }
    }

    private void connectServer(String name, McpServerDefinition definition) {
        if (name == null || name.isBlank() || definition == null) {
            return;
        }
        if (stdioClients.containsKey(name)) {
            return;
        }
        McpAsyncClient client = buildAsyncClient(name, definition);
        stdioClients.put(name, client);
    }

    private void disconnectServer(String name) {
        McpAsyncClient client = stdioClients.remove(name);
        if (client != null) {
            try {
                client.close();
            } catch (Exception ignored) {
            }
        }
    }

    private McpAsyncClient buildAsyncClient(String name, McpServerDefinition definition) {
        ServerParameters params = buildServerParameters(definition);
        StdioClientTransport transport = new StdioClientTransport(params, JSON_MAPPER);
        McpAsyncClient client = McpClient.async(transport)
                .requestTimeout(Duration.ofSeconds(20))
                .loggingConsumer(notification -> publishLog(name, notification))
                .build();
        client.initialize().block(INIT_TIMEOUT);
        return client;
    }

    private Mono<Void> publishLog(String serverName, LoggingMessageNotification notification) {
        return getVoidMono(serverName, notification, eventPublisher);
    }

    private ServerParameters buildServerParameters(McpServerDefinition definition) {
        String command = definition.command();
        List<String> args = new ArrayList<>(definition.args());

        if (isWindows() && requiresCmdWrapper(command)) {
            List<String> wrapped = new ArrayList<>();
            wrapped.add("/c");
            wrapped.add(command);
            wrapped.addAll(args);
            command = "cmd.exe";
            args = wrapped;
        }

        ServerParameters.Builder builder = ServerParameters.builder(command);
        if (!args.isEmpty()) {
            builder.args(args);
        }
        if (!definition.env().isEmpty()) {
            builder.env(definition.env());
        }
        return builder.build();
    }

    private boolean requiresCmdWrapper(String command) {
        if (command == null) {
            return false;
        }
        String lower = command.toLowerCase(Locale.ROOT);
        if (lower.endsWith(".cmd") || lower.endsWith(".bat")) {
            return true;
        }
        return WINDOWS_WRAPPED_COMMANDS.contains(lower);
    }

    private boolean isWindows() {
        return System.getProperty("os.name").toLowerCase(Locale.ROOT).contains("win");
    }

    @PreDestroy
    public void shutdown() {
        stdioClients.keySet().forEach(this::disconnectServer);
    }
}
