package yunxun.ai.canary.backend.model.dto.data;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class DataResourceUpdateRequest {
    private String title;
    private String visibility;
    private List<Long> allowedNodes;
    private Map<String, Object> meta;
}
