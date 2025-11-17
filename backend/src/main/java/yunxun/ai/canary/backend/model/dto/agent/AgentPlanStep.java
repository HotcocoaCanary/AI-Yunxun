package yunxun.ai.canary.backend.model.dto.agent;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgentPlanStep {
    private String id;
    private String title;
    private String tool;
    private String objective;
    private String status; // pending / running / completed
}
