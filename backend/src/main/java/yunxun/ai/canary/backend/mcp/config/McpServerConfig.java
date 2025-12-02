package yunxun.ai.canary.backend.mcp.config;

import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import yunxun.ai.canary.backend.mcp.service.tool.LlmTool;
import yunxun.ai.canary.backend.mcp.service.tool.Neo4jTool;

@Configuration
public class McpServerConfig {

    @Bean
    public ToolCallbackProvider serverToolCallbacks(LlmTool llmTool, Neo4jTool neo4jTool) {
        return MethodToolCallbackProvider.builder()
                .toolObjects(llmTool, neo4jTool)
                .build();
    }
}
