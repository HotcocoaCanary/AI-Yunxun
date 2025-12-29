# generate_sunburst_chart - 旭日图工具

## 工具名称

`generate_sunburst_chart`

## 描述

生成旭日图，用于显示多级层次化数据，如组织结构、文件系统层次结构或类别分解。

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
  "title": "技术栈层次结构",
  "data": [
    {
      "name": "技术",
      "value": 100,
      "children": [
        {
          "name": "前端",
          "value": 60,
          "children": [
            { "name": "React", "value": 30 },
            { "name": "Vue", "value": 30 }
          ]
        },
        {
          "name": "后端",
          "value": 40,
          "children": [
            { "name": "Java", "value": 25 },
            { "name": "Python", "value": 15 }
          ]
        }
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
    "text": "技术栈层次结构"
  },
  "tooltip": {
    "trigger": "item"
  },
  "series": [
    {
      "type": "sunburst",
      "data": [
        {
          "name": "技术",
          "value": 100,
          "children": [
            {
              "name": "前端",
              "value": 60,
              "children": [
                { "name": "React", "value": 30 },
                { "name": "Vue", "value": 30 }
              ]
            }
          ]
        }
      ],
      "radius": [0, "90%"],
      "center": ["50%", "50%"],
      "sort": undefined,
      "emphasis": {
        "focus": "ancestor"
      },
      "label": {
        "show": true,
        "fontSize": 12,
        "color": "#000",
        "minAngle": 10
      },
      "itemStyle": {
        "borderRadius": 7,
        "borderWidth": 2,
        "borderColor": "#fff"
      },
      "levels": [
        {},
        {
          "r0": "15%",
          "r": "35%",
          "itemStyle": {
            "borderWidth": 2
          },
          "label": {
            "rotate": "tangential"
          }
        },
        {
          "r0": "35%",
          "r": "70%",
          "label": {
            "align": "right"
          }
        },
        {
          "r0": "70%",
          "r": "72%",
          "label": {
            "position": "outside",
            "padding": 3,
            "silent": false
          },
          "itemStyle": {
            "borderWidth": 3
          }
        }
      ]
    }
  ]
}
```

## Java 实现要点

1. **数据结构**：
   - 输入是数组，可以包含多个根节点
   - 每个节点都有 name 和 value（必填）
   - children 是可选且递归的

2. **半径配置**：
   - radius: [0, "90%"]（从中心到 90%）

3. **层级配置（levels）**：
   - 不同层级可以有不同的样式和标签配置
   - r0: 内半径，r: 外半径
   - 默认配置了 4 个层级

4. **标签配置**：
   - minAngle: 10（最小角度，小于此角度不显示标签）
   - 不同层级有不同的标签位置和旋转

5. **样式配置**：
   - borderRadius: 7（圆角）
   - borderWidth: 2（边框宽度）
   - borderColor: "#fff"（白色边框）

