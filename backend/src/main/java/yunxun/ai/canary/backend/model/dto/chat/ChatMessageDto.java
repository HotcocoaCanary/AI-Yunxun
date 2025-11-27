package yunxun.ai.canary.backend.model.dto.chat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import yunxun.ai.canary.backend.model.dto.chart.ChartSpecDto;
import yunxun.ai.canary.backend.model.dto.graph.GraphDataDto;

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
