package yunxun.ai.canary.backend.chat.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "chat_messages")
public class ChatMessageDoc {

    @Id
    private String id;

    private Long sessionId;

    private String role;

    private Content content;

    private List<Double> embedding;

    private Long tokens;

    @CreatedDate
    private Instant createdAt;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Content {
        private String markdown;
        private Map<String, Object> graph;
        private Map<String, Object> table;
        private Map<String, Object> chart;
        private List<Map<String, Object>> attachments;
    }
}
