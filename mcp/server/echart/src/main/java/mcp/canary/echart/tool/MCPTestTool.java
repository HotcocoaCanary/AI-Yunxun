package mcp.canary.echart.tool;

import org.springaicommunity.mcp.annotation.McpTool;
import org.springaicommunity.mcp.annotation.McpToolParam;
import org.springframework.stereotype.Component;

@Component
public class MCPTestTool {
    @McpTool
    public String test(@McpToolParam String name) {
        return "Hello, World!" + name;
    }
}
