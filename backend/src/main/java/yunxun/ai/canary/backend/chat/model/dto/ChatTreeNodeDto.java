package yunxun.ai.canary.backend.chat.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatTreeNodeDto {
    private String id;
    private String type; // "group" | "session"
    private String name;
    private String parentId;

    @Builder.Default
    private List<ChatTreeNodeDto> children = new ArrayList<>();
    private Instant lastMessageAt;
}
