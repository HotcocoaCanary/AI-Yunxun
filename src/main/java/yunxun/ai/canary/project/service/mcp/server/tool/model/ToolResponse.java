package yunxun.ai.canary.project.service.mcp.server.tool.model;

public record ToolResponse(
        boolean ok,
        Object data,
        ToolError error,
        ToolMeta meta
) {
}

