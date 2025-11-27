package yunxun.ai.canary.backend.service.mcp.tool;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

@Component
public class RagTool {

    @Tool(name = "rag_answer", description = "RAG answer with vector search + LLM")
    public String answer(
            @ToolParam(description = "user question") String question,
            @ToolParam(description = "context") String context
    ) {
        return "RAG answer placeholder for: " + question;
    }
}
