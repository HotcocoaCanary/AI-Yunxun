package ai.canary.mcp.echart.service;

import ai.canary.mcp.echart.model.LineChartData;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LineChartConfigService {

    private final ObjectMapper objectMapper;

    public String generateConfig(LineChartData data) throws Exception {
        validateData(data);
        
        Map<String, Object> config = new HashMap<>();
        
        // Title
        if (data.getTitle() != null && !data.getTitle().isEmpty()) {
            Map<String, Object> title = new HashMap<>();
            title.put("text", data.getTitle());
            title.put("left", "center");
            config.put("title", title);
        }
        
        // X Axis
        Map<String, Object> xAxis = new HashMap<>();
        xAxis.put("type", "category");
        xAxis.put("data", data.getXAxisData());
        config.put("xAxis", xAxis);
        
        // Y Axis
        Map<String, Object> yAxis = new HashMap<>();
        yAxis.put("type", "value");
        config.put("yAxis", yAxis);
        
        // Series
        List<Map<String, Object>> series = data.getSeries().stream()
                .map(s -> {
                    Map<String, Object> seriesMap = new HashMap<>();
                    seriesMap.put("name", s.getName());
                    seriesMap.put("type", "line");
                    seriesMap.put("data", s.getData());
                    return seriesMap;
                })
                .collect(Collectors.toList());
        config.put("series", series);
        
        // Tooltip
        Map<String, Object> tooltip = new HashMap<>();
        tooltip.put("trigger", "axis");
        config.put("tooltip", tooltip);
        
        // Legend
        if (data.getSeries().size() > 1) {
            Map<String, Object> legend = new HashMap<>();
            legend.put("data", data.getSeries().stream()
                    .map(LineChartData.SeriesData::getName)
                    .collect(Collectors.toList()));
            legend.put("top", "10%");
            config.put("legend", legend);
        }
        
        return objectMapper.writeValueAsString(config);
    }

    private void validateData(LineChartData data) {
        if (data == null) {
            throw new IllegalArgumentException("LineChartData cannot be null");
        }
        if (data.getXAxisData() == null || data.getXAxisData().isEmpty()) {
            throw new IllegalArgumentException("X axis data cannot be null or empty");
        }
        if (data.getSeries() == null || data.getSeries().isEmpty()) {
            throw new IllegalArgumentException("Series data cannot be null or empty");
        }
        for (LineChartData.SeriesData series : data.getSeries()) {
            if (series.getData() == null || series.getData().isEmpty()) {
                throw new IllegalArgumentException("Series data values cannot be null or empty");
            }
            if (series.getData().size() != data.getXAxisData().size()) {
                throw new IllegalArgumentException("Series data size must match xAxis data size");
            }
        }
    }
}

