# generate_line_chart - 折线图工具

## 工具名称

`generate_line_chart`

## 描述

生成折线图，用于显示数据随时间或其他连续变量的趋势变化。支持平滑曲线、面积填充、多系列对比。

## 输入参数

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| `title` | string | 否 | - | 图表标题 |
| `axisXTitle` | string | 否 | "" | X 轴标题（通常是时间） |
| `axisYTitle` | string | 否 | "" | Y 轴标题 |
| `data` | Array\<DataItem\> | 是 | - | 图表数据 |
| `width` | number | 否 | 800 | 图表宽度（像素），**仅用于 `outputType = "png"` 渲染** |
| `height` | number | 否 | 600 | 图表高度（像素），**仅用于 `outputType = "png"` 渲染** |
| `smooth` | boolean | 否 | false | 是否使用平滑曲线 |
| `showArea` | boolean | 否 | false | 是否填充区域 |
| `showSymbol` | boolean | 否 | true | 是否显示数据点标记 |
| `stack` | boolean | 否 | false | 是否堆叠（多系列时） |
| `outputType` | "option" | 否 | "option" | 输出类型，**当前版本仅支持 `"option"`** |

### DataItem 结构

```typescript
{
  time: string;        // 时间点，如 "2015" 或 "2023-01-01"
  value: number;       // 数值
  group?: string;      // 分组名称（可选），用于多系列
}
```

## 输入示例

### 基础折线图

```json
{
  "title": "销售趋势",
  "axisXTitle": "年份",
  "axisYTitle": "销售额（万元）",
  "data": [
    { "time": "2015", "value": 23 },
    { "time": "2016", "value": 32 },
    { "time": "2017", "value": 45 },
    { "time": "2018", "value": 38 }
  ],
  "outputType": "option"
}
```

### 平滑曲线 + 区域填充

```json
{
  "title": "销售趋势（平滑）",
  "axisXTitle": "年份",
  "axisYTitle": "销售额（万元）",
  "data": [
    { "time": "2015", "value": 23 },
    { "time": "2016", "value": 32 },
    { "time": "2017", "value": 45 }
  ],
  "smooth": true,
  "showArea": true,
  "outputType": "option"
}
```

### 多系列对比

```json
{
  "title": "多产品线销售对比",
  "axisXTitle": "年份",
  "axisYTitle": "销售额（万元）",
  "data": [
    { "time": "2015", "value": 23, "group": "产品A" },
    { "time": "2015", "value": 18, "group": "产品B" },
    { "time": "2016", "value": 32, "group": "产品A" },
    { "time": "2016", "value": 25, "group": "产品B" }
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
    "text": "销售趋势"
  },
  "tooltip": {
    "trigger": "axis"
  },
  "xAxis": {
    "type": "category",
    "boundaryGap": false,
    "data": ["2015", "2016", "2017", "2018"],
    "name": "年份"
  },
  "yAxis": {
    "type": "value",
    "name": "销售额（万元）"
  },
  "series": [
    {
      "type": "line",
      "data": [23, 32, 45, 38],
      "smooth": false,
      "showSymbol": true,
      "areaStyle": {}
    }
  ],
  "legend": {
    // 多系列时存在
    "left": "center",
    "orient": "horizontal",
    "bottom": 10
  }
}
```

## Java 实现要点

1. **时间排序**：需要确保 time 字段按时间顺序排列

2. **多系列处理**：
   - 提取所有唯一的 time 作为 xAxis.data
   - 按 group 分组，为每个 group 创建一个 series
   - 对于缺失的时间点，使用 `null` 填充

3. **系列配置**：
   - `smooth`: 控制曲线平滑度
   - `areaStyle: {}`: 启用区域填充
   - `showSymbol`: 控制数据点标记显示
   - `stack`: 堆叠时设置 stack 名称

4. **坐标轴配置**：
   - xAxis: type="category", boundaryGap=false（时间序列常用）

