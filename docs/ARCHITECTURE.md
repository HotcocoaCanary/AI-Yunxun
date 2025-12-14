# AI-Yunxun 架构文档

## 概述

本文档详细说明 AI-Yunxun 系统的架构设计，重点关注 Spring Boot 3 + WebFlux 响应式架构的实现。

## 技术栈

### 核心框架
- **Spring Boot**: 3.5.7
- **Spring WebFlux**: 响应式 Web 框架
- **Spring AI**: 1.0.3 (MCP 协议支持)
- **Java**: 17

### 响应式编程
- **Reactor Core**: Mono/Flux 响应式编程模型
- **Reactor Extra**: 响应式工具库

### 数据库
- **MongoDB**: ReactiveMongoRepository (完全响应式)
- **Neo4j**: Spring Data Neo4j (阻塞操作包装为响应式)
- **Redis**: ReactiveRedisTemplate (完全响应式)
- **MySQL**: JPA (阻塞，可选包装为响应式)

### AI 集成
- **智谱AI SDK**: GLM-4.5-Flash
- **MCP Protocol**: Spring AI MCP Server (WebFlux SSE)

## 架构设计

### 分层架构

```
┌─────────────────────────────────────────┐
│         API 层 (Controller)              │
│  - McpChatController                    │
│  - 返回 Mono/Flux                       │
└─────────────────────────────────────────┘
                  │
                  ▼
┌─────────────────────────────────────────┐
│         Service 层                       │
│  - McpChatService                       │
│  - ZhipuAiChatAdapter                   │
│  - GraphChartService                    │
│  - 使用 Mono/Flux 编排业务逻辑           │
└─────────────────────────────────────────┘
                  │
        ┌─────────┴─────────┐
        ▼                   ▼
┌──────────────┐   ┌─────────────────────┐
│ Repository   │   │   Infrastructure    │
│  层          │   │   层                │
│              │   │                     │
│ - Reactive   │   │ - Neo4jGraphService │
│   MongoRepo  │   │ - Neo4jQueryService │
│              │   │ - 响应式包装方法     │
└──────────────┘   └─────────────────────┘
        │                   │
        ▼                   ▼
┌─────────────────────────────────────────┐
│           数据库层                       │
│  - MongoDB (响应式)                     │
│  - Neo4j (阻塞包装)                     │
│  - Redis (响应式)                       │
│  - MySQL (阻塞)                         │
└─────────────────────────────────────────┘
```

### MCP 工具层

```
┌─────────────────────────────────────────┐
│         MCP Server (WebFlux SSE)        │
│  - /sse                                  │
│  - /mcp/message                         │
└─────────────────────────────────────────┘
                  │
                  ▼
┌─────────────────────────────────────────┐
│         Tool 层                          │
│  ┌──────────┐  ┌──────────┐  ┌──────┐  │
│  │ MongoTool│  │ Neo4jTool│  │EChart│  │
│  │          │  │          │  │Tool  │  │
│  │响应式操作│  │响应式包装│  │同步  │  │
│  └──────────┘  └──────────┘  └──────┘  │
└─────────────────────────────────────────┘
```

## 响应式编程模型

### Mono 和 Flux

- **Mono**: 表示 0 或 1 个元素的异步序列
- **Flux**: 表示 0 到 N 个元素的异步序列

### 数据流示例

#### 同步方法 → 响应式方法

```java
// 阻塞方法
public String findNode(String label, String key, String value) {
    // 阻塞操作
    return neo4jClient.query(cypher).fetch().one();
}

// 响应式包装
public Mono<String> findNodeReactive(String label, String key, String value) {
    return Mono.fromCallable(() -> findNode(label, key, value))
            .subscribeOn(Schedulers.boundedElastic());
}
```

#### 响应式 Repository

```java
// MongoDB Repository
public interface RawPaperDocumentRepository 
        extends ReactiveMongoRepository<RawPaperDocument, String> {
    Flux<RawPaperDocument> findByTopic(String topic);
}

// 使用
public Mono<String> findByTopic(String topic) {
    return repository.findByTopic(topic)
            .collectList()
            .map(docs -> objectMapper.writeValueAsString(docs));
}
```

## MCP 工具实现

### 工具方法签名

MCP Tool 方法必须返回 `String`（这是 Spring AI 的限制）。因此，我们：

1. 在工具方法内部使用响应式操作
2. 使用 `block()` 等待结果（在工具方法内是允许的）
3. 或者使用 `Mono.fromCallable()` 包装阻塞操作

### 示例：MongoTool

```java
@Tool(name = "mongo_find_by_topic")
public String findByTopic(@ToolParam String topic) {
    // 使用响应式 Repository，但需要 block() 转换为 String
    List<RawPaperDocument> docs = repository.findByTopic(topic)
            .collectList()
            .block(Duration.ofSeconds(10));
    
    return objectMapper.writeValueAsString(docs);
}
```

### 示例：Neo4jCrudTool

```java
@Tool(name = "neo4j_find_node")
public String findNode(@ToolParam String label, ...) {
    // 调用响应式包装方法
    return graphService.findNodeReactive(label, ...)
            .block(Duration.ofSeconds(10));
}
```

## Controller 层

### 响应式 Controller

```java
@RestController
@RequestMapping("/api/chat")
public class McpChatController {
    
    // 同步接口 → 响应式接口
    @PostMapping
    public Mono<ChatResponse> chat(@RequestBody Mono<ChatRequest> requestMono) {
        return requestMono
                .flatMap(request -> chatService.chatReactive(request.getMessage()))
                .map(result -> convertToResponse(result));
    }
    
    // 流式接口（已支持）
    @PostMapping("/stream")
    public Flux<ServerSentEvent<String>> chatStream(
            @RequestBody Mono<ChatRequest> requestMono) {
        return requestMono
                .flatMapMany(request -> chatService.chatStream(request.getMessage()))
                .map(this::convertToSSE);
    }
}
```

## Service 层

### 响应式服务方法

```java
@Service
public class McpChatService {
    
    // 添加响应式版本
    public Mono<ChatResult> chatReactive(String message) {
        return Mono.fromCallable(() -> chat(message))
                .subscribeOn(Schedulers.boundedElastic());
    }
    
    // 流式方法（已支持）
    public Flux<ChatStreamEvent> chatStream(String message) {
        // 已实现，返回 Flux
    }
}
```

## 数据库访问

### MongoDB (完全响应式)

```java
@Repository
public interface RawPaperDocumentRepository 
        extends ReactiveMongoRepository<RawPaperDocument, String> {
    Flux<RawPaperDocument> findByTopic(String topic);
}
```

### Neo4j (阻塞包装为响应式)

```java
@Service
public class Neo4jGraphService {
    
    // 阻塞方法（保留）
    public String findNode(String label, ...) {
        // 使用 Neo4jClient（阻塞）
    }
    
    // 响应式包装
    public Mono<String> findNodeReactive(String label, ...) {
        return Mono.fromCallable(() -> findNode(label, ...))
                .subscribeOn(Schedulers.boundedElastic());
    }
}
```

### Redis (完全响应式)

```java
@Service
public class CacheService {
    private final ReactiveRedisTemplate<String, Object> redisTemplate;
    
    public Mono<Void> set(String key, Object value) {
        return redisTemplate.opsForValue().set(key, value);
    }
}
```

## 错误处理

### 响应式错误处理

```java
return chatService.chatReactive(message)
        .onErrorResume(e -> {
            log.error("Chat failed", e);
            return Mono.just(ChatResult.error("处理失败: " + e.getMessage()));
        })
        .timeout(Duration.ofSeconds(30));
```

## 性能优化

### 调度器使用

- **Schedulers.immediate()**: 当前线程执行
- **Schedulers.boundedElastic()**: 用于阻塞操作（推荐用于数据库、HTTP 调用）
- **Schedulers.parallel()**: CPU 密集型任务
- **Schedulers.single()**: 单线程顺序执行

### 最佳实践

1. **阻塞操作包装**: 使用 `Mono.fromCallable()` + `subscribeOn(Schedulers.boundedElastic())`
2. **避免在响应式链中 block()**: 仅在工具方法内部使用
3. **使用超时**: `timeout(Duration.ofSeconds(30))`
4. **背压处理**: Flux 自动处理背压

## 流式输出

### Server-Sent Events (SSE)

```java
@PostMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
public Flux<ServerSentEvent<String>> chatStream(@RequestBody Mono<ChatRequest> request) {
    return request
            .flatMapMany(req -> chatService.chatStream(req.getMessage()))
            .map(event -> ServerSentEvent.<String>builder()
                    .event(event.type().name().toLowerCase())
                    .data(serialize(event))
                    .build());
}
```

## 配置

### application.yml

```yaml
spring:
  application:
    name: AI-Yunxun
  
  # MongoDB 响应式配置
  data:
    mongodb:
      host: localhost
      port: 27017
      # 自动使用响应式驱动
  
  # Redis 响应式配置
  data:
    redis:
      host: localhost
      port: 6379
      # Lettuce 自动使用响应式
  
  # Neo4j 配置（阻塞，但包装为响应式）
  neo4j:
    uri: bolt://localhost:7687
  
  # MCP Server (WebFlux)
  ai:
    mcp:
      server:
        enabled: true
        sse-endpoint: /sse
        sse-message-endpoint: /mcp/message
```

## 测试

### 响应式测试

```java
@SpringBootTest
class McpChatServiceTest {
    
    @Test
    void testChatReactive() {
        StepVerifier.create(chatService.chatReactive("Hello"))
                .expectNextMatches(result -> result.replyText() != null)
                .verifyComplete();
    }
}
```

## 总结

AI-Yunxun 采用 Spring Boot 3 + WebFlux 响应式架构，提供了：

1. **非阻塞 I/O**: 提高并发性能
2. **流式输出**: 基于 SSE 的实时响应
3. **响应式数据库**: MongoDB 和 Redis 完全响应式
4. **阻塞操作包装**: Neo4j 等阻塞操作通过调度器包装为响应式
5. **MCP 集成**: 基于 WebFlux SSE 的 MCP Server

这种架构特别适合需要高并发和实时流式输出的场景。

