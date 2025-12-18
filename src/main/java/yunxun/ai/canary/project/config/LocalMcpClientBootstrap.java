package yunxun.ai.canary.project.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.transport.HttpClientSseClientTransport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.web.server.WebServer;
import org.springframework.boot.web.context.WebServerApplicationContext;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import jakarta.annotation.PreDestroy;

@Component
@ConditionalOnProperty(name = "yunxun.mcp.local-client.enabled", havingValue = "true", matchIfMissing = false)
public class LocalMcpClientBootstrap {

    private static final Logger log = LoggerFactory.getLogger(LocalMcpClientBootstrap.class);

    private final ObjectMapper objectMapper;
    private final String sseEndpoint;

    private volatile McpSyncClient localClient;

    public LocalMcpClientBootstrap(
            ObjectMapper objectMapper,
            @org.springframework.beans.factory.annotation.Value("${yunxun.mcp.local-client.sse-endpoint:/sse}") String sseEndpoint) {
        this.objectMapper = objectMapper;
        this.sseEndpoint = sseEndpoint;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void initializeClientsAfterReady(ApplicationReadyEvent event) {
        if (!(event.getApplicationContext() instanceof WebServerApplicationContext wsCtx)) {
            return;
        }

        WebServer webServer = wsCtx.getWebServer();
        if (webServer == null || webServer.getPort() <= 0) {
            return;
        }

        int port = webServer.getPort();
        String baseUrl = "http://localhost:" + port;

        HttpClientSseClientTransport transport = HttpClientSseClientTransport.builder(baseUrl)
                .sseEndpoint(sseEndpoint)
                .objectMapper(objectMapper)
                .build();

        McpSyncClient client = McpClient.sync(transport)
                .requestTimeout(Duration.ofSeconds(10))
                .initializationTimeout(Duration.ofSeconds(10))
                .build();
        this.localClient = client;

        Duration maxWait = Duration.ofSeconds(20);
        Duration retryBackoff = Duration.ofSeconds(1);
        Instant deadline = Instant.now().plus(maxWait);

        while (Instant.now().isBefore(deadline)) {
            try {
                client.initialize();
                log.info("Local MCP client initialized: {}{}", baseUrl, sseEndpoint);
                return;
            }
            catch (Exception ex) {
                log.warn("Local MCP client initialize failed, retrying: {}", ex.getMessage());
                try {
                    Thread.sleep(retryBackoff.toMillis());
                }
                catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        }
    }

    @PreDestroy
    public void close() {
        McpSyncClient client = this.localClient;
        if (client != null) {
            try {
                client.close();
            }
            catch (Exception ignored) {
            }
        }
    }
}
