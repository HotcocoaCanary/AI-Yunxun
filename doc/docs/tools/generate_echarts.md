# generate_echarts - 通用 ECharts 工具

## 工具名称

`generate_echarts`

## 描述

使用完整的 ECharts 配置动态生成图表。这是最灵活的工具，接受完整的 ECharts option 配置。

适用于需要完全自定义图表配置的场景，或者需要利用 ECharts 所有高级特性的场景。

## 输入参数

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| `echartsOption` | string | 是 | - | ECharts 配置的 JSON 字符串 |
| `width` | number | 否 | 800 | 图表宽度（像素），**仅用于 `outputType = "png"` 渲染** |
| `height` | number | 否 | 600 | 图表高度（像素），**仅用于 `outputType = "png"` 渲染** |
| `outputType` | "option" | 否 | "option" | 输出类型，**当前版本仅支持 `"option"`** |

### echartsOption 参数说明

`echartsOption` 必须是有效的 ECharts 配置 JSON 字符串。

**示例：**
```json
{
  "title": {
    "text": "ECharts Entry Example",
    "left": "center",
    "top": "2%"
  },
  "tooltip": {},
  "xAxis": {
    "data": ["shirt", "cardigan", "chiffon", "pants", "heels", "socks"]
  },
  "yAxis": {},
  "series": [{
    "name": "Sales",
    "type": "bar",
    "data": [5, 20, 36, 10, 10, 20]
  }]
}
```

**验证规则：**
- 必须是有效的 JSON 字符串
- 必须是对象类型（不能是数组或基本类型）
- 如果包含 Cartesian 系列（bar, line, scatter），必须配置 xAxis 和 yAxis

## 输入示例

```json
{
  "echartsOption": "{\"title\":{\"text\":\"销售数据\",\"left\":\"center\"},\"xAxis\":{\"type\":\"category\",\"data\":[\"一月\",\"二月\",\"三月\"]},\"yAxis\":{\"type\":\"value\"},\"series\":[{\"type\":\"bar\",\"data\":[120,200,150]}]}",
  "outputType": "option"
}
```

## 输出格式

### outputType = "option" (推荐用于 Java 实现)

```json
{
  "content": [
    {
      "type": "text",
      "text": "{\"title\":{\"text\":\"销售数据\",\"left\":\"center\"},\"xAxis\":{\"type\":\"category\",\"data\":[\"一月\",\"二月\",\"三月\"]},\"yAxis\":{\"type\":\"value\"},\"series\":[{\"type\":\"bar\",\"data\":[120,200,150]}]}"
    }
  ]
}
```

**使用方式：**
```javascript
// 前端使用
const option = JSON.parse(response.content[0].text);
const chart = echarts.init(dom);
chart.setOption(option);
```

### outputType = "png"（暂未实现）

返回 PNG 图片（URL 或 Base64）。**此功能将在后续版本中实现。**

## 注意事项

1. **JSON 字符串转义**：`echartsOption` 作为 JSON 字符串传递时，需要正确转义内部的双引号
2. **配置验证**：工具会验证基本的配置完整性（如 Cartesian 图表必须有坐标轴）
3. **灵活性**：这是唯一可以完全自定义所有 ECharts 配置的工具
4. **参数说明**：`width` 和 `height` 参数仅在 `outputType = "png"` 时使用，当前版本仅支持 `outputType = "option"`

## Java 实现建议

```java
@Component
public class GenerateEChartsTool {
    
    @McpTool(name = "generate_echarts", description = "使用完整的 ECharts 配置动态生成图表")
    public Map<String, Object> generateChart(
        @McpToolParam(description = "ECharts 配置的 JSON 字符串") String echartsOption,
        @McpToolParam(description = "输出类型，当前仅支持 option") String outputType
    ) {
        // 1. 验证 outputType
        if (outputType == null || "option".equals(outputType)) {
            outputType = "option";
        } else {
            throw new IllegalArgumentException("当前版本仅支持 outputType = 'option'");
        }
        
        // 2. 验证 echartsOption 是有效的 JSON
        ObjectMapper mapper = new ObjectMapper();
        JsonNode optionNode;
        try {
            optionNode = mapper.readTree(echartsOption);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid ECharts option JSON: " + e.getMessage());
        }
        
        // 3. 验证基本结构（可选但推荐）
        validateEChartsOption(optionNode);
        
        // 4. 构建 MCP 响应（当前仅支持 option）
        Map<String, Object> content = new HashMap<>();
        content.put("type", "text");
        content.put("text", echartsOption);
        
        Map<String, Object> response = new HashMap<>();
        response.put("content", Arrays.asList(content));
        return response;
    }
    
    private void validateEChartsOption(JsonNode option) {
        // 验证基本结构
        // 例如：Cartesian 图表必须有坐标轴
    }
}
```

