# 文档索引

## 总体设计

- **[system-design.md](system-design.md)**  
  总体系统设计：三个模块（Neo4j MCP Server、ECharts MCP Server、Next.js 客户端）的职责与配合、数据流、项目依赖（Java 17、Node.js v22、Neo4j 等）�?

## 模块架构设计

- **[neo4j-mcp-server-architecture.md](neo4j-mcp-server-architecture.md)**  
  Neo4j MCP Server：系统设计、项目结构、依赖、MCP Tool（get-neo4j-schema、read-neo4j-cypher、write-neo4j-cypher）的输入输出与通知发送逻辑�?

- **[echart-mcp-server-architecture.md](echart-mcp-server-architecture.md)**  
  ECharts MCP Server：系统设计、项目结构、依赖、MCP Tool（generate_graph_chart、generate_graph_gl_chart）的输入输出与通知发送逻辑�?

- **[nextjs-client-architecture.md](nextjs-client-architecture.md)**  
  Next.js 客户端：页面布局与组件、接�?MCP 的逻辑、接�?SSE 通知的逻辑（status、text、chart、tool_log）�?

## 技术方案（计划�?

- 技术方案与实施顺序见项目根目录下的计划书（�?`.cursor/plans/` �?Cursor 计划面板中的「MCP 双服务器与前端客户端技术方案」），与上述 doc 文档一致�?
