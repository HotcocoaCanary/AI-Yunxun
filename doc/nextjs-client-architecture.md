# Next.js 前端系统设计文档

## 1. 定位与职责

MCP 客户端**不再使用 Java 后端**，仅保留 **Next.js 单层**（前端页面 + API Route），通过 **SSE 接收 MCP 相关通知**做效果展示。逻辑尽量简单，核心放在两个 MCP 服务器（Neo4j、ECharts）的开发。

- **API Route**：接收对话请求、调用 LLM（智谱或 OpenAI 兼容）、通过 Node/TS MCP SDK 连接 Neo4j/ECharts MCP 服务器（STDIO），将流式回复与工具结果通过 **SSE** 推送给前端；并暴露**图谱工具直接调用接口**，供前端不经过大模型即可“捏造请求”展示图谱。
- **页面**：对话输入、消息列表、工具状态/日志、**关系图 / GL 关系图**展示。展示支持两种形态：**option**（前端用 ECharts + echarts-gl 渲染）或 **image**（PNG/SVG base64，用 `<img>` 展示，无需 ECharts），便于返回直接嵌入大模型回复或降低前端开发量。

---

## 2. 页面布局

### 2.1 整体结构

```
+----------------------------------------------------------+
| Header（标题、连接/状态）                                    |
+------------------------+-----------------------------------+
| 主区                    | 侧区                              |
| 对话列表                | MCP 工具状态（当前调用中/成功/失败）  |
| +------------------+   | 简要日志（最近几条）                 |
| | 用户 / 助手消息   |   | （可选）MCP 服务器列表（可写死 env）  |
| +------------------+   +-----------------------------------+
| 输入框 + 发送           | 图表区                            |
|                        | ECharts 容器（关系图 / GL 关系图）   |
+------------------------+-----------------------------------+
```

- **主区**：对话列表（用户消息、助手流式文本）、底部输入框与发送按钮。
- **侧区**：上方为工具状态与简要日志；下方为图表区，用于展示 MCP 返回的 ECharts option（graph 或 graphGL）。
- 侧区可收缩或固定宽度，以“效果展示”为主，不做完整 MCP 导入/编辑/删除管理。

### 2.2 组件划分建议

| 组件 | 职责 |
|------|------|
| **Layout** | 整体布局：Header + 主区 + 侧区，可含简单状态（如“已连接/未连接”）。 |
| **ChatPanel** | 主区：消息列表 + 输入框。维护 `messages` 状态，接收 SSE 的 `text` 事件追加到当前助手消息。 |
| **MessageList** | 渲染消息列表，区分 user/assistant，支持流式文本展示。 |
| **ChatInput** | 输入框 + 发送按钮，提交时 POST `/api/chat` 并建立 SSE 连接。 |
| **SidePanel** | 侧区容器：工具状态 + 日志 + 图表区。 |
| **ToolStatus** | 展示当前工具调用状态（调用中/成功/失败），数据来自 SSE 的 `tool_log` 或 `tool_status`（若实现）。 |
| **ToolLogs** | 简要日志列表，最近 N 条，来自 SSE 的 `tool_log` 或从 MCP 日志解析。 |
| **ChartPanel** | 图表区：接收 SSE 的 `chart` 事件。若 payload 为 **option**（JSON），用 ECharts + echarts-gl 根据 `series[].type` 渲染；若为 **image**（如 base64 PNG/SVG），用 `<img src="data:image/...;base64,...">` 展示，无需 ECharts。 |

以上组件可按 Next.js App Router（如 `app/page.tsx`、`app/components/...`）或 Pages Router 组织。

---

## 3. 接入 MCP 的逻辑（API Route）

### 3.1 流程

1. 前端 POST `/api/chat`，body：`{ conversationId, message }`，并建立 **SSE** 连接（同一请求流式读 body 或通过 GET/SSE 端点均可，以实现简单为准）。
2. API Route 内：
   - 调用 **LLM API**（智谱或 OpenAI 兼容）流式对话。
   - 若 LLM 返回 **tool_calls**（如需要查 Neo4j 或画图），则通过 **Node/TS MCP SDK** 调用对应 MCP 服务器：
     - Neo4j MCP、ECharts MCP 以 **STDIO** 子进程方式启动（命令与参数来自 **环境变量**或简单 JSON 配置）。
     - 将工具结果（如 ECharts option JSON）再喂回 LLM 或直接通过 SSE 推送给前端（如 `chart` 事件）。
   - 将流式文本通过 SSE `text` 事件推送；状态通过 `status`（思考中/完成）；可选 `tool_log`/`tool_status` 推送工具名与状态。

### 3.2 配置

- **MCP 服务器**：Neo4j MCP、ECharts MCP 的启动命令（如 `java -jar neo4j-mcp.jar`、`java -jar echart-mcp.jar`）及参数写在 **环境变量**（如 `NEO4J_MCP_CMD`、`ECHART_MCP_CMD`）或项目内一份简单 JSON 配置中。
- API Route 启动时按配置 spawn 子进程，建立 MCP 客户端连接；不在页面上做“导入/编辑/删除”等完整管理，以降低复杂度。

### 3.3 技术选型

- **MCP 客户端**：使用 Node/TS 生态的 MCP SDK（如 `@modelcontextprotocol/sdk` 或 Spring AI 文档中提到的等效库），通过 STDIO 与 Java MCP 进程通信。
- **LLM**：智谱或任意 OpenAI 兼容 API，在 API Route 内通过 fetch/流式读即可。

### 3.4 前端直接调用图谱工具（不经过大模型）

为支持**前端不通过大模型、自己捏造请求展示图谱**，Next.js 需提供**图谱工具直接调用接口**，与 ECharts MCP 工具的输入输出一致，便于前端直接请求并展示。

**建议接口**：

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/tools/echart/graph` | 关系图：body 与 MCP 工具 generate_graph_chart 输入一致（title、data、layout、width、height、theme、**outputType**）。 |
| POST | `/api/tools/echart/graph-gl` | GL 关系图：body 与 generate_graph_gl_chart 一致。 |

**请求体**（与 [echart-mcp-server-architecture.md](echart-mcp-server-architecture.md) 4.1 一致）：`{ title?, data: { nodes, edges? }, layout?, width?, height?, theme?, outputType?: "option" \| "png" \| "svg" }`。

**响应**：

- 当 `outputType = option`：返回 `{ type: "option", option: <ECharts option 对象> }`，前端用 ECharts 渲染。
- 当 `outputType = png` 或 `svg`：返回 `{ type: "image", data: "<base64>", mimeType: "image/png" }` 或 SVG 字符串；前端用 `<img src="data:image/...;base64,...">` 展示，**无需引入 ECharts**。

**实现**：API Route 内通过 MCP SDK 调用 ECharts MCP 的 generate_graph_chart / generate_graph_gl_chart，或直接调用本地/远程渲染服务（若采用 ECharts 文档中的渲染方案 B/C），将结果按上表返回。这样前端既可走对话流（SSE 收到 chart 事件），也可在任意时刻 POST 上述接口传入手写 data 展示图谱。

---

## 4. 接收通知的逻辑（SSE）

### 4.1 事件类型（尽量简单）

| 事件类型 | 说明 | 前端处理 |
|----------|------|----------|
| **status** | 状态：思考中 / 完成 | 更新 Header 或侧区状态文案（如“思考中…”/“完成”）。 |
| **text** | 流式文本片段 | 追加到当前助手消息的 content，并滚动到底部。 |
| **chart** | 图表数据：可为 **option**（ECharts option JSON 字符串）或 **image**（如 base64 PNG/SVG 或 data URL） | 若为 option：解析 JSON，若 `series` 中含 `type: 'graphGL'` 则用已注册 echarts-gl 的实例渲染，否则用普通 graph 渲染。若为 image：直接用 `<img src="...">` 展示，**无需 ECharts**，便于大模型返回中嵌入的图像直接显示。 |
| **tool_log**（可选） | 工具名、状态、简短日志 | 追加到 ToolLogs，并更新 ToolStatus（调用中/成功/失败）。 |

不引入 WebSocket；MCP 的 logging 若需展示，可在 API Route 内调用 MCP 时收集，并随 SSE 以 `tool_log` 形式推送。

### 4.2 前端消费方式

- 使用 **EventSource** 或 **fetch + ReadableStream** 消费 SSE。
- 按行解析 `event:` 与 `data:`，根据 `event` 类型分发到上述处理逻辑。
- 收到 `chart` 时：若 payload 为 option JSON，解析后传入 ECharts 实例 `setOption(option)`（graphGL 需已注册 echarts-gl）；若 payload 为 image（base64 或 data URL），则用 `<img src="...">` 展示，无需 ECharts。

### 4.3 图谱展示

- **双模式**：（1）**option 模式**：引入 ECharts 5 与 echarts-gl，ChartPanel 内根据 `series[].type` 渲染 graph/graphGL，支持主题、tooltip、拖拽/缩放。（2）**image 模式**：chart 事件携带 PNG/SVG（base64 或 data URL）时，用 `<img>` 展示，无需 ECharts，便于返回直接嵌入大模型回复、降低前端开发量。
- 前端直接调用 `/api/tools/echart/graph` 或 `/api/tools/echart/graph-gl` 时，可按需传 `outputType: "option"` 或 `"png"`/`"svg"`，分别走 ECharts 渲染或 img 展示。

---

## 5. 与 MCP 服务器的关系

- **Neo4j MCP、ECharts MCP** 均为 **Java 进程**，由 Next.js API Route 通过 MCP SDK 以 **STDIO** 启动并连接。
- 部署时需保证运行环境（如 Node 自托管）允许 **spawn 子进程**；若部署到 Vercel 等无子进程环境，需改为远程 MCP 连接（若 SDK 支持）或仅在本地/自托管 Node 中运行 Next.js。

---

## 6. 小结

- **页面**：主区对话 + 侧区状态/日志/图表；组件按 Layout、ChatPanel、MessageList、ChatInput、SidePanel、ToolStatus、ToolLogs、ChartPanel 划分。
- **接入 MCP**：API Route 内用 Node MCP SDK 按 env 或配置启动 Neo4j/ECharts MCP 子进程，在 LLM 返回 tool_calls 时调用对应工具，并将结果喂回 LLM 或直接推 chart。
- **接收通知**：仅通过一条 **SSE** 连接接收 status、text、chart、可选 tool_log，前端按事件类型更新 UI 与图表，逻辑保持简单，以效果展示为主。
