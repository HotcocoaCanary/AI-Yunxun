package yunxun.ai.canary.backend.qa.model.dto.agent;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgentChartPayload {
    private String chartType;
    private String title;
    private Map<String, Object> options; // 直接适配前端 ECharts
}
