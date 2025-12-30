package mcp.canary.client.model;

import java.time.Instant;

/**
 * 工具日志事件（从 MCP server logging 通知转换而来）。
 */
public record ToolLogEvent(
        String server,
        String level,
        String logger,
        String message,
        Instant timestamp
) {
}



