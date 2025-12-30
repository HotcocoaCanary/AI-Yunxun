# MCP ECharts 工具文档

本文档详细说明 mcp-echarts 项目提供的所有 MCP 工具，从 MCP (Model Context Protocol) 角度进行描述。

## 目录

- [概述](#概述)
- [通用参数](#通用参数)
- [输出格式](#输出格式)
- [工具列表](#工具列表)

## 概述

mcp-echarts 提供了 16+ 个 MCP 工具，用于生成各种类型的 ECharts 图表。所有工具都遵循统一的输入输出规范。

### 工具分类

1. **通用工具**: `generate_echarts` - 接受完整的 ECharts 配置
2. **专用图表工具**: 针对特定图表类型的简化参数工具
   - 基础图表：bar, line, pie, scatter
   - 高级图表：radar, funnel, gauge, sankey
   - 树形图表：tree, treemap, sunburst
   - 特殊图表：heatmap, candlestick, boxplot, graph, parallel

## 通用参数

所有专用图表工具都支持以下通用参数：

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| `title` | string | 否 | - | 图表标题 |
| `width` | number | 否 | 800 | 图表宽度（像素），**仅用于 `outputType = "png"` 渲染** |
| `height` | number | 否 | 600 | 图表高度（像素），**仅用于 `outputType = "png"` 渲染** |
| `outputType` | "option" | 否 | "option" | 输出类型，**当前版本仅支持 `"option"`** |

**重要说明：**
- `width` 和 `height` 参数仅在 `outputType = "png"` 时使用，用于控制渲染图片的尺寸
- 当 `outputType = "option"` 时，这两个参数会被忽略（前端 ECharts 实例会根据容器大小自动调整）
- `outputType = "png"` 功能将在后续版本中实现

## 输出格式

### MCP 标准输出格式

所有工具返回的数据都遵循 MCP 协议的 content 格式：

```typescript
{
  content: Array<{
    type: "text" | "image",
    text?: string,      // type="text" 时存在
    data?: string,      // type="image" 时存在（Base64）
    mimeType?: string   // type="image" 时存在
  }>
}
```

### outputType = "png"（暂未实现）

当 `outputType` 为 `"png"` 时，返回渲染后的 PNG 图片。**此功能将在后续版本中实现。**

**注意**：当前版本仅支持 `outputType = "option"`。

### outputType = "option"

当 `outputType` 为 `"option"` 时，返回完整的 ECharts 配置 JSON 字符串：

```json
{
  "content": [
    {
      "type": "text",
      "text": "{\"title\":{\"text\":\"Chart Title\"},\"series\":[...]}"
    }
  ]
}
```

**注意**: `text` 字段包含的是完整的 ECharts option JSON 字符串，可以直接用于：

```javascript
const option = JSON.parse(response.content[0].text);
const chart = echarts.init(dom);
chart.setOption(option);
```

## 工具列表

### 1. [generate_echarts](tools/generate_echarts.md) - 通用 ECharts 工具
### 2. [generate_bar_chart](tools/generate_bar_chart.md) - 柱状图
### 3. [generate_line_chart](tools/generate_line_chart.md) - 折线图
### 4. [generate_pie_chart](tools/generate_pie_chart.md) - 饼图
### 5. [generate_scatter_chart](tools/generate_scatter_chart.md) - 散点图
### 6. [generate_radar_chart](tools/generate_radar_chart.md) - 雷达图
### 7. [generate_funnel_chart](tools/generate_funnel_chart.md) - 漏斗图
### 8. [generate_gauge_chart](tools/generate_gauge_chart.md) - 仪表盘
### 9. [generate_treemap_chart](tools/generate_treemap_chart.md) - 矩形树图
### 10. [generate_sunburst_chart](tools/generate_sunburst_chart.md) - 旭日图
### 11. [generate_heatmap_chart](tools/generate_heatmap_chart.md) - 热力图
### 12. [generate_candlestick_chart](tools/generate_candlestick_chart.md) - K线图
### 13. [generate_boxplot_chart](tools/generate_boxplot_chart.md) - 箱线图
### 14. [generate_graph_chart](tools/generate_graph_chart.md) - 关系图
### 15. [generate_parallel_chart](tools/generate_parallel_chart.md) - 平行坐标系
### 16. [generate_sankey_chart](tools/generate_sankey_chart.md) - 桑基图
### 17. [generate_tree_chart](tools/generate_tree_chart.md) - 树图

## MCP 工具实现说明

### Java 实现方式

本项目使用 Spring AI MCP Server 框架实现，参考项目中的 Neo4j 工具实现方式。

**依赖说明：**
- 使用 `spring-ai-starter-mcp-server-webmvc` 依赖（见 `pom.xml`）
- 使用 `@McpTool` 注解标记工具方法
- 使用 `@McpToolParam` 注解标记参数

**实现示例：**
```java
@Component
public class EChartMCPTool {
    
    @McpTool(name = "generate_bar_chart", description = "生成柱状图")
    public Map<String, Object> generateBarChart(
        @McpToolParam(description = "图表标题") String title,
        @McpToolParam(description = "图表数据") List<DataItem> data
    ) {
        // 构建 ECharts option
        ObjectNode option = buildBarChartOption(title, data);
        
        // 转换为 JSON 字符串
        String optionJson = objectMapper.writeValueAsString(option);
        
        // 返回 MCP 标准格式
        Map<String, Object> content = new HashMap<>();
        content.put("type", "text");
        content.put("text", optionJson);
        
        Map<String, Object> response = new HashMap<>();
        response.put("content", Arrays.asList(content));
        return response;
    }
}
```

**参考实现：**
- 参考项目：[mcp-echarts](https://github.com/hustcc/mcp-echarts) - TypeScript 实现，可作为逻辑参考
- 本地参考：`mcp/server/neo4j/src/main/java/mcp/canary/neo4j/tool/Neo4jMCPTool.java`

### 错误处理

所有工具的错误处理遵循 MCP 协议的错误响应格式。当发生错误时，应抛出异常，框架会自动转换为 MCP 错误响应。

**错误处理示例：**
```java
@McpTool(name = "generate_bar_chart", description = "生成柱状图")
public Map<String, Object> generateBarChart(
    @McpToolParam(description = "图表数据") List<DataItem> data
) {
    if (data == null || data.isEmpty()) {
        throw new IllegalArgumentException("数据不能为空");
    }
    
    // 验证数据格式
    for (DataItem item : data) {
        if (item.getCategory() == null || item.getValue() == null) {
            throw new IllegalArgumentException("数据项必须包含 category 和 value 字段");
        }
    }
    
    // ... 处理逻辑
}
```

## 使用建议

### 对于 Java 实现

1. **输出格式**：
   - **当前版本仅实现 `"option"` 格式**：返回 ECharts 配置 JSON 字符串
   - `"png"` 格式将在后续版本中实现

2. **参数处理**：
   - `width` 和 `height` 参数仅在 `outputType = "png"` 时使用
   - 当 `outputType = "option"` 时，这两个参数会被忽略

3. **参数验证**：
   - 使用 Bean Validation 或手动验证
   - 验证失败时抛出 `IllegalArgumentException` 或其他合适的异常
   - 提供清晰的错误信息

4. **数据处理**：
   - 按照每个工具的规范处理输入数据
   - 构建标准的 ECharts option 对象（使用 Jackson ObjectNode）
   - 序列化为 JSON 字符串返回

5. **通用工具实现**：
   - `generate_echarts` 工具最灵活，接受完整的 option JSON
   - 专用工具提供简化的参数，适合 LLM 调用

