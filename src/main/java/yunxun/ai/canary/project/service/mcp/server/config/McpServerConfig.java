package yunxun.ai.canary.project.service.mcp.server.config;

import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import yunxun.ai.canary.project.service.mcp.server.tool.MongoTool;
import yunxun.ai.canary.project.service.mcp.server.tool.Neo4jTool;
import yunxun.ai.canary.project.service.mcp.server.tool.EChartGenerateTool;
import yunxun.ai.canary.project.service.mcp.server.tool.WebSearchTool;

/**
 * MCP 服务器配置
 * <p>
 * 注册所有本地 MCP 工具，供 AI 模型通过 MCP 协议调用。
 * Spring AI MCP 服务器会自动发现并注册此配置的 ToolCallbackProvider。
 * <p>
 * 工具注册说明：
 * - MongoTool: 始终注册
 * - Neo4jCrudTool: 始终注册
 * - EChartGenerateTool: 始终注册
 * - WebSearchTool: 仅当 web.search.enabled=true 时注册（本地搜索实现）
 *   否则使用外部 MCP 服务器（mcp-searxng），通过 application.yml 中的 
 *   spring.ai.mcp.client.servers.websearch-mcp 配置。
 * 
 * @see yunxun.ai.canary.project.service.mcp.server.tool.MongoTool
 * @see Neo4jTool
 * @see yunxun.ai.canary.project.service.mcp.server.tool.EChartGenerateTool
 * @see yunxun.ai.canary.project.service.mcp.server.tool.WebSearchTool
 */
@Configuration
public class McpServerConfig {

    /**
     * 注册 MCP 工具回调提供者
     * <p>
     * 此 Bean 会被 Spring AI MCP 服务器自动发现，并将工具暴露给 MCP 客户端。
     * 工具通过 @Tool 和 @ToolParam 注解定义，MethodToolCallbackProvider 会自动扫描这些注解。
     * <p>
     * 包含四个工具集：
     * <ol>
     *   <li><b>MongoTool</b> - MongoDB 文档的 CRUD 操作
     *     <ul>
     *       <li>mongo_save_document - 保存文档</li>
     *       <li>mongo_find_by_topic - 按主题查询</li>
     *       <li>mongo_find_by_id - 按ID查询</li>
     *       <li>mongo_update_document - 更新文档</li>
     *       <li>mongo_delete_document - 删除文档</li>
     *       <li>mongo_find_all - 查询所有文档</li>
     *     </ul>
     *   </li>
     *   <li><b>Neo4jCrudTool</b> - Neo4j 图谱的节点和关系 CRUD 操作，以及特殊查询
     *     <ul>
     *       <li>节点操作：neo4j_create_node, neo4j_find_node, neo4j_update_node, neo4j_delete_node</li>
     *       <li>关系操作：neo4j_create_relationship, neo4j_find_relationship, neo4j_update_relationship, neo4j_delete_relationship</li>
     *       <li>特殊查询：neo4j_find_path, neo4j_find_neighbors, neo4j_fuzzy_search</li>
     *     </ul>
     *   </li>
     *   <li><b>EChartGenerateTool</b> - ECharts 图表生成工具
     *     <ul>
     *       <li>echart_generate - 根据原始数据生成 ECharts 图表配置（支持 bar, line, pie, graph, scatter）</li>
     *     </ul>
     *   </li>
     *   <li><b>WebSearchTool</b> - 网络搜索工具（可选，仅当 web.search.enabled=true 时注册）
     *     <ul>
     *       <li>web_search - 执行网络搜索，获取实时信息和数据</li>
     *     </ul>
     *   </li>
     * </ol>
     * 
     * @param mongoTool MongoDB 工具实例
     * @param neo4JTool Neo4j 工具实例
     * @param echartGenerateTool ECharts 图表生成工具实例
     * @param webSearchTool Web搜索工具实例（可选，通过@Autowired注入，如果Bean不存在则为null）
     * @return ToolCallbackProvider 实例，包含所有注册的工具
     */
    @Bean
    public ToolCallbackProvider serverToolCallbacks(
            MongoTool mongoTool,
            Neo4jTool neo4JTool,
            EChartGenerateTool echartGenerateTool,
            @Autowired(required = false) WebSearchTool webSearchTool) {
        
        MethodToolCallbackProvider.Builder builder = MethodToolCallbackProvider.builder()
                .toolObjects(mongoTool, neo4JTool, echartGenerateTool);
        
        // 如果WebSearchTool Bean存在（即web.search.enabled=true），则注册它
        // 否则使用外部MCP搜索服务器（如mcp-searxng）
        if (webSearchTool != null) {
            builder.toolObjects(webSearchTool);
        }
        
        return builder.build();
    }
}
