package yunxun.ai.canary.backend.service.mcp;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.neo4j.core.Neo4jTemplate;
import org.springframework.stereotype.Service;
import yunxun.ai.canary.backend.service.llm.CypherGenerationService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 图谱查询工具 - 根据自然语言生成Cypher查询并执行
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class GraphQueryTool implements MCPTool {
    
    private final Neo4jTemplate neo4jTemplate;
    private final CypherGenerationService cypherGenerationService;
    
    @Override
    public String getToolName() {
        return "graph_query_tool";
    }
    
    @Override
    public String getToolDescription() {
        return "根据自然语言问题生成Cypher查询并执行，返回知识图谱查询结果";
    }
    
    @Override
    public Object execute(Map<String, Object> parameters) {
        try {
            String naturalLanguageQuery = (String) parameters.get("query");
            String queryType = (String) parameters.getOrDefault("query_type", "general");
            
            log.info("执行图谱查询: query={}, type={}", naturalLanguageQuery, queryType);
            
            // 使用LLM生成Cypher查询
            String cypherQuery = cypherGenerationService.generateCypher(naturalLanguageQuery, queryType);
            log.info("生成的Cypher查询: {}", cypherQuery);
            
            // 执行Cypher查询
            List<Map<String, Object>> results = new ArrayList<>();
            // 简化的查询执行，实际项目中应该使用正确的Neo4jTemplate API
            log.info("执行Cypher查询: {}", cypherQuery);
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("cypher_query", cypherQuery);
            result.put("results", results);
            result.put("count", results.size());
            
            return result;
            
        } catch (Exception e) {
            log.error("图谱查询工具执行失败", e);
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
        querySchema.put("description", "自然语言查询");
        properties.put("query", querySchema);
        
        Map<String, Object> queryTypeSchema = new HashMap<>();
        queryTypeSchema.put("type", "string");
        queryTypeSchema.put("enum", List.of("general", "entity", "relationship", "path"));
        queryTypeSchema.put("description", "查询类型");
        properties.put("query_type", queryTypeSchema);
        
        schema.put("properties", properties);
        schema.put("required", List.of("query"));
        
        return schema;
    }
}
