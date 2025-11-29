package yunxun.ai.canary.backend.graph.model.dto;

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
public class GraphDataDto {
    @Builder.Default
    private List<GraphNodeDto> nodes = new ArrayList<>();

    @Builder.Default
    private List<GraphEdgeDto> edges = new ArrayList<>();
}
