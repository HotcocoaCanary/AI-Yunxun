package yunxun.ai.canary.backend.service.llm;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 模拟LLM服务 - 暂时替代真实的Spring AI
 * 后续可以替换为真实的LLM服务
 */
@Service
@Slf4j
public class MockLLMService {
    
    /**
     * 模拟LLM调用
     */
    public String callLLM(String prompt) {
        try {
            log.info("模拟LLM调用: {}", prompt.substring(0, Math.min(100, prompt.length())));
            
            // 模拟处理时间
            Thread.sleep(1000);
            
            // 根据提示内容返回模拟响应
            if (prompt.contains("意图分析")) {
                return generateMockIntentResponse(prompt);
            } else if (prompt.contains("Cypher查询")) {
                return generateMockCypherResponse(prompt);
            } else if (prompt.contains("分析报告")) {
                return generateMockAnalysisResponse(prompt);
            } else {
                return generateMockGeneralResponse(prompt);
            }
            
        } catch (Exception e) {
            log.error("模拟LLM调用失败", e);
            return "抱歉，LLM服务暂时不可用。";
        }
    }
    
    /**
     * 生成模拟意图分析响应
     */
    private String generateMockIntentResponse(String prompt) {
        return """
                {
                    "type": "GRAPH_QUERY",
                    "confidence": 0.85,
                    "keywords": ["机器学习", "深度学习", "神经网络"],
                    "entities": ["TensorFlow", "PyTorch", "CNN"],
                    "timeRange": {
                        "startTime": "2020",
                        "endTime": "2024",
                        "timeUnit": "year"
                    },
                    "domain": "人工智能",
                    "parameters": {
                        "limit": 50
                    }
                }
                """;
    }
    
    /**
     * 生成模拟Cypher响应
     */
    private String generateMockCypherResponse(String prompt) {
        return """
                MATCH (e:KnowledgeEntity)-[r]->(target:KnowledgeEntity)
                WHERE e.name CONTAINS '机器学习' OR e.name CONTAINS '深度学习'
                RETURN e, r, target
                LIMIT 20
                """;
    }
    
    /**
     * 生成模拟分析报告响应
     */
    private String generateMockAnalysisResponse(String prompt) {
        return """
                # 分析报告
                
                ## 数据概览
                本次查询共找到 25 个相关实体，涉及 15 个关系。
                
                ## 关键发现
                1. 机器学习领域发展迅速，相关论文数量呈指数增长
                2. 深度学习技术成为主流，CNN、RNN等模型广泛应用
                3. 跨领域合作增多，计算机视觉与自然语言处理结合紧密
                
                ## 趋势分析
                - 2020-2024年间，相关研究论文数量增长300%
                - 新兴技术如Transformer、GPT模型受到广泛关注
                - 产业应用逐渐成熟，商业化程度提高
                
                ## 结论和建议
                建议继续关注深度学习前沿技术，加强跨领域合作。
                """;
    }
    
    /**
     * 生成模拟通用响应
     */
    private String generateMockGeneralResponse(String prompt) {
        return "这是一个模拟的LLM响应。在实际应用中，这里会调用真实的大语言模型。";
    }
}
