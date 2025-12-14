package yunxun.ai.canary.backend.mcp.client.config;

import ai.z.openapi.ZhipuAiClient;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import yunxun.ai.canary.backend.mcp.client.service.ZhipuAiChatAdapter;

/**
 * MCP 客户端配置
 * <p>
 * 配置智谱AI客户端和适配器，用于替代本地 Ollama 模型
 */
@Configuration
public class McpClientConfig {

    /**
     * 创建智谱AI客户端 Bean
     *
     * @param apiKey API密钥，从配置文件中读取
     * @return ZhipuAiClient 实例
     */
    @Bean
    public ZhipuAiClient zhipuAiClient(@Value("${spring.ai.zhipu.api-key}") String apiKey) {
        return ZhipuAiClient.builder()
                .apiKey(apiKey)
                .build();
    }

    /**
     * 创建智谱AI聊天适配器 Bean
     *
     * @param client 智谱 AI客户端
     * @param model  模型名称，从配置文件中读取
     * @return ZhipuAiChatAdapter 实例
     */
    @Bean
    public ZhipuAiChatAdapter zhipuAiChatAdapter(
            ZhipuAiClient client,
            @Value("${spring.ai.zhipu.model:glm-4.5-flash}") String model) {
        return new ZhipuAiChatAdapter(client, model);
    }

    /**
     * 创建 ChatClient Bean（保留用于兼容性，如果MCP还需要）
     * 注意：现在主要使用 ZhipuAiChatAdapter，这个Bean可能不再需要
     */
    @Bean
    public ChatClient chatClient(ChatClient.Builder builder) {
        return builder.build();
    }
}

