package yunxun.ai.canary.backend.data.model.dto;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class DataResourceCreateRequest {
    private String type;
    private String title;
    private String visibility; // private | public
    private List<Long> allowedNodes;
    private Map<String, Object> meta;
}
