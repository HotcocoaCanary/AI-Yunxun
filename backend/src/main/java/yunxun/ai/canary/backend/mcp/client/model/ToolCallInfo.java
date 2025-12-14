package yunxun.ai.canary.backend.mcp.client.model;

import java.util.Map;

/**
 * 工具调用信息
 * 用于追踪和记录 MCP 工具调用
 */
public record ToolCallInfo(
        /*
          工具组名称：neo4j-crud, mongo-crud, echart-generate, web-search
         */
        String toolGroup,
        
        /*
          具体工具名，如 "neo4j_find_node", "mongo_save_document", "echart_generate"
         */
        String toolName,
        
        /*
          工具调用参数
         */
        Map<String, Object> args
) {
    
    /**
     * 从工具名推断工具组
     */
    public static String inferToolGroup(String toolName) {
        if (toolName == null) {
            return "unknown";
        }
        if (toolName.startsWith("neo4j_")) {
            return "neo4j-crud";
        } else if (toolName.startsWith("mongo_")) {
            return "mongo-crud";
        } else if (toolName.startsWith("echart_") || toolName.startsWith("generate_chart")) {
            return "echart-generate";
        } else if (toolName.startsWith("web_search") || toolName.startsWith("search_web")) {
            return "web-search";
        }
        return "unknown";
    }
}

