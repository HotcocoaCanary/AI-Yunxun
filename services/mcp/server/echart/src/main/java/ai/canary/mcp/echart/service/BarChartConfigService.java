package ai.canary.mcp.echart.service;

import ai.canary.mcp.echart.model.BarChartData;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class BarChartConfigService {

    private final ObjectMapper objectMapper;

    public String generateConfig(BarChartData data) throws Exception {
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
        xAxis.put("data", data.getCategories());
        config.put("xAxis", xAxis);
        
        // Y Axis
        Map<String, Object> yAxis = new HashMap<>();
        yAxis.put("type", "value");
        if (data.getValueName() != null && !data.getValueName().isEmpty()) {
            Map<String, Object> yAxisName = new HashMap<>();
            yAxisName.put("text", data.getValueName());
            yAxis.put("name", yAxisName.get("text"));
        }
        config.put("yAxis", yAxis);
        
        // Series
        Map<String, Object> series = new HashMap<>();
        series.put("type", "bar");
        series.put("data", data.getValues());
        config.put("series", series);
        
        // Tooltip
        Map<String, Object> tooltip = new HashMap<>();
        tooltip.put("trigger", "axis");
        config.put("tooltip", tooltip);
        
        return objectMapper.writeValueAsString(config);
    }

    private void validateData(BarChartData data) {
        if (data == null) {
            throw new IllegalArgumentException("BarChartData cannot be null");
        }
        if (data.getCategories() == null || data.getCategories().isEmpty()) {
            throw new IllegalArgumentException("Categories cannot be null or empty");
        }
        if (data.getValues() == null || data.getValues().isEmpty()) {
            throw new IllegalArgumentException("Values cannot be null or empty");
        }
        if (data.getCategories().size() != data.getValues().size()) {
            throw new IllegalArgumentException("Categories size must match values size");
        }
    }
}

