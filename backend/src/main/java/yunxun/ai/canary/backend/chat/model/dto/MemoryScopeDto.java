package yunxun.ai.canary.backend.chat.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MemoryScopeDto {
    private String sessionId;
    private List<String> memorySessionIds;
}
