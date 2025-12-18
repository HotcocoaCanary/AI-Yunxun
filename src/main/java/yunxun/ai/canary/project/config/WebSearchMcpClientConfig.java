package yunxun.ai.canary.project.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.client.transport.ServerParameters;
import io.modelcontextprotocol.client.transport.StdioClientTransport;
import io.modelcontextprotocol.client.transport.HttpClientSseClientTransport;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class WebSearchMcpClientConfig {

    @Bean(name = "webSearchMcpClient")
    @ConditionalOnProperty(name = "web.search.mcp.mode", havingValue = "sse", matchIfMissing = true)
    @ConditionalOnProperty(name = "web.search.mcp.url")
    public McpSyncClient webSearchMcpClient(
            ObjectMapper objectMapper,
            @Value("${web.search.mcp.url}") String baseUrl,
            @Value("${web.search.mcp.sse-endpoint:/sse}") String sseEndpoint) {

        HttpClientSseClientTransport transport = HttpClientSseClientTransport.builder(baseUrl)
                .sseEndpoint(sseEndpoint)
                .objectMapper(objectMapper)
                .build();

        return McpClient.sync(transport)
                .requestTimeout(Duration.ofSeconds(10))
                .initializationTimeout(Duration.ofSeconds(10))
                .build();
    }

    @Bean(name = "webSearchMcpClient")
    @ConditionalOnProperty(name = "web.search.mcp.mode", havingValue = "stdio")
    @ConditionalOnProperty(name = "web.search.mcp.searxng-url")
    public McpSyncClient webSearchMcpClientStdio(
            ObjectMapper objectMapper,
            @Value("${web.search.mcp.command:mcp-searxng}") String command,
            @Value("${web.search.mcp.searxng-url}") String searxngUrl) {

        ServerParameters params = ServerParameters.builder(command)
                .addEnvVar("SEARXNG_URL", searxngUrl)
                .build();

        StdioClientTransport transport = new StdioClientTransport(params, objectMapper);

        return McpClient.sync(transport)
                .requestTimeout(Duration.ofSeconds(30))
                .initializationTimeout(Duration.ofSeconds(30))
                .build();
    }
}
