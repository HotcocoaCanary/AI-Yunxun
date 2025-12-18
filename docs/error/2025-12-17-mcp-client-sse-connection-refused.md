# MCP Client 启动时报错：Connection refused / Timeout

## 现象

启动应用时出现：

- `Fatal SSE error, not retrying: Connection refused: ... localhost/127.0.0.1:8080`
- `BeanCreationException: ... mcpSyncClients ... TimeoutException ... within 20000ms`
- 请求地址类似：`GET http://localhost:8080/sse/sse`

## 原因

1) `spring.ai.mcp.client.sse.connections.local-mcp.url` 配置写成了 `http://localhost:${server.port}/sse`，同时又配置了 `sse-endpoint: /sse`，最终拼成了错误地址 `/sse/sse`。  
2) Spring AI 的 MCP Client AutoConfiguration 默认会在 **Spring 容器初始化阶段**就对每个 SSE 连接执行 `client.initialize()`；当目标端口此时还没监听（例如连接的是本应用自身端口，WebServer 还没启动）或目标 MCP Server 未启动，就会 `Connection refused`，进而触发初始化超时并导致应用启动失败。  
3) 当 `spring.ai.mcp.client.toolcallback.enabled=true` 时，MCP Server 在启动创建 `mcpSyncServer` 过程中会调用 `client.listTools()` 进行“工具发现”，这会进一步触发对上游 MCP Server 的连接；如果上游不可用/端口未监听，同样会导致启动失败。

## 解决方案

已在 `src/main/resources/application.yml` 做调整：

- 后续如需配置 SSE 连接：`url` 只写“服务基地址”（不要带 `/sse`），`sse-endpoint` 再单独配置
- （保留）关闭启动阶段的预初始化与 ToolCallback 自动注册：
  - `spring.ai.mcp.client.initialized: false`
  - `spring.ai.mcp.client.toolcallback.enabled: false`
- 如需“启动后连接自身 MCP Server”：建议关闭 `spring.ai.mcp.client.enabled`，改用 `ApplicationReadyEvent` 创建/初始化本地 client（配置项 `yunxun.mcp.local-client.*`）

如需连接外部 MCP Server，请将 `url` 改为外部服务地址，并确保服务先启动。
