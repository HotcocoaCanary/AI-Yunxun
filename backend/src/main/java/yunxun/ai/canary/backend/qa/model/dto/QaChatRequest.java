package yunxun.ai.canary.backend.qa.model.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class QaChatRequest {

    private Long userId;

    private Long sessionId;

    @NotBlank
    private String question;
}

