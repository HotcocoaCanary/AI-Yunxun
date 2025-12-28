package ai.canary.mcp.echart.tool;

import ai.canary.mcp.echart.model.RelationGraphData;
import ai.canary.mcp.echart.service.RelationGraphConfigService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springaicommunity.mcp.annotation.McpTool;
import org.springaicommunity.mcp.annotation.McpToolParam;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GenerateRelationGraphConfigTool {

    private final RelationGraphConfigService relationGraphConfigService;
    private final ObjectMapper objectMapper;

    @McpTool(name = "generate_relation_graph_config", description = "Generate ECharts relation graph configuration JSON from normalized data. Input should be JSON with title, nodes (array of {id, name, category?, value?, properties?}), and links (array of {source, target, name?, value?, properties?}).")
    public String generateRelationGraphConfig(
            @McpToolParam(description = "JSON string containing relation graph data: {title: string, nodes: [{id: string, name: string, category?: string, value?: number, properties?: object}], links: [{source: string, target: string, name?: string, value?: number, properties?: object}]}", required = true) String data) {
        try {
            RelationGraphData chartData = objectMapper.readValue(data, RelationGraphData.class);
            return relationGraphConfigService.generateConfig(chartData);
        } catch (Exception e) {
            return "{\"error\": \"Failed to generate relation graph config: " + e.getMessage() + "\"}";
        }
    }
}

