package yunxun.ai.canary.project.app.dto;

public record ChatStreamEvent(
        String traceId,
        String type,
        Object data
) {
}

