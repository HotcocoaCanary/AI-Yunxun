package yunxun.ai.canary.backend.service.mcp.tools;

import jakarta.annotation.Resource;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;
import yunxun.ai.canary.backend.model.dto.crawler.CrawlResult;
import yunxun.ai.canary.backend.service.crawler.PaperStorageService;
import yunxun.ai.canary.backend.service.crawler.WebCrawlerService;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class CrawlerTool {

    @Resource
    private WebCrawlerService webCrawlerService;

    @Resource
    private PaperStorageService paperStorageService;

    @Tool(name = "crawler_arxiv", description = "根据查询词从 arXiv 检索论文并入库")
    public List<Map<String, Object>> crawlArxiv(
            @ToolParam(description = "关键词") String query,
            @ToolParam(description = "最大条数") Integer maxResults
    ) {
        List<CrawlResult> results = webCrawlerService.crawlArxiv(query, maxResults == null ? 5 : maxResults);
        return paperStorageService.saveResults(results).stream()
                .map(doc -> Map.<String, Object>of(
                        "id", doc.getId(),
                        "title", doc.getTitle(),
                        "source", doc.getSource(),
                        "summary", doc.getSummary()
                ))
                .collect(Collectors.toList());
    }

    @Tool(name = "crawler_generic", description = "抓取任意网页内容并保存")
    public Map<String, Object> crawlUrl(
            @ToolParam(description = "网页 URL") String url
    ) {
        return webCrawlerService.crawlGenericUrl(url)
                .map(paperStorageService::saveResult)
                .map(doc -> Map.<String, Object>of(
                        "id", doc.getId(),
                        "title", doc.getTitle(),
                        "summary", doc.getSummary(),
                        "url", doc.getUrl()
                ))
                .orElse(Map.of("message", "未能抓取到内容"));
    }
}
