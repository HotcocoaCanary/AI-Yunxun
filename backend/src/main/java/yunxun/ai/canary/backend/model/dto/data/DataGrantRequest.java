package yunxun.ai.canary.backend.model.dto.data;

import lombok.Data;

import java.util.List;

@Data
public class DataGrantRequest {
    private List<Long> allowedNodes;
    private String visibility; // optional override
}
