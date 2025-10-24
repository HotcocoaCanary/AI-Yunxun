package yunxun.ai.canary.backend.service.llm;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import yunxun.ai.canary.backend.model.entity.KnowledgeEntity;

import java.util.ArrayList;
import java.util.List;

/**
 * 实体抽取服务
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EntityExtractionService {
    
    private static final String ENTITY_EXTRACTION_PROMPT = """
            你是一个专业的实体抽取专家。请从给定的学术论文文本中抽取实体，并按照JSON格式返回结果。
            
            实体类型包括：
            - Person: 人物（作者、研究者等）
            - Organization: 组织（大学、研究所、公司等）
            - Concept: 概念（理论、方法、技术等）
            - Method: 方法（算法、技术、工具等）
            - Field: 领域（学科、研究方向等）
            
            请从以下文本中抽取实体：
            
            文本: {text}
            
            请以JSON格式返回，格式如下：
            [
                {
                    "name": "实体名称",
                    "type": "实体类型",
                    "description": "实体描述",
                    "confidence": 0.95
                }
            ]
            
            只返回JSON数组，不要包含其他内容。
            """;
    
    public List<KnowledgeEntity> extractEntities(String text) {
        try {
            // 简化的实体抽取逻辑，实际项目中应该调用LLM
            List<KnowledgeEntity> entities = new ArrayList<>();
            
            // 这里应该调用LLM进行实体抽取
            // 为了演示，返回空列表
            log.info("实体抽取功能待实现");
            return entities;
            
        } catch (Exception e) {
            log.error("实体抽取失败", e);
            return new ArrayList<>();
        }
    }
    
    private List<KnowledgeEntity> parseEntityResponse(String response) {
        List<KnowledgeEntity> entities = new ArrayList<>();
        
        try {
            // 这里应该使用JSON解析库，为了简化示例，使用简单的字符串解析
            // 实际项目中应该使用Jackson或Gson等库
            String[] entityStrings = response.split("\\{");
            
            for (String entityString : entityStrings) {
                if (entityString.trim().isEmpty()) continue;
                
                KnowledgeEntity entity = new KnowledgeEntity();
                // 简单的字符串解析逻辑
                // 实际项目中应该使用JSON解析
                entity.setName(extractValue(entityString, "name"));
                entity.setType(extractValue(entityString, "type"));
                entity.setDescription(extractValue(entityString, "description"));
                entity.setConfidence(Double.parseDouble(extractValue(entityString, "confidence")));
                
                if (entity.getName() != null && !entity.getName().isEmpty()) {
                    entities.add(entity);
                }
            }
            
        } catch (Exception e) {
            log.error("解析实体响应失败", e);
        }
        
        return entities;
    }
    
    private String extractValue(String json, String key) {
        try {
            String pattern = "\"" + key + "\"\\s*:\\s*\"([^\"]+)\"";
            java.util.regex.Pattern p = java.util.regex.Pattern.compile(pattern);
            java.util.regex.Matcher m = p.matcher(json);
            if (m.find()) {
                return m.group(1);
            }
        } catch (Exception e) {
            log.warn("提取值失败: {}", key);
        }
        return "";
    }
}
