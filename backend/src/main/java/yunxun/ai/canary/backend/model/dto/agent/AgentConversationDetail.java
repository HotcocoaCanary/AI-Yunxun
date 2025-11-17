package yunxun.ai.canary.backend.model.dto.agent;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgentConversationDetail {
    private String id;
    private String title;
    @Builder.Default
    private List<AgentMessage> history = new ArrayList<>();
    @Builder.Default
    private List<String> enabledTools = new ArrayList<>();
}
