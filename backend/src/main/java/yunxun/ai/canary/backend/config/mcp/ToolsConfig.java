package yunxun.ai.canary.backend.config.mcp;

import jakarta.annotation.Resource;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import yunxun.ai.canary.backend.service.mcp.tool.AnalyticsTool;
import yunxun.ai.canary.backend.service.mcp.tool.CrawlerTool;
import yunxun.ai.canary.backend.service.mcp.tool.GraphTool;
import yunxun.ai.canary.backend.service.mcp.tool.LlmTool;
import yunxun.ai.canary.backend.service.mcp.tool.RagTool;
import yunxun.ai.canary.backend.service.config.tools.UserToolService;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class ToolsConfig {

    @Resource
    private GraphTool neo4jGraphTool;

    @Resource
    private LlmTool llmTool;

    @Resource
    private RagTool ragTool;

    @Resource
    private CrawlerTool crawlerTool;

    @Resource
    private AnalyticsTool analyticsTool;

    @Resource
    @Lazy
    private UserToolService userToolService;

    @Bean
    public ToolCallbackProvider toolCallbackProvider() {
        List<Object> tools = new ArrayList<>();
        List<String> enabled = userToolService.enabledToolNames(null);
        if (enabled.isEmpty() || enabled.contains("kg_query")) tools.add(neo4jGraphTool);
        if (enabled.isEmpty() || enabled.contains("llm_answer")) tools.add(llmTool);
        if (enabled.isEmpty() || enabled.contains("rag_answer")) tools.add(ragTool);
        if (enabled.isEmpty() || enabled.contains("web_crawl")) tools.add(crawlerTool);
        if (enabled.isEmpty() || enabled.contains("graph_ingest_dataset")) tools.add(analyticsTool);

        return MethodToolCallbackProvider.builder()
                .toolObjects(tools.toArray())
                .build();
    }

    public List<Object> toolObjectsForUser(Long userId) {
        List<String> enabled = userToolService.enabledToolNames(userId);
        List<Object> tools = new ArrayList<>();
        if (enabled.contains("kg_query")) tools.add(neo4jGraphTool);
        if (enabled.contains("llm_answer")) tools.add(llmTool);
        if (enabled.contains("rag_answer")) tools.add(ragTool);
        if (enabled.contains("web_crawl")) tools.add(crawlerTool);
        if (enabled.contains("graph_ingest_dataset")) tools.add(analyticsTool);
        return tools;
    }
}


