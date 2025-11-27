package yunxun.ai.canary.backend.model.entity.toolstate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "user_tools")
public class UserToolDoc {

    @Id
    private String id;

    private Long userId; // null or 0 for global defaults

    @Builder.Default
    private List<ToolState> tools = List.of();

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ToolState {
        private String name;
        private boolean enabled;
        private String displayName;
        private String description;
    }
}
