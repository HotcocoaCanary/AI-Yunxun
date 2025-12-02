package yunxun.ai.canary.backend.data.model.dto;

import lombok.Data;

@Data
public class DataResourceQuery {
    private String visibility; // private|public|all
    private String type;
    private Long sessionId; // optional: filter by session authorization
}
