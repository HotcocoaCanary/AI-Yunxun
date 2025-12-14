package yunxun.ai.canary.backend.mcp.client.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring AI ChatClient 的基础配置
 * <p>
 * ChatClient.Builder 由 Spring AI（Ollama starter）自动配置，
 * 这里我们简单地暴露一个具体的 ChatClient Bean，
 * 以便应用服务可以注入并使用它。
 */
@Configuration
public class McpClientConfig {

    /**
     * 创建 ChatClient Bean
     * 用于与 AI 模型进行对话交互
     */
    @Bean
    public ChatClient chatClient(ChatClient.Builder builder) {
        return builder.build();
    }
}

