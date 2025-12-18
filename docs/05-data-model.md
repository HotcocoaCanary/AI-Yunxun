# 05. 数据存储设计（草案）

## 存储选型与职责建议

- **MongoDB**：文档型数据（对话记录、知识条目、工具执行记录、缓存搜索结果等）
- **Neo4j**：图谱数据（实体/关系/路径检索）
- **MySQL**：结构化业务数据（账号/配置/任务/权限等；本阶段可暂不落库）
- **Redis**：缓存与会话状态（短期记忆、限流计数、热点查询缓存）

## Mongo（建议集合）

1. `chat_sessions`
   - `sessionId`、`createdAt`、`updatedAt`、`summary?`
2. `chat_messages`
   - `sessionId`、`role(user|assistant|tool)`、`content`、`toolName?`、`toolArgs?`、`toolResult?`、`createdAt`
3. `documents`
   - `id`、`topic`、`content`、`tags?`、`source?`、`createdAt`

## Neo4j（建议标签/关系）

> 先定义最小可用模型，后续再扩展。

- 节点（Labels）
  - `Entity { name, type, aliases? }`
  - `Document { id, topic, source? }`
- 关系（Relationships）
  - `(Document)-[:MENTIONS]->(Entity)`
  - `(Entity)-[:RELATED_TO { reason? }]->(Entity)`

## Redis（建议 key）

- `chat:session:{sessionId}:context`：短期上下文/摘要缓存（TTL）
- `rate:ip:{ip}`：限流计数器
- `cache:web_search:{hash(query)}`：搜索缓存（TTL）

