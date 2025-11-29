package yunxun.ai.canary.backend.auth.model.dto;

import lombok.Data;

@Data
public class RegisterRequest {
    private String email;
    private String password;
    private String displayName;
}
