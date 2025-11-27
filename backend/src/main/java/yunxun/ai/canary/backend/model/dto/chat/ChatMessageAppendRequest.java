package yunxun.ai.canary.backend.model.dto.chat;

import lombok.Data;

@Data
public class ChatMessageAppendRequest {
    private Long sessionId;
    private String role;
    private String content;
}
