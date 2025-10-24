package yunxun.ai.canary.backend.model.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.util.List;
import java.util.Map;

/**
 * 查询意图分析结果
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QueryIntent {
    
    /**
     * 查询类型
     */
    private QueryType type;
    
    /**
     * 置信度
     */
    private Double confidence;
    
    /**
     * 提取的关键词
     */
    private List<String> keywords;
    
    /**
     * 提取的实体
     */
    private List<String> entities;
    
    /**
     * 时间范围
     */
    private TimeRange timeRange;
    
    /**
     * 领域/学科
     */
    private String domain;
    
    /**
     * 查询参数
     */
    private Map<String, Object> parameters;
    
    /**
     * 原始查询文本
     */
    private String originalQuery;
    
    /**
     * 查询类型枚举
     */
    public enum QueryType {
        GRAPH_QUERY,        // 图谱查询
        DATA_ANALYSIS,      // 数据分析
        LITERATURE_REVIEW,  // 文献综述
        TREND_ANALYSIS,     // 趋势分析
        CRAWL_DATA,         // 数据爬取
        GENERAL_QUERY       // 通用查询
    }
    
    /**
     * 时间范围
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TimeRange {
        private String startTime;
        private String endTime;
        private String timeUnit; // year, month, day
    }
}
