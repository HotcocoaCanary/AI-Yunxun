package yunxun.ai.canary.backend.model.dto.graph;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GraphNodeInput {
    private String id;
    private String label;
    private Map<String, Object> properties;
}
