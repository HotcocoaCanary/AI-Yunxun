package yunxun.ai.canary.backend.mcp.server.config;

import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import yunxun.ai.canary.backend.mcp.server.tool.MongoCrudTool;
import yunxun.ai.canary.backend.mcp.server.tool.Neo4jCrudTool;
import yunxun.ai.canary.backend.mcp.server.tool.EchartGenerateTool;

/**
 * MCP 服务器配置
 * 注册所有 MCP 工具，供 AI 模型调用
 */
@Configuration
public class McpServerConfig {

    /**
     * 注册 MCP 工具回调提供者
     * 包含三个工具集：
     * 1. MongoCrud - MongoDB 文档的 CRUD 操作
     * 2. Neo4jCrud - Neo4j 图谱的节点和关系 CRUD 操作，以及特殊查询（路径查询、邻居查询、模糊查询）
     * 3. EchartGenerateTool - ECharts 图表生成工具（支持 bar, line, pie, graph, scatter）
     */
    @Bean
    public ToolCallbackProvider serverToolCallbacks(
            MongoCrudTool mongoCrudTool,
            Neo4jCrudTool neo4JCrudTool,
            EchartGenerateTool echartGenerateTool) {
        return MethodToolCallbackProvider.builder()
                .toolObjects(mongoCrudTool, neo4JCrudTool, echartGenerateTool)
                .build();
    }
}
