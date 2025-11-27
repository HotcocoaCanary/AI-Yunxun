package yunxun.ai.canary.backend.model.dto.chat;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AgentQueryPayloadDto {
    private String question;
    private MemoryScopeDto memoryScope;
}
