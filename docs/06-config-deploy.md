# 06. 配置与部署

## 配置策略（建议）

- `src/main/resources/application.yml`：仅保留**非敏感**默认值（端口、开关、默认模型名等）
- 敏感信息（API Key/数据库密码）：使用环境变量注入，或使用 `application-local.yml`（不提交，加入 `.gitignore`）
- 通过 Spring Profile 区分：`local` / `docker` / `prod`

## 本地运行（建议）

1. 准备依赖：MySQL / MongoDB / Redis / Neo4j（可直接用 `docker-compose.yml`）
2. 启动后端：`./mvnw spring-boot:run`

> 说明：目前工程存在缺失类（`LlmClient/ZhipuAiLlmClient`），实现前可能无法成功启动；后续编码阶段会先修复编译问题。

## Docker Compose

仓库提供了 `docker-compose.yml`，包含：

- MySQL、MongoDB、Redis、Neo4j
- Ollama（可选，本地推理；当前 compose 默认会拉取 `qwen3:8b`）
- app（构建并运行后端）

启动：`docker compose up -d`

## 配置项说明（节选）

- `spring.ai.zhipu.api-key`：智谱 API Key（建议通过环境变量 `SPRING_AI_ZHIPU_API_KEY` 注入，避免明文提交）
- `spring.ai.zhipu.model`：智谱模型名（如 `glm-4.5-flash`）
- `spring.ai.ollama.*`：Ollama 配置（若选用本地推理）
- `spring.ai.mcp.server.*`：本地 MCP Server（SSE 端点、能力开关）
- `spring.ai.mcp.client.*`：MCP Client 连接配置（用于对接外部 MCP Server）

## 前端静态资源（本次迭代加入）

- 目录：`src/main/resources/static/`（Spring Boot 默认静态资源目录）
- 页面：`src/main/resources/static/index.html` 默认会映射到 `GET /`
- 建议结构：
  - `src/main/resources/static/assets/app.css`
  - `src/main/resources/static/assets/app.js`
  - `src/main/resources/static/vendor/`（你提供的第三方 JS 包，如 ECharts）

## 外部 MCP（联网搜索）运行方式（建议二选一）

1) **外置服务（推荐）**：将 `mcp-searxng` 作为单独进程/容器运行，后端通过 MCP Client 连接。

2) **内置到 app 容器（不推荐）**：在同一容器内管理多进程（需要额外的进程管理器/脚本与健康检查策略）。

> 当前 `Dockerfile` 已全局安装 `mcp-searxng`，但并未启动它；是否要“内置运行”需要你确认偏好。

## 安全建议（必须）

- 不要在仓库中明文保存任何 API Key/密码；使用环境变量或外部 `application-local.yml`（不提交）
- 联网搜索与数据库写入能力要有审计与限流（至少记录 query/写入目标/耗时/结果）
