package yunxun.ai.canary.backend.auth.model.dto;

import lombok.Data;

@Data
public class LoginRequest {
    private String email;
    private String password;
}
