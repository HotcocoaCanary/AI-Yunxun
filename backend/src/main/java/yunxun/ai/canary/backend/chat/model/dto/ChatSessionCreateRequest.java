package yunxun.ai.canary.backend.chat.model.dto;

import lombok.Data;

@Data
public class ChatSessionCreateRequest {
    private String type; // group | session
    private String title;
    private Long parentId;
}
