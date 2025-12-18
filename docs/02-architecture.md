# 02. 整体架构

## 包结构（现有设计）

`README.md` 中的设计可映射到 Java 包：

- `app`：应用入口与编排层（HTTP/SSE -> 一次完整问答流程）
- `config`：全局配置（Spring Boot/WebFlux/外部依赖）
- `repository`：数据访问层（Mongo/Neo4j/…）
- `service`：核心能力层（LLM、MCP、图表等）

## 运行时组件（建议形态）

```mermaid
flowchart LR
  U[前端/调用方] -->|HTTP/SSE| API[Chat API(WebFlux)]
  U --> UI[Web UI(HTML/JS/CSS)]
  UI -->|调用| API
  API --> ORCH[问答编排/Agent Orchestrator]
  ORCH --> LLM[LLM Client]
  ORCH --> MCPc[MCP Client(可选)]
  MCPc --> MCPsLocal[MCP Server(本地工具)]
  MCPc --> MCPsExt[外部 MCP Server(搜索)]
  MCPsLocal --> DB[(Mongo/Neo4j/MySQL/Redis)]
  MCPsExt --> NET[(Internet)]
  ORCH --> DB
```

说明：

- **Chat API**：对外提供问答接口，支持一次性响应或流式 SSE
- **Web UI**：静态页面（HTML + JS + CSS），用于对话交互与图表展示，调用 Chat API；静态资源位于 `src/main/resources/static/`
- **Orchestrator**：维护对话上下文、选择工具、聚合工具结果、生成最终答案
- **LLM Client**：对接具体大模型（智谱/Ollama 等），支持工具调用（Tool Call）与流式输出
- **MCP Server**：把本地工具暴露为 MCP（DB/图表等）
- **MCP Client**：把外部/本地 MCP 工具“接入”为可调用工具（尤其是联网搜索）

## 关键数据流（问答一次的典型流程）

1. 调用方发起问题（可携带 `sessionId`、历史摘要等）
2. Orchestrator 组装提示词（system + memory + user）
3. LLM 产生回答或发起工具调用（如：先查 DB，不足再 web search）
4. Orchestrator 执行工具（本地直调或经 MCP Client 调用）
5. 将工具结果回灌给 LLM，得到最终答案（可附带图表 `option`）
6. 持久化：保存对话、工具调用记录、抽取到的结构化知识（可选）

## 架构原则（用于后续细化）

- 工具接口先稳定：参数/返回结构尽量明确、可回放、可测试
- 可观测性：每次问答保留 `traceId`、tool 调用耗时与错误
- 安全边界：联网搜索、DB 写入需要明确白名单/限流/审计
- 可扩展：工具与存储分层，后续可按同样模式增加向量数据库/Embedding 与 RAG 相关能力
