package yunxun.ai.canary.backend.qa.service.llm;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.ChatClient.ChatClientRequestSpec;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LlmService {

    private final ChatClient chatClient;
    private final LlmPromptTemplates templates;

    public LlmService(ChatClient.Builder chatClientBuilder, LlmPromptTemplates templates) {
        this.chatClient = chatClientBuilder.build();
        this.templates = templates;
    }

    public String chat(String userInput) {
        Prompt prompt = templates.buildChatPrompt(List.of(), userInput);
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
