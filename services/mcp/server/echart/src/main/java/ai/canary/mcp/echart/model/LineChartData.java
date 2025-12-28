package ai.canary.mcp.echart.model;

import lombok.Data;
import java.util.List;

@Data
public class LineChartData {
    private String title;
    private List<String> xAxisData;
    private List<SeriesData> series;
    
    @Data
    public static class SeriesData {
        private String name;
        private List<Number> data;
    }
}

