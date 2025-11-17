package yunxun.ai.canary.backend.service.mcp.tools;

import jakarta.annotation.Resource;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import yunxun.ai.canary.backend.algo.llm.LlmService;

import java.util.HashMap;
import java.util.Map;

/**
 * 算法模块暴露的大模型工具。
 */
@Component
public class LlmTool {

    @Resource
    @Lazy
    private LlmService llmService;

    @Tool(name = "llm_answer_question", description = "调用大模型回答问题，可携带额外上下文")
    public Map<String, Object> answerQuestion(
            @ToolParam(description = "用户问题") String question,
            @ToolParam(description = "可选上下文片段") String context,
            @ToolParam(description = "回答风格说明，如“要点列举”") String answerStyle,
            @ToolParam(description = "温度，0-1 之间，可选") Double temperature
    ) {
        String answer = llmService.answerQuestion(question, context, answerStyle, temperature);
        return Map.of("answer", answer);
    }

    @Tool(name = "llm_summarize_document", description = "总结一段文本，支持额外输出要求")
    public Map<String, Object> summarize(
            @ToolParam(description = "需要总结的正文内容") String content,
            @ToolParam(description = "总结要求，比如输出格式、语言") String instruction,
            @ToolParam(description = "温度，0-1 之间，可选") Double temperature
    ) {
        String summary = llmService.summarize(content, instruction, temperature);
        return Map.of("summary", summary);
    }

    @Tool(name = "llm_completion", description = "自定义 system/user 提示模板进行补全")
    public Map<String, Object> completion(
            @ToolParam(description = "系统提示词") String systemPrompt,
            @ToolParam(description = "用户提示词") String userPrompt,
            @ToolParam(description = "温度，0-1 之间，可选") Double temperature
    ) {
        String result = llmService.completion(systemPrompt, userPrompt, temperature);
        Map<String, Object> response = new HashMap<>();
        response.put("result", result);
        response.put("system_prompt_used", systemPrompt);
        return response;
    }
}
