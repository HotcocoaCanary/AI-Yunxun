package yunxun.ai.canary.backend.service.agent.llm;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.ChatClient.ChatClientRequestSpec;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Service;

@Service
public class LlmService {

    private static final String SYSTEM_PROMPT = "You are Yunxun assistant. Provide concise and safe answers.";

    private final ChatClient chatClient;
    private final LlmPromptTemplates templates;

    public LlmService(ChatClient chatClient, LlmPromptTemplates templates) {
        this.chatClient = chatClient;
        this.templates = templates;
    }

    public String chat(String userInput) {
        Prompt prompt = templates.buildChatPrompt(SYSTEM_PROMPT, java.util.List.of(), userInput);
        return callModel(prompt, null);
    }

    private String callModel(Prompt prompt, Double temperature) {
        ChatClientRequestSpec request = chatClient.prompt(prompt);
        ChatOptions options = ChatOptions.builder()
                .temperature(temperature)
                .build();
        return request.options(options).call().content();
    }
}
