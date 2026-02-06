# MCP Client（前端）

这是 MCP client 的前端项目，用 Next.js 做页面，用 TS 负责把大模型、MCP 工具链路串起来。页面通过 `/api/chat` 走 SSE 流式返回。

## 项目结构

- `src/app`
  - `page.tsx`：主页面，左侧是状态栏，右侧是聊天区
  - `layout.tsx`：全局布局
  - `globals.css`：整体样式
  - `api/chat/route.ts`：SSE 接口，负责把模型输出、工具调用过程推给前端
- `src/domain`
  - `llm/LLMService.ts`：调用智谱接口，解析流式结果和 tool_calls
  - `chat/ServerChatService.ts`：负责“模型 -> 工具 -> 模型”的串行逻辑
- `src/infra`
  - `mcp/client.ts`：MCP TypeScript SDK 的简单封装
  - `mcp/manager.ts`：管理多个 MCP server，统一拉工具列表、调用工具
  - `zhipu/zhipu-ai.ts`：智谱 API 调用
- `src/types/chat.ts`：消息与工具调用的类型
- `src/ui/components`：UI 组件，包含聊天卡片、工具卡片、ECharts 展示

## 运行流程（实际代码逻辑）

1. 页面提交消息到 `/api/chat`。
2. `ServerChatService` 调用 `LLMService` 请求智谱模型。
3. 如果模型返回 `tool_calls`，就由 `McpManager` 去调用对应的 MCP 工具。
4. 工具结果被追加到对话里，继续下一轮模型请求，直到模型不再要工具。
5. 整个过程通过 SSE 把事件流推到前端，前端实时更新消息、工具卡片和状态。

## UI 设计（当前页面）

- 左侧固定一栏，展示标题、状态、连接信息。
- 右侧是聊天区，包含：
  - 用户消息卡片
  - 助手消息卡片（可显示状态、思维链、工具调用）
  - 工具结果卡片（如果工具名以 `echart` 开头，会渲染 ECharts 图）
- 输入区有两个开关：
  - Deep thinking（控制 `thinking` 参数）
  - Web search（控制 `web_search` 参数）

## 配置

需要的环境变量在 `mcp/client/.env.local`：

- `ZHIPUAI_API_KEY`：智谱 API Key
- `ECHART_MCP_SERVER`：ECharts MCP server 的 SSE 地址
- `NEO4J_MCP_SERVER`：Neo4j MCP server 的 SSE 地址
