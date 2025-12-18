# 08. 前端 UI（HTML + JS + CSS）

目标：提供一个足够简单但可用的 Web 页面，用于验证“问答 + MCP 工具（DB/搜索/图表）”闭环；不引入复杂前端工程化与构建链。

## 技术约束

- 仅使用 HTML + 原生 JS + CSS
- 第三方 JS 包以“静态文件”方式引入（你提供并放入 `src/main/resources/static/vendor/`）
- 前后端同源部署（页面由后端静态资源直接提供），默认不处理跨域

## 页面与交互（建议）

### 页面布局

- 顶部：标题 + 连接状态（SSE/接口可用性）
- 中间左侧：对话区（按 `user/assistant/tool` 渲染气泡）
- 中间右侧：信息区
  - Sources（来源列表：web url、mongo id、neo4j 引用等）
  - Chart（ECharts 渲染容器）
  - Debug（可折叠：tool_call/tool_result 事件与原始 JSON）
- 底部：输入框 + 发送按钮 + “流式输出”开关

### 关键交互

1. 输入问题 -> 点击发送
2. 若开启流式：
   - 逐步追加 `token` 到 assistant 气泡
   - 展示 `tool_call`/`tool_result`（用于调试与验收）
   - 收到 `final` 后渲染 sources 与 chart
3. 若关闭流式：调用 `POST /api/chat` 一次性返回并渲染

## 与后端接口对接

### 一次性接口

- `POST /api/chat`：返回 `{ answer, sources, charts }`

### 流式接口（SSE）

- `POST /api/chat/stream` + `Accept: text/event-stream`
- 浏览器端建议用 `fetch()` 读取 `ReadableStream`（因为 `EventSource` 仅支持 GET）
- 事件类型与建议字段见：`docs/03-api.md`

## 静态资源目录建议

```text
src/main/resources/static/
  index.html
  assets/
    app.css
    app.js
  vendor/
    echarts.min.js           # 你提供
```

## 图表渲染（ECharts）

当 `final.charts[*].type == "echarts"` 时：

- 页面确保已加载 `vendor/echarts.min.js`
- 在 `div#chart` 上 `echarts.init()` 并对 `option` 调用 `setOption(option, true)`

