package yunxun.ai.canary.backend.service.mcp.tool;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

@Component
public class LlmTool {

    @Tool(name = "llm_answer", description = "Call LLM to answer a question")
    public String answer(@ToolParam(description = "question") String question) {
        return "LLM answer placeholder for: " + question;
    }
}
