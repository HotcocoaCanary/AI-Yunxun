package yunxun.ai.canary.backend.service.llm;

import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class LlmPromptTemplates {

    public Prompt buildChatPrompt(String systemInstruction, List<Message> history, String userContent) {
        String historyText = history == null ? "" : history.stream()
                .map(Object::toString)
                .collect(Collectors.joining("\n"));
        PromptTemplate template = new PromptTemplate("{system}\nHistory:\n{history}\nUser:\n{user}");
        return template.create(Map.of(
                "system", systemInstruction,
                "history", historyText,
                "user", userContent
        ));
    }
}
