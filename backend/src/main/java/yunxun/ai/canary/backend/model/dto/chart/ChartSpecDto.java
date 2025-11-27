package yunxun.ai.canary.backend.model.dto.chart;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChartSpecDto {
    private String id;
    private String title;
    private String type; // bar | line | pie | table
    private String xField;
    private String yField;
    private String seriesField;

    @Builder.Default
    private List<Map<String, Object>> data = new ArrayList<>();

    @Builder.Default
    private Map<String, Object> extraConfig = new HashMap<>();
}
