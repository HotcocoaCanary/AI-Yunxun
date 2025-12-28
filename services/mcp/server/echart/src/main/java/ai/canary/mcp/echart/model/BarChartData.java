package ai.canary.mcp.echart.model;

import lombok.Data;
import java.util.List;

@Data
public class BarChartData {
    private String title;
    private List<String> categories;
    private List<Number> values;
    private String valueName;
}

