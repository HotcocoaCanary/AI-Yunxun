# generate_pie_chart - 饼图工具

## 工具名称

`generate_pie_chart`

## 描述

生成饼图，用于显示各部分占整体的比例关系。支持普通饼图和环形图（甜甜圈图）。

## 输入参数

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| `title` | string | 否 | - | 图表标题 |
| `data` | Array\<DataItem\> | 是 | - | 图表数据 |
| `width` | number | 否 | 800 | 图表宽度（像素），**仅用于 `outputType = "png"` 渲染** |
| `height` | number | 否 | 600 | 图表高度（像素），**仅用于 `outputType = "png"` 渲染** |
| `innerRadius` | number | 否 | 0 | 内半径（0-1），0为饼图，>0为环形图 |
| `outputType` | "option" | 否 | "option" | 输出类型，**当前版本仅支持 `"option"`** |

### DataItem 结构

```typescript
{
  category: string;    // 类别名称，如 "Category A"
  value: number;       // 数值，如 27
}
```

## 输入示例

### 普通饼图

```json
{
  "title": "市场份额分布",
  "data": [
    { "category": "产品A", "value": 27 },
    { "category": "产品B", "value": 25 },
    { "category": "产品C", "value": 18 },
    { "category": "其他", "value": 30 }
  ],
  "outputType": "option"
}
```

### 环形图（甜甜圈图）

```json
{
  "title": "市场份额分布（环形）",
  "data": [
    { "category": "产品A", "value": 27 },
    { "category": "产品B", "value": 25 },
    { "category": "产品C", "value": 18 }
  ],
  "innerRadius": 0.6,
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
    "text": "市场份额分布"
  },
  "tooltip": {
    "trigger": "item",
    "formatter": "{a} <br/>{b}: {c} ({d}%)"
  },
  "legend": {
    "left": "center",
    "orient": "horizontal",
    "top": "bottom"
  },
  "series": [
    {
      "type": "pie",
      "data": [
        { "name": "产品A", "value": 27 },
        { "name": "产品B", "value": 25 },
        { "name": "产品C", "value": 18 },
        { "name": "其他", "value": 30 }
      ],
      "radius": "70%",
      "emphasis": {
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

**环形图时的 radius 配置：**
```json
{
  "radius": ["60%", "70%"]  // innerRadius > 0 时
}
```

## Java 实现要点

1. **数据转换**：
   - category → name
   - value → value
   - 构建 `[{name: string, value: number}]` 数组

2. **半径配置**：
   - `innerRadius = 0`: radius = "70%"
   - `innerRadius > 0`: radius = [`${innerRadius * 100}%`, "70%"]

3. **图例配置**：
   - 自动显示所有 category
   - 位置：bottom center

4. **工具提示**：
   - trigger: "item"
   - formatter: "{a} <br/>{b}: {c} ({d}%)" （显示百分比）

