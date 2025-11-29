package yunxun.ai.canary.backend.data.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DataResourceDto {
    private String id;
    private Long ownerId;
    private String type;
    private String title;
    private String visibility; // private | public
    private List<Long> allowedNodes;
    private Map<String, Object> meta;
    private Instant createdAt;
    private Instant updatedAt;
}
