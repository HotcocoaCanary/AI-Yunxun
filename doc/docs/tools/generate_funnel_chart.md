# generate_funnel_chart - 漏斗图工具

## 工具名称

`generate_funnel_chart`

## 描述

生成漏斗图，用于可视化数据在通过各个阶段时的逐步减少过程，常用于显示转化率。

## 输入参数

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| `title` | string | 否 | - | 图表标题 |
| `data` | Array\<DataItem\> | 是 | - | 图表数据 |
| `width` | number | 否 | 800 | 图表宽度（像素），**仅用于 `outputType = "png"` 渲染** |
| `height` | number | 否 | 600 | 图表高度（像素），**仅用于 `outputType = "png"` 渲染** |
| `outputType` | "option" | 否 | "option" | 输出类型，**当前版本仅支持 `"option"`** |

### DataItem 结构

```typescript
{
  category: string;    // 阶段类别名称，如 "Browse Website"
  value: number;       // 该阶段的值，如 50000
}
```

## 输入示例

```json
{
  "title": "用户转化漏斗",
  "data": [
    { "category": "浏览网站", "value": 50000 },
    { "category": "加入购物车", "value": 35000 },
    { "category": "生成订单", "value": 25000 },
    { "category": "完成支付", "value": 20000 }
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
    "text": "用户转化漏斗"
  },
  "tooltip": {
    "trigger": "item"
  },
  "legend": {
    "left": "center",
    "orient": "horizontal",
    "bottom": 10,
    "data": ["浏览网站", "加入购物车", "生成订单", "完成支付"]
  },
  "series": [
    {
      "type": "funnel",
      "data": [
        { "name": "浏览网站", "value": 50000 },
        { "name": "加入购物车", "value": 35000 },
        { "name": "生成订单", "value": 25000 },
        { "name": "完成支付", "value": 20000 }
      ],
      "left": "10%",
      "top": 60,
      "width": "80%",
      "height": "80%",
      "min": 0,
      "max": 50000,
      "minSize": "0%",
      "maxSize": "100%",
      "sort": "descending",
      "gap": 2,
      "label": {
        "show": true,
        "position": "inside",
        "fontSize": 12,
        "color": "#fff"
      },
      "labelLine": {
        "length": 10,
        "lineStyle": {
          "width": 1,
          "type": "solid"
        }
      },
      "itemStyle": {
        "borderColor": "#fff",
        "borderWidth": 1
      },
      "emphasis": {
        "label": {
          "fontSize": 16
        }
      }
    }
  ]
}
```

## Java 实现要点

1. **数据转换**：
   - category → name
   - value → value
   - 构建 `[{name: string, value: number}]` 数组

2. **最大值计算**：
   - max = 所有 value 中的最大值

3. **排序**：
   - sort: "descending"（降序，最大的在上面）

4. **配置要点**：
   - min: 0
   - minSize: "0%"
   - maxSize: "100%"
   - gap: 2（阶段之间的间距）

5. **标签配置**：
   - position: "inside"（标签在内部）
   - color: "#fff"（白色文字，因为背景通常有颜色）

