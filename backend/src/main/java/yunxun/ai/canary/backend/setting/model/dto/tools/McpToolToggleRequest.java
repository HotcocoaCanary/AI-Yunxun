package yunxun.ai.canary.backend.setting.model.dto.tools;

import lombok.Data;

@Data
public class McpToolToggleRequest {
    private String name;
    private boolean enabled;
}
