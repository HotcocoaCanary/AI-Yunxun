# ECharts MCP Server 系统架构设计

## 1. 系统设计


### 1.1 职责

- 作为 **MCP 服务�?*：接�?MCP 客户端的工具调用（generate_graph_chart）�?
- **构建 ECharts option**：根据传入的 nodes/edges 与布局等参数，生成符合 ECharts 规范�?JSON�?
- **可选：服务端渲�?*：当 outputType �?png �?svg 时，�?option 渲染为图像并返回（base64 �?MCP image content），使返回可直接嵌入 LLM 回复或前端无需 ECharts 即可展示�?
- **日志通知**：在工具执行各阶段通过 MCP `loggingNotification` 发�?INFO/ERROR，便于客户端展示“调用中/成功/失败”�?

### 1.2 架构示意

```mermaid
flowchart LR
  MCPClient[MCP Client]
  EChartTool[EChartMCPTool]
  GraphService[GraphChartService]
  Render[可�? 渲染服务/子进程]
  MCPClient -->|STDIO| EChartTool
  EChartTool --> GraphService
  EChartTool -->|option| Render
  Render -->|png/svg base64| EChartTool
  EChartTool -->|loggingNotification| MCPClient
  EChartTool -->|option �?image| MCPClient
```

---

## 2. 项目结构（目标状态）

精简后仅保留关系图相关代码：

```
mcp/server/echart/
├── pom.xml
└── src/main/
    ├── java/mcp/canary/echart/
    �?  ├── EchartApplication.java      # Spring Boot 入口
    �?  ├── model/
    �?  �?  ├── GraphData.java          # 图数据：nodes + edges
    �?  �?  ├── GraphNode.java          # 节点：id, name, value?, category?
    �?  �?  └── GraphEdge.java          # 边：source, target, value?
    �?  ├── service/
    �?  �?  ├── GraphChartService.java   # 关系�?option 构建
    �?  └── tool/
    �?      └── EChartMCPTool.java       # 仅保�?generate_graph_chart
    └── resources/
        └── application.yml
```

**待移�?*：BarChartService、LineChartService、PieChartService，以�?EChartMCPTool 中的 generateBarChart、generateLineChart、generatePieChart；若 DataItem 仅被上述图表使用可一并删除�?

---

## 3. 依赖

| 依赖 | 说明 |
|------|------|
| 父模�?`mcp/server` | Spring Boot 3.x、spring-ai-starter-mcp-server-webmvc、spring-boot-starter-web、lombok �?|
| Jackson（通常�?Spring Boot 带入�?| 用于构建 ObjectNode/ArrayNode �?JSON 序列�?|
| io.modelcontextprotocol / MCP 相关 | �?spring-ai-starter-mcp-server-webmvc 传�?|

- **outputType = option**：仅需 Jackson 构建 JSON，无需额外运行时�?
- **outputType = png / svg**：需**服务端渲�?*。Java �?ECharts 运行时，可选方案：�?）内�?调用 **Node 子进�?*（复�?example �?ECharts + canvas 渲染逻辑）；�?）独�?**Node 渲染服务**（HTTP 接收 option，返�?PNG/SVG），ECharts MCP �?Next.js 调用该服务；�?）Java 侧仅支持 option，由 Next.js API Route 在调�?MCP 拿到 option 后，再调渲染服务得到图像并返回前端。在架构中明确一种即可�?

---

## 4. MCP Tool 输入输出规范与通知逻辑

### 4.1 统一输入结构（两个工具共用）

| 参数�?| 类型 | 必填 | 说明 |
|--------|------|------|------|
| **title** | string | �?| 图表标题 |
| **data** | object | �?| 图数据，见下�?|
| **data.nodes** | array | �?| 节点列表，至�?1 个。每项：`id`(string)、`name`(string)、`value`(number, 可�?、`category`(string, 可�? |
| **data.edges** | array | �?| 边列表，默认 []。每项：`source`(string)、`target`(string)、`value`(number, 可�? |
| **layout** | string | �?| 布局：`force` / `circular` / `none`，默�?`force` |
| **width** | number | �?| 画布宽度（像素），渲�?png/svg 时有效，默认 800 |
| **height** | number | �?| 画布高度（像素），默�?600 |
| **theme** | string | �?| 主题：`default` / `dark`，默�?`default` |
| **outputType** | string | �?| 输出类型：`option` / `png` / `svg`，默�?`option` |

**data 示例**�?

```json
{
  "nodes": [
    { "id": "a", "name": "A", "category": "类型1" },
    { "id": "b", "name": "B", "value": 10 }
  ],
  "edges": [
    { "source": "a", "target": "b", "value": 1 }
  ]
}
```

### 4.2 统一输出规范

| outputType | MCP 返回格式 | 用�?|
|------------|--------------|------|
| **png** | `content: [{ type: "image", data: "<base64>", mimeType: "image/png" }]` �?`type: "text", text: "data:image/png;base64,..."` | 可直接嵌�?LLM 回复（如 Markdown 图片）、前端用 `<img src="data:image/png;base64,...">` 展示�?*无需前端 ECharts** |
| **svg** | `content: [{ type: "text", text: "<SVG 字符�?" }]` �?image 类型�?png | 同上，SVG 可缩放、体积小 |

当支�?png/svg 时，返回可直接被大模型或前端使用，降低前端开发量；前端也可不通过大模型，直接请求“图谱工具接口”（�?Next.js 文档）传入相同参数获�?option �?image 并展示�?

### 4.3 generate_graph_chart

| Item | Description |
|------|-------------|
| **name** | `generate_graph_chart` |
| **description** | Generate a graph option for ECharts. |
| **input** | See 4.1 unified input. |
| **output** | See 4.2 unified output. |
\n\n---

## 5. 通知发送逻辑（统一�?

- **入口**：每个工具方法均接收 `McpSyncServerExchange exchange`�?
- **发送时�?*：工具开始执行、关键步骤（如“正在处理数据”）、成功结束、异常时�?
- **实现**：`sendLog(exchange, LogginGLevel.INFO|ERROR, message)`，内部调�?`exchange.loggingNotification(LoggingMessageNotification.builder().level(...).logger("echart-tool").data(message).build())`�?
- **logger 名称**：`echart-tool`，便于客户端�?logger 区分 ECharts 工具并展示状态�?

---

## 6. 服务端渲染（outputType = png / svg）实现思路

为使返回**可直接嵌入大模型回复或前端用 img 展示**，建议支�?outputType �?png �?svg。Java �?ECharts 运行时，可选方案：

| 方案 | 说明 |
|------|------|
| **A. Java 调用 Node 子进�?* | Java 构建�?option 后，spawn Node 脚本（复�?example �?ECharts + @napi-rs/canvas 渲染逻辑），传入 option �?width/height/theme，脚本返�?base64 PNG/SVG 或写入临时文件；Java 将结果放�?MCP 响应�?|
| **B. 独立 Node 渲染服务** | 单独起一�?Node HTTP 服务（如 POST /render，body �?option + outputType），返回 PNG/SVG �?base64。ECharts MCP（Java）在 outputType �?png/svg 时，�?option 请求该服务，把得到的图像放入 MCP 响应；或�?Next.js API Route 在拿�?option 后请求该服务，再返回给前端�?|
| **C. �?Java 返回 option，由 Next.js 负责渲染** | ECharts MCP 只支�?outputType=option；Next.js 提供单独接口（如 POST /api/tools/echart/render），接收 option + outputType，在 Node 侧用 ECharts 渲染�?PNG/SVG 再返回。大模型嵌入时可�?Next.js 在流式回复中调用该接口得�?image 再推送�?|

任选其一并在实现中统一；A �?B 使“返回可直接嵌入”在 MCP 层完成，C 使前�?LLM 侧仍只需对接 Next.js�?

---

## 7. 配置

当前无特殊业务配置；若需端口或日志级别，可在 `application.yml` 中按 Spring Boot 惯例配置。若采用服务端渲染方�?A，需配置 Node 可执行路径及脚本路径；若采用 B，需配置渲染服务 URL。服务以 MCP STDIO 模式运行，由 MCP 客户端（�?Next.js API Route）启动子进程并连接�?
