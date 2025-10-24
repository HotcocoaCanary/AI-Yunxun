package yunxun.ai.canary.backend.model.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.util.List;
import java.util.Map;

/**
 * 智能查询响应DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IntelligentQueryResponseDTO {
    
    /**
     * 原始查询
     */
    private String originalQuery;
    
    /**
     * 查询意图类型
     */
    private String intent;
    
    /**
     * 置信度
     */
    private Double confidence;
    
    /**
     * Cypher查询语句（如果是图谱查询）
     */
    private String cypherQuery;
    
    /**
     * 图谱查询结果
     */
    private List<Map<String, Object>> graphResults;
    
    /**
     * 数据查询结果
     */
    private List<Map<String, Object>> dataResults;
    
    /**
     * 文献查询结果
     */
    private List<Map<String, Object>> literatureResults;
    
    /**
     * 趋势数据
     */
    private List<Map<String, Object>> trendData;
    
    /**
     * 图谱可视化数据
     */
    private Map<String, Object> visualizationData;
    
    /**
     * 图表数据
     */
    private Map<String, Object> chartData;
    
    /**
     * 分析报告
     */
    private String analysisReport;
    
    /**
     * 文献综述
     */
    private String literatureReview;
    
    /**
     * 数据爬取结果
     */
    private Map<String, Object> crawlResult;
    
    /**
     * 响应消息
     */
    private String message;
    
    /**
     * 执行时间（毫秒）
     */
    private Long executionTime;
}
