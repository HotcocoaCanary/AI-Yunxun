# AI-云寻

AI-云寻是一个基于 MCP 协议的项目，目标是让大模型更容易用上数据分析工具。现在的做法是：用 MCP Java SDK 写 MCP server，用 MCP TypeScript SDK 写 MCP client，用 TS 代码把一轮对话里的“模型 -> 工具 -> 再次模型”串起来。模型侧用智谱 `glm-4.7-flash`。本质上就是用 function call 让模型调用工具，MCP 把这套流程的细节规范化。

通信走 SSE。MCP server 可以部署在服务器上。目前不支持 HTTP 协议和 stdio 协议。

当前内置工具：
- Neo4j 操作工具
- ECharts 关系图工具（后续可能扩展）

MCP 官方开发文档：https://modelcontextprotocol.io/docs

更细的结构和设计在下面这些文档里：
- `mcp/client/README.md`
- `mcp/server/neo4j/README.md`
- `mcp/server/echart/README.md`
