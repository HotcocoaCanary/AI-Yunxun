package mcp.canary.client.model;

/**
 * MCP 服务器配置模型。
 * <p>
 * 说明：为了简化 REST 接口，这里将 {@code id} 约定为 {@code name}。
 */
public record MCPServerConfig(
        String id,
        String name,
        String url,
        String protocol
) {
    public MCPServerConfig {
        if (id == null || id.isBlank()) {
            id = name;
        }
    }
}



