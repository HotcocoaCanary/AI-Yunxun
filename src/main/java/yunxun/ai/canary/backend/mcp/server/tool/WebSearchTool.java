package yunxun.ai.canary.backend.mcp.server.tool;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 网络搜索工具（可选实现）
 * 
 * 注意：推荐使用外部 MCP 搜索服务器（如 websearch-mcp）而不是本地实现
 * 
 * 配置外部 MCP 搜索服务器的方法：
 * 1. 安装 websearch-mcp: npm install -g websearch-mcp
 * 2. 在 application.yml 中配置 MCP 客户端连接到搜索服务器
 * 3. 搜索服务器会自动注册为 MCP 工具，AI 模型可以直接调用
 * 
 * 如果需要在本地实现，可以取消下面的 @Component 注释并实现搜索逻辑
 */
// @Component  // 默认不启用，使用外部 MCP 服务器
public class WebSearchTool {

    @Value("${web.search.enabled:false}")
    private boolean enabled;

    /**
     * 执行网络搜索
     * 
     * 推荐：使用外部 MCP 搜索服务器（如 websearch-mcp）
     * 外部服务器会自动提供搜索工具，无需本地实现
     */
    @Tool(
            name = "web_search",
            description = "执行网络搜索，获取实时信息和数据。根据查询关键词搜索相关内容，返回搜索结果列表。适合用于获取统计数据、趋势信息、官方数据等。注意：推荐使用外部 MCP 搜索服务器提供的搜索工具。"
    )
    public String search(
            @ToolParam(description = "搜索关键词，例如：'2015-2024 考研人数 统计数据'、'各学科论文数量分布'")
            String query,

            @ToolParam(description = "返回结果数量，默认 10 条")
            Integer numResults) {
        
        return """
                {
                  "message": "网络搜索功能应通过外部 MCP 搜索服务器提供",
                  "suggestion": "请配置 websearch-mcp 或其他 MCP 搜索服务器",
                  "query": "%s"
                }
                """.formatted(query);
    }
}


