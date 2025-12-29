# generate_scatter_chart - 散点图工具

## 工具名称

`generate_scatter_chart`

## 描述

生成散点图，用于显示两个变量之间的关系，帮助发现相关性或分布模式。

## 输入参数

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| `title` | string | 否 | - | 图表标题 |
| `axisXTitle` | string | 否 | "" | X 轴标题 |
| `axisYTitle` | string | 否 | "" | Y 轴标题 |
| `data` | Array\<DataItem\> | 是 | - | 图表数据 |
| `width` | number | 否 | 800 | 图表宽度（像素），**仅用于 `outputType = "png"` 渲染** |
| `height` | number | 否 | 600 | 图表高度（像素），**仅用于 `outputType = "png"` 渲染** |
| `outputType` | "option" | 否 | "option" | 输出类型，**当前版本仅支持 `"option"`** |

### DataItem 结构

```typescript
{
  x: number;    // X 坐标值
  y: number;    // Y 坐标值
}
```

## 输入示例

```json
{
  "title": "身高与体重关系",
  "axisXTitle": "身高（cm）",
  "axisYTitle": "体重（kg）",
  "data": [
    { "x": 160, "y": 50 },
    { "x": 165, "y": 55 },
    { "x": 170, "y": 60 },
    { "x": 175, "y": 65 },
    { "x": 180, "y": 70 }
  ],
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
    "text": "身高与体重关系"
  },
  "tooltip": {
    "trigger": "item"
  },
  "xAxis": {
    "type": "value",
    "name": "身高（cm）",
    "scale": true
  },
  "yAxis": {
    "type": "value",
    "name": "体重（kg）",
    "scale": true
  },
  "series": [
    {
      "type": "scatter",
      "data": [[160, 50], [165, 55], [170, 60], [175, 65], [180, 70]],
      "symbolSize": 8,
      "emphasis": {
        "focus": "series",
        "itemStyle": {
          "shadowBlur": 10,
          "shadowOffsetX": 0,
          "shadowColor": "rgba(0, 0, 0, 0.5)"
        }
      }
    }
  ]
}
```

## Java 实现要点

1. **数据转换**：
   - 将 `{x: number, y: number}` 转换为 `[x, y]` 数组
   - 最终数据格式：`[[x1, y1], [x2, y2], ...]`

2. **坐标轴配置**：
   - 两个坐标轴都是 type="value"（数值型）
   - scale: true（自动缩放）

3. **系列配置**：
   - symbolSize: 8（点的大小）
   - emphasis.focus: "series"（高亮整个系列）

