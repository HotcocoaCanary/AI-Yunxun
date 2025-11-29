package yunxun.ai.canary.backend.data.model.dto;

import lombok.Data;

import java.util.List;

@Data
public class DataGrantRequest {
    private List<Long> allowedNodes;
    private String visibility; // optional override
}
