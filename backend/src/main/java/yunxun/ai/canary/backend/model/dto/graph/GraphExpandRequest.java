package yunxun.ai.canary.backend.model.dto.graph;

import lombok.Data;

@Data
public class GraphExpandRequest {
    private String nodeId;
    private int limit = 20;
}
