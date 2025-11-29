package yunxun.ai.canary.backend.qa.model.dto.agent;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AgentMessage {
    private String role; // user / assistant / tool
    private String content;
    private Instant timestamp;
}
