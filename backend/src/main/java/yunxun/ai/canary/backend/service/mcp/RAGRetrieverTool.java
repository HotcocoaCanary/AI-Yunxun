package yunxun.ai.canary.backend.service.mcp;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import yunxun.ai.canary.backend.service.rag.RAGService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * RAG检索工具 - 使用向量数据库检索相关论文摘要
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RAGRetrieverTool implements MCPTool {
    
    private final RAGService ragService;
    
    @Override
    public String getToolName() {
        return "rag_retriever_tool";
    }
    
    @Override
    public String getToolDescription() {
        return "使用RAG框架检索相关论文摘要，支持语义搜索和相似度匹配";
    }
    
    @Override
    public Object execute(Map<String, Object> parameters) {
        try {
            String query = (String) parameters.get("query");
            Integer topK = (Integer) parameters.getOrDefault("top_k", 5);
            Double threshold = (Double) parameters.getOrDefault("threshold", 0.7);
            
            log.info("执行RAG检索: query={}, topK={}, threshold={}", query, topK, threshold);
            
            // 使用RAG服务检索相关文档
            List<Map<String, Object>> results = ragService.retrieveRelevantDocuments(query, topK, threshold);
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("query", query);
            result.put("results", results);
            result.put("count", results.size());
            
            return result;
            
        } catch (Exception e) {
            log.error("RAG检索工具执行失败", e);
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("error", e.getMessage());
            return result;
        }
    }
    
    @Override
    public Map<String, Object> getParameterSchema() {
        Map<String, Object> schema = new HashMap<>();
        schema.put("type", "object");
        
        Map<String, Object> properties = new HashMap<>();
        
        Map<String, Object> querySchema = new HashMap<>();
        querySchema.put("type", "string");
        querySchema.put("description", "检索查询文本");
        properties.put("query", querySchema);
        
        Map<String, Object> topKSchema = new HashMap<>();
        topKSchema.put("type", "integer");
        topKSchema.put("minimum", 1);
        topKSchema.put("maximum", 50);
        topKSchema.put("description", "返回结果数量");
        properties.put("top_k", topKSchema);
        
        Map<String, Object> thresholdSchema = new HashMap<>();
        thresholdSchema.put("type", "number");
        thresholdSchema.put("minimum", 0.0);
        thresholdSchema.put("maximum", 1.0);
        thresholdSchema.put("description", "相似度阈值");
        properties.put("threshold", thresholdSchema);
        
        schema.put("properties", properties);
        schema.put("required", List.of("query"));
        
        return schema;
    }
}
