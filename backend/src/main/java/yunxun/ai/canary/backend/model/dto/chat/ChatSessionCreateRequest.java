package yunxun.ai.canary.backend.model.dto.chat;

import lombok.Data;

@Data
public class ChatSessionCreateRequest {
    private String type; // group | session
    private String title;
    private Long parentId;
}
