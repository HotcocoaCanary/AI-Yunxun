package yunxun.ai.canary.backend.mcp.service.tool;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

@Component
public class CrawlerTool {

    @Tool(name = "web_crawl", description = "Crawl a given URL and return plain text summary")
    public String crawl(@ToolParam(description = "target url") String url) {
        return "Crawled: " + url;
    }
}
