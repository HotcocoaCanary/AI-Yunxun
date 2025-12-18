package yunxun.ai.canary.project.config;

import ai.z.openapi.ZhipuAiClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import yunxun.ai.canary.project.service.llm.LlmClient;
import yunxun.ai.canary.project.service.llm.NoopLlmClient;
import yunxun.ai.canary.project.service.llm.ZhipuAiLlmClient;

/**
 * LLM 客户端配置
 */
@Configuration
public class LlmClientConfig {

    @Bean
    @ConditionalOnExpression("T(org.springframework.util.StringUtils).hasText('${spring.ai.zhipu.api-key:}')")
    public ZhipuAiClient zhipuAiClient(@Value("${spring.ai.zhipu.api-key}") String apiKey) {
        return ZhipuAiClient.builder()
                .apiKey(apiKey)
                .build();
    }

    @Bean
    @ConditionalOnExpression("T(org.springframework.util.StringUtils).hasText('${spring.ai.zhipu.api-key:}')")
    public LlmClient zhipuLlmClient(
            ZhipuAiClient client,
            @Value("${spring.ai.zhipu.model:glm-4.5-flash}") String model) {
        return new ZhipuAiLlmClient(client, model);
    }

    @Bean
    @ConditionalOnMissingBean(LlmClient.class)
    public LlmClient llmClientFallback() {
        return new NoopLlmClient(
                "未配置 LLM：请设置环境变量 SPRING_AI_ZHIPU_API_KEY（或在 application-local.yml 中配置 spring.ai.zhipu.api-key）"
        );
    }
}
