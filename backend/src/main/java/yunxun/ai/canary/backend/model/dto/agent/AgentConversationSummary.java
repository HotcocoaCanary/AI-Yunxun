package yunxun.ai.canary.backend.model.dto.agent;

import java.time.Instant;

public record AgentConversationSummary(String id, String title, Instant updatedAt) {
}
