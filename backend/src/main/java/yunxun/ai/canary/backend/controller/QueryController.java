package yunxun.ai.canary.backend.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import yunxun.ai.canary.backend.model.entity.QueryLog;
import yunxun.ai.canary.backend.model.entity.User;
import yunxun.ai.canary.backend.repository.QueryLogRepository;
import yunxun.ai.canary.backend.service.mcp.GraphQueryTool;
import yunxun.ai.canary.backend.service.mcp.RAGRetrieverTool;
import yunxun.ai.canary.backend.service.rag.RAGService;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 查询控制器
 */
@RestController
@RequestMapping("/api/query")
@RequiredArgsConstructor
@Slf4j
public class QueryController {
    
    private final GraphQueryTool graphQueryTool;
    private final RAGRetrieverTool ragRetrieverTool;
    private final RAGService ragService;
    private final QueryLogRepository queryLogRepository;
    
    /**
     * 自然语言查询
     */
    @PostMapping("/natural")
    public ResponseEntity<Map<String, Object>> naturalLanguageQuery(
            @RequestBody Map<String, String> request,
            Authentication authentication) {
        
        long startTime = System.currentTimeMillis();
        User user = (User) authentication.getPrincipal();
        
        try {
            String query = request.get("query");
            String queryType = request.getOrDefault("type", "general");
            
            log.info("用户查询: user={}, query={}, type={}", user.getUsername(), query, queryType);
            
            // 执行图谱查询
            Map<String, Object> graphParams = new HashMap<>();
            graphParams.put("query", query);
            graphParams.put("query_type", queryType);
            Object graphResult = graphQueryTool.execute(graphParams);
            
            // 执行RAG检索
            Map<String, Object> ragParams = new HashMap<>();
            ragParams.put("query", query);
            ragParams.put("top_k", 5);
            Object ragResult = ragRetrieverTool.execute(ragParams);
            
            // 生成增强回答
            String enhancedAnswer = generateEnhancedAnswer(query, ragResult);
            
            long executionTime = System.currentTimeMillis() - startTime;
            
            // 记录查询日志
            QueryLog queryLog = new QueryLog();
            queryLog.setUserId(user.getId());
            queryLog.setQueryText(query);
            queryLog.setQueryType("NATURAL_LANGUAGE");
            queryLog.setResponse(enhancedAnswer);
            queryLog.setExecutionTime(executionTime);
            queryLog.setSuccess(true);
            queryLogRepository.save(queryLog);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("query", query);
            response.put("graph_result", graphResult);
            response.put("rag_result", ragResult);
            response.put("enhanced_answer", enhancedAnswer);
            response.put("execution_time", executionTime);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("查询失败", e);
            
            long executionTime = System.currentTimeMillis() - startTime;
            
            // 记录失败日志
            QueryLog queryLog = new QueryLog();
            queryLog.setUserId(user.getId());
            queryLog.setQueryText(request.get("query"));
            queryLog.setQueryType("NATURAL_LANGUAGE");
            queryLog.setResponse(null);
            queryLog.setExecutionTime(executionTime);
            queryLog.setSuccess(false);
            queryLog.setErrorMessage(e.getMessage());
            queryLogRepository.save(queryLog);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 获取查询历史
     */
    @GetMapping("/history")
    public ResponseEntity<Map<String, Object>> getQueryHistory(Authentication authentication) {
        try {
            User user = (User) authentication.getPrincipal();
            List<QueryLog> logs = queryLogRepository.findByUserIdOrderByCreatedAtDesc(user.getId());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("logs", logs);
            response.put("count", logs.size());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("获取查询历史失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    private String generateEnhancedAnswer(String query, Object ragResult) {
        try {
            // 这里应该调用LLM生成增强回答
            // 为了简化示例，返回基本回答
            return "基于知识图谱和RAG检索的结果，为您提供相关信息的综合分析。";
        } catch (Exception e) {
            log.error("生成增强回答失败", e);
            return "抱歉，无法生成回答。";
        }
    }
}
