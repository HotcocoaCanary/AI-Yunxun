package yunxun.ai.canary.backend.model.dto.intelligence;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class IntelligentQueryResponse {
    private boolean success;
    private String message;
    private IntelligentQueryPayload data;
}
