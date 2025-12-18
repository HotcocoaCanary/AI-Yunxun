# 04. MCP 工具规格（草案）

目标：把“数据库检索/存储、联网搜索、图表生成”抽象为可被模型调用的 MCP Tools，并保持参数/返回稳定、可测试、可审计。

本规范优先约束 **工具输入/输出**，实现阶段再落到：

- MCP Server：`src/main/java/yunxun/ai/canary/project/service/mcp/server/**`
- DB 访问：`src/main/java/yunxun/ai/canary/project/repository/**`

## 0. 统一约定

### 0.1 命名与版本

- 工具命名：`snake_case`
- 工具语义稳定后再考虑版本化：`xxx_v2` 或在 `description` 中声明版本

### 0.2 通用返回结构（建议）

所有工具返回统一 envelope，便于前端/日志聚合与回放：

```json
{
  "ok": true,
  "data": {},
  "error": null,
  "meta": { "traceId": "optional", "tookMs": 12 }
}
```

错误时：

```json
{
  "ok": false,
  "data": null,
  "error": { "code": "INVALID_ARGUMENT", "message": "topic 不能为空", "detail": {} },
  "meta": { "traceId": "optional" }
}
```

### 0.3 通用限制（建议）

- `maxResults` 默认 5，最大 20（避免上下文爆炸）
- 单次返回 JSON 建议 ≤ 200KB
- 工具执行超时默认 5s（搜索可放宽到 10s）

### 0.4 通用错误码（建议）

- `INVALID_ARGUMENT`：参数缺失/格式错误/超出限制
- `NOT_FOUND`：查询无结果
- `DB_ERROR`：数据库异常（连接、超时、语法等）
- `UPSTREAM_ERROR`：外部依赖异常（搜索服务/第三方 API）
- `FORBIDDEN`：命中安全策略（越权写入、非法 label/type 等）

## 1. MongoDB 工具（文档存储/检索）

用途：存对话、知识条目、工具调用记录、搜索缓存等“文档型数据”。

安全建议：工具层不直接暴露“任意 collection 任意写入”，先限制在可控的数据模型内（如 `documents`/`chat_messages`）。

### 1.1 数据类型（工具视角）

`Document`（建议）：

```json
{
  "id": "string",
  "topic": "string",
  "content": "string",
  "tags": ["string"],
  "source": { "type": "web|user|system", "url": "string?", "title": "string?" },
  "createdAt": "RFC3339",
  "updatedAt": "RFC3339"
}
```

### 1.2 工具：`mongo_save_document`

用途：新增一条文档（知识条目/长文本片段/总结等）。

输入：

```json
{ "topic": "string", "content": "string", "tags": ["string"], "source": { } }
```

输出 `data`：

```json
{ "id": "string" }
```

约束：

- `topic`、`content` 必填
- `content` 建议限制长度（如 ≤ 20k 字符）

### 1.3 工具：`mongo_find_by_topic`

用途：按主题检索（适合“我之前存过的某个知识/结论”）。

输入：

```json
{ "topic": "string", "limit": 5 }
```

输出 `data`：

```json
{ "items": [ { "id": "…", "topic": "…", "content": "…", "tags": [] } ] }
```

### 1.4 工具：`mongo_find_by_id`

输入：

```json
{ "id": "string" }
```

输出 `data`：`Document`

### 1.5 工具：`mongo_update_document`

输入：

```json
{ "id": "string", "patch": { "topic": "string?", "content": "string?", "tags": ["string"] } }
```

输出 `data`：

```json
{ "updated": true }
```

### 1.6 工具：`mongo_delete_document`

输入：

```json
{ "id": "string" }
```

输出 `data`：

```json
{ "deleted": true }
```

### 1.7 工具：`mongo_find_all`（可选/调试）

输入：

```json
{ "limit": 20 }
```

输出 `data`：

```json
{ "items": [ { "id": "…", "topic": "…", "content": "…" } ] }
```

## 2. Neo4j 工具（图谱存储/检索）

用途：存实体/关系网络，支持路径查询、邻居扩展、模糊匹配等。

安全建议：

- label/type 建议白名单化（如 `Entity`/`Document`，`RELATED_TO`/`MENTIONS`）
- 不允许模型执行任意 Cypher（除非在强审计/强隔离环境）

### 2.1 工具：`neo4j_create_node`

输入：

```json
{ "label": "Entity", "properties": { "name": "string", "type": "string" } }
```

输出 `data`：

```json
{ "id": "string", "label": "Entity", "properties": { } }
```

> 备注：建议使用稳定 `id`（UUID）作为节点主键，而不是 Neo4j 内部 id。

### 2.2 工具：`neo4j_find_node`

输入（二选一）：

```json
{ "id": "string" }
```

或

```json
{ "label": "Entity", "property": "name", "value": "北京" }
```

输出 `data`：

```json
{ "id": "string", "label": "Entity", "properties": { } }
```

### 2.3 工具：`neo4j_update_node`

输入：

```json
{ "id": "string", "patch": { "aliases": ["string"] } }
```

输出 `data`：

```json
{ "updated": true }
```

### 2.4 工具：`neo4j_delete_node`

输入：

```json
{ "id": "string", "detach": true }
```

输出 `data`：

```json
{ "deleted": true }
```

### 2.5 工具：`neo4j_create_relationship`

输入：

```json
{
  "fromId": "string",
  "toId": "string",
  "type": "RELATED_TO",
  "properties": { "reason": "string?" }
}
```

输出 `data`：

```json
{ "id": "string" }
```

### 2.6 工具：`neo4j_find_relationship`

输入：

```json
{ "id": "string" }
```

输出 `data`：

```json
{ "id": "string", "type": "RELATED_TO", "fromId": "string", "toId": "string", "properties": { } }
```

### 2.7 工具：`neo4j_update_relationship`

输入：

```json
{ "id": "string", "patch": { "reason": "string" } }
```

输出 `data`：

```json
{ "updated": true }
```

### 2.8 工具：`neo4j_delete_relationship`

输入：

```json
{ "id": "string" }
```

输出 `data`：

```json
{ "deleted": true }
```

### 2.9 工具：`neo4j_find_path`

输入：

```json
{ "fromId": "string", "toId": "string", "maxDepth": 4, "types": ["RELATED_TO", "MENTIONS"] }
```

输出 `data`（示例）：

```json
{ "nodes": [ { "id": "…" } ], "relationships": [ { "id": "…" } ] }
```

### 2.10 工具：`neo4j_find_neighbors`

输入：

```json
{ "id": "string", "depth": 1, "types": ["RELATED_TO"] }
```

输出 `data`：

```json
{ "nodes": [ { "id": "…" } ], "relationships": [ { "id": "…" } ] }
```

### 2.11 工具：`neo4j_fuzzy_search`

输入：

```json
{ "query": "string", "labels": ["Entity"], "limit": 10 }
```

输出 `data`：

```json
{ "items": [ { "id": "…", "label": "Entity", "properties": { }, "score": 0.82 } ] }
```

> 备注：实现可基于 Neo4j Full-Text Index；需要初始化索引与字段策略。

## 3. Web Search 工具（联网搜索）

用途：获取实时信息；返回结构化结果，便于模型引用与前端展示。

### 3.1 工具：`web_search`

输入：

```json
{ "query": "string", "maxResults": 5, "language": "zh-CN", "recencyDays": 30 }
```

输出 `data`：

```json
{
  "items": [
    { "title": "…", "url": "…", "snippet": "…", "source": "searxng", "publishedAt": "RFC3339?" }
  ]
}
```

实现策略（两种选一）：

1. **本地实现**：启用 `web.search.enabled=true` 后注册 `WebSearchTool`（适合快速自研/简单爬取）
2. **外部 MCP**：对接 `mcp-searxng`，由 MCP Client 调用外部工具（推荐，隔离外部不确定性）

## 4. ECharts 工具（图表生成）

用途：把结构化数据转成 ECharts `option`，前端直接渲染；工具负责“补默认值/纠错/规整字段”。

### 4.1 工具：`echart_generate`

输入（通用）：

```json
{
  "chartType": "bar|line|pie|scatter|graph",
  "title": "string?",
  "data": [ { "x": "…", "y": 1 } ],
  "mapping": { "xField": "x", "yField": "y", "seriesField": "category?" },
  "options": { "unit": "string?", "stack": false, "smooth": false }
}
```

输出 `data`：

```json
{ "option": { } }
```

特殊：`graph` 类型（图谱可视化）输入建议为：

```json
{
  "chartType": "graph",
  "title": "string?",
  "graph": {
    "nodes": [ { "id": "string", "name": "string", "category": "string?" } ],
    "links": [ { "source": "string", "target": "string", "label": "string?" } ]
  }
}
```

## 5. 实现落点（与当前代码对齐）

- 工具注册：`src/main/java/yunxun/ai/canary/project/service/mcp/server/config/McpServerConfig.java`
- 工具类（待实现方法并加 `@Tool/@ToolParam`）：
  - `src/main/java/yunxun/ai/canary/project/service/mcp/server/tool/MongoTool.java`
  - `src/main/java/yunxun/ai/canary/project/service/mcp/server/tool/Neo4jTool.java`
  - `src/main/java/yunxun/ai/canary/project/service/mcp/server/tool/WebSearchTool.java`
  - `src/main/java/yunxun/ai/canary/project/service/mcp/server/tool/EChartGenerateTool.java`

