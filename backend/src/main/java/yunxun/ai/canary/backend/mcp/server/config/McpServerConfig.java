package yunxun.ai.canary.backend.mcp.server.config;

import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import yunxun.ai.canary.backend.mcp.server.tool.MongoTool;
import yunxun.ai.canary.backend.mcp.server.tool.Neo4jGraphTool;
import yunxun.ai.canary.backend.mcp.server.tool.GraphChartTool;

/**
 * MCP 服务器配置
 * 注册所有 MCP 工具，供 AI 模型调用
 */
@Configuration
public class McpServerConfig {

    /**
     * 注册 MCP 工具回调提供者
     * 包含三个工具集：
     * 1. MongoTool - MongoDB 文档的 CRUD 操作
     * 2. Neo4jGraphTool - Neo4j 图谱的节点和关系 CRUD 操作
     * 3. GraphChartTool - 图表生成工具
     */
    @Bean
    public ToolCallbackProvider serverToolCallbacks(
            MongoTool mongoTool,
            Neo4jGraphTool neo4jGraphTool,
            GraphChartTool graphChartTool) {
        return MethodToolCallbackProvider.builder()
                .toolObjects(mongoTool, neo4jGraphTool, graphChartTool)
                .build();
    }
}
