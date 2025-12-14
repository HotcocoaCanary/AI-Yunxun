package yunxun.ai.canary.project.config;

import ai.z.openapi.ZhipuAiClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import yunxun.ai.canary.project.service.llm.LlmClient;
import yunxun.ai.canary.project.service.llm.ZhipuAiLlmClient;

/**
 * LLM 客户端配置
 */
@Configuration
public class LlmClientConfig {

    @Bean
    public ZhipuAiClient zhipuAiClient(@Value("${spring.ai.zhipu.api-key}") String apiKey) {
        return ZhipuAiClient.builder()
                .apiKey(apiKey)
                .build();
    }

    @Bean
    public LlmClient llmClient(
            ZhipuAiClient client,
            @Value("${spring.ai.zhipu.model:glm-4.5-flash}") String model) {
        return new ZhipuAiLlmClient(client, model);
    }
}

