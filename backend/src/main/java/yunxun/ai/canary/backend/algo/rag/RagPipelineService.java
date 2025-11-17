package yunxun.ai.canary.backend.algo.rag;

import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import yunxun.ai.canary.backend.algo.llm.LlmService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 负责文档入库、语义检索与结合 LLM 的回答生成。
 */
@Service
public class RagPipelineService {

    private final VectorStore vectorStore;
    private final LlmService llmService;

    public RagPipelineService(VectorStore vectorStore, LlmService llmService) {
        this.vectorStore = vectorStore;
        this.llmService = llmService;
    }

    public void ingestDocument(String documentId,
                               String title,
                               String content,
                               Map<String, Object> metadata) {
        if (!StringUtils.hasText(content)) {
            throw new IllegalArgumentException("文档内容不能为空");
        }
        Map<String, Object> meta = new HashMap<>();
        if (!CollectionUtils.isEmpty(metadata)) {
            meta.putAll(metadata);
        }
        meta.put("documentId", documentId);
        if (StringUtils.hasText(title)) {
            meta.put("title", title);
        }
        Document document = new Document(content, meta);
        vectorStore.add(List.of(document));
    }

    public List<Document> similaritySearch(String query, int topK, double similarityThreshold) {
        SearchRequest request = SearchRequest.builder()
                .query(query)
                .topK(topK)
                .similarityThreshold(similarityThreshold)
                .build();
        return vectorStore.similaritySearch(request);
    }

    public RagResult answerWithRag(String question, int topK, double similarityThreshold) {
        List<Document> documents = similaritySearch(question, topK, similarityThreshold);
        String context = documents.stream()
                .map(doc -> {
                    Object title = doc.getMetadata().getOrDefault("title", doc.getMetadata().get("documentId"));
                    return "- 来源: " + (title != null ? title : "未知") + "\n" + doc.getText();
                })
                .collect(Collectors.joining("\n\n"));
        String answer = llmService.answerWithContext(question, context);
        return new RagResult(answer, documents);
    }

    public record RagResult(String answer, List<Document> documents) {
        public List<Map<String, Object>> toContextList() {
            List<Map<String, Object>> list = new ArrayList<>();
            for (Document document : documents) {
                Map<String, Object> info = new HashMap<>(document.getMetadata());
                info.put("content", document.getText());
                list.add(info);
            }
            return list;
        }
    }
}
