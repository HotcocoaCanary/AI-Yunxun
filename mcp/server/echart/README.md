# ECharts MCP Server

这是一个基于 MCP Java SDK 的 ECharts 工具服务，使用 Spring Boot 启动，通过 SSE 暴露一个生成图表 option 的工具。

## 项目结构

- `src/main/java/mcp/canary/echart/EchartApplication.java`
  - 启动入口
- `src/main/java/mcp/canary/echart/module`
  - `EChartModule`：统一的 `toEChartNode()` 接口
  - `graph`：关系图相关结构
    - `GraphOption`：总的 option，包含 title、tooltip、series
    - `GraphSeries`：`graph` 类型的 series，负责 layout、nodes、edges、categories
    - `GraphNode` / `GraphEdge` / `GraphCategory` / `GraphTitle`
- `src/main/java/mcp/canary/echart/tool/GraphEChartMCPTool.java`
  - MCP 工具入口：`generate_graph_chart`
- `src/main/resources/application.yml`
  - MCP server 配置

## MCP 工具说明

- `generate_graph_chart`
  - 入参包含 `title`、`layout`、`nodes`、`edges`、`categories`
  - `layout` 只支持 `force` 和 `circular`，默认 `force`
  - `categories` 为空时，会根据 `nodes[].categoryName` 自动生成分类
  - 输出是完整的 ECharts option JSON

## MCP SDK 的具体用法（代码里用到的点）

- 工具方法使用 `@McpTool` 和 `@McpToolParam` 注解暴露给 MCP client。
- `McpSyncServerExchange` 用来向 MCP client 发送日志。
- 日志通过 `LoggingMessageNotification` 和 `LoggingLevel` 发送。
- `application.yml` 里配置 `spring.ai.mcp.server.protocol: SSE`，走 SSE 通信。

## 配置

`src/main/resources/application.yml` 里包含这几项：

- `server.port`：默认 8083
- `spring.ai.mcp.server.protocol`：SSE
