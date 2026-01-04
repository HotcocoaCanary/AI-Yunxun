package mcp.canary.client.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 智谱 AI（ZhiPuAI）相关配置。
 * <p>
 * 说明：具体的模型参数通过 {@code spring.ai.zhipuai.*} 配置项完成（见 application.yml）。
 */
@Configuration
public class ZhipuAiConfig {

    @Bean
    public ChatClient chatClient(ChatModel chatModel) {
        return ChatClient.create(chatModel);
    }
}





