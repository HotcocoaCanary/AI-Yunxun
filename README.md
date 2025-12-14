# AI-Yunxun 智能知识图谱系统

基于图数据库和自然语言实现的学术论文知识图谱系统，提供智能问答、知识图谱可视化、数据分析等功能。

## 📋 项目简介

AI-Yunxun 是一个集成了大语言模型（LLM）和知识图谱的智能查询系统，通过自然语言交互，帮助用户查询和分析学术论文知识图谱。系统采用 Spring Boot 3 + WebFlux 响应式架构，通过 MCP（Model Context Protocol）协议实现 AI 模型与工具的无缝集成。

## ✨ 核心特性

### 🤖 智能问答
- **自然语言查询**: 支持用自然语言提问，系统自动理解意图
- **两阶段处理**: 工具调用 + 自然语言生成，确保回答准确且易读
- **流式输出**: 基于 WebFlux 的 Server-Sent Events (SSE) 实现实时流式响应
- **多模态响应**: 同时返回文本回答、图谱数据和图表可视化

### 🕸️ 知识图谱可视化
- **交互式图谱**: 基于 ECharts 的力导向图，支持拖拽、缩放、点击交互
- **实时查询**: 通过 Neo4j 图数据库实时查询实体和关系
- **动态渲染**: 根据查询结果动态生成图谱可视化

### 📊 数据分析
- **多种图表类型**: 支持柱状图、折线图、饼图、力导向图、散点图等
- **ECharts 集成**: 基于 ECharts 的强大可视化能力
- **智能洞察**: 自动生成数据洞察摘要和要点

### 🔧 MCP 工具集成
- **标准化工具**: 通过 MCP 协议将后端能力封装为工具
- **自动调用**: AI 模型根据用户问题自动选择合适的工具
- **可扩展性**: 易于添加新的工具和功能
- **网络搜索**: 集成 websearch-mcp，支持实时网络搜索和数据获取

## 🏗️ 系统架构

```
┌─────────────────────────────────────────────────────────────┐
│                      API 层 (WebFlux)                        │
│  ┌──────────────────────────────────────────────────────┐   │
│  │  McpChatController                                   │   │
│  │  POST /api/chat (Mono<ChatResponse>)                │   │
│  │  POST /api/chat/stream (Flux<ServerSentEvent>)      │   │
│  └──────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────┘
                            │
                            ▼
┌─────────────────────────────────────────────────────────────┐
│                    Service 层 (响应式)                       │
│  ┌──────────────────────────────────────────────────────┐   │
│  │  McpChatService                                       │   │
│  │  - 两阶段对话处理                                     │   │
│  │  - 工具调用协调                                       │   │
│  │  - 流式响应处理                                       │   │
│  └──────────────────────────────────────────────────────┘   │
│                            │                                 │
│        ┌───────────────────┼───────────────────┐              │
│        ▼                   ▼                   ▼              │
│  ┌──────────┐      ┌──────────┐      ┌──────────┐          │
│  │ Neo4j    │      │ Chart   │      │ MongoDB  │          │
│  │ GraphTool│      │ Tool    │      │ Tool     │          │
│  └──────────┘      └──────────┘      └──────────┘          │
└─────────────────────────────────────────────────────────────┘
                            │
                            │ MCP Protocol (WebFlux SSE)
                            ▼
┌─────────────────────────────────────────────────────────────┐
│                    AI 模型层 (智谱AI)                         │
│  ┌──────────────────────────────────────────────────────┐   │
│  │  GLM-4.5-Flash                                        │   │
│  │  - 自然语言理解                                      │   │
│  │  - 工具调用决策                                      │   │
│  │  - 自然语言生成                                      │   │
│  └──────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────┘
                            │
                            ▼
┌─────────────────────────────────────────────────────────────┐
│                      数据存储层 (响应式)                      │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐   │
│  │  Neo4j   │  │ MongoDB  │  │  MySQL   │  │  Redis   │   │
│  │ 图谱数据 │  │ 论文文档  │  │ 业务数据 │  │  缓存    │   │
│  │(阻塞包装)│  │(响应式)  │  │(阻塞)    │  │(响应式)  │   │
│  └──────────┘  └──────────┘  └──────────┘  └──────────┘   │
└─────────────────────────────────────────────────────────────┘
```

## 🚀 快速开始

### 环境要求

- **Java**: 17+
- **Maven**: 3.6+
- **Docker**: 20.10+
- **Docker Compose**: 2.0+

### 一键启动（推荐）

#### 1. 克隆项目

```bash
git clone <repository-url>
cd AI-Yunxun
```

#### 2. 启动所有服务

```bash
docker-compose up -d
```

这个命令会启动以下服务：
- **数据库服务**: MySQL, MongoDB, Redis, Neo4j
- **AI 模型**: Ollama (qwen3:8b) - 可选，当前使用智谱AI
- **应用服务**: Spring Boot 3 + WebFlux 应用 (端口 8080)

#### 3. 查看服务状态

```bash
docker-compose ps
```

#### 4. 查看日志

```bash
# 查看所有服务日志
docker-compose logs -f

# 查看应用服务日志
docker-compose logs -f app
```

#### 5. 访问应用

等待所有服务启动完成后（约 1-2 分钟），访问：

- **应用 API**: http://localhost:8080
- **健康检查**: http://localhost:8080/actuator/health
- **Neo4j 浏览器**: http://localhost:7474

### 服务说明

#### 数据库服务

所有数据库服务已自动配置，默认账号密码均为 `ai_yunxun`：

- **MySQL**: `localhost:3306` (用户: `ai_yunxun`, 密码: `ai_yunxun`)
- **MongoDB**: `localhost:27017` (用户: `ai_yunxun`, 密码: `ai_yunxun`)
- **Redis**: `localhost:6379` (密码: `ai_yunxun`)
- **Neo4j**: `localhost:7687` (用户: `neo4j`, 密码: `ai_yunxun`)

#### 网络搜索（可选）

系统已配置 `mcp-searxng` 支持网络搜索功能（无需 API Key）。要启用搜索功能：

1. **安装 mcp-searxng**（在宿主机上）：
   ```bash
   npm install -g mcp-searxng
   ```

2. **配置已自动启用**：`application.yml` 中的配置已启用

3. **重启应用服务**：搜索工具会自动注册

> 💡 **提示**：`mcp-searxng` 使用公共 SearXNG 实例，无需 API Key，开箱即用。

### 手动启动（开发模式）

#### 环境要求

- **Java**: 17+
- **Maven**: 3.6+

#### 启动步骤

1. **启动数据库服务**（使用 Docker Compose）:
   ```bash
   docker-compose up -d mysql mongodb redis neo4j
   ```

2. **配置应用**:
   编辑 `src/main/resources/application.yml`，确保数据库连接配置正确。

3. **启动应用**:
   ```bash
   mvn spring-boot:run
   ```

详细的手动配置说明请参考 [架构文档](./docs/ARCHITECTURE.md)。

## 📖 使用指南

### API 接口

#### 同步聊天接口

```bash
POST /api/chat
Content-Type: application/json

{
  "message": "查找与机器学习相关的论文"
}
```

响应：
```json
{
  "reply": "根据查询结果，找到了以下与机器学习相关的论文...",
  "graphJson": "{\"nodes\":[...], \"edges\":[...]}",
  "chartJson": "{\"chartType\":\"bar\", ...}",
  "toolCalls": [...]
}
```

#### 流式聊天接口

```bash
POST /api/chat/stream
Content-Type: application/json

{
  "message": "近10年考研人数变化趋势，画一个柱状图"
}
```

响应：Server-Sent Events (SSE) 流

### 工具使用

系统提供以下 MCP 工具：

1. **MongoDB 工具** (`mongo_*`)
   - `mongo_save_document`: 保存文档
   - `mongo_find_by_topic`: 按主题查询
   - `mongo_find_by_id`: 按 ID 查询
   - `mongo_update_document`: 更新文档
   - `mongo_delete_document`: 删除文档
   - `mongo_find_all`: 查询所有文档

2. **Neo4j 图谱工具** (`neo4j_*`)
   - `neo4j_create_node`: 创建节点
   - `neo4j_find_node`: 查询节点
   - `neo4j_update_node`: 更新节点
   - `neo4j_delete_node`: 删除节点
   - `neo4j_create_relationship`: 创建关系
   - `neo4j_find_relationship`: 查询关系
   - `neo4j_find_path`: 路径查询
   - `neo4j_find_neighbors`: 邻居查询
   - `neo4j_fuzzy_search`: 模糊查询

3. **图表生成工具** (`echart_generate`)
   - 支持 bar（柱状图）、line（折线图）、pie（饼图）、graph（力导向图）、scatter（散点图）

4. **网络搜索工具** (`web_search`)
   - 通过外部 MCP 搜索服务器提供（如 mcp-searxng）

## 🛠️ 技术栈

### 后端技术
- **框架**: Spring Boot 3.5.7
- **Web 框架**: Spring WebFlux (响应式)
- **AI 框架**: Spring AI 1.0.3
- **协议**: MCP (Model Context Protocol) - WebFlux SSE 传输
- **数据库**:
  - Neo4j (知识图谱) - 阻塞操作包装为响应式
  - MongoDB (文档存储) - ReactiveMongoRepository
  - MySQL (关系数据) - JPA（阻塞，可选包装为响应式）
  - Redis (缓存) - ReactiveRedisTemplate
- **AI 模型**: 智谱AI (GLM-4.5-Flash)

### 响应式架构
- **WebFlux**: 非阻塞 HTTP 处理
- **Reactor**: Mono/Flux 响应式编程
- **Server-Sent Events**: 流式数据推送
- **响应式数据库**: MongoDB Reactive, Redis Reactive

## 📁 项目结构

```
AI-Yunxun/
├── src/main/java/yunxun/ai/canary/backend/
│   ├── BackendApplication.java        # 应用主类
│   ├── mcp/                           # MCP 协议层
│   │   ├── client/                    # MCP 客户端
│   │   │   ├── config/                # 客户端配置
│   │   │   ├── controller/            # API 控制器
│   │   │   ├── model/                 # 数据模型
│   │   │   └── service/               # 服务层
│   │   └── server/                    # MCP 服务器
│   │       ├── config/                # 服务器配置
│   │       ├── tool/                  # 工具实现
│   │       └── prompt/                # Prompt 管理
│   ├── db/                            # 数据库访问层
│   │   ├── mongo/                     # MongoDB
│   │   └── neo4j/                     # Neo4j
│   └── graph/                         # 图表服务
│       ├── model/dto/                 # 数据传输对象
│       └── service/                   # 图表服务
├── src/main/resources/
│   └── application.yml                # 配置文件
├── pom.xml                            # Maven 配置
├── Dockerfile                         # Docker 构建文件
├── docker-compose.yml                 # Docker Compose 配置
└── README.md                          # 本文档
```

## 🔄 核心流程

### 对话处理流程

```
用户输入问题
    │
    ▼
POST /api/chat (Mono<ChatRequest>)
    │
    ▼
McpChatController (响应式)
    │
    ▼
McpChatService.chatReactive()
    │
    ├─► 阶段一: 工具调用 (Mono)
    │   │
    │   ├─► AI 模型分析意图
    │   │
    │   ├─► 调用 MCP 工具
    │   │   ├─► Neo4jGraphTool (Mono<String>)
    │   │   ├─► EChartGenerateTool
    │   │   └─► MongoTool (响应式 Repository)
    │   │
    │   └─► 提取 GRAPH_JSON/CHART_JSON
    │
    └─► 阶段二: 自然语言生成 (Mono)
        │
        ├─► 基于工具结果生成回答
        │
        └─► 返回 Mono<ChatResult>
            ├─► reply: 自然语言回答
            ├─► graphJson: 图谱数据
            └─► chartJson: 图表数据
```

### 流式响应流程

```
用户输入问题
    │
    ▼
POST /api/chat/stream (Mono<ChatRequest>)
    │
    ▼
McpChatController (Flux<ServerSentEvent>)
    │
    ▼
McpChatService.chatStream()
    │
    ├─► Flux<ToolCallEvent>  (工具调用事件)
    ├─► Flux<ContentEvent>   (内容流式输出)
    ├─► Flux<GraphEvent>     (图谱数据)
    └─► Flux<ChartEvent>     (图表数据)
```

## 📚 详细文档

- [架构文档](./docs/ARCHITECTURE.md) - 详细的架构设计和响应式编程说明

## 🐛 故障排除

### 常见问题

1. **端口冲突**
   - 确保 8080 端口未被占用
   - 可在配置文件中修改端口

2. **数据库连接失败**
   - 检查数据库服务是否启动
   - 验证连接配置是否正确

3. **AI 模型不可用**
   - 检查智谱AI API Key 配置
   - 确认 `application.yml` 中的配置正确

4. **流式输出中断**
   - 检查网络连接
   - 查看应用日志排查错误

### 日志查看

- **应用日志**: `logs/app.log`
- **Docker 日志**: `docker-compose logs -f app`

## 🚧 开发计划

### 已完成 ✅
- Spring Boot 3 + WebFlux 架构
- MCP 工具集成（WebFlux SSE）
- 响应式数据库访问（MongoDB, Redis）
- 流式输出支持
- 两阶段对话处理
- 图谱可视化
- 图表生成

### 进行中 🚧
- 真实数据源集成
- 更多图表类型支持
- 性能优化

### 计划中 📋
- 向量检索（Chroma）
- 智能查询服务
- 数据爬取服务
- 用户认证系统

## 📄 许可证

MIT License

## 🤝 贡献

欢迎提交 Issue 和 Pull Request 来改进项目。

---

**AI-Yunxun** - 让知识图谱更智能，让数据更易理解！
