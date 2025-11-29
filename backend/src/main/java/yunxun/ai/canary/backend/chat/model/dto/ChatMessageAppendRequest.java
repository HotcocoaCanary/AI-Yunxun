package yunxun.ai.canary.backend.chat.model.dto;

import lombok.Data;

@Data
public class ChatMessageAppendRequest {
    private Long sessionId;
    private String role;
    private String content;
}
