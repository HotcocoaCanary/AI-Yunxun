package yunxun.ai.canary.backend.model.dto.intelligence;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IntelligentQueryPayload {
    private String analysisReport;
    private Map<String, Object> chartData;
    private Map<String, Object> visualizationData;
}
