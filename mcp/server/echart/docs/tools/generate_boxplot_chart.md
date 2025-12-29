# generate_boxplot_chart - 箱线图工具

## 工具名称

`generate_boxplot_chart`

## 描述

生成箱线图，用于显示不同类别之间的数据统计摘要，用于比较数据点在不同类别之间的分布。

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
  category: string;    // 类别名称，如 "Category A"
  value: number;       // 数据值
  group?: string;      // 分组名称（可选），用于分组
}
```

## 输入示例

```json
{
  "title": "不同类别数据分布",
  "axisXTitle": "类别",
  "axisYTitle": "数值",
  "data": [
    { "category": "A", "value": 10 },
    { "category": "A", "value": 12 },
    { "category": "A", "value": 15 },
    { "category": "B", "value": 20 },
    { "category": "B", "value": 22 },
    { "category": "B", "value": 25 }
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
    "text": "不同类别数据分布"
  },
  "tooltip": {
    "trigger": "item"
  },
  "xAxis": {
    "type": "category",
    "data": ["A", "B"],
    "name": "类别",
    "boundaryGap": true
  },
  "yAxis": {
    "type": "value",
    "name": "数值"
  },
  "series": [
    {
      "name": "boxplot",
      "type": "boxplot",
      "data": [
        [10, 12, 12.5, 13, 15],
        [20, 22, 22.5, 23, 25]
      ]
    }
  ]
}
```

**注意**：boxplot 数据格式为 `[min, Q1, median, Q3, max]`

## Java 实现要点

1. **统计计算**：
   - 按 category（和可选的 group）分组数据
   - 对每组数据计算箱线图统计量：
     - min（最小值）
     - Q1（第一四分位数，25%）
     - median（中位数，50%）
     - Q3（第三四分位数，75%）
     - max（最大值）

2. **四分位数计算**：
   ```java
   // 伪代码
   Collections.sort(values);
   int n = values.size();
   double q1 = percentile(values, 0.25);
   double median = percentile(values, 0.5);
   double q3 = percentile(values, 0.75);
   double[] boxplotData = {min, q1, median, q3, max};
   ```

3. **数据格式**：
   - 每个 category 对应一个 `[min, Q1, median, Q3, max]` 数组
   - 如果有多组，需要为每个组合计算统计量

4. **坐标轴配置**：
   - xAxis: type="category", data=所有唯一 category
   - yAxis: type="value"
   - boundaryGap: true（类别之间有间隔）

5. **分组处理**：
   - 如果数据包含 group 字段，需要按 category 和 group 组合进行分组
   - 可能需要使用不同的 series 来显示不同组

