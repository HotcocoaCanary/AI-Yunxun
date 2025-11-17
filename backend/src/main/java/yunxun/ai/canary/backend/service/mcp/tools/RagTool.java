package yunxun.ai.canary.backend.service.mcp.tools;

import jakarta.annotation.Resource;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;
import yunxun.ai.canary.backend.algo.rag.RagPipelineService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * RAG 相关的 MCP 工具。
 */
@Component
public class RagTool {

    @Resource
    private RagPipelineService ragPipelineService;

    @Tool(name = "rag_ingest_document", description = "将文本写入向量库，供后续 RAG 检索使用")
    public String ingestDocument(
            @ToolParam(description = "文档唯一 ID") String documentId,
            @ToolParam(description = "文档标题") String title,
            @ToolParam(description = "正文内容") String content,
            @ToolParam(description = "附加元数据，例如作者、年份") Map<String, Object> metadata
    ) {
        ragPipelineService.ingestDocument(documentId, title, content, metadata);
        return "文档 " + documentId + " 已完成入库向量化";
    }

    @Tool(name = "rag_semantic_search", description = "基于语义相似度检索相关文档片段")
    public List<Map<String, Object>> semanticSearch(
            @ToolParam(description = "检索问题或查询语句") String query,
            @ToolParam(description = "返回结果数量") int topK,
            @ToolParam(description = "相似度阈值，0-1，数值越大越严格") double similarityThreshold
    ) {
        return ragPipelineService.similaritySearch(query, topK, similarityThreshold).stream()
                .map(doc -> {
                    Map<String, Object> payload = new HashMap<>(doc.getMetadata());
                    payload.put("content", doc.getText());
                    return payload;
                })
                .collect(Collectors.toList());
    }

    @Tool(name = "rag_answer_question", description = "结合检索结果与 LLM 给出最终回答")
    public Map<String, Object> ragAnswer(
            @ToolParam(description = "用户问题") String question,
            @ToolParam(description = "返回结果数量") int topK,
            @ToolParam(description = "相似度阈值，0-1，数值越大越严格") double similarityThreshold
    ) {
        RagPipelineService.RagResult result = ragPipelineService.answerWithRag(question, topK, similarityThreshold);
        Map<String, Object> response = new HashMap<>();
        response.put("answer", result.answer());
        response.put("contexts", result.toContextList());
        return response;
    }
}
