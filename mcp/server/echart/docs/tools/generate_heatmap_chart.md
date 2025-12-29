# generate_heatmap_chart - 热力图工具

## 工具名称

`generate_heatmap_chart`

## 描述

生成热力图，用于显示数据密度或强度分布，如用户活动模式（按时间和日期）、相关性矩阵等。

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
  x: string | number;    // X 轴值，如 "Mon" 或 0
  y: string | number;    // Y 轴值，如 "AM" 或 0
  value: number;         // 热力值，如 5
}
```

## 输入示例

```json
{
  "title": "用户活动热力图",
  "axisXTitle": "星期",
  "axisYTitle": "时间段",
  "data": [
    { "x": "Mon", "y": "12AM", "value": 5 },
    { "x": "Mon", "y": "1AM", "value": 3 },
    { "x": "Tue", "y": "12AM", "value": 8 },
    { "x": "Tue", "y": "1AM", "value": 6 }
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
    "text": "用户活动热力图",
    "top": "3%"
  },
  "tooltip": {
    "position": "top"
  },
  "grid": {
    "height": "60%",
    "top": "15%",
    "right": "15%",
    "bottom": "10%"
  },
  "xAxis": {
    "type": "category",
    "data": ["Mon", "Tue"],
    "name": "星期",
    "splitArea": {
      "show": true
    }
  },
  "yAxis": {
    "type": "category",
    "data": ["12AM", "1AM"],
    "name": "时间段",
    "splitArea": {
      "show": true
    }
  },
  "visualMap": {
    "min": 3,
    "max": 8,
    "calculable": true,
    "orient": "horizontal",
    "left": "center",
    "bottom": "15%",
    "inRange": {
      "color": [
        "#313695", "#4575b4", "#74add1", "#abd9e9", "#e0f3f8",
        "#ffffcc", "#fee090", "#fdae61", "#f46d43", "#d73027", "#a50026"
      ]
    }
  },
  "series": [
    {
      "type": "heatmap",
      "data": [[0, 0, 5], [0, 1, 3], [1, 0, 8], [1, 1, 6]],
      "label": {
        "show": true,
        "fontSize": 10
      },
      "emphasis": {
        "itemStyle": {
          "shadowBlur": 10,
          "shadowColor": "rgba(0, 0, 0, 0.5)"
        }
      }
    }
  ]
}
```

## Java 实现要点

1. **数据转换**：
   - 提取所有唯一的 x 值和 y 值
   - 对 x 和 y 值进行排序
   - 将 `{x, y, value}` 转换为 `[xIndex, yIndex, value]` 格式
   - 缺失的数据点用 0 填充

2. **数据映射**：
   ```java
   // 伪代码
   List<Object> xValues = extractUniqueX(data).sorted();
   List<Object> yValues = extractUniqueY(data).sorted();
   
   Map<String, Integer> dataMap = new HashMap<>();
   for (DataItem item : data) {
       dataMap.put(item.getX() + "_" + item.getY(), item.getValue());
   }
   
   List<int[]> heatmapData = new ArrayList<>();
   for (int i = 0; i < xValues.size(); i++) {
       for (int j = 0; j < yValues.size(); j++) {
           int value = dataMap.getOrDefault(
               xValues.get(i) + "_" + yValues.get(j), 
               0
           );
           heatmapData.add(new int[]{i, j, value});
       }
   }
   ```

3. **visualMap 配置**：
   - min/max: 计算所有 value 的最小值和最大值
   - inRange.color: 使用预定义的渐变色数组

4. **坐标轴配置**：
   - 两个坐标轴都是 type="category"
   - splitArea.show: true（显示分割区域）

