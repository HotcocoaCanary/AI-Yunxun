package yunxun.ai.canary.backend.model.dto.graph;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GraphEdgeDto {
    private String id;
    private String source;
    private String target;
    private String type;

    @Builder.Default
    private Map<String, Object> properties = new HashMap<>();
}
