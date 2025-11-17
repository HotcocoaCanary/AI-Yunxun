package yunxun.ai.canary.backend.config.mcp;

import jakarta.annotation.Resource;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import yunxun.ai.canary.backend.service.mcp.tools.AnalyticsTool;
import yunxun.ai.canary.backend.service.mcp.tools.CrawlerTool;
import yunxun.ai.canary.backend.service.mcp.tools.GraphTool;
import yunxun.ai.canary.backend.service.mcp.tools.LlmTool;
import yunxun.ai.canary.backend.service.mcp.tools.RagTool;

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

    @Bean
    public ToolCallbackProvider toolCallbackProvider() {
        return MethodToolCallbackProvider.builder()
                .toolObjects(neo4jGraphTool, llmTool, ragTool, crawlerTool, analyticsTool)
                .build();
    }
}
