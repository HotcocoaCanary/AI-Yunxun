package mcp.canary.server.tool;

import org.springaicommunity.mcp.annotation.McpTool;
import org.springaicommunity.mcp.annotation.McpToolParam;
import org.springframework.stereotype.Component;

@Component // 必须加
public class MyTools {

    @McpTool(name = "hello", description = "打招呼工具")
    public String sayHello(@McpToolParam(description = "名字") String name) {
        return "你好, " + name + "! 这是来自 Server 8081 的问候。";
    }
}