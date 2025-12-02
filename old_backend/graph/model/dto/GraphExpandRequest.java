package yunxun.ai.canary.backend.graph.model.dto;

import lombok.Data;

@Data
public class GraphExpandRequest {
    private String nodeId;
    private int limit = 20;
}
