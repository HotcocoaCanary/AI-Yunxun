package yunxun.ai.canary.project.app.dto;

import java.util.List;

public record ChatResponse(
        String traceId,
        String sessionId,
        String answer,
        List<ChatSource> sources,
        List<ChatChart> charts
) {
}

