# 03. 对外接口（草案）

> 说明：当前 `ChatController` 为空壳，本文件先对齐接口形态；实现阶段再落地到 WebFlux Controller/Router。

## 0) Web UI（本次迭代加入）

- `GET /`：返回静态页面（默认 `src/main/resources/static/index.html`）
- 静态资源：建议放在 `src/main/resources/static/assets/` 或 `src/main/resources/static/vendor/`，以便直接通过浏览器加载

## 1) Chat：一次性响应

- `POST /api/chat`

请求（示例）：

```json
{
  "sessionId": "optional",
  "message": "用户问题",
  "context": {
    "language": "zh-CN",
    "timezone": "Asia/Shanghai"
  }
}
```

响应（示例）：

```json
{
  "sessionId": "xxx",
  "answer": "最终回答",
  "sources": [
    { "type": "web", "title": "…", "url": "…" },
    { "type": "mongo", "id": "…" }
  ],
  "charts": [
    { "type": "echarts", "option": { } }
  ]
}
```

## 2) Chat：SSE 流式输出（建议）

- `POST /api/chat/stream`
- `Content-Type: application/json`
- `Accept: text/event-stream`

事件类型（建议）：

- `token`：LLM token 增量
- `tool_call`：模型触发工具调用（含 name + args）
- `tool_result`：工具返回结果（可用于前端调试/可视化）
- `final`：最终结构化响应（answer/sources/charts）
- `error`：错误信息

## 3) MCP Server（现有配置）

`application.yml` 中配置了 MCP SSE 端点（Spring AI MCP Server WebFlux）：

- SSE：`GET /sse`
- 消息：`POST /mcp/message`

这些端点用于 MCP Client 与 MCP Server 的协议交互；是否直接对前端开放需要在安全策略上单独决策。
