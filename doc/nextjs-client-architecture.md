# Next.js 前端系统设计文档

## 1. 定位与职�?

MCP 客户�?*不再使用 Java 后端**，仅保留 **Next.js 单层**（前端页�?+ API Route），通过 **SSE 接收 MCP 相关通知**做效果展示。逻辑尽量简单，核心放在两个 MCP 服务器（Neo4j、ECharts）的开发�?

- **API Route**：接收对话请求、调�?LLM（智谱或 OpenAI 兼容）、通过 Node/TS MCP SDK 连接 Neo4j/ECharts MCP 服务器（STDIO），将流式回复与工具结果通过 **SSE** 推送给前端；并暴露**图谱工具直接调用接口**，供前端不经过大模型即可“捏造请求”展示图谱�?

---

## 2. 页面布局

### 2.1 整体结构

```
+----------------------------------------------------------+
| Header（标题、连�?状态）                                    |
+------------------------+-----------------------------------+
| 主区                    | 侧区                              |
| 对话列表                | MCP 工具状态（当前调用�?成功/失败�? |
| +------------------+   | 简要日志（最近几条）                 |
| | 用户 / 助手消息   |   | （可选）MCP 服务器列表（可写�?env�? |
| +------------------+   +-----------------------------------+
| 输入�?+ 发�?          | 图表�?                           |
|                        | ECharts 容器（关系图 / GL 关系图）   |
+------------------------+-----------------------------------+
```

- **主区**：对话列表（用户消息、助手流式文本）、底部输入框与发送按钮�?
- 侧区可收缩或固定宽度，以“效果展示”为主，不做完整 MCP 导入/编辑/删除管理�?

### 2.2 组件划分建议

| 组件 | 职责 |
|------|------|
| **Layout** | 整体布局：Header + 主区 + 侧区，可含简单状态（如“已连接/未连接”）�?|
| **ChatPanel** | 主区：消息列�?+ 输入框。维�?`messages` 状态，接收 SSE �?`text` 事件追加到当前助手消息�?|
| **MessageList** | 渲染消息列表，区�?user/assistant，支持流式文本展示�?|
| **ChatInput** | 输入�?+ 发送按钮，提交�?POST `/api/chat` 并建�?SSE 连接�?|
| **SidePanel** | 侧区容器：工具状�?+ 日志 + 图表区�?|
| **ToolStatus** | 展示当前工具调用状态（调用�?成功/失败），数据来自 SSE �?`tool_log` �?`tool_status`（若实现）�?|
| **ToolLogs** | 简要日志列表，最�?N 条，来自 SSE �?`tool_log` 或从 MCP 日志解析�?|

以上组件可按 Next.js App Router（如 `app/page.tsx`、`app/components/...`）或 Pages Router 组织�?

---

## 3. 接入 MCP 的逻辑（API Route�?

### 3.1 流程

1. 前端 POST `/api/chat`，body：`{ conversationId, message }`，并建立 **SSE** 连接（同一请求流式�?body 或通过 GET/SSE 端点均可，以实现简单为准）�?
2. API Route 内：
   - 调用 **LLM API**（智谱或 OpenAI 兼容）流式对话�?
   - �?LLM 返回 **tool_calls**（如需要查 Neo4j 或画图），则通过 **Node/TS MCP SDK** 调用对应 MCP 服务器：
     - Neo4j MCP、ECharts MCP �?**STDIO** 子进程方式启动（命令与参数来�?**环境变量**或简�?JSON 配置）�?
     - 将工具结果（�?ECharts option JSON）再喂回 LLM 或直接通过 SSE 推送给前端（如 `chart` 事件）�?
   - 将流式文本通过 SSE `text` 事件推送；状态通过 `status`（思考中/完成）；可�?`tool_log`/`tool_status` 推送工具名与状态�?

### 3.2 配置

- **MCP 服务�?*：Neo4j MCP、ECharts MCP 的启动命令（�?`java -jar neo4j-mcp.jar`、`java -jar echart-mcp.jar`）及参数写在 **环境变量**（如 `NEO4J_MCP_CMD`、`ECHART_MCP_CMD`）或项目内一份简�?JSON 配置中�?
- API Route 启动时按配置 spawn 子进程，建立 MCP 客户端连接；不在页面上做“导�?编辑/删除”等完整管理，以降低复杂度�?

### 3.3 技术选型

- **MCP 客户�?*：使�?Node/TS 生态的 MCP SDK（如 `@modelcontextprotocol/sdk` �?Spring AI 文档中提到的等效库），通过 STDIO �?Java MCP 进程通信�?
- **LLM**：智谱或任意 OpenAI 兼容 API，在 API Route 内通过 fetch/流式读即可�?

### 3.4 前端直接调用图谱工具（不经过大模型）

为支�?*前端不通过大模型、自己捏造请求展示图�?*，Next.js 需提供**图谱工具直接调用接口**，与 ECharts MCP 工具的输入输出一致，便于前端直接请求并展示�?

**建议接口**�?

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/tools/echart/graph` | 关系图：body �?MCP 工具 generate_graph_chart 输入一致（title、data、layout、width、height、theme�?*outputType**）�?|

**请求�?*（与 [echart-mcp-server-architecture.md](echart-mcp-server-architecture.md) 4.1 一致）：`{ title?, data: { nodes, edges? }, layout?, width?, height?, theme?, outputType?: "option" \| "png" \| "svg" }`�?

**响应**�?

- �?`outputType = option`：返�?`{ type: "option", option: <ECharts option 对象> }`，前端用 ECharts 渲染�?
- �?`outputType = png` �?`svg`：返�?`{ type: "image", data: "<base64>", mimeType: "image/png" }` �?SVG 字符串；前端�?`<img src="data:image/...;base64,...">` 展示�?*无需引入 ECharts**�?


---

## 4. 接收通知的逻辑（SSE�?

### 4.1 事件类型（尽量简单）

| 事件类型 | 说明 | 前端处理 |
|----------|------|----------|
| **status** | 状态：思考中 / 完成 | 更新 Header 或侧区状态文案（如“思考中…�?“完成”）�?|
| **text** | 流式文本片段 | 追加到当前助手消息的 content，并滚动到底部�?|
| **tool_log**（可选） | 工具名、状态、简短日�?| 追加�?ToolLogs，并更新 ToolStatus（调用中/成功/失败）�?|

不引�?WebSocket；MCP �?logging 若需展示，可�?API Route 内调�?MCP 时收集，并随 SSE �?`tool_log` 形式推送�?

### 4.2 前端消费方式

- 使用 **EventSource** �?**fetch + ReadableStream** 消费 SSE�?
- 按行解析 `event:` �?`data:`，根�?`event` 类型分发到上述处理逻辑�?

### 4.3 图谱展示


---

## 5. �?MCP 服务器的关系

- **Neo4j MCP、ECharts MCP** 均为 **Java 进程**，由 Next.js API Route 通过 MCP SDK �?**STDIO** 启动并连接�?
- 部署时需保证运行环境（如 Node 自托管）允许 **spawn 子进�?*；若部署�?Vercel 等无子进程环境，需改为远程 MCP 连接（若 SDK 支持）或仅在本地/自托�?Node 中运�?Next.js�?

---

## 6. 小结

- **页面**：主区对�?+ 侧区状�?日志/图表；组件按 Layout、ChatPanel、MessageList、ChatInput、SidePanel、ToolStatus、ToolLogs、ChartPanel 划分�?
- **接入 MCP**：API Route 内用 Node MCP SDK �?env 或配置启�?Neo4j/ECharts MCP 子进程，�?LLM 返回 tool_calls 时调用对应工具，并将结果喂回 LLM 或直接推 chart�?
- **接收通知**：仅通过一�?**SSE** 连接接收 status、text、chart、可�?tool_log，前端按事件类型更新 UI 与图表，逻辑保持简单，以效果展示为主�?
