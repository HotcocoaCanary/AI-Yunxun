# generate_gauge_chart - 仪表盘工具

## 工具名称

`generate_gauge_chart`

## 描述

生成仪表盘图表，用于显示单个指标的当前状态，如 CPU 使用率、完成进度、性能评分等。支持显示多个仪表盘。

## 输入参数

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| `title` | string | 否 | - | 图表标题 |
| `data` | Array\<DataItem\> | 是 | - | 仪表盘数据 |
| `width` | number | 否 | 800 | 图表宽度（像素），**仅用于 `outputType = "png"` 渲染** |
| `height` | number | 否 | 600 | 图表高度（像素），**仅用于 `outputType = "png"` 渲染** |
| `min` | number | 否 | 0 | 最小值 |
| `max` | number | 否 | 100 | 最大值 |
| `outputType` | "option" | 否 | "option" | 输出类型，**当前版本仅支持 `"option"`** |

### DataItem 结构

```typescript
{
  name: string;    // 指标名称，如 "CPU Usage"
  value: number;   // 当前值，如 75
}
```

## 输入示例

### 单个仪表盘

```json
{
  "title": "CPU 使用率",
  "data": [
    { "name": "CPU Usage", "value": 75 }
  ],
  "min": 0,
  "max": 100,
  "outputType": "option"
}
```

### 多个仪表盘

```json
{
  "title": "系统指标监控",
  "data": [
    { "name": "CPU", "value": 75 },
    { "name": "Memory", "value": 60 },
    { "name": "Disk", "value": 45 }
  ],
  "min": 0,
  "max": 100,
  "outputType": "option"
}
```

## 输出格式

### outputType = "option"

单个仪表盘的 ECharts option 结构：

```json
{
  "title": {
    "left": "center",
    "text": "CPU 使用率"
  },
  "series": [
    {
      "name": "CPU Usage",
      "type": "gauge",
      "data": [{ "name": "CPU Usage", "value": 75 }],
      "center": ["50%", "55%"],
      "radius": "80%",
      "min": 0,
      "max": 100,
      "startAngle": 180,
      "endAngle": 0,
      "axisLine": {
        "lineStyle": {
          "width": 6,
          "color": [
            [0.3, "#67e0e3"],
            [0.7, "#37a2da"],
            [1, "#fd666d"]
          ]
        }
      },
      "pointer": {
        "itemStyle": {
          "color": "inherit"
        }
      },
      "axisTick": {
        "distance": -30,
        "length": 8,
        "lineStyle": {
          "color": "#fff",
          "width": 2
        }
      },
      "splitLine": {
        "distance": -30,
        "length": 30,
        "lineStyle": {
          "color": "#fff",
          "width": 4
        }
      },
      "axisLabel": {
        "color": "inherit",
        "distance": 40,
        "fontSize": 12
      },
      "detail": {
        "valueAnimation": true,
        "formatter": "{value}",
        "color": "inherit",
        "fontSize": 20,
        "offsetCenter": [0, "30%"]
      },
      "title": {
        "offsetCenter": [0, "50%"],
        "fontSize": 14
      }
    }
  ]
}
```

多个仪表盘时：
- 每个仪表盘占用一个 series
- center 位置根据数量动态计算：`[(100 / (count + 1)) * (index + 1)]%, 60%]`
- radius 根据数量动态调整：`Math.min(80 / count, 30)%`

## Java 实现要点

1. **单个 vs 多个**：
   - 单个：center = ["50%", "55%"], radius = "80%"
   - 多个：需要计算每个的位置和大小

2. **颜色分段**：
   - axisLine.lineStyle.color 使用分段颜色
   - [0.3, "#67e0e3"] 表示 0-30% 使用该颜色
   - [0.7, "#37a2da"] 表示 30-70% 使用该颜色
   - [1, "#fd666d"] 表示 70-100% 使用该颜色

3. **角度配置**：
   - startAngle: 180（起始角度）
   - endAngle: 0（结束角度）
   - 形成半圆形仪表盘

4. **数据格式**：
   - series.data = `[{name: string, value: number}]`

