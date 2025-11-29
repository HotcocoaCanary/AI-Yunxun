package yunxun.ai.canary.backend.graph.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GraphRelationshipInput {
    private String id;
    private String label;
    private String startLabel;
    private String startId;
    private String endLabel;
    private String endId;
    private Map<String, Object> properties;
}
