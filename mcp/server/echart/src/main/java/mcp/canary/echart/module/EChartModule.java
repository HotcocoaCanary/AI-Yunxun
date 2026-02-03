package mcp.canary.echart.module;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public interface EChartModule {

    ObjectMapper MAPPER = new ObjectMapper();

    JsonNode toEChartNode();
}
