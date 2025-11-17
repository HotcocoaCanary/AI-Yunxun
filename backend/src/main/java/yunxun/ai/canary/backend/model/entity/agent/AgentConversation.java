package yunxun.ai.canary.backend.model.entity.agent;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;
import yunxun.ai.canary.backend.model.dto.agent.AgentMessage;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "agent_conversations")
public class AgentConversation {

    @Id
    private String id;

    private String title;

    @Builder.Default
    private List<AgentMessage> history = new ArrayList<>();

    @Builder.Default
    private List<String> enabledTools = new ArrayList<>();

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;
}
