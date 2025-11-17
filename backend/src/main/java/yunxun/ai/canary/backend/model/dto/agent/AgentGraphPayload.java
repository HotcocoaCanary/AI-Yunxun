package yunxun.ai.canary.backend.model.dto.agent;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgentGraphPayload {
    private List<Map<String, Object>> nodes;
    private List<Map<String, Object>> links;
}
