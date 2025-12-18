package yunxun.ai.canary.project.service.mcp.server.tool.model;

import java.time.Duration;
import java.time.Instant;

public final class ToolResponses {

    private ToolResponses() {
    }

    public static ToolResponse ok(Object data, String traceId, Instant startedAt) {
        return new ToolResponse(true, data, null, new ToolMeta(traceId, tookMs(startedAt)));
    }

    public static ToolResponse error(String code, String message, Object detail, String traceId, Instant startedAt) {
        return new ToolResponse(false, null, new ToolError(code, message, detail), new ToolMeta(traceId, tookMs(startedAt)));
    }

    private static long tookMs(Instant startedAt) {
        if (startedAt == null) {
            return 0L;
        }
        return Duration.between(startedAt, Instant.now()).toMillis();
    }
}

