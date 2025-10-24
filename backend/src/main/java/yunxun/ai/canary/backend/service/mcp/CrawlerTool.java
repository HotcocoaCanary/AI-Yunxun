package yunxun.ai.canary.backend.service.mcp;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import yunxun.ai.canary.backend.model.entity.Paper;
import yunxun.ai.canary.backend.repository.mongodb.PaperRepository;
import yunxun.ai.canary.backend.service.crawler.ArxivCrawlerService;
import yunxun.ai.canary.backend.service.crawler.CnkiCrawlerService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * 爬虫工具 - 负责从学术网站爬取论文数据
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CrawlerTool implements MCPTool {
    
    private final ArxivCrawlerService arxivCrawlerService;
    private final CnkiCrawlerService cnkiCrawlerService;
    private final PaperRepository paperRepository;
    
    @Override
    public String getToolName() {
        return "crawler_tool";
    }
    
    @Override
    public String getToolDescription() {
        return "从学术网站（arXiv、知网等）爬取论文数据并存储到MongoDB";
    }
    
    @Override
    public Object execute(Map<String, Object> parameters) {
        try {
            String source = (String) parameters.get("source");
            Integer maxPapers = (Integer) parameters.getOrDefault("max_papers", 100);
            String query = (String) parameters.get("query");
            
            log.info("开始爬取数据: source={}, maxPapers={}, query={}", source, maxPapers, query);
            
            List<Paper> papers;
            switch (source.toLowerCase()) {
                case "arxiv":
                    papers = arxivCrawlerService.crawlPapers(query, maxPapers);
                    break;
                case "cnki":
                    papers = cnkiCrawlerService.crawlPapers(query, maxPapers);
                    break;
                default:
                    throw new IllegalArgumentException("不支持的数据源: " + source);
            }
            
            // 异步保存到数据库
            CompletableFuture.runAsync(() -> {
                try {
                    paperRepository.saveAll(papers);
                    log.info("成功保存 {} 篇论文到数据库", papers.size());
                } catch (Exception e) {
                    log.error("保存论文数据失败", e);
                }
            });
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("papers_count", papers.size());
            result.put("message", "爬取任务已启动，数据正在后台处理中");
            
            return result;
            
        } catch (Exception e) {
            log.error("爬虫工具执行失败", e);
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
        
        Map<String, Object> sourceSchema = new HashMap<>();
        sourceSchema.put("type", "string");
        sourceSchema.put("enum", List.of("arxiv", "cnki", "google_scholar"));
        sourceSchema.put("description", "数据源");
        properties.put("source", sourceSchema);
        
        Map<String, Object> querySchema = new HashMap<>();
        querySchema.put("type", "string");
        querySchema.put("description", "搜索关键词");
        properties.put("query", querySchema);
        
        Map<String, Object> maxPapersSchema = new HashMap<>();
        maxPapersSchema.put("type", "integer");
        maxPapersSchema.put("minimum", 1);
        maxPapersSchema.put("maximum", 10000);
        maxPapersSchema.put("description", "最大爬取数量");
        properties.put("max_papers", maxPapersSchema);
        
        schema.put("properties", properties);
        schema.put("required", List.of("source", "query"));
        
        return schema;
    }
}
