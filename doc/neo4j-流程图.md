# Neo4j 模块流程图

下面给出 Neo4j 工具的两个核心流程：查询流程和写入流程。

## 查询流程（read-neo4j-cypher）

```mermaid
flowchart TD
    A[客户端发起查询请求] --> B[服务端接收查询请求]
    B --> C{是否为只读查询}
    C -- 包含写入关键词 --> D[返回错误：只支持只读查询]
    C -- 只读查询 --> E[执行查询语句]
    E --> F[执行去重处理（基于 name 属性）]
    F --> G[调用 APOC 合并同名节点]
    G --> H[返回查询结果]
```

说明：
- 查询流程会在读取后做去重，基于 `name` 属性合并节点（依赖 APOC）。
- 工具入口在 `mcp/server/neo4j/src/main/java/mcp/canary/neo4j/tool/Neo4jMCPTool.java`。

## 写入流程（write-neo4j-cypher）

```mermaid
flowchart TD
    A[客户端发起写入请求] --> B[服务端接收写入请求]
    B --> C[执行写入语句]
    C --> D[统计写入结果（节点/关系/属性）]
    D --> E[返回统计结果]
```

说明：
- 写入流程不依赖 APOC。
- 结果返回写入统计信息，便于前端展示写入结果。
