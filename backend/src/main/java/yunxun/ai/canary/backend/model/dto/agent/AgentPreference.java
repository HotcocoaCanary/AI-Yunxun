package yunxun.ai.canary.backend.model.dto.agent;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgentPreference {
    private String answerStyle;
    private String language;
    private List<String> preferredChartTypes;
}
