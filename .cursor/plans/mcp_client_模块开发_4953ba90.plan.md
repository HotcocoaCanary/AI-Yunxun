---
name: MCP Client 模块开发
overview: 开发一个MCP客户端模块，实现大模型对话（GLM-4.5-Flash深度思考+流式输出）、MCP服务器动态管理、EChart图表展示、工具调用状态感知等功能。
todos:
  - id: "1"
    content: 创建client模块基础结构（pom.xml, application.yml, ClientApplication.java）
    status: completed
  - id: "2"
    content: 实现智谱AI配置（ZhipuAiConfig.java）和基础对话服务（ChatService.java）
    status: completed
    dependencies:
      - "1"
  - id: "3"
    content: 实现MCP服务器管理（MCPService.java, MCPServerConfig.java, mcp-servers.json）
    status: completed
    dependencies:
      - "1"
  - id: "4"
    content: 实现MCP客户端连接服务（MCPClientService.java），注册日志消费者
    status: completed
    dependencies:
      - "3"
  - id: "5"
    content: 实现对话控制器（ChatController.java），支持SSE流式输出
    status: completed
    dependencies:
      - "2"
      - "4"
  - id: "6"
    content: 实现MCP管理控制器（MCPController.java），支持动态添加/删除服务器
    status: completed
    dependencies:
      - "3"
  - id: "7"
    content: 实现WebSocket配置（WebSocketConfig.java），推送工具日志到前端
    status: completed
    dependencies:
      - "4"
  - id: "8"
    content: 开发前端界面（index.html, style.css, app.js），实现对话和状态展示
    status: completed
    dependencies:
      - "5"
      - "7"
  - id: "9"
    content: 集成EChart图表渲染，支持在对话中显示和交互
    status: completed
    dependencies:
      - "8"
---

# MCP Client 模块开发计划

## 项目结构

```
mcp/
├── server/          # 现有server模块
└── client/          # 新建client模块
    ├── pom.xml
    └── src/main/
        ├── java/mcp/canary/client/
        │   ├── ClientApplication.java
        │   ├── config/
        │   │   ├── ZhipuAiConfig.java          # 智谱AI配置
        │   │   └── WebSocketConfig.java        # WebSocket配置（用于日志推送）
        │   ├── controller/
        │   │   ├── ChatController.java         # 对话API
        │   │   ├── MCPController.java         # MCP服务器管理API
        │   │   └── WebSocketController.java   # WebSocket端点
        │   ├── service/
        │   │   ├── ChatService.java           # 对话服务（集成GLM-4.5-Flash）
        │   │   ├── MCPService.java            # MCP服务器管理服务
        │   │   └── MCPClientService.java      # MCP客户端连接服务
        │   ├── model/
        │   │   ├── ChatMessage.java           # 对话消息模型
        │   │   ├── MCPServerConfig.java       # MCP服务器配置模型
        │   │   └── ToolLogEvent.java          # 工具日志事件
        │   └── dto/
        │       ├── ChatRequest.java
        │       └── ChatResponse.java
        └── resources/
            ├── application.yml
            ├── mcp-servers.json                # MCP服务器配置文件
            └── static/
                ├── index.html                  # 主页面
                ├── css/
                │   └── style.css
                └── js/
                    └── app.js                  # 前端逻辑
```

## 核心功能实现

### 1. 依赖配置 (pom.xml)

- Spring Boot Web
- Spring AI ZhipuAI (智谱平台集成)
- Spring AI MCP Client (MCP客户端)
- Spring WebSocket (日志推送)
- Jackson (JSON处理)
- Lombok

### 2. 智谱AI集成 (ZhipuAiConfig.java)

配置GLM-4.5-Flash模型，支持：

- 深度思考功能（reasoning参数）
- 流式输出（stream=true）
- API Key: da2a344e9ab04b5da76e7e352893b412.AjEnXNNc2YG0aBHm

### 3. MCP服务器管理 (MCPService.java)

- 读取/写入 `mcp-servers.json` 配置文件
- 支持动态添加/删除MCP服务器
- MCP服务器配置格式：
```json
{
  "name": "echart-server",
  "url": "http://localhost:8081",
  "protocol": "SSE"
}
```


### 4. MCP客户端连接 (MCPClientService.java)

- 使用Spring AI MCP Client连接MCP服务器
- 注册日志消费者（loggingConsumer）接收工具调用日志
- 将日志事件发布为Spring事件，通过WebSocket推送到前端

### 5. 对话服务 (ChatService.java)

- 使用Spring AI的ZhiPuAiChatModel进行对话
- 支持流式输出（SSE）
- 检测深度思考状态（从流式响应中的reasoning_content字段）
- 处理工具调用（tool_calls），调用MCP服务器工具
- 识别EChart图表数据并标记

### 6. 对话控制器 (ChatController.java)

- `POST /api/chat` - 发送消息，返回SSE流
- SSE流包含：
  - 普通文本内容
  - 深度思考内容（reasoning_content）
  - 工具调用状态
  - 图表数据

### 7. MCP管理控制器 (MCPController.java)

- `GET /api/mcp/servers` - 获取MCP服务器列表
- `POST /api/mcp/servers` - 添加MCP服务器
- `DELETE /api/mcp/servers/{id}` - 删除MCP服务器

### 8. WebSocket配置 (WebSocketConfig.java)

- 配置WebSocket端点 `/ws`
- 监听工具日志事件，推送到前端
- 前端订阅 `/topic/tool-logs` 接收工具调用日志

### 9. 前端实现 (index.html + app.js)

极简聊天界面：

- 消息展示区域
- 输入框和发送按钮
- 工具调用状态栏（显示日志）
- 深度思考状态指示器
- EChart图表渲染区域

功能：

- 通过EventSource接收SSE流式消息
- 通过WebSocket接收工具调用日志
- 识别深度思考内容（reasoning_content前缀）
- 检测图表数据并渲染EChart
- 图表交互（缩放、点击等）

### 10. 配置文件 (application.yml)

```yaml
server:
  port: 8082

spring:
  ai:
    zhipuai:
      api-key: da2a344e9ab04b5da76e7e352893b412.AjEnXNNc2YG0aBHm
      chat:
        options:
          model: glm-4.5-flash
          temperature: 0.7
          stream: true
```

## 关键技术点

1. **深度思考检测**：从SSE流中识别`reasoning_content`字段，前端显示"思考中..."状态
2. **工具调用日志**：MCP客户端注册`loggingConsumer`，接收server端发送的日志通知
3. **图表识别**：检测响应中的EChart option JSON，自动渲染图表
4. **流式处理**：使用SseEmitter实现SSE流式输出

## 开发顺序

1. 创建client模块基础结构（pom.xml, application.yml）
2. 实现智谱AI配置和基础对话功能
3. 实现MCP服务器管理（JSON配置读写）
4. 实现MCP客户端连接和日志接收
5. 实现SSE流式对话接口
6. 实现WebSocket日志推送
7. 开发前端界面（HTML+CSS+JS）
8. 集成EChart图表渲染
9. 测试完整流程

## 注意事项

- MCP服务器配置使用标准JSON格式
- 前端优先使用原生HTML+CSS+JS，避免引入框架
- 深度思考状态通过检测流式输出中的特定字段识别
- 工具调用日志通过WebSocket实时推送
- 图表在对话消息中直接渲染，支持交互功能