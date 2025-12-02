package yunxun.ai.canary.backend.qa.service.llm;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.ChatClient.ChatClientRequestSpec;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.stereotype.Service;
import yunxun.ai.canary.backend.mcp.service.prompt.PromptRegistry;

@Service
public class LlmService {

    private final ChatClient chatClient;
    private final PromptRegistry promptRegistry;

    public LlmService(ChatClient.Builder chatClientBuilder, PromptRegistry promptRegistry) {
        this.chatClient = chatClientBuilder.build();
        this.promptRegistry = promptRegistry;
    }

    /**
     * 通用对话入口：
     * - 使用 qa-system 提示词作为系统角色
     * - 将用户输入作为 user 消息发送给大模型
     */
    public String chat(String userInput) {
        return callModel(userInput, 0.8);
    }

    private String callModel(String userInput, Double temperature) {
        String systemPrompt = promptRegistry.getContent("qa-system");

        ChatClientRequestSpec request = chatClient.prompt()
                .system(systemPrompt)
                .user(userInput);

        ChatOptions options = ChatOptions.builder()
                .temperature(temperature)
                .build();

        return request.options(options).call().content();
    }
}
