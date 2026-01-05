package mcp.canary.client.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.LinkedHashMap;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public record McpServersConfig(Map<String, McpServerDefinition> mcpServers) {
    public McpServersConfig {
        if (mcpServers == null) {
            mcpServers = new LinkedHashMap<>();
        } else {
            mcpServers = new LinkedHashMap<>(mcpServers);
        }
    }
}
