package yunxun.ai.canary.backend.algo.llm.prompt;

import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 集中管理 LLM 相关提示词模板，便于复用与维护。
 */
@Component
public class LlmPromptTemplates {

    private final SystemPromptTemplate defaultSystemPrompt = new SystemPromptTemplate("""
            你是 AI-Yunxun 项目的智能助手，擅长分析学术知识图谱、科研趋势与高校数据。
            回答时：
            - 使用简体中文；
            - 如有上下文请优先引用，并用 Markdown 结构化输出；
            - 无上下文时可基于常识回答，但需明确提示。
            """);

    private final PromptTemplate qaTemplate = new PromptTemplate("""
            {context_block}
            {style_block}
            问题：{question}
            """);

    private final PromptTemplate summarizeTemplate = new PromptTemplate("""
            请阅读以下内容并生成概要：

            {content}

            输出要求：{instruction}
            """);

    public Prompt buildAnswerPrompt(String question, String context, String answerStyle) {
        Map<String, Object> params = new HashMap<>();
        params.put("question", question);
        params.put("context_block", buildContextBlock(context));
        params.put("style_block", buildStyleBlock(answerStyle));
        return composePrompt(params, qaTemplate);
    }

    public Prompt buildSummarizePrompt(String content, String instruction) {
        Map<String, Object> params = new HashMap<>();
        params.put("content", content);
        params.put("instruction", StringUtils.hasText(instruction) ? instruction : "突出关键实体与指标");
        return composePrompt(params, summarizeTemplate);
    }

    public Prompt buildCustomPrompt(String systemPrompt, String userPrompt) {
        SystemPromptTemplate system = StringUtils.hasText(systemPrompt)
                ? new SystemPromptTemplate(systemPrompt)
                : defaultSystemPrompt;
        Message systemMessage = system.createMessage(Map.of());
        PromptTemplate userTemplate = new PromptTemplate(userPrompt);
        Message userMessage = userTemplate.createMessage(Map.of());
        return new Prompt(List.of(systemMessage, userMessage));
    }

    private Prompt composePrompt(Map<String, Object> params, PromptTemplate userTemplate) {
        Message systemMessage = defaultSystemPrompt.createMessage(Map.of());
        Message userMessage = userTemplate.createMessage(params);
        return new Prompt(List.of(systemMessage, userMessage));
    }

    private String buildContextBlock(String context) {
        if (!StringUtils.hasText(context)) {
            return "（当前未提供额外上下文，将基于模型已有知识回答。）";
        }
        return """
                已知上下文：
                %s
                """.formatted(context);
    }

    private String buildStyleBlock(String style) {
        if (!StringUtils.hasText(style)) {
            return "回答风格：保持客观、条理清晰。";
        }
        return "回答风格：" + style;
    }
}
