package yunxun.ai.canary.backend.chat.model.dto;

import lombok.Data;

@Data
public class ChatNodeCreateRequest {
    private String type; // group | session
    private String name;
    private String parentId;
}
