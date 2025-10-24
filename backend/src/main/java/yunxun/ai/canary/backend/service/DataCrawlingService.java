package yunxun.ai.canary.backend.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import yunxun.ai.canary.backend.model.dto.QueryIntent;
import yunxun.ai.canary.backend.service.crawler.ArxivCrawlerService;
import yunxun.ai.canary.backend.service.crawler.CnkiCrawlerService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * 数据爬取服务 - 根据查询意图智能爬取相关数据
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DataCrawlingService {
    
    private final ArxivCrawlerService arxivCrawlerService;
    private final CnkiCrawlerService cnkiCrawlerService;
    private final DataProcessingService dataProcessingService;
    
    /**
     * 根据查询意图爬取数据
     */
    public Map<String, Object> crawlDataByIntent(QueryIntent intent) {
        try {
            log.info("开始根据意图爬取数据: intent={}, keywords={}", intent.getType(), intent.getKeywords());
            
            Map<String, Object> result = new HashMap<>();
            result.put("intent", intent.getType().name());
            result.put("keywords", intent.getKeywords());
            result.put("status", "started");
            
            // 异步执行爬取任务
            CompletableFuture.runAsync(() -> {
                try {
                    executeCrawlingTask(intent);
                    log.info("数据爬取任务完成: intent={}", intent.getType());
                } catch (Exception e) {
                    log.error("数据爬取任务失败: intent={}", intent.getType(), e);
                }
            });
            
            return result;
            
        } catch (Exception e) {
            log.error("启动数据爬取失败", e);
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("status", "failed");
            errorResult.put("error", e.getMessage());
            return errorResult;
        }
    }
    
    /**
     * 执行爬取任务
     */
    private void executeCrawlingTask(QueryIntent intent) {
        try {
            List<String> keywords = intent.getKeywords();
            String domain = intent.getDomain();
            
            // 根据领域和关键词选择爬取策略
            if (isComputerScienceDomain(domain) || isAIDomain(keywords)) {
                // 计算机科学和AI领域，优先爬取arXiv
                crawlFromArxiv(keywords, intent);
            } else if (isChineseDomain(domain) || isChineseKeywords(keywords)) {
                // 中文领域，爬取CNKI
                crawlFromCnki(keywords, intent);
            } else {
                // 通用领域，两个都爬取
                crawlFromArxiv(keywords, intent);
                crawlFromCnki(keywords, intent);
            }
            
        } catch (Exception e) {
            log.error("执行爬取任务失败", e);
        }
    }
    
    /**
     * 从arXiv爬取数据
     */
    private void crawlFromArxiv(List<String> keywords, QueryIntent intent) {
        try {
            log.info("开始从arXiv爬取数据: keywords={}", keywords);
            
            // 构建查询字符串
            String queryString = buildQueryString(keywords);
            
            // 执行爬取
            List<yunxun.ai.canary.backend.model.entity.Paper> papers = arxivCrawlerService.crawlPapers(queryString, 100);
            
            // 处理爬取到的数据
            dataProcessingService.processPapers(papers);
            
            log.info("arXiv数据爬取完成: {} 篇论文", papers.size());
            
        } catch (Exception e) {
            log.error("从arXiv爬取数据失败", e);
        }
    }
    
    /**
     * 从CNKI爬取数据
     */
    private void crawlFromCnki(List<String> keywords, QueryIntent intent) {
        try {
            log.info("开始从CNKI爬取数据: keywords={}", keywords);
            
            // 构建查询字符串
            String queryString = buildQueryString(keywords);
            
            // 执行爬取
            List<yunxun.ai.canary.backend.model.entity.Paper> papers = cnkiCrawlerService.crawlPapers(queryString, 50);
            
            // 处理爬取到的数据
            dataProcessingService.processPapers(papers);
            
            log.info("CNKI数据爬取完成: {} 篇论文", papers.size());
            
        } catch (Exception e) {
            log.error("从CNKI爬取数据失败", e);
        }
    }
    
    /**
     * 构建查询字符串
     */
    private String buildQueryString(List<String> keywords) {
        if (keywords == null || keywords.isEmpty()) {
            return "machine learning";
        }
        
        // 将关键词用AND连接
        return String.join(" AND ", keywords);
    }
    
    /**
     * 判断是否为计算机科学领域
     */
    private boolean isComputerScienceDomain(String domain) {
        if (domain == null) return false;
        String lowerDomain = domain.toLowerCase();
        return lowerDomain.contains("computer") || 
               lowerDomain.contains("cs") ||
               lowerDomain.contains("computing");
    }
    
    /**
     * 判断是否为AI领域
     */
    private boolean isAIDomain(List<String> keywords) {
        if (keywords == null) return false;
        String keywordsStr = String.join(" ", keywords).toLowerCase();
        return keywordsStr.contains("artificial intelligence") ||
               keywordsStr.contains("machine learning") ||
               keywordsStr.contains("deep learning") ||
               keywordsStr.contains("neural network") ||
               keywordsStr.contains("ai") ||
               keywordsStr.contains("ml");
    }
    
    /**
     * 判断是否为中文领域
     */
    private boolean isChineseDomain(String domain) {
        if (domain == null) return false;
        String lowerDomain = domain.toLowerCase();
        return lowerDomain.contains("chinese") ||
               lowerDomain.contains("china") ||
               lowerDomain.contains("中文");
    }
    
    /**
     * 判断是否为中文关键词
     */
    private boolean isChineseKeywords(List<String> keywords) {
        if (keywords == null) return false;
        return keywords.stream().anyMatch(keyword -> 
            keyword.matches(".*[\\u4e00-\\u9fa5].*"));
    }
}
