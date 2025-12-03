package yunxun.ai.canary.backend.qa.service.llm;

import ai.z.openapi.ZhipuAiClient;
import ai.z.openapi.service.model.ChatCompletionCreateParams;
import ai.z.openapi.service.model.ChatCompletionResponse;
import ai.z.openapi.service.model.ChatMessage;
import ai.z.openapi.service.model.ChatMessageRole;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import yunxun.ai.canary.backend.mcp.service.prompt.PromptRegistry;

import java.util.Collections;

/**
 * LLM ���÷���Ŀǰͨ������ GLM ���ģ�ͣ�zai-sdk�����ô�ģ�͡�
 * ��������лر��� Ollama��ֻ��Ҫ�������滻ʵ�ּ��ɡ�
 */
@SuppressWarnings("LossyEncoding")
@Service
public class LlmService {

    private final ZhipuAiClient zhipuClient;
    private final String model;
    private final PromptRegistry promptRegistry;

    public LlmService(
            @Value("${zhipu.api-key}") String apiKey,
            @Value("${zhipu.model:glm-4.5-flash}") String model,
            PromptRegistry promptRegistry) {
        this.zhipuClient = ZhipuAiClient.builder()
                .apiKey(apiKey)
                .build();
        this.model = model;
        this.promptRegistry = promptRegistry;
    }

    /**
     * ͨ�öԻ���ڣ�
     * - ʹ�� qa-system ��ʾ����Ϊϵͳ˵��
     * - ��ϵͳ��ʾ�����û�����ϲ�Ϊһ�ζԻ����͸���ģ��
     */
    public String chat(String userInput) {
        return callModel(userInput);
    }

    private String callModel(String userInput) {
        String systemPrompt = promptRegistry.getContent("qa-system");
        String mergedContent = systemPrompt + "\n\n�û����룺\n" + userInput;

        ChatMessage message = ChatMessage.builder()
                .role(ChatMessageRole.USER.value())
                .content(mergedContent)
                .build();

        ChatCompletionCreateParams request = ChatCompletionCreateParams.builder()
                .model(model)
                .messages(Collections.singletonList(message))
                .build();

        ChatCompletionResponse response = zhipuClient.chat().createChatCompletion(request);
        if (response != null && response.isSuccess()
                && response.getData() != null
                && response.getData().getChoices() != null
                && !response.getData().getChoices().isEmpty()
                && response.getData().getChoices().get(0).getMessage() != null) {
            Object content = response.getData().getChoices().get(0).getMessage().getContent();
            return content != null ? content.toString() : "";
        }

        String msg = response != null ? response.getMsg() : "δ֪����";
        throw new IllegalStateException("�������״�ģ��ʧ��: " + msg);
    }
}
