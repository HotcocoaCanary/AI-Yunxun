package yunxun.ai.canary.project.service.chat;

import yunxun.ai.canary.project.service.mcp.server.tool.model.ToolResponse;

import java.util.Map;

public record ToolTrace(
        String name,
        Map<String, Object> args,
        ToolResponse result
) {
}

