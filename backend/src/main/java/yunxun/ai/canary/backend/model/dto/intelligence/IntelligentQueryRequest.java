package yunxun.ai.canary.backend.model.dto.intelligence;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class IntelligentQueryRequest {

    @NotBlank(message = "query must not be blank")
    private String query;

    /**
     * Optional hint used by the frontend to distinguish between graph / chart queries.
     */
    private String type;
}
