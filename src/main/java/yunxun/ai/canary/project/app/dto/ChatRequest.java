package yunxun.ai.canary.project.app.dto;

import java.util.Map;

public record ChatRequest(
        String sessionId,
        String message,
        Map<String, Object> context
) {
}

