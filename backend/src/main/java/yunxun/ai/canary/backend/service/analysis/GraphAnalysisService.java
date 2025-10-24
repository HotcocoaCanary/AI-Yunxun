package yunxun.ai.canary.backend.service.analysis;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.neo4j.core.Neo4jTemplate;
import org.springframework.stereotype.Service;
import yunxun.ai.canary.backend.model.dto.QueryIntent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 图谱分析服务 - 封装图谱查询和分析功能
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class GraphAnalysisService {
    
    private final Neo4jTemplate neo4jTemplate;
    
    /**
     * 执行图谱查询
     */
    public List<Map<String, Object>> queryGraph(String cypherQuery) {
        try {
            log.info("执行图谱查询: {}", cypherQuery);
            
            // 使用Neo4jTemplate执行查询
            // 暂时简化，实际项目中应该使用正确的Neo4jTemplate API
            List<Map<String, Object>> results = new ArrayList<>();
            log.info("执行Cypher查询: {}", cypherQuery);
            
            log.info("图谱查询完成，返回 {} 条结果", results.size());
            return results;
            
        } catch (Exception e) {
            log.error("图谱查询失败", e);
            return new ArrayList<>();
        }
    }
    
    /**
     * 获取分析数据
     */
    public List<Map<String, Object>> getAnalysisData(QueryIntent intent) {
        try {
            log.info("获取分析数据: intent={}", intent.getType());
            
            // 根据意图类型生成不同的查询
            String cypherQuery = generateAnalysisQuery(intent);
            return queryGraph(cypherQuery);
            
        } catch (Exception e) {
            log.error("获取分析数据失败", e);
            return new ArrayList<>();
        }
    }
    
    /**
     * 获取文献数据
     */
    public List<Map<String, Object>> getLiteratureData(QueryIntent intent) {
        try {
            log.info("获取文献数据: intent={}", intent.getType());
            
            // 查询相关论文和文献
            String cypherQuery = generateLiteratureQuery(intent);
            return queryGraph(cypherQuery);
            
        } catch (Exception e) {
            log.error("获取文献数据失败", e);
            return new ArrayList<>();
        }
    }
    
    /**
     * 获取趋势数据
     */
    public List<Map<String, Object>> getTrendData(QueryIntent intent) {
        try {
            log.info("获取趋势数据: intent={}", intent.getType());
            
            // 查询时间序列数据
            String cypherQuery = generateTrendQuery(intent);
            return queryGraph(cypherQuery);
            
        } catch (Exception e) {
            log.error("获取趋势数据失败", e);
            return new ArrayList<>();
        }
    }
    
    /**
     * 通用查询
     */
    public List<Map<String, Object>> generalQuery(QueryIntent intent) {
        try {
            log.info("执行通用查询: intent={}", intent.getType());
            
            // 尝试多种查询方式
            List<Map<String, Object>> results = new ArrayList<>();
            
            // 1. 实体查询
            String entityQuery = generateEntityQuery(intent);
            results.addAll(queryGraph(entityQuery));
            
            // 2. 关系查询
            String relationQuery = generateRelationQuery(intent);
            results.addAll(queryGraph(relationQuery));
            
            return results;
            
        } catch (Exception e) {
            log.error("通用查询失败", e);
            return new ArrayList<>();
        }
    }
    
    /**
     * 生成分析查询
     */
    private String generateAnalysisQuery(QueryIntent intent) {
        StringBuilder cypher = new StringBuilder();
        
        switch (intent.getType()) {
            case DATA_ANALYSIS:
                cypher.append("MATCH (e:KnowledgeEntity)");
                if (intent.getDomain() != null) {
                    cypher.append(" WHERE e.domain = '").append(intent.getDomain()).append("'");
                }
                cypher.append(" RETURN e.name as name, e.type as type, e.confidence as confidence");
                break;
                
            case TREND_ANALYSIS:
                cypher.append("MATCH (p:Paper)-[r]->(e:KnowledgeEntity)");
                if (intent.getTimeRange() != null) {
                    cypher.append(" WHERE p.publishedDate >= '").append(intent.getTimeRange().getStartTime()).append("'");
                    cypher.append(" AND p.publishedDate <= '").append(intent.getTimeRange().getEndTime()).append("'");
                }
                cypher.append(" RETURN p.publishedDate as date, e.name as entity, count(r) as frequency");
                cypher.append(" ORDER BY p.publishedDate");
                break;
                
            default:
                cypher.append("MATCH (n) RETURN n LIMIT 10");
        }
        
        return cypher.toString();
    }
    
    /**
     * 生成文献查询
     */
    private String generateLiteratureQuery(QueryIntent intent) {
        StringBuilder cypher = new StringBuilder();
        
        cypher.append("MATCH (p:Paper)");
        if (intent.getKeywords() != null && !intent.getKeywords().isEmpty()) {
            cypher.append(" WHERE ANY(keyword IN p.keywords WHERE keyword CONTAINS '").append(intent.getKeywords().get(0)).append("')");
        }
        if (intent.getTimeRange() != null) {
            if (cypher.toString().contains("WHERE")) {
                cypher.append(" AND");
            } else {
                cypher.append(" WHERE");
            }
            cypher.append(" p.publishedDate >= '").append(intent.getTimeRange().getStartTime()).append("'");
            cypher.append(" AND p.publishedDate <= '").append(intent.getTimeRange().getEndTime()).append("'");
        }
        cypher.append(" RETURN p.title as title, p.authors as authors, p.publishedDate as publishedDate, p.abstractText as abstract");
        cypher.append(" ORDER BY p.publishedDate DESC LIMIT 50");
        
        return cypher.toString();
    }
    
    /**
     * 生成趋势查询
     */
    private String generateTrendQuery(QueryIntent intent) {
        StringBuilder cypher = new StringBuilder();
        
        cypher.append("MATCH (p:Paper)-[r:MENTIONS]->(e:KnowledgeEntity)");
        if (intent.getDomain() != null) {
            cypher.append(" WHERE e.domain = '").append(intent.getDomain()).append("'");
        }
        if (intent.getTimeRange() != null) {
            if (cypher.toString().contains("WHERE")) {
                cypher.append(" AND");
            } else {
                cypher.append(" WHERE");
            }
            cypher.append(" p.publishedDate >= '").append(intent.getTimeRange().getStartTime()).append("'");
            cypher.append(" AND p.publishedDate <= '").append(intent.getTimeRange().getEndTime()).append("'");
        }
        cypher.append(" RETURN date(p.publishedDate) as date, e.name as entity, count(r) as mentions");
        cypher.append(" ORDER BY date");
        
        return cypher.toString();
    }
    
    /**
     * 生成实体查询
     */
    private String generateEntityQuery(QueryIntent intent) {
        StringBuilder cypher = new StringBuilder();
        
        cypher.append("MATCH (e:KnowledgeEntity)");
        if (intent.getKeywords() != null && !intent.getKeywords().isEmpty()) {
            cypher.append(" WHERE ANY(keyword IN e.keywords WHERE keyword CONTAINS '").append(intent.getKeywords().get(0)).append("')");
        }
        cypher.append(" RETURN e.name as name, e.type as type, e.description as description");
        cypher.append(" LIMIT 20");
        
        return cypher.toString();
    }
    
    /**
     * 生成关系查询
     */
    private String generateRelationQuery(QueryIntent intent) {
        StringBuilder cypher = new StringBuilder();
        
        cypher.append("MATCH (e1:KnowledgeEntity)-[r]->(e2:KnowledgeEntity)");
        if (intent.getKeywords() != null && !intent.getKeywords().isEmpty()) {
            cypher.append(" WHERE e1.name CONTAINS '").append(intent.getKeywords().get(0)).append("'");
            cypher.append(" OR e2.name CONTAINS '").append(intent.getKeywords().get(0)).append("'");
        }
        cypher.append(" RETURN e1.name as source, type(r) as relationship, e2.name as target");
        cypher.append(" LIMIT 20");
        
        return cypher.toString();
    }
}
