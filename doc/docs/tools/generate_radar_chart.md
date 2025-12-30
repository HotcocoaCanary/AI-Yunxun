# generate_radar_chart - 雷达图工具

## 工具名称

`generate_radar_chart`

## 描述

生成雷达图，用于显示多维数据（四个或更多维度），适用于评估不同实体在多个维度上的表现。

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
  name: string;        // 维度名称，如 "Design"
  value: number;       // 该维度的数值
  group?: string;      // 分组名称（可选），用于多系列对比
}
```

## 输入示例

### 单系列雷达图

```json
{
  "title": "产品评价",
  "data": [
    { "name": "Design", "value": 70 },
    { "name": "Performance", "value": 85 },
    { "name": "Camera", "value": 80 },
    { "name": "Battery", "value": 75 },
    { "name": "Price", "value": 90 }
  ],
  "outputType": "option"
}
```

### 多系列对比

```json
{
  "title": "产品对比评价",
  "data": [
    { "name": "Design", "value": 70, "group": "iPhone" },
    { "name": "Design", "value": 75, "group": "Huawei" },
    { "name": "Performance", "value": 85, "group": "iPhone" },
    { "name": "Performance", "value": 80, "group": "Huawei" },
    { "name": "Camera", "value": 80, "group": "iPhone" },
    { "name": "Camera", "value": 85, "group": "Huawei" }
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
    "text": "产品评价",
    "top": "5%"
  },
  "tooltip": {
    "trigger": "item"
  },
  "radar": {
    "indicator": [
      { "name": "Design", "max": 100 },
      { "name": "Performance", "max": 100 },
      { "name": "Camera", "max": 100 },
      { "name": "Battery", "max": 100 },
      { "name": "Price", "max": 100 }
    ],
    "radius": "60%",
    "splitNumber": 4,
    "axisName": {
      "formatter": "{value}",
      "color": "#666"
    },
    "splitArea": {
      "areaStyle": {
        "color": ["rgba(250, 250, 250, 0.3)", "rgba(200, 200, 200, 0.3)"]
      }
    }
  },
  "series": [
    {
      "type": "radar",
      "data": [
        {
          "value": [70, 85, 80, 75, 90],
          "name": "产品评价"
        }
      ]
    }
  ],
  "legend": {
    // 多系列时存在
    "left": "center",
    "orient": "horizontal",
    "bottom": "5%"
  }
}
```

## Java 实现要点

1. **维度提取**：
   - 收集所有唯一的 `name` 作为维度（indicator）
   - 按名称排序以确保一致性

2. **最大值计算**：
   - 计算所有值的最大值
   - 向上取整到 10 的倍数（如 87 → 100）
   - 所有维度使用统一的最大值（避免 alignTicks 警告）

3. **多系列处理**：
   - 按 `group` 分组
   - 为每个 group 构建 value 数组（按维度顺序）
   - 缺失维度用 0 填充

4. **数据顺序**：
   - series.data 中的 value 数组必须与 indicator 的顺序一致

