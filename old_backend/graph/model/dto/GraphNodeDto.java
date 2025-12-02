package yunxun.ai.canary.backend.graph.model.dto;

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
public class GraphNodeDto {
    private String id;
    private String label;
    private String type;

    @Builder.Default
    private Map<String, Object> properties = new HashMap<>();
}
