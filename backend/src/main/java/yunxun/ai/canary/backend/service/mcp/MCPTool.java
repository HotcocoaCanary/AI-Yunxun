package yunxun.ai.canary.backend.service.mcp;

import java.util.Map;

/**
 * MCP工具基础接口
 */
public interface MCPTool {
    
    /**
     * 工具名称
     */
    String getToolName();
    
    /**
     * 工具描述
     */
    String getToolDescription();
    
    /**
     * 执行工具
     * @param parameters 参数
     * @return 执行结果
     */
    Object execute(Map<String, Object> parameters);
    
    /**
     * 获取工具参数模式
     * @return 参数模式
     */
    Map<String, Object> getParameterSchema();
}
