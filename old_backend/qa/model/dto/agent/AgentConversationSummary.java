package yunxun.ai.canary.backend.qa.model.dto.agent;

import java.time.Instant;

public record AgentConversationSummary(String id, String title, Instant updatedAt) {
}
