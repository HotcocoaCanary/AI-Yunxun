# generate_bar_chart - 柱状图工具

## 工具名称

`generate_bar_chart`

## 描述

生成柱状图，用于显示不同类别之间的数值比较。支持单系列、多系列分组和堆叠显示。

## 输入参数

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| `title` | string | 否 | - | 图表标题 |
| `axisXTitle` | string | 否 | "" | X 轴标题 |
| `axisYTitle` | string | 否 | "" | Y 轴标题 |
| `data` | Array\<DataItem\> | 是 | - | 图表数据 |
| `width` | number | 否 | 800 | 图表宽度（像素），**仅用于 `outputType = "png"` 渲染** |
| `height` | number | 否 | 600 | 图表高度（像素），**仅用于 `outputType = "png"` 渲染** |
| `group` | boolean | 否 | false | 是否启用分组（多系列时） |
| `stack` | boolean | 否 | false | 是否启用堆叠（多系列时） |
| `outputType` | "option" | 否 | "option" | 输出类型，**当前版本仅支持 `"option"`** |

### DataItem 结构

```typescript
{
  category: string;    // 类别名称，如 "Category A"
  value: number;       // 数值，如 10
  group?: string;      // 分组名称（可选），用于多系列
}
```

**注意**：
- `group` 和 `stack` 不能同时为 `true`
- 启用 `group` 或 `stack` 时，数据中必须包含 `group` 字段

## 输入示例

### 单系列柱状图

```json
{
  "title": "月度销售数据",
  "axisXTitle": "月份",
  "axisYTitle": "销售额（万元）",
  "data": [
    { "category": "一月", "value": 120 },
    { "category": "二月", "value": 200 },
    { "category": "三月", "value": 150 }
  ],
  "outputType": "option"
}
```

### 分组柱状图

```json
{
  "title": "各产品线销售对比",
  "axisXTitle": "季度",
  "axisYTitle": "销售额（万元）",
  "data": [
    { "category": "Q1", "value": 100, "group": "产品A" },
    { "category": "Q1", "value": 120, "group": "产品B" },
    { "category": "Q2", "value": 150, "group": "产品A" },
    { "category": "Q2", "value": 180, "group": "产品B" }
  ],
  "group": true,
  "outputType": "option"
}
```

### 堆叠柱状图

```json
{
  "title": "各产品线销售堆叠",
  "axisXTitle": "季度",
  "axisYTitle": "销售额（万元）",
  "data": [
    { "category": "Q1", "value": 100, "group": "产品A" },
    { "category": "Q1", "value": 120, "group": "产品B" },
    { "category": "Q2", "value": 150, "group": "产品A" },
    { "category": "Q2", "value": 180, "group": "产品B" }
  ],
  "stack": true,
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
    "text": "月度销售数据"
  },
  "tooltip": {
    "trigger": "axis"
  },
  "xAxis": {
    "type": "category",
    "data": ["一月", "二月", "三月"],
    "name": "月份"
  },
  "yAxis": {
    "type": "value",
    "name": "销售额（万元）"
  },
  "series": [
    {
      "type": "bar",
      "data": [120, 200, 150]
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

1. **数据转换**：
   - 单系列：提取所有 category 作为 xAxis.data，提取所有 value 作为 series.data
   - 多系列分组：按 group 分组，为每个 group 创建独立的 series

2. **分组处理**：
```java
// 伪代码示例
if (hasGroups && (group || stack)) {
    Map<String, List<DataItem>> groupMap = data.stream()
        .collect(Collectors.groupingBy(item -> item.getGroup()));
    
    List<String> categories = extractUniqueCategories(data);
    
    for (Map.Entry<String, List<DataItem>> entry : groupMap.entrySet()) {
        Series series = new Series();
        series.setName(entry.getKey());
        series.setType("bar");
        series.setStack(stack ? "Total" : null);
        series.setData(fillValuesForCategories(categories, entry.getValue()));
        seriesList.add(series);
    }
}
```

3. **坐标轴配置**：
   - xAxis: type="category", data=所有唯一 category
   - yAxis: type="value"

