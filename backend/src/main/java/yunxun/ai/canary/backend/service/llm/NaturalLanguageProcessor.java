package yunxun.ai.canary.backend.service.llm;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import yunxun.ai.canary.backend.model.dto.QueryIntent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 自然语言处理器 - 集成大模型进行自然语言理解和生成
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class NaturalLanguageProcessor {
    
    private final MockLLMService mockLLMService;
    
    private static final String INTENT_ANALYSIS_PROMPT = """
            你是一个专业的学术查询意图分析专家。请分析用户的自然语言查询，识别查询意图和相关参数。
            
            查询类型包括：
            - GRAPH_QUERY: 图谱查询（如"找到与机器学习相关的所有论文"）
            - DATA_ANALYSIS: 数据分析（如"分析深度学习领域的发展趋势"）
            - LITERATURE_REVIEW: 文献综述（如"总结自然语言处理的最新进展"）
            - TREND_ANALYSIS: 趋势分析（如"机器学习领域近5年的发展趋势"）
            - CRAWL_DATA: 数据爬取（如"爬取人工智能相关的论文"）
            - GENERAL_QUERY: 通用查询（其他类型）
            
            请分析以下查询：
            查询: {query}
            
            请以JSON格式返回分析结果：
            {{
                "type": "查询类型",
                "confidence": 0.95,
                "keywords": ["关键词1", "关键词2"],
                "entities": ["实体1", "实体2"],
                "timeRange": {{
                    "startTime": "2020",
                    "endTime": "2024",
                    "timeUnit": "year"
                }},
                "domain": "领域名称",
                "parameters": {{
                    "param1": "value1"
                }}
            }}
            """;
    
    private static final String CYPHER_GENERATION_PROMPT = """
            你是一个Neo4j Cypher查询专家。根据查询意图生成相应的Cypher查询语句。
            
            知识图谱包含以下节点类型：
            - KnowledgeEntity: 知识实体（人物、组织、概念、方法等）
            - Paper: 论文
            
            关系类型包括：
            - CO_AUTHOR: 合著关系
            - CITE: 引用关系
            - RELATED_TO: 相关关系
            - PUBLISHED_BY: 发表关系
            
            查询意图: {intent}
            关键词: {keywords}
            实体: {entities}
            领域: {domain}
            
            请生成相应的Cypher查询语句：
            """;
    
    private static final String ANALYSIS_REPORT_PROMPT = """
            你是一个专业的学术数据分析专家。请基于查询结果生成详细的分析报告。
            
            查询意图: {intent}
            查询结果: {results}
            
            请生成包含以下内容的分析报告：
            1. 数据概览
            2. 关键发现
            3. 趋势分析
            4. 结论和建议
            
            分析报告：
            """;
    
    /**
     * 分析查询意图
     */
    public QueryIntent analyzeIntent(String query) {
        try {
            log.info("开始分析查询意图: {}", query);
            
            String prompt = INTENT_ANALYSIS_PROMPT.replace("{query}", query);
            String response = mockLLMService.callLLM(prompt);
            
            // 解析JSON响应
            QueryIntent intent = parseIntentResponse(response);
            intent.setOriginalQuery(query);
            
            log.info("查询意图分析完成: type={}, confidence={}", intent.getType(), intent.getConfidence());
            return intent;
            
        } catch (Exception e) {
            log.error("查询意图分析失败", e);
            // 返回默认意图
            return QueryIntent.builder()
                    .type(QueryIntent.QueryType.GENERAL_QUERY)
                    .confidence(0.5)
                    .keywords(List.of())
                    .entities(List.of())
                    .originalQuery(query)
                    .build();
        }
    }
    
    /**
     * 生成Cypher查询语句
     */
    public String generateCypherQuery(QueryIntent intent) {
        try {
            log.info("开始生成Cypher查询: intent={}", intent.getType());
            
            String prompt = CYPHER_GENERATION_PROMPT
                    .replace("{intent}", intent.getType().name())
                    .replace("{keywords}", String.join(", ", intent.getKeywords()))
                    .replace("{entities}", String.join(", ", intent.getEntities()))
                    .replace("{domain}", intent.getDomain() != null ? intent.getDomain() : "");
            
            String response = mockLLMService.callLLM(prompt);
            
            // 清理返回的Cypher查询
            String cypherQuery = cleanCypherQuery(response);
            
            log.info("Cypher查询生成完成: {}", cypherQuery);
            return cypherQuery;
            
        } catch (Exception e) {
            log.error("Cypher查询生成失败", e);
            // 返回默认查询
            return "MATCH (n) RETURN n LIMIT 10";
        }
    }
    
    /**
     * 生成分析报告
     */
    public String generateAnalysisReport(QueryIntent intent, List<Map<String, Object>> results) {
        try {
            log.info("开始生成分析报告: intent={}, results={}", intent.getType(), results.size());
            
            String prompt = ANALYSIS_REPORT_PROMPT
                    .replace("{intent}", intent.getType().name())
                    .replace("{results}", formatResultsForLLM(results));
            
            String response = mockLLMService.callLLM(prompt);
            
            log.info("分析报告生成完成");
            return response;
            
        } catch (Exception e) {
            log.error("分析报告生成失败", e);
            return "抱歉，无法生成分析报告。";
        }
    }
    
    /**
     * 生成数据分析报告
     */
    public String generateDataAnalysisReport(QueryIntent intent, List<Map<String, Object>> data) {
        // 类似generateAnalysisReport，但专门针对数据分析
        return generateAnalysisReport(intent, data);
    }
    
    /**
     * 生成文献综述
     */
    public String generateLiteratureReview(QueryIntent intent, List<Map<String, Object>> literature) {
        try {
            log.info("开始生成文献综述: intent={}, literature={}", intent.getType(), literature.size());
            
            String prompt = String.format("""
                    请基于以下文献数据生成文献综述：
                    
                    查询领域: %s
                    文献数据: %s
                    
                    请生成包含以下内容的文献综述：
                    1. 研究背景
                    2. 主要研究方向
                    3. 重要研究成果
                    4. 研究趋势
                    5. 未来展望
                    
                    文献综述：
                    """, intent.getDomain(), formatResultsForLLM(literature));
            
            String response = mockLLMService.callLLM(prompt);
            
            log.info("文献综述生成完成");
            return response;
            
        } catch (Exception e) {
            log.error("文献综述生成失败", e);
            return "抱歉，无法生成文献综述。";
        }
    }
    
    /**
     * 生成趋势分析报告
     */
    public String generateTrendAnalysisReport(QueryIntent intent, List<Map<String, Object>> trendData) {
        // 类似generateAnalysisReport，但专门针对趋势分析
        return generateAnalysisReport(intent, trendData);
    }
    
    /**
     * 生成通用分析
     */
    public String generateGeneralAnalysis(QueryIntent intent, List<Map<String, Object>> results) {
        // 类似generateAnalysisReport，但针对通用查询
        return generateAnalysisReport(intent, results);
    }
    
    /**
     * 解析意图分析响应
     */
    private QueryIntent parseIntentResponse(String response) {
        try {
            // 这里应该使用JSON解析库，为了简化示例，使用简单的字符串解析
            // 实际项目中应该使用Jackson或Gson等库
            QueryIntent.QueryIntentBuilder builder = QueryIntent.builder();
            
            // 简单的解析逻辑
            if (response.contains("\"type\": \"GRAPH_QUERY\"")) {
                builder.type(QueryIntent.QueryType.GRAPH_QUERY);
            } else if (response.contains("\"type\": \"DATA_ANALYSIS\"")) {
                builder.type(QueryIntent.QueryType.DATA_ANALYSIS);
            } else if (response.contains("\"type\": \"LITERATURE_REVIEW\"")) {
                builder.type(QueryIntent.QueryType.LITERATURE_REVIEW);
            } else if (response.contains("\"type\": \"TREND_ANALYSIS\"")) {
                builder.type(QueryIntent.QueryType.TREND_ANALYSIS);
            } else if (response.contains("\"type\": \"CRAWL_DATA\"")) {
                builder.type(QueryIntent.QueryType.CRAWL_DATA);
            } else {
                builder.type(QueryIntent.QueryType.GENERAL_QUERY);
            }
            
            // 设置默认值
            builder.confidence(0.8);
            builder.keywords(new ArrayList<>());
            builder.entities(new ArrayList<>());
            builder.parameters(new HashMap<>());
            
            return builder.build();
            
        } catch (Exception e) {
            log.error("解析意图响应失败", e);
            return QueryIntent.builder()
                    .type(QueryIntent.QueryType.GENERAL_QUERY)
                    .confidence(0.5)
                    .keywords(new ArrayList<>())
                    .entities(new ArrayList<>())
                    .parameters(new HashMap<>())
                    .build();
        }
    }
    
    /**
     * 清理Cypher查询
     */
    private String cleanCypherQuery(String cypherQuery) {
        cypherQuery = cypherQuery.trim();
        if (cypherQuery.startsWith("```cypher")) {
            cypherQuery = cypherQuery.substring(9);
        }
        if (cypherQuery.startsWith("```")) {
            cypherQuery = cypherQuery.substring(3);
        }
        if (cypherQuery.endsWith("```")) {
            cypherQuery = cypherQuery.substring(0, cypherQuery.length() - 3);
        }
        return cypherQuery.trim();
    }
    
    /**
     * 格式化结果供LLM处理
     */
    private String formatResultsForLLM(List<Map<String, Object>> results) {
        if (results == null || results.isEmpty()) {
            return "无数据";
        }
        
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < Math.min(results.size(), 10); i++) { // 限制数量避免过长
            Map<String, Object> result = results.get(i);
            sb.append("结果 ").append(i + 1).append(": ").append(result.toString()).append("\n");
        }
        
        if (results.size() > 10) {
            sb.append("... 还有 ").append(results.size() - 10).append(" 个结果");
        }
        
        return sb.toString();
    }
}
