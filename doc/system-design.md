# AI-Yunxun 总体系统设计

## 1. 概述

本项目由三个核心模块组成�?*Neo4j MCP Server**�?*ECharts MCP Server**�?*Next.js 客户�?*。两�?MCP 服务器以 Java 实现，通过 STDIO �?MCP 客户端通信；客户端仅保�?Next.js 单层（前�?+ API Route），通过 SSE 接收 MCP 相关通知做效果展示，开发重心放�?MCP 服务器的完善与扩展�?

---

## 2. 三个模块的职责与配合

### 2.1 模块职责

| 模块 | 职责 |
|------|------|
| **Neo4j MCP Server** | 提供 Neo4j 图数据库�?schema 查询�?Cypher 读写能力（get-neo4j-schema、read-neo4j-cypher、write-neo4j-cypher），供大模型在对话中调用�?|

### 2.2 配合关系

```mermaid
flowchart LR
  subgraph nextjs [Next.js 客户端]
    UI[页面]
    API[API Route]
  end
  subgraph mcp_servers [MCP 服务�?Java]
    Neo4j[Neo4j MCP]
    EChart[ECharts MCP]
  end
  User[用户] --> UI
  UI -->|POST /api/chat + SSE| API
  API -->|LLM API| LLM[智谱等]
  API -->|STDIO| Neo4j
  API -->|STDIO| EChart
  API -->|SSE: text/chart/status| UI
  Neo4j -->|loggingNotification| API
  EChart -->|loggingNotification + option JSON| API
  Neo4j --> Neo4jDB[(Neo4j DB)]
```

- **用户** 在前端输入消�?�?**Next.js 页面** POST `/api/chat` 并建�?SSE 连接�?
- **API Route** 调用 **LLM** 流式对话；若 LLM 返回 **tool_calls**，则通过 **Node MCP SDK** �?**STDIO** 调用 **Neo4j MCP** �?**ECharts MCP**�?
- **Neo4j MCP** 执行 Cypher 并返回结果；**ECharts MCP** 返回 ECharts option JSON。API Route 将工具结果喂�?LLM 或直接通过 SSE �?`chart` 事件推给前端�?
- **MCP 服务�?* 在执行过程中通过 MCP **loggingNotification** 发送日志；API Route 可收集并�?SSE �?`tool_log` 等推给前端，用于“调用中/成功/失败”与日志面板�?

### 2.3 数据流小�?

1. 用户输入 �?前端 POST `/api/chat`（conversationId + message），建立 SSE�?
2. API Route：LLM 流式对话 �?若返�?tool_calls �?调用 Neo4j/ECharts MCP（STDIO）→ 将结果喂�?LLM 或推 chart�?
3. 前端：SSE �?`text` 追加到消息，`chart` 交给 ECharts 渲染，`status`/`tool_log` 更新状态与日志�?
4. 不依�?Java 客户端后端，不引�?WebSocket；MCP 相关通知均通过 SSE 传递，以效果展示为主�?

---

## 3. 项目其他依赖�?

### 3.1 开发与运行环境

| 依赖�?| 版本/说明 |
|--------|------------|
| **Java** | 17（MCP 服务器为 Spring Boot 3.x，需 JDK 17+�?|
| **Node.js** | v22（Next.js 客户端开发与运行；建�?LTS �?v22�?|
| **Neo4j** | 需单独部署（如 Docker），�?Neo4j MCP Server 连接；若使用 get-neo4j-schema，需安装 **APOC** 插件 |
| **Maven** | 用于构建两个 Java MCP 服务器（mcp/server/neo4j、mcp/server/echart�?|

### 3.2 可�?外部依赖

| 依赖�?| 说明 |
|--------|------|
| **智谱 / OpenAI 兼容 API** | Next.js API Route 调用 LLM 时使用；API Key 等通过环境变量配置 |

### 3.3 项目目录与文档索�?

- **mcp/server/neo4j**：Neo4j MCP Server，详�?[neo4j-mcp-server-architecture.md](neo4j-mcp-server-architecture.md)�?
- **mcp/server/echart**：ECharts MCP Server，详�?[echart-mcp-server-architecture.md](echart-mcp-server-architecture.md)�?
- **mcp/client-next**（或新建 Next.js 项目）：Next.js 客户端，详见 [nextjs-client-architecture.md](nextjs-client-architecture.md)�?
- **doc/system-design.md**：本文档，总体系统设计与依赖说明�?

---

## 4. 部署与运行注�?

- **Neo4j MCP Server**：需先启�?Neo4j 实例并配�?URI/用户�?密码；若使用 get-neo4j-schema，需安装 APOC�?
- **ECharts MCP Server**：无外部数据库依赖，�?MCP STDIO 模式运行，由 Next.js API Route 启动子进程�?
- **Next.js 客户�?*：API Route 通过 MCP SDK �?STDIO 启动 Java MCP 进程，需确保运行环境允许 **子进�?*（如自托�?Node）；若部署到 Vercel 等无子进程环境，需改为远程 MCP 或仅在本地运行�?
- **Java 客户�?*（mcp/client，Spring Boot + Spring AI）：新方案下不再使用，可保留作参考或移除�?
