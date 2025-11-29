package yunxun.ai.canary.backend.chat.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import yunxun.ai.canary.backend.analytics.model.dto.ChartSpecDto;
import yunxun.ai.canary.backend.graph.model.dto.GraphDataDto;

import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageDto {
    private String id;
    private String role; // "user" | "assistant" | "system"
    private String content;
    private Instant createdAt;
    private GraphDataDto graph;
    private List<ChartSpecDto> charts;
}
