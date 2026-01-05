package mcp.canary.client.controller;

import mcp.canary.client.model.McpServerDefinition;
import mcp.canary.client.model.McpServersConfig;
import mcp.canary.client.service.MCPClientService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * MCP server management endpoints (Claude Desktop mcpServers format).
 */
@RestController
@RequestMapping("/api/mcp")
public class MCPController {

    private final MCPClientService mcpClientService;

    public MCPController(MCPClientService mcpClientService) {
        this.mcpClientService = mcpClientService;
    }

    @GetMapping("/servers")
    public McpServersConfig listServers() {
        return new McpServersConfig(mcpClientService.listServers());
    }

    @PutMapping("/servers")
    public McpServersConfig replaceServers(@RequestBody McpServersConfig config) {
        return mcpClientService.replaceServers(config);
    }

    @PostMapping("/servers/{name}")
    @ResponseStatus(HttpStatus.CREATED)
    public McpServerDefinition addServer(@PathVariable String name,
                                         @RequestBody McpServerDefinition definition) {
        return mcpClientService.addServer(name, definition);
    }

    @DeleteMapping("/servers/{name}")
    public Map<String, Object> deleteServer(@PathVariable String name) {
        boolean ok = mcpClientService.deleteServer(name);
        return Map.of("deleted", ok, "name", name);
    }
}
