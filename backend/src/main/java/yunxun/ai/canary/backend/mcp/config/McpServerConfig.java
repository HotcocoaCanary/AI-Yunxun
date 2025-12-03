package yunxun.ai.canary.backend.mcp.config;

import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import yunxun.ai.canary.backend.mcp.server.tool.MongoTool;
import yunxun.ai.canary.backend.mcp.server.tool.Neo4jGraphTool;

@Configuration
public class McpServerConfig {

    @Bean
    public ToolCallbackProvider serverToolCallbacks(
            MongoTool mongoTool,
            Neo4jGraphTool neo4jGraphTool) {
        return MethodToolCallbackProvider.builder()
                .toolObjects(mongoTool, neo4jGraphTool)
                .build();
    }
}
