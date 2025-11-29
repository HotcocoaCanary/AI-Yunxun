package yunxun.ai.canary.backend.mcp.model.dto;

import lombok.Data;

import java.util.List;

@Data
public class McpChatRequest {
    private Long userId;
    private Long sessionId;
    private List<SimpleMessage> messages;
    private Boolean graphRequested;
    private Boolean chartRequested;
    private Boolean stream;

    @Data
    public static class SimpleMessage {
        private String role;
        private String content;
    }
}
