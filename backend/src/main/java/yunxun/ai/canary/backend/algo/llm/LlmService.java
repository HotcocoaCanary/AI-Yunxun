package yunxun.ai.canary.backend.algo.llm;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.ChatClient.ChatClientRequestSpec;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Service;
import yunxun.ai.canary.backend.algo.llm.prompt.LlmPromptTemplates;

/**
 * 面向业务的 LLM 能力封装，完全基于 Spring AI。
 */
@Service
public class LlmService {

    private final ChatClient chatClient;
    private final LlmPromptTemplates templates;

    public LlmService(ChatClient chatClient, LlmPromptTemplates templates) {
        this.chatClient = chatClient;
        this.templates = templates;
    }

    public String answerQuestion(String question, String context, String answerStyle, Double temperature) {
        Prompt prompt = templates.buildAnswerPrompt(question, context, answerStyle);
        return callModel(prompt, temperature);
    }

    public String summarize(String content, String instruction, Double temperature) {
        Prompt prompt = templates.buildSummarizePrompt(content, instruction);
        return callModel(prompt, temperature);
    }

    public String completion(String systemPrompt, String userPrompt, Double temperature) {
        Prompt prompt = templates.buildCustomPrompt(systemPrompt, userPrompt);
        return callModel(prompt, temperature);
    }

    public String answerWithContext(String question, String context) {
        Prompt prompt = templates.buildAnswerPrompt(question, context, "学术写作风格，引用信息来源并提供条列式结论");
        return callModel(prompt, 0.2d);
    }

    private String callModel(Prompt prompt, Double temperature) {
        ChatClientRequestSpec requestSpec = chatClient.prompt(prompt);
        if (temperature != null) {
            ChatOptions options = ChatOptions.builder()
                    .temperature(temperature)
                    .build();
            requestSpec = requestSpec.options(options);
        }
        return requestSpec.call().content();
    }
}
