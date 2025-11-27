package yunxun.ai.canary.backend.model.dto.tools;

import lombok.Data;

@Data
public class McpToolToggleRequest {
    private String name;
    private boolean enabled;
}
