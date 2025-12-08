package yunxun.ai.canary.backend.mcp.client.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Basic configuration for the Spring AI {@link ChatClient}.
 * <p>
 * The {@link ChatClient.Builder} is auto-configured by Spring AI (Ollama starter),
 * and here we simply expose a concrete {@link ChatClient} bean so that
 * application services can inject and use it.
 */
@Configuration
public class McpClientConfig {

    @Bean
    public ChatClient chatClient(ChatClient.Builder builder) {
        return builder.build();
    }
}

