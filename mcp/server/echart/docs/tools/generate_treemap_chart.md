# generate_treemap_chart - 矩形树图工具

## 工具名称

`generate_treemap_chart`

## 描述

生成矩形树图，用于显示层次化数据，可以直观地显示同一级别项目之间的比较，常用于显示磁盘空间使用情况等。

## 输入参数

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| `title` | string | 否 | - | 图表标题 |
| `data` | Array\<TreeNode\> | 是 | - | 树形数据（数组，支持多个根节点） |
| `width` | number | 否 | 800 | 图表宽度（像素），**仅用于 `outputType = "png"` 渲染** |
| `height` | number | 否 | 600 | 图表高度（像素），**仅用于 `outputType = "png"` 渲染** |
| `outputType` | "option" | 否 | "option" | 输出类型，**当前版本仅支持 `"option"`** |

### TreeNode 结构（递归）

```typescript
{
  name: string;              // 节点名称
  value: number;             // 节点值（影响大小）
  children?: TreeNode[];     // 子节点（可选）
}
```

## 输入示例

```json
{
  "title": "磁盘空间使用",
  "data": [
    {
      "name": "系统",
      "value": 100,
      "children": [
        { "name": "Windows", "value": 60 },
        { "name": "Program Files", "value": 40 }
      ]
    },
    {
      "name": "用户",
      "value": 200,
      "children": [
        { "name": "Documents", "value": 80 },
        { "name": "Pictures", "value": 70 },
        { "name": "Videos", "value": 50 }
      ]
    }
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
    "text": "磁盘空间使用"
  },
  "tooltip": {
    "trigger": "item"
  },
  "series": [
    {
      "type": "treemap",
      "data": [
        {
          "name": "系统",
          "value": 100,
          "children": [
            { "name": "Windows", "value": 60 },
            { "name": "Program Files", "value": 40 }
          ]
        },
        {
          "name": "用户",
          "value": 200,
          "children": [
            { "name": "Documents", "value": 80 },
            { "name": "Pictures", "value": 70 },
            { "name": "Videos", "value": 50 }
          ]
        }
      ],
      "left": "3%",
      "right": "3%",
      "bottom": "3%",
      "label": {
        "show": true,
        "formatter": "{b}",
        "fontSize": 12,
        "color": "#fff"
      },
      "emphasis": {
        "focus": "descendant",
        "itemStyle": {
          "borderWidth": 3
        },
        "label": {
          "fontSize": 16
        }
      },
      "breadcrumb": {
        "show": false
      },
      "roam": false,
      "nodeClick": "zoomToNode"
    }
  ]
}
```

## Java 实现要点

1. **数据结构**：
   - 输入是数组，可以包含多个根节点
   - 每个节点都有 name 和 value（必填）
   - children 是可选且递归的

2. **递归处理**：
   - 需要递归构建树结构
   - 保持原有的层次关系

3. **配置要点**：
   - breadcrumb.show: false（隐藏面包屑导航）
   - roam: false（禁用漫游）
   - nodeClick: "zoomToNode"（点击节点缩放）

4. **标签配置**：
   - formatter: "{b}"（显示节点名称）
   - color: "#fff"（白色文字）

