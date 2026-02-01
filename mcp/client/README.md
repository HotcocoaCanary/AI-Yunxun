# Next.js 客户端（MCP 对话与图谱）

与 [doc/nextjs-client-architecture.md](../doc/nextjs-client-architecture.md) 一致：对话 + SSE + 图谱直接调用接口 + 双模式展示（option / image）。

## 环境

- Node.js v22
- 需先启动 **Neo4j MCP**、**ECharts MCP**（Java），并配置 SSE 地址

## 配置

复制 `.env.example` 为 `.env` 并填写：

- **LLM_API_KEY**：智谱或 OpenAI 兼容 API Key（可选；未配置时对话仅演示 SSE）
- **LLM_BASE_URL**：LLM API 根地址（可选，默认智谱）
- **NEO4J_MCP_URL**：Neo4j MCP SSE 地址（如 `http://localhost:8082/sse`）
- **ECHART_MCP_URL**：ECharts MCP SSE 地址（如 `http://localhost:8081/sse`）

## 运行

```bash
npm install
npm run dev
```

访问 http://localhost:3000。主区为对话列表与输入框，侧区为工具状态、日志与图表。点击「加载示例关系图」可直接调用 `POST /api/tools/echart/graph` 展示图谱（不经过大模型）。

## API

- **POST /api/chat**：body `{ conversationId?, message }`，返回 SSE 流（event: status / text / chart / tool_log）
- **POST /api/tools/echart/graph**：关系图，body 同 [echart-mcp-server-architecture.md 4.1](../doc/echart-mcp-server-architecture.md)，返回 `{ type: "option", option }` 或 `{ type: "image", data, mimeType }`
- **POST /api/tools/echart/graph-gl**：GL 关系图，请求/响应格式同上

## 联调

1. 启动 Neo4j（含 APOC）、Neo4j MCP、ECharts MCP
2. 配置 `.env` 中的 MCP URL 与 LLM
3. `npm run dev` 启动本客户端
4. 发对话或点击「加载示例关系图」验收
