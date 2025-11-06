package yunxun.ai.canary.backend.config.mcp;

import jakarta.annotation.Resource;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import yunxun.ai.canary.backend.service.mcp.tools.Neo4jGraphTool;

@Configuration
public class ToolsConfig {

    @Resource
    private Neo4jGraphTool neo4jGraphTool;

    @Bean
    public ToolCallbackProvider neo4jTool() {
        return MethodToolCallbackProvider.builder()
                .toolObjects(neo4jGraphTool)  // 使用 Spring 注入的 Bean
                .build();
    }
}
