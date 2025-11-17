package yunxun.ai.canary.backend.model.dto.graph;

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
public class GraphIngestionRequest {

    @Builder.Default
    private List<GraphNodeInput> nodes = new ArrayList<>();

    @Builder.Default
    private List<GraphRelationshipInput> relationships = new ArrayList<>();
}
