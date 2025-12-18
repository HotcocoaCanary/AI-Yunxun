# 01. 目标与范围

## 项目目标

构建一个基于 Spring Boot 3 + WebFlux 的智能问答机器人（后端服务），并为其提供若干 MCP（Model Context Protocol）能力，使其具备：

1. 数据库存储：把对话/知识/结构化数据写入数据库
2. 数据库检索：从数据库中检索并返回可用于回答的问题相关信息
3. 联网搜索：当本地数据不足时，进行实时网络搜索并汇总结果
4. ECharts 图表生成：将结构化数据转换为 ECharts `option`，供前端直接渲染

实现约束：MCP 能力基于 Spring AI MCP Server/Client 构建，便于后续扩展向量数据库与 RAG。

## 交付形态（本阶段）

- 后端提供 Chat API（HTTP + 可选 SSE 流式）
- 后端内置 MCP Server 暴露本地工具（DB/图表等）
- 后端可通过 MCP Client 连接外部 MCP Server（如 `mcp-searxng`）用于联网搜索
- 提供简单 Web UI（HTML + JS + CSS，位于 `src/main/resources/static/`），用于对话、展示来源与渲染 ECharts 图表

## 不在本阶段范围（可后续扩展）

- 账号体系、权限/多租户、计费
- 复杂前端工程化（如 React/Vue、Node 构建链、SSR 等）
- 向量数据库/Embedding/RAG（若需要再评估）

## 术语

- **LLM**：大模型服务（如智谱/本地 Ollama）
- **Tool/工具**：LLM 可调用的外部能力（DB、搜索、图表等）
- **MCP**：模型上下文协议；工具以协议方式被暴露与调用
- **SSE**：Server-Sent Events，服务端向客户端单向推送流式消息

## 代码现状盘点（基于当前仓库）

- 工程：Maven + Java 17 + Spring Boot 3.5.7 + Spring AI 1.0.3（`pom.xml`）
- 已存在结构骨架（`README.md` + `src/main/java/yunxun/ai/canary/project/**`）
- 已存在 MCP Server 配置骨架（`.../service/mcp/server/config/McpServerConfig.java`）
- MCP 工具类目前为空壳（`MongoTool/Neo4jTool/WebSearchTool/EChartGenerateTool` 尚无方法实现）
- `LlmClientConfig.java` 引用的 `LlmClient/ZhipuAiLlmClient` 目前缺失（需要补齐后才能编译运行）
- `application.yml` 中包含敏感配置（LLM API Key）：建议改为环境变量/外部配置注入，不在仓库明文保存
