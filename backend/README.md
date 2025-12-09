# AI-Yunxun 后端架构文档

## 📋 项目概览

**项目名称**: AI-Yunxun Backend  
**技术栈**: Spring Boot 3.5.7 + Java 17  
**架构模式**: 分层架构 + MCP (Model Context Protocol) 工具集成  
**主要功能**: 基于自然语言的智能知识图谱查询与可视化系统

---

## 🏗️ 整体架构

```
┌─────────────────────────────────────────────────────────────┐
│                     前端层 (Next.js)                        │
│              /api/chat 接口调用                              │
└─────────────────────────────────────────────────────────────┘
                            │
                            ▼
┌─────────────────────────────────────────────────────────────┐
│                    Controller 层                             │
│  ┌──────────────────────────────────────────────────────┐   │
│  │  McpChatController                                   │   │
│  │  - POST /api/chat                                    │   │
│  │  - 接收用户消息，返回回复、图谱JSON、图表JSON        │   │
│  └──────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────┘
                            │
                            ▼
┌─────────────────────────────────────────────────────────────┐
│                    Service 层                                │
│  ┌──────────────────────────────────────────────────────┐   │
│  │  McpChatService                                       │   │
│  │  - 两阶段对话处理：工具调用 + 自然语言生成            │   │
│  │  - 提取 GRAPH_JSON 和 CHART_JSON 标记                │   │
│  └──────────────────────────────────────────────────────┘   │
│  ┌──────────────────────────────────────────────────────┐   │
│  │  GraphChartService                                    │   │
│  │  - 图表生成服务（柱状图、折线图等）                   │   │
│  │  - 返回 ECharts 格式的图表配置                        │   │
│  └──────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────┘
                            │
                ┌───────────┴───────────┐
                ▼                       ▼
┌───────────────────────────┐  ┌───────────────────────────┐
│    MCP Server 工具层       │  │     数据库访问层           │
│  ┌─────────────────────┐  │  │  ┌─────────────────────┐  │
│  │  Neo4jGraphTool     │  │  │  │  Neo4jGraphService  │  │
│  │  - 节点/关系 CRUD   │  │  │  │  - 图谱操作封装      │  │
│  └─────────────────────┘  │  │  └─────────────────────┘  │
│  ┌─────────────────────┐  │  │  ┌─────────────────────┐  │
│  │  GraphChartTool     │  │  │  │  Neo4jQueryService │  │
│  │  - 图表生成工具     │  │  │  │  - Cypher查询执行   │  │
│  └─────────────────────┘  │  │  └─────────────────────┘  │
│  ┌─────────────────────┐  │  │  ┌─────────────────────┐  │
│  │  MongoTool          │  │  │  │  RawPaperDocument   │  │
│  │  - 文档存储工具     │  │  │  │    Repository        │  │
│  └─────────────────────┘  │  │  └─────────────────────┘  │
└───────────────────────────┘  └───────────────────────────┘
                │                       │
                └───────────┬───────────┘
                            ▼
┌─────────────────────────────────────────────────────────────┐
│                      数据存储层                               │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐   │
│  │  Neo4j   │  │ MongoDB  │  │  MySQL   │  │  Redis   │   │
│  │ 图谱数据 │  │ 论文文档  │  │ 业务数据 │  │  缓存    │   │
│  └──────────┘  └──────────┘  └──────────┘  └──────────┘   │
└─────────────────────────────────────────────────────────────┘
                            │
                            ▼
┌─────────────────────────────────────────────────────────────┐
│                    AI 模型层                                 │
│  ┌──────────────────────────────────────────────────────┐   │
│  │  Ollama (qwen3:8b)                                    │   │
│  │  - 通过 Spring AI ChatClient 调用                     │   │
│  │  - 支持工具调用和自然语言生成                          │   │
│  └──────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────┘
```

---

## 📦 模块结构

### 1. 主应用入口

**文件**: `BackendApplication.java`

```java
@SpringBootApplication
@EnableScheduling        // 定时任务支持
@EnableMongoAuditing     // MongoDB审计
@EnableAsync             // 异步处理支持
public class BackendApplication {
    public static void main(String[] args) {
        SpringApplication.run(BackendApplication.class, args);
    }
}
```

### 2. MCP 客户端模块 (`mcp.client`)

负责与前端交互，提供聊天接口。

#### 2.1 Controller

**文件**: `mcp/client/controller/McpChatController.java`

- **路径**: `/api/chat`
- **方法**: `POST`
- **功能**: 接收用户消息，返回结构化响应
- **请求体**:
  ```json
  {
    "message": "用户问题"
  }
  ```
- **响应体**:
  ```json
  {
    "reply": "自然语言回复",
    "graphJson": "图谱数据JSON（可选）",
    "chartJson": "图表数据JSON（可选）"
  }
  ```

#### 2.2 Service

**文件**: `mcp/client/service/McpChatService.java`

**两阶段处理机制**:

1. **阶段一: 工具调用阶段**
   - 系统提示词引导模型调用 MCP 工具
   - 提取 `GRAPH_JSON:` 和 `CHART_JSON:` 标记
   - 只输出结构化 JSON，不输出自然语言

2. **阶段二: 自然语言生成阶段**
   - 将工具结果反馈给模型
   - 生成自然语言回答
   - 隐藏底层 JSON 细节

**关键方法**:
```java
public ChatResult chat(String message)
```

#### 2.3 Config

**文件**: `mcp/client/config/McpClientConfig.java`

- 配置 Spring AI ChatClient Bean
- 自动集成 Ollama 模型

### 3. MCP 服务器模块 (`mcp.server`)

将业务能力封装为 MCP 工具，供 AI 模型调用。

#### 3.1 Tools

**Neo4jGraphTool** (`mcp/server/tool/Neo4jGraphTool.java`)

- **节点操作**:
  - `neo4j_create_node`: 创建节点
  - `neo4j_delete_node`: 删除节点
  - `neo4j_find_node`: 查找节点
  - `neo4j_update_node`: 更新节点

- **关系操作**:
  - `neo4j_create_relationship`: 创建关系
  - `neo4j_delete_relationship`: 删除关系
  - `neo4j_find_relationship`: 查找关系
  - `neo4j_update_relationship`: 更新关系

- **委托**: 所有操作委托给 `Neo4jGraphService` 执行

**GraphChartTool** (`mcp/server/tool/GraphChartTool.java`)

- **工具名**: `generate_chart`
- **功能**: 根据问题生成图表配置
- **支持类型**: bar, line, pie, force
- **参数**:
  - `question`: 用户问题
  - `chartType`: 图表类型
  - `dataSource`: 数据源（可选）
  - `metric`: 指标（可选）
  - `dimensions`: 维度（可选）
  - `timeRangePreset`: 时间范围（可选）
  - `limit`: 数据行数限制（可选）

**MongoTool** (`mcp/server/tool/MongoTool.java`)

- **工具名**: `mongo_save_raw_text`
- **功能**: 保存原始文本到 MongoDB
- **参数**:
  - `topic`: 主题
  - `source`: 来源（可选）
  - `content`: 文本内容

#### 3.2 Config

**文件**: `mcp/server/config/McpServerConfig.java`

- 配置 MCP 服务器端点
- SSE 端点: `/sse`, `/mcp/message`
- 启用工具、资源、提示词能力

#### 3.3 Prompt

**文件**: `mcp/server/prompt/PromptRegistry.java`

- 提示词注册与管理

### 4. 图谱服务模块 (`graph`)

提供图表生成能力。

#### 4.1 Service

**文件**: `graph/service/GraphChartService.java`

- 生成 ECharts 格式的图表配置
- 当前为演示版本，生成模拟数据
- 支持柱状图、折线图等

**关键方法**:
```java
public ChartResponse generateChart(ChartRequest request)
```

#### 4.2 DTO

**ChartRequest** (`graph/model/dto/ChartRequest.java`)

- 图表请求参数
- 使用 Builder 模式构建

**ChartResponse** (`graph/model/dto/ChartResponse.java`)

- 图表响应数据
- 字段:
  - `chartType`: 图表类型
  - `engine`: 渲染引擎（echarts）
  - `title`: 标题
  - `chartSpec`: ECharts 配置
  - `data`: 数据行
  - `insightSummary`: 洞察摘要
  - `insightBullets`: 洞察要点

### 5. 数据库访问模块 (`db`)

#### 5.1 Neo4j (`db.neo4j`)

**Neo4jGraphService** (`db/neo4j/Neo4jGraphService.java`)

- 节点 CRUD 操作
- 关系 CRUD 操作
- 使用 Neo4jClient 执行 Cypher 查询

**关键方法**:
```java
// 节点操作
public String createNode(String label, Map<String, Object> properties)
public String deleteNode(String label, String propertyKey, String propertyValue)
public String findNode(String label, String propertyKey, String propertyValue, Integer limit)
public String updateNode(String label, String propertyKey, String propertyValue, Map<String, Object> properties)

// 关系操作
public String createRelationship(...)
public String deleteRelationship(...)
public String findRelationship(...)
public String updateRelationship(...)
```

**Neo4jQueryService** (`db/neo4j/Neo4jQueryService.java`)

- 执行任意 Cypher 查询
- 将结果转换为 JSON 格式
- 处理 Node、Relationship、Path 等复杂类型

**关键方法**:
```java
public String runQueryAsJson(String cypher)
```

#### 5.2 MongoDB (`db.mongo`)

**RawPaperDocumentRepository** (`db/mongo/RawPaperDocumentRepository.java`)

- 继承 `MongoRepository<RawPaperDocument, String>`
- 提供按主题查询: `findByTopic(String topic)`

**RawPaperDocument** (`db/mongo/model/RawPaperDocument.java`)

- 论文文档实体
- 字段:
  - `id`: 文档ID
  - `topic`: 主题
  - `title`: 标题
  - `summary`: 摘要
  - `sourceType`: 来源类型
  - `createdAt`: 创建时间

---

## 🔄 核心流程

### 对话处理流程

```
用户输入消息
    │
    ▼
McpChatController.chat()
    │
    ▼
McpChatService.chat()
    │
    ├─► 阶段一: 工具调用
    │   │
    │   ├─► ChatClient.prompt()
    │   │   └─► 系统提示: 调用工具，输出 GRAPH_JSON/CHART_JSON
    │   │
    │   ├─► AI 模型分析意图
    │   │
    │   ├─► 调用 MCP 工具
    │   │   ├─► Neo4jGraphTool (查询图谱)
    │   │   ├─► GraphChartTool (生成图表)
    │   │   └─► MongoTool (存储文档)
    │   │
    │   └─► 提取 JSON 标记
    │
    └─► 阶段二: 自然语言生成
        │
        ├─► ChatClient.prompt()
        │   └─► 系统提示: 基于工具结果生成自然语言回答
        │
        └─► 返回 ChatResult
            ├─► replyText: 自然语言回复
            ├─► graphJson: 图谱数据（可选）
            └─► chartJson: 图表数据（可选）
```

### 图谱查询流程

```
用户问题: "Bob 认识谁？"
    │
    ▼
AI 模型分析 → 调用 neo4j_find_relationship
    │
    ▼
Neo4jGraphTool.findRelationship()
    │
    ▼
Neo4jGraphService.findRelationship()
    │
    ▼
Neo4jQueryService.runQueryAsJson()
    │
    ▼
执行 Cypher: MATCH (s:Person)-[r:KNOWS]->(e:Person) ...
    │
    ▼
转换为 JSON 格式
    │
    ▼
返回: GRAPH_JSON: {"nodes":[...], "edges":[...]}
    │
    ▼
前端 GraphPanel 渲染可视化
```

### 图表生成流程

```
用户问题: "近10年考研人数变化趋势，画一个柱状图"
    │
    ▼
AI 模型分析 → 调用 generate_chart
    │
    ▼
GraphChartTool.generateChart()
    │
    ▼
GraphChartService.generateChart()
    │
    ├─► 解析问题意图
    ├─► 生成模拟数据（当前版本）
    ├─► 构建 ECharts 配置
    └─► 生成洞察摘要
    │
    ▼
返回: CHART_JSON: { ChartResponse ... }
    │
    ▼
前端 ChartPanel 渲染图表
```

---

## 🗄️ 数据存储

### Neo4j (知识图谱)

- **用途**: 存储实体和关系
- **连接**: `bolt://localhost:7687`
- **认证**: username: neo4j, password: ai_yunxun
- **配置**:
  ```yaml
  spring:
    neo4j:
      uri: bolt://localhost:7687
      authentication:
        username: neo4j
        password: ai_yunxun
  ```

### MongoDB (文档存储)

- **用途**: 存储论文原始文档
- **连接**: `localhost:27017`
- **数据库**: ai_yunxun
- **认证**: username: ai_yunxun, password: ai_yunxun
- **配置**:
  ```yaml
  spring:
    data:
      mongodb:
        host: localhost
        port: 27017
        username: ai_yunxun
        password: ai_yunxun
        database: ai_yunxun
  ```

### MySQL (关系数据库)

- **用途**: 业务数据存储（用户、日志等）
- **连接**: `jdbc:mysql://localhost:3306/ai_yunxun`
- **认证**: username: ai_yunxun, password: ai_yunxun
- **ORM**: JPA + MyBatis Plus
- **配置**:
  ```yaml
  spring:
    datasource:
      url: jdbc:mysql://localhost:3306/ai_yunxun
      username: ai_yunxun
      password: ai_yunxun
      driver-class-name: com.mysql.cj.jdbc.Driver
  ```

### Redis (缓存)

- **用途**: 缓存和会话管理
- **连接**: `localhost:6379`
- **认证**: password: ai_yunxun
- **配置**:
  ```yaml
  spring:
    data:
      redis:
        host: localhost
        port: 6379
        password: ai_yunxun
        database: 0
  ```

---

## 🤖 AI 集成

### Spring AI

- **版本**: 1.0.3
- **模型**: Ollama (qwen3:8b)
- **基础URL**: `http://localhost:11434`

### ChatClient

- **配置**: `McpClientConfig`
- **功能**:
  - 自然语言理解
  - 工具调用决策
  - 自然语言生成

### MCP 协议

- **服务器模式**: 将后端能力暴露为工具
- **客户端模式**: 前端通过 SSE 调用工具
- **工具类型**:
  - Tool: 可执行操作
  - Resource: 资源访问
  - Prompt: 提示词模板

---

## 📝 关键设计模式

### 1. 分层架构

- Controller → Service → Repository
- 职责清晰，便于维护

### 2. 工具委托模式

- MCP Tool 层只负责工具注册
- 实际逻辑委托给 Service 层
- 例如: `Neo4jGraphTool` → `Neo4jGraphService`

### 3. 两阶段对话处理

- 阶段一: 工具调用，获取结构化数据
- 阶段二: 基于数据生成自然语言回答
- 优点: 分离关注点，提高可控性

### 4. DTO 模式

- 请求/响应使用 DTO
- 数据库实体与 API 接口分离
- 提高安全性和灵活性

---

## 🔧 配置说明

### application.yml 关键配置

```yaml
server:
  port: 8080

spring:
  application:
    name: AI-Yunxun
  main:
    allow-circular-references: true

  # MySQL 配置
  datasource:
    url: jdbc:mysql://localhost:3306/ai_yunxun
    username: ai_yunxun
    password: ai_yunxun
    driver-class-name: com.mysql.cj.jdbc.Driver

  # MongoDB 配置
  data:
    mongodb:
      host: localhost
      port: 27017
      username: ai_yunxun
      password: ai_yunxun
      database: ai_yunxun

    # Redis 配置
    redis:
      host: localhost
      port: 6379
      password: ai_yunxun
      database: 0

  # Neo4j 配置
  neo4j:
    uri: bolt://localhost:7687
    authentication:
      username: neo4j
      password: ai_yunxun

  # Spring AI 配置
  ai:
    ollama:
      base-url: http://localhost:11434
      chat:
        options:
          model: qwen3:8b
    mcp:
      server:
        enabled: true
        sse-endpoint: /sse
        sse-message-endpoint: /mcp/message
      client:
        enabled: true
        servers:
          local-mcp:
            transport: sse
            sse:
              base-url: "http://localhost:8080"

# 日志配置
logging:
  level:
    root: INFO
    io.modelcontextprotocol: TRACE
    org.springframework.ai.mcp: TRACE
  file:
    name: logs/app.log
```

---

## 📊 依赖关系

### 核心依赖

```xml
<!-- Spring Boot Starters -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-aop</artifactId>
</dependency>

<!-- Spring AI -->
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-starter-model-ollama</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-starter-mcp-server-webmvc</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-starter-mcp-client</artifactId>
</dependency>

<!-- 数据库 -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-neo4j</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-mongodb</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-redis</artifactId>
</dependency>
<dependency>
    <groupId>com.baomidou</groupId>
    <artifactId>mybatis-plus-spring-boot3-starter</artifactId>
    <version>3.5.11</version>
</dependency>
<dependency>
    <groupId>mysql</groupId>
    <artifactId>mysql-connector-java</artifactId>
    <version>8.0.33</version>
</dependency>
```

---

## 🚀 扩展方向

### 1. 数据源集成

- 当前 `GraphChartService` 使用模拟数据
- 可集成真实数据源（Neo4j、MongoDB、MySQL）

### 2. 更多图表类型

- 支持更多 ECharts 图表类型
- 支持自定义图表配置

### 3. 向量检索

- 集成向量数据库（Chroma）
- 支持语义搜索

### 4. 智能查询服务

- 实现 `IntelligentQueryService`
- 支持意图分析和智能路由

### 5. 数据爬取服务

- 实现 `DataCrawlingService`
- 支持从 arXiv、CNKI 等爬取论文

---

## 🎯 总结

当前后端架构采用 **Spring Boot + Spring AI + MCP** 的技术组合，实现了：

1. **智能对话**: 通过两阶段处理，实现工具调用和自然语言生成的分离
2. **知识图谱**: 基于 Neo4j 的图谱查询和可视化
3. **图表生成**: 支持多种图表类型的生成和展示
4. **模块化设计**: 清晰的层次结构，便于扩展和维护
5. **多数据源**: 支持 Neo4j、MongoDB、MySQL、Redis 等多种存储

架构设计遵循了**单一职责原则**和**依赖倒置原则**，为后续功能扩展提供了良好的基础。
