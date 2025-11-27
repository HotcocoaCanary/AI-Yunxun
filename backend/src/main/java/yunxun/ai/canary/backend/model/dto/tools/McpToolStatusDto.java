package yunxun.ai.canary.backend.model.dto.tools;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class McpToolStatusDto {
    private String name;
    private String displayName;
    private boolean enabled;
    private String description;

    @Builder.Default
    private List<String> tags = new ArrayList<>();
}
