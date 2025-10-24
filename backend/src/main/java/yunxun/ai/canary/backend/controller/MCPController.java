package yunxun.ai.canary.backend.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import yunxun.ai.canary.backend.service.mcp.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * MCP工具控制器
 */
@RestController
@RequestMapping("/api/mcp")
@RequiredArgsConstructor
@Slf4j
public class MCPController {
    
    private final CrawlerTool crawlerTool;
    private final GraphQueryTool graphQueryTool;
    private final RAGRetrieverTool ragRetrieverTool;
    private final DBServiceTool dbServiceTool;
    
    /**
     * 获取所有可用工具
     */
    @GetMapping("/tools")
    public ResponseEntity<Map<String, Object>> getAvailableTools() {
        Map<String, Object> tools = new HashMap<>();
        
        tools.put("crawler_tool", Map.of(
                "name", crawlerTool.getToolName(),
                "description", crawlerTool.getToolDescription(),
                "schema", crawlerTool.getParameterSchema()
        ));
        
        tools.put("graph_query_tool", Map.of(
                "name", graphQueryTool.getToolName(),
                "description", graphQueryTool.getToolDescription(),
                "schema", graphQueryTool.getParameterSchema()
        ));
        
        tools.put("rag_retriever_tool", Map.of(
                "name", ragRetrieverTool.getToolName(),
                "description", ragRetrieverTool.getToolDescription(),
                "schema", ragRetrieverTool.getParameterSchema()
        ));
        
        tools.put("db_service_tool", Map.of(
                "name", dbServiceTool.getToolName(),
                "description", dbServiceTool.getToolDescription(),
                "schema", dbServiceTool.getParameterSchema()
        ));
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("tools", tools);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * 执行爬虫工具
     */
    @PostMapping("/crawler")
    public ResponseEntity<Map<String, Object>> executeCrawler(@RequestBody Map<String, Object> request) {
        try {
            Object result = crawlerTool.execute(request);
            return ResponseEntity.ok((Map<String, Object>) result);
        } catch (Exception e) {
            log.error("执行爬虫工具失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 执行图谱查询工具
     */
    @PostMapping("/graph-query")
    public ResponseEntity<Map<String, Object>> executeGraphQuery(@RequestBody Map<String, Object> request) {
        try {
            Object result = graphQueryTool.execute(request);
            return ResponseEntity.ok((Map<String, Object>) result);
        } catch (Exception e) {
            log.error("执行图谱查询工具失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 执行RAG检索工具
     */
    @PostMapping("/rag-retriever")
    public ResponseEntity<Map<String, Object>> executeRAGRetriever(@RequestBody Map<String, Object> request) {
        try {
            Object result = ragRetrieverTool.execute(request);
            return ResponseEntity.ok((Map<String, Object>) result);
        } catch (Exception e) {
            log.error("执行RAG检索工具失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 执行数据库服务工具
     */
    @PostMapping("/db-service")
    public ResponseEntity<Map<String, Object>> executeDBService(@RequestBody Map<String, Object> request) {
        try {
            Object result = dbServiceTool.execute(request);
            return ResponseEntity.ok((Map<String, Object>) result);
        } catch (Exception e) {
            log.error("执行数据库服务工具失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}
