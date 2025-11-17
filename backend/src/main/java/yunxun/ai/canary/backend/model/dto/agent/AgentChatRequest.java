package yunxun.ai.canary.backend.model.dto.agent;

import jakarta.validation.constraints.NotBlank;
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
public class AgentChatRequest {

    private String conversationId;

    @NotBlank(message = "message 不能为空")
    private String message;

    @Builder.Default
    private List<AgentMessage> history = new ArrayList<>();

    @Builder.Default
    private List<String> enabledTools = new ArrayList<>();

    @Builder.Default
    private AgentPreference preference = new AgentPreference();
}
