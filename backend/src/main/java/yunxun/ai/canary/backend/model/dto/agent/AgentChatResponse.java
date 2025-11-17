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
public class AgentChatResponse {

    private String conversationId;

    private String answer;

    @Builder.Default
    private List<AgentPlanStep> plan = new ArrayList<>();

    @Builder.Default
    private List<AgentChartPayload> charts = new ArrayList<>();

    private AgentGraphPayload graph;

    @Builder.Default
    private List<AgentDocumentSnippet> documents = new ArrayList<>();

    @Builder.Default
    private List<String> usedTools = new ArrayList<>();
}
