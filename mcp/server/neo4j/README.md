# Neo4j MCP Server

这是一个基于 MCP Java SDK 的 Neo4j 工具服务，使用 Spring Boot 启动，通过 SSE 暴露 MCP 工具给客户端调用。

## 项目结构

- `src/main/java/mcp/canary/neo4j/Neo4jApplication.java`
  - 启动入口，禁用了 Spring Boot 的 Neo4j 自动配置，改用自定义连接
- `src/main/java/mcp/canary/neo4j/db/Neo4jConnection.java`
  - 基于 Neo4j Java Driver 的连接管理，启动时校验连通性
- `src/main/java/mcp/canary/neo4j/service/Neo4jService.java`
  - 读写 Cypher 的服务层
  - 读操作后会触发基于 `name` 的去重逻辑（APOC 合并）
- `src/main/java/mcp/canary/neo4j/tool/Neo4jMCPTool.java`
  - MCP 工具入口，包含三个工具：`get-neo4j-schema`、`read-neo4j-cypher`、`write-neo4j-cypher`
- `src/main/resources/application.yml`
  - MCP server 配置 + Neo4j 连接配置

## MCP 工具说明

- `get-neo4j-schema`
  - 调用 `apoc.meta.data()` 获取 label、属性等信息
  - 依赖 APOC 插件
- `read-neo4j-cypher`
  - 只允许读查询（包含 `CREATE/MERGE/DELETE/SET` 会直接报错）
  - 读完会执行 `deduplicateNodesByName()`，用 `apoc.refactor.mergeNodes` 合并同名节点
- `write-neo4j-cypher`
  - 执行写操作，返回统计结果（节点数、关系数、属性数等）

## MCP SDK 的具体用法（代码里用到的点）

- 工具方法使用 `@McpTool` 和 `@McpToolParam` 注解暴露给 MCP client。
- `McpSyncServerExchange` 用来向 MCP client 发送日志。
- 日志通过 `LoggingMessageNotification` 和 `LoggingLevel` 发送。
- `application.yml` 里配置 `spring.ai.mcp.server.protocol: SSE`，走 SSE 通信。

## 配置

`src/main/resources/application.yml` 里包含这几项：

- `server.port`：默认 8082
- `neo4j.uri`：如 `neo4j://localhost:7687`
- `neo4j.username`
- `neo4j.password`
- `neo4j.database`

注意：`get-neo4j-schema` 和去重逻辑依赖 APOC 插件。
