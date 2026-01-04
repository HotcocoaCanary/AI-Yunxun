package mcp.canary.client.controller;

import mcp.canary.client.model.MCPServerConfig;
import mcp.canary.client.service.MCPClientService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * MCP 服务器管理接口：动态添加/删除服务器配置，并同步维护 MCP client 连接。
 */
@RestController
@RequestMapping("/api/mcp")
public class MCPController {

    private final MCPClientService mcpClientService;

    public MCPController(MCPClientService mcpClientService) {
        this.mcpClientService = mcpClientService;
    }

    @GetMapping("/servers")
    public List<MCPServerConfig> listServers() {
        return mcpClientService.listServers();
    }

    @PostMapping("/servers")
    @ResponseStatus(HttpStatus.CREATED)
    public MCPServerConfig addServer(@RequestBody MCPServerConfig server) {
        return mcpClientService.addServer(server);
    }

    @DeleteMapping("/servers/{id}")
    public Map<String, Object> deleteServer(@PathVariable String id) {
        boolean ok = mcpClientService.deleteServer(id);
        return Map.of("deleted", ok, "id", id);
    }
}





