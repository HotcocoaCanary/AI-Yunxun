# generate_sankey_chart - 桑基图工具

## 工具名称

`generate_sankey_chart`

## 描述

生成桑基图，用于可视化数据在不同阶段或类别之间的流动，如用户从访问页面到完成购买的用户旅程。

## 输入参数

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| `title` | string | 否 | - | 图表标题 |
| `data` | Array\<DataItem\> | 是 | - | 流动数据 |
| `width` | number | 否 | 800 | 图表宽度（像素），**仅用于 `outputType = "png"` 渲染** |
| `height` | number | 否 | 600 | 图表高度（像素），**仅用于 `outputType = "png"` 渲染** |
| `nodeAlign` | "left" \| "right" \| "justify" | 否 | "justify" | 节点对齐方式 |
| `outputType` | "option" | 否 | "option" | 输出类型，**当前版本仅支持 `"option"`** |

### DataItem 结构

```typescript
{
  source: string;    // 源节点名称，如 "Landing Page"
  target: string;    // 目标节点名称，如 "Product Page"
  value: number;     // 流动值，如 50000
}
```

## 输入示例

```json
{
  "title": "用户转化流程",
  "data": [
    { "source": "Landing Page", "target": "Product Page", "value": 50000 },
    { "source": "Product Page", "target": "Add to Cart", "value": 35000 },
    { "source": "Add to Cart", "target": "Checkout", "value": 25000 },
    { "source": "Checkout", "target": "Complete Purchase", "value": 20000 }
  ],
  "nodeAlign": "justify",
  "outputType": "option"
}
```

## 输出格式

### outputType = "option"

返回的 ECharts option 结构：

```json
{
  "title": {
    "left": "center",
    "text": "用户转化流程"
  },
  "tooltip": {
    "trigger": "item",
    "triggerOn": "mousemove"
  },
  "series": [
    {
      "type": "sankey",
      "data": [
        { "name": "Landing Page" },
        { "name": "Product Page" },
        { "name": "Add to Cart" },
        { "name": "Checkout" },
        { "name": "Complete Purchase" }
      ],
      "links": [
        { "source": "Landing Page", "target": "Product Page", "value": 50000 },
        { "source": "Product Page", "target": "Add to Cart", "value": 35000 },
        { "source": "Add to Cart", "target": "Checkout", "value": 25000 },
        { "source": "Checkout", "target": "Complete Purchase", "value": 20000 }
      ],
      "emphasis": {
        "focus": "adjacency"
      },
      "lineStyle": {
        "color": "gradient",
        "curveness": 0.5
      },
      "label": {
        "fontSize": 12
      },
      "nodeAlign": "justify"
    }
  ]
}
```

## Java 实现要点

1. **节点提取**：
   - 从 data 中提取所有唯一的 source 和 target
   - 构建 nodes 数组：`[{name: string}]`

2. **链接构建**：
   - links 直接使用输入的 data（保持 source, target, value）

3. **节点对齐**：
   - "left": 左对齐
   - "right": 右对齐
   - "justify": 两端对齐（默认，推荐）

4. **样式配置**：
   - lineStyle.color: "gradient"（渐变色）
   - curveness: 0.5（边的弯曲度）
   - emphasis.focus: "adjacency"（高亮相邻节点和边）

5. **数据格式**：
   - data: 节点数组 `[{name: string}]`
   - links: 链接数组 `[{source: string, target: string, value: number}]`

6. **注意事项**：
   - 确保所有 links 中的 source 和 target 都在 data 中存在
   - value 越大，流动的宽度越粗

