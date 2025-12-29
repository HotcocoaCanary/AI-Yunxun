# generate_candlestick_chart - K线图工具

## 工具名称

`generate_candlestick_chart`

## 描述

生成 K 线图（蜡烛图），用于金融数据可视化，如股票价格、加密货币价格或其他 OHLC（开盘-最高-最低-收盘）数据。可选显示成交量。

## 输入参数

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| `title` | string | 否 | - | 图表标题 |
| `data` | Array\<DataItem\> | 是 | - | K线数据 |
| `width` | number | 否 | 800 | 图表宽度（像素），**仅用于 `outputType = "png"` 渲染** |
| `height` | number | 否 | 600 | 图表高度（像素），**仅用于 `outputType = "png"` 渲染** |
| `showVolume` | boolean | 否 | false | 是否显示成交量图 |
| `outputType` | "option" | 否 | "option" | 输出类型，**当前版本仅支持 `"option"`** |

### DataItem 结构

```typescript
{
  date: string;        // 日期字符串，如 "2023-01-01"
  open: number;        // 开盘价
  high: number;        // 最高价
  low: number;         // 最低价
  close: number;       // 收盘价
  volume?: number;     // 成交量（可选）
}
```

## 输入示例

### 基础 K 线图

```json
{
  "title": "股票价格",
  "data": [
    { "date": "2023-01-01", "open": 100, "high": 110, "low": 95, "close": 105 },
    { "date": "2023-01-02", "open": 105, "high": 115, "low": 100, "close": 112 },
    { "date": "2023-01-03", "open": 112, "high": 118, "low": 108, "close": 110 }
  ],
  "outputType": "option"
}
```

### 带成交量的 K 线图

```json
{
  "title": "股票价格与成交量",
  "data": [
    {
      "date": "2023-01-01",
      "open": 100,
      "high": 110,
      "low": 95,
      "close": 105,
      "volume": 10000
    },
    {
      "date": "2023-01-02",
      "open": 105,
      "high": 115,
      "low": 100,
      "close": 112,
      "volume": 15000
    }
  ],
  "showVolume": true,
  "outputType": "option"
}
```

## 输出格式

### outputType = "option"

基础 K 线图的 ECharts option 结构：

```json
{
  "title": {
    "left": "center",
    "text": "股票价格"
  },
  "tooltip": {
    "trigger": "axis",
    "axisPointer": {
      "type": "cross"
    }
  },
  "xAxis": {
    "type": "category",
    "data": ["2023-01-01", "2023-01-02", "2023-01-03"],
    "boundaryGap": false,
    "axisLine": {
      "onZero": false
    },
    "splitLine": {
      "show": false
    }
  },
  "yAxis": {
    "scale": true,
    "splitArea": {
      "show": true
    }
  },
  "series": [
    {
      "name": "Candlestick",
      "type": "candlestick",
      "data": [
        [100, 105, 95, 110],
        [105, 112, 100, 115],
        [112, 110, 108, 118]
      ],
      "itemStyle": {
        "color": "#ef232a",
        "color0": "#14b143",
        "borderColor": "#ef232a",
        "borderColor0": "#14b143"
      }
    }
  ]
}
```

**注意**：K 线数据格式为 `[open, close, low, high]`

带成交量时的额外配置：
- 添加第二个 yAxis（用于成交量）
- 添加第二个 series（type="bar"，显示成交量）
- 使用 grid 布局分离两个图表区域

## Java 实现要点

1. **数据排序**：
   - 必须按日期排序数据

2. **数据转换**：
   - 提取 dates 作为 xAxis.data
   - OHLC 数据转换为 `[open, close, low, high]` 数组
   - volume 数据单独提取（如果 showVolume=true）

3. **颜色配置**：
   - color: "#ef232a"（红色，收盘价高于开盘价）
   - color0: "#14b143"（绿色，收盘价低于开盘价）

4. **成交量处理**：
   - 如果 showVolume=true，需要创建第二个 series（type="bar"）
   - 使用两个 yAxis，一个用于价格，一个用于成交量
   - 使用 grid 配置布局（上下分布）

5. **坐标轴配置**：
   - xAxis.boundaryGap: false
   - yAxis.scale: true（自动缩放）

