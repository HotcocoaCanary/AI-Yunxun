package yunxun.ai.canary.backend.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import yunxun.ai.canary.backend.model.dto.*;
import yunxun.ai.canary.backend.service.llm.NaturalLanguageProcessor;
import yunxun.ai.canary.backend.service.analysis.GraphAnalysisService;
import yunxun.ai.canary.backend.service.visualization.ChartGenerationService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 智能查询服务 - 上层服务，对用户隐藏MCP工具
 * 用户只需要通过自然语言查询，就能获得图谱和数据分析
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class IntelligentQueryService {
    
    private final NaturalLanguageProcessor naturalLanguageProcessor;
    private final GraphAnalysisService graphAnalysisService;
    private final ChartGenerationService chartGenerationService;
    private final DataCrawlingService dataCrawlingService;
    
    /**
     * 智能自然语言查询
     * 用户输入自然语言，系统自动分析意图并返回图谱和数据分析
     */
    public ApiResponseDTO<IntelligentQueryResponseDTO> intelligentQuery(String naturalLanguageQuery, String userId) {
        try {
            log.info("收到智能查询请求: user={}, query={}", userId, naturalLanguageQuery);
            
            // 1. 自然语言处理 - 分析用户意图
            QueryIntent intent = naturalLanguageProcessor.analyzeIntent(naturalLanguageQuery);
            log.info("分析查询意图: {}", intent);
            
            // 2. 根据意图执行相应操作
            IntelligentQueryResponseDTO response = executeQueryByIntent(intent, naturalLanguageQuery, userId);
            
            return ApiResponseDTO.success("查询成功", response);
            
        } catch (Exception e) {
            log.error("智能查询失败", e);
            return ApiResponseDTO.failure("查询失败: " + e.getMessage());
        }
    }
    
    /**
     * 根据查询意图执行相应操作
     */
    private IntelligentQueryResponseDTO executeQueryByIntent(QueryIntent intent, String query, String userId) {
        IntelligentQueryResponseDTO.IntelligentQueryResponseDTOBuilder responseBuilder = 
            IntelligentQueryResponseDTO.builder()
                .originalQuery(query)
                .intent(intent.getType().name())
                .confidence(intent.getConfidence());
        
        switch (intent.getType()) {
            case GRAPH_QUERY:
                return executeGraphQuery(intent, responseBuilder);
                
            case DATA_ANALYSIS:
                return executeDataAnalysis(intent, responseBuilder);
                
            case LITERATURE_REVIEW:
                return executeLiteratureReview(intent, responseBuilder);
                
            case TREND_ANALYSIS:
                return executeTrendAnalysis(intent, responseBuilder);
                
            case CRAWL_DATA:
                return executeDataCrawling(intent, responseBuilder);
                
            default:
                return executeGeneralQuery(intent, responseBuilder);
        }
    }
    
    /**
     * 执行图谱查询
     */
    private IntelligentQueryResponseDTO executeGraphQuery(QueryIntent intent, 
            IntelligentQueryResponseDTO.IntelligentQueryResponseDTOBuilder builder) {
        try {
            // 生成Cypher查询
            String cypherQuery = naturalLanguageProcessor.generateCypherQuery(intent);
            
            // 执行图谱查询
            List<Map<String, Object>> graphResults = graphAnalysisService.queryGraph(cypherQuery);
            
            // 生成图谱可视化数据
            Map<String, Object> visualizationData = chartGenerationService.generateGraphVisualization(graphResults);
            
            // 生成分析报告
            String analysisReport = naturalLanguageProcessor.generateAnalysisReport(intent, graphResults);
            
            return builder
                .cypherQuery(cypherQuery)
                .graphResults(graphResults)
                .visualizationData(visualizationData)
                .analysisReport(analysisReport)
                .build();
                
        } catch (Exception e) {
            log.error("图谱查询执行失败", e);
            throw new RuntimeException("图谱查询失败: " + e.getMessage());
        }
    }
    
    /**
     * 执行数据分析
     */
    private IntelligentQueryResponseDTO executeDataAnalysis(QueryIntent intent, 
            IntelligentQueryResponseDTO.IntelligentQueryResponseDTOBuilder builder) {
        try {
            // 获取相关数据
            List<Map<String, Object>> data = graphAnalysisService.getAnalysisData(intent);
            
            // 生成图表数据
            Map<String, Object> chartData = chartGenerationService.generateAnalysisCharts(data, intent);
            
            // 生成分析报告
            String analysisReport = naturalLanguageProcessor.generateDataAnalysisReport(intent, data);
            
            return builder
                .dataResults(data)
                .chartData(chartData)
                .analysisReport(analysisReport)
                .build();
                
        } catch (Exception e) {
            log.error("数据分析执行失败", e);
            throw new RuntimeException("数据分析失败: " + e.getMessage());
        }
    }
    
    /**
     * 执行文献综述
     */
    private IntelligentQueryResponseDTO executeLiteratureReview(QueryIntent intent, 
            IntelligentQueryResponseDTO.IntelligentQueryResponseDTOBuilder builder) {
        try {
            // 获取相关文献
            List<Map<String, Object>> literature = graphAnalysisService.getLiteratureData(intent);
            
            // 生成文献综述
            String literatureReview = naturalLanguageProcessor.generateLiteratureReview(intent, literature);
            
            // 生成趋势图表
            Map<String, Object> trendCharts = chartGenerationService.generateTrendCharts(literature);
            
            return builder
                .literatureResults(literature)
                .literatureReview(literatureReview)
                .chartData(trendCharts)
                .build();
                
        } catch (Exception e) {
            log.error("文献综述执行失败", e);
            throw new RuntimeException("文献综述失败: " + e.getMessage());
        }
    }
    
    /**
     * 执行趋势分析
     */
    private IntelligentQueryResponseDTO executeTrendAnalysis(QueryIntent intent, 
            IntelligentQueryResponseDTO.IntelligentQueryResponseDTOBuilder builder) {
        try {
            // 获取趋势数据
            List<Map<String, Object>> trendData = graphAnalysisService.getTrendData(intent);
            
            // 生成趋势图表
            Map<String, Object> trendCharts = chartGenerationService.generateTrendAnalysis(trendData, intent);
            
            // 生成趋势分析报告
            String trendReport = naturalLanguageProcessor.generateTrendAnalysisReport(intent, trendData);
            
            return builder
                .trendData(trendData)
                .chartData(trendCharts)
                .analysisReport(trendReport)
                .build();
                
        } catch (Exception e) {
            log.error("趋势分析执行失败", e);
            throw new RuntimeException("趋势分析失败: " + e.getMessage());
        }
    }
    
    /**
     * 执行数据爬取
     */
    private IntelligentQueryResponseDTO executeDataCrawling(QueryIntent intent, 
            IntelligentQueryResponseDTO.IntelligentQueryResponseDTOBuilder builder) {
        try {
            // 执行数据爬取
            Map<String, Object> crawlResult = dataCrawlingService.crawlDataByIntent(intent);
            
            return builder
                .crawlResult(crawlResult)
                .message("数据爬取任务已启动，请稍后查询结果")
                .build();
                
        } catch (Exception e) {
            log.error("数据爬取执行失败", e);
            throw new RuntimeException("数据爬取失败: " + e.getMessage());
        }
    }
    
    /**
     * 执行通用查询
     */
    private IntelligentQueryResponseDTO executeGeneralQuery(QueryIntent intent, 
            IntelligentQueryResponseDTO.IntelligentQueryResponseDTOBuilder builder) {
        try {
            // 尝试多种查询方式
            List<Map<String, Object>> results = graphAnalysisService.generalQuery(intent);
            
            // 生成通用分析报告
            String analysisReport = naturalLanguageProcessor.generateGeneralAnalysis(intent, results);
            
            return builder
                .dataResults(results)
                .analysisReport(analysisReport)
                .build();
                
        } catch (Exception e) {
            log.error("通用查询执行失败", e);
            throw new RuntimeException("查询失败: " + e.getMessage());
        }
    }
}
