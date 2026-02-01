# Neo4j MCP Server

基于 MCP 的 Neo4j 数据库工具服务，提供 `get-neo4j-schema`、`read-neo4j-cypher`、`write-neo4j-cypher` 三个工具。

## 依赖说明

### get-neo4j-schema 与 APOC 插件

工具 **get-neo4j-schema** 通过 Cypher 调用 `apoc.meta.data()` 和 `apoc.map.fromPairs()` 获取数据库节点类型、属性及关系信息，因此**依赖 Neo4j 的 APOC 插件**。

- **未安装 APOC 时**：调用 `get-neo4j-schema` 会执行失败，Neo4j 将返回与 APOC 过程不存在相关的错误（如 "Unknown function" 或 "Unknown procedure"），MCP 工具会将该异常抛出并记录 ERROR 日志。
- **安装方式**：
  - Neo4j 4.x/5.x 企业版或 Desktop 通常已预装或可从插件市场安装。
  - 社区版：从 [Neo4j APOC 文档](https://neo4j.com/labs/apoc/) 下载与 Neo4j 版本匹配的 APOC jar，放入 Neo4j 的 `plugins` 目录，重启 Neo4j。
  - Docker：在镜像或 docker-compose 中挂载 APOC 插件并确保 Neo4j 启动时加载。

`read-neo4j-cypher` 与 `write-neo4j-cypher` 不依赖 APOC，仅需正常 Neo4j 连接即可。

## 连接配置

Neo4j 连接由 `src/main/resources/application.yml` 中的以下四项配置（见该文件内注释）：

- **neo4j.uri**：Neo4j 连接 URI（如 `neo4j://localhost:7687`）
- **neo4j.username**：用户名
- **neo4j.password**：密码
- **neo4j.database**：数据库名（Neo4j 4.x 默认为 `neo4j`）

修改上述配置后重启本服务即可切换连接的 Neo4j 实例；若连接失败，启动或首次执行 Cypher 时会抛出连接异常。
