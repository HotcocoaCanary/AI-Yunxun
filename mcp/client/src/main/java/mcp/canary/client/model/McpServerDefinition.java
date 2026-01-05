package mcp.canary.client.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public record McpServerDefinition(
        String command,
        List<String> args,
        Map<String, String> env
) {
    public McpServerDefinition {
        if (args == null) {
            args = List.of();
        }
        if (env == null) {
            env = Map.of();
        }
    }
}
