package yunxun.ai.canary.backend.qa.service.llm;

import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class LlmPromptTemplates {

    /**
     * 系统提示词：智能数据分析助手，强调简洁和安全。
     */
    private static final String SYSTEM_PROMPT = "你是智能数据分析助手，请提供简洁、安全的回答。";

    /**
     * 构建通用对话 Prompt，将系统说明、历史消息和当前用户输入拼接在一起。
     */
    public Prompt buildChatPrompt(List<Message> history, String userContent) {
        String historyText = history == null ? "" : history.stream()
                .map(Object::toString)
                .collect(Collectors.joining("\n"));
        PromptTemplate template = new PromptTemplate(
                "{system}\n对话历史:\n{history}\n用户:\n{user}"
        );
        return template.create(Map.of(
                "system", SYSTEM_PROMPT,
                "history", historyText,
                "user", userContent
        ));
    }
}

