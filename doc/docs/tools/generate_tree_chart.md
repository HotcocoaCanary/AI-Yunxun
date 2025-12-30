# generate_tree_chart - 树图工具

## 工具名称

`generate_tree_chart`

## 描述

生成树图，用于显示层次化数据结构，如组织结构图、家谱或文件目录结构。

## 输入参数

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| `title` | string | 否 | - | 图表标题 |
| `data` | TreeNode | 是 | - | 树形数据（单个根节点） |
| `width` | number | 否 | 800 | 图表宽度（像素），**仅用于 `outputType = "png"` 渲染** |
| `height` | number | 否 | 600 | 图表高度（像素），**仅用于 `outputType = "png"` 渲染** |
| `layout` | "orthogonal" \| "radial" | 否 | "orthogonal" | 布局类型 |
| `orient` | "LR" \| "RL" \| "TB" \| "BT" | 否 | "LR" | 方向（仅 orthogonal 有效） |
| `outputType` | "option" | 否 | "option" | 输出类型，**当前版本仅支持 `"option"`** |

### TreeNode 结构（递归）

```typescript
{
  name: string;              // 节点名称
  value?: number;            // 节点值（可选）
  children?: TreeNode[];     // 子节点（可选，递归）
}
```

**注意**：输入是单个根节点对象，不是数组。

### layout 和 orient 说明

- `layout = "orthogonal"`（正交布局）：
  - `orient = "LR"`: 从左到右
  - `orient = "RL"`: 从右到左
  - `orient = "TB"`: 从上到下
  - `orient = "BT"`: 从下到上

- `layout = "radial"`（径向布局）：
  - 从中心向外辐射
  - orient 参数无效

## 输入示例

### 正交布局（从左到右）

```json
{
  "title": "组织结构图",
  "data": {
    "name": "CEO",
    "children": [
      {
        "name": "CTO",
        "children": [
          { "name": "前端团队" },
          { "name": "后端团队" }
        ]
      },
      {
        "name": "CFO",
        "children": [
          { "name": "财务部" }
        ]
      }
    ]
  },
  "layout": "orthogonal",
  "orient": "LR",
  "outputType": "option"
}
```

### 径向布局

```json
{
  "title": "组织结构图（径向）",
  "data": {
    "name": "Root",
    "children": [
      { "name": "Child 1" },
      { "name": "Child 2", "children": [{ "name": "Grandchild" }] }
    ]
  },
  "layout": "radial",
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
    "text": "组织结构图"
  },
  "tooltip": {
    "trigger": "item",
    "triggerOn": "mousemove"
  },
  "series": [
    {
      "type": "tree",
      "data": [
        {
          "name": "CEO",
          "children": [
            {
              "name": "CTO",
              "children": [
                { "name": "前端团队" },
                { "name": "后端团队" }
              ]
            },
            {
              "name": "CFO",
              "children": [
                { "name": "财务部" }
              ]
            }
          ]
        }
      ],
      "layout": "orthogonal",
      "orient": "LR",
      "symbol": "emptyCircle",
      "symbolSize": 7,
      "initialTreeDepth": -1,
      "itemStyle": {
        "color": "#4154f3",
        "borderWidth": 2
      },
      "lineStyle": {
        "color": "#ccc",
        "width": 1.5,
        "curveness": 0.5
      },
      "label": {
        "position": "right",
        "verticalAlign": "middle",
        "align": "left",
        "fontSize": 12
      },
      "leaves": {
        "label": {
          "position": "right",
          "verticalAlign": "middle",
          "align": "left"
        }
      },
      "emphasis": {
        "focus": "descendant"
      },
      "expandAndCollapse": true,
      "animationDuration": 550,
      "animationDurationUpdate": 750
    }
  ]
}
```

**注意**：data 是包含单个根节点的数组 `[rootNode]`

## Java 实现要点

1. **数据结构**：
   - 输入是单个 TreeNode 对象（根节点）
   - 需要包装成数组：`[rootNode]`
   - 递归结构，需要递归构建

2. **标签位置**：
   - 根据 layout 和 orient 动态计算标签位置
   - orthogonal + LR: position="right", align="left"
   - orthogonal + RL: position="left", align="right"
   - orthogonal + TB: position="bottom", align="center"
   - orthogonal + BT: position="top", align="center"
   - radial: position="top", align="center"

3. **配置要点**：
   - initialTreeDepth: -1（展开所有层级）
   - expandAndCollapse: true（允许展开/折叠）
   - emphasis.focus: "descendant"（高亮时聚焦后代节点）

4. **样式配置**：
   - symbol: "emptyCircle"（空圆）
   - symbolSize: 7（节点大小）
   - lineStyle.curveness: 0.5（边的弯曲度）

5. **递归处理**：
   - 需要递归函数来处理 children
   - 保持树结构的完整性

