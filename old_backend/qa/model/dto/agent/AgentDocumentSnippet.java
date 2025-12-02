package yunxun.ai.canary.backend.qa.model.dto.agent;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgentDocumentSnippet {
    private String documentId;
    private String title;
    private String source;
    private String url;
    private String summary;
}
