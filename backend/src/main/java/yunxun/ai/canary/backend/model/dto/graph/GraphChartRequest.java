package yunxun.ai.canary.backend.model.dto.graph;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GraphChartRequest {
    private String chartType;
    private String cypher;
    private String xField;
    private String yField;
    private String seriesField;
    private String title;
}
