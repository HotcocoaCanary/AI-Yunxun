# generate_parallel_chart - 平行坐标系工具

## 工具名称

`generate_parallel_chart`

## 描述

生成平行坐标系图表，用于显示多维数据（四个或更多维度），如比较不同产品在多个属性上的表现。

## 输入参数

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| `title` | string | 否 | - | 图表标题 |
| `data` | Array\<DataItem\> | 是 | - | 数据项 |
| `dimensions` | Array\<string\> | 是 | - | 维度名称数组 |
| `width` | number | 否 | 800 | 图表宽度（像素），**仅用于 `outputType = "png"` 渲染** |
| `height` | number | 否 | 600 | 图表高度（像素），**仅用于 `outputType = "png"` 渲染** |
| `outputType` | "option" | 否 | "option" | 输出类型，**当前版本仅支持 `"option"`** |

### DataItem 结构

```typescript
{
  name: string;        // 数据项名称，如 "Product A"
  values: number[];    // 每个维度的值数组
}
```

**注意**：`values` 数组的长度必须与 `dimensions` 数组的长度相同。

## 输入示例

```json
{
  "title": "产品多维度对比",
  "dimensions": ["Price", "Quality", "Service", "Value"],
  "data": [
    { "name": "Product A", "values": [4.2, 3.4, 2.3, 1.8] },
    { "name": "Product B", "values": [3.8, 4.1, 3.2, 2.5] },
    { "name": "Product C", "values": [4.5, 2.9, 2.8, 2.0] }
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
    "text": "产品多维度对比"
  },
  "tooltip": {
    "trigger": "item"
  },
  "parallelAxis": [
    {
      "dim": 0,
      "name": "Price",
      "min": 3.62,
      "max": 4.62,
      "nameLocation": "start"
    },
    {
      "dim": 1,
      "name": "Quality",
      "min": 2.71,
      "max": 4.31,
      "nameLocation": "start"
    },
    {
      "dim": 2,
      "name": "Service",
      "min": 2.07,
      "max": 3.27,
      "nameLocation": "start"
    },
    {
      "dim": 3,
      "name": "Value",
      "min": 1.62,
      "max": 2.62,
      "nameLocation": "start"
    }
  ],
  "parallel": {
    "left": "5%",
    "right": "13%",
    "bottom": "20%",
    "top": "15%",
    "parallelAxisDefault": {
      "type": "value",
      "nameLocation": "end",
      "nameGap": 20,
      "nameTextStyle": {
        "fontSize": 12
      }
    }
  },
  "series": [
    {
      "name": "Product A",
      "type": "parallel",
      "data": [
        {
          "name": "Product A",
          "value": [4.2, 3.4, 2.3, 1.8]
        }
      ],
      "lineStyle": {
        "width": 2,
        "opacity": 0.7
      },
      "smooth": true
    },
    {
      "name": "Product B",
      "type": "parallel",
      "data": [
        {
          "name": "Product B",
          "value": [3.8, 4.1, 3.2, 2.5]
        }
      ],
      "lineStyle": {
        "width": 2,
        "opacity": 0.7
      },
      "smooth": true
    }
  ],
  "legend": {
    "bottom": 30,
    "data": ["Product A", "Product B", "Product C"]
  }
}
```

## Java 实现要点

1. **维度配置（parallelAxis）**：
   - 为每个维度创建一个 parallelAxis 配置
   - 计算该维度在所有数据项中的最小值和最大值
   - min = 实际最小值 - 范围的 10%
   - max = 实际最大值 + 范围的 10%
   - dim: 维度索引（0, 1, 2, ...）

2. **数据验证**：
   - 确保每个 data item 的 values 长度等于 dimensions 长度

3. **系列构建**：
   - 每个 data item 创建一个独立的 series
   - series.data = `[{name: string, value: number[]}]`
   - value 数组直接使用 data item 的 values

4. **配置要点**：
   - parallelAxisDefault: 所有轴的默认配置
   - smooth: true（平滑曲线）
   - lineStyle.opacity: 0.7（半透明，便于观察重叠）

5. **布局配置**：
   - parallel 对象控制整体布局
   - left/right/top/bottom 控制边距

