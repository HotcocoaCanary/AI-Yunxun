package yunxun.ai.canary.project.service.chat;

import java.util.Map;

public record PlannerDecision(
        String action,
        String name,
        Map<String, Object> args
) {
    public boolean isFinal() {
        return action == null || action.isBlank() || "final".equalsIgnoreCase(action);
    }
}

