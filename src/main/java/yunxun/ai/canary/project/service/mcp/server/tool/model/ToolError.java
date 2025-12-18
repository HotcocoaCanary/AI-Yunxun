package yunxun.ai.canary.project.service.mcp.server.tool.model;

public record ToolError(
        String code,
        String message,
        Object detail
) {
}

