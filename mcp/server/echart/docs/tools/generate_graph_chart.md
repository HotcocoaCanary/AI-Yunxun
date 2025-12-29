# generate_graph_chart - 关系图工具

## 工具名称

`generate_graph_chart`

## 描述

生成关系图（网络图），用于显示实体（节点）之间的关系（边），如社交网络中的关系。

## 输入参数

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| `title` | string | 否 | - | 图表标题 |
| `data` | DataObject | 是 | - | 图和边数据 |
| `width` | number | 否 | 800 | 图表宽度（像素），**仅用于 `outputType = "png"` 渲染** |
| `height` | number | 否 | 600 | 图表高度（像素），**仅用于 `outputType = "png"` 渲染** |
| `layout` | "force" \| "circular" \| "none" | 否 | "force" | 布局算法 |
| `outputType` | "option" | 否 | "option" | 输出类型，**当前版本仅支持 `"option"`** |

### DataObject 结构

```typescript
{
  nodes: Array<{
    id: string;              // 节点唯一标识
    name: string;            // 节点显示名称
    value?: number;          // 节点值（影响大小）
    category?: string;       // 节点类别（影响颜色）
  }>,
  edges: Array<{
    source: string;          // 源节点 id
    target: string;          // 目标节点 id
    value?: number;          // 边的权重（影响粗细）
  }>
}
```

## 输入示例

```json
{
  "title": "社交网络关系图",
  "data": {
    "nodes": [
      { "id": "node1", "name": "Alice", "category": "A" },
      { "id": "node2", "name": "Bob", "category": "A" },
      { "id": "node3", "name": "Charlie", "category": "B" }
    ],
    "edges": [
      { "source": "node1", "target": "node2", "value": 1 },
      { "source": "node2", "target": "node3", "value": 2 }
    ]
  },
  "layout": "force",
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
    "text": "社交网络关系图"
  },
  "tooltip": {
    "trigger": "item"
  },
  "legend": {
    "data": ["A", "B"],
    "left": "center",
    "bottom": 10
  },
  "series": [
    {
      "type": "graph",
      "layout": "force",
      "data": [
        { "id": "node1", "name": "Alice", "category": 0, "value": 1 },
        { "id": "node2", "name": "Bob", "category": 0, "value": 1 },
        { "id": "node3", "name": "Charlie", "category": 1, "value": 1 }
      ],
      "links": [
        { "source": "node1", "target": "node2", "value": 1 },
        { "source": "node2", "target": "node3", "value": 2 }
      ],
      "categories": [
        { "name": "A" },
        { "name": "B" }
      ],
      "roam": true,
      "label": {
        "show": true,
        "position": "right"
      },
      "labelLayout": {
        "hideOverlap": true
      },
      "scaleLimit": {
        "min": 0.4,
        "max": 2
      },
      "lineStyle": {
        "color": "source",
        "curveness": 0.3
      }
    }
  ]
}
```

## Java 实现要点

1. **节点验证**：
   - 验证所有 edge 的 source 和 target 都在 nodes 中
   - 过滤掉无效的边

2. **类别处理**：
   - 提取所有唯一的 category
   - 为每个 category 分配索引（用于 categories 数组）
   - 在节点数据中使用索引而不是类别名称

3. **数据转换**：
   - nodes: 保持 id, name, value，category 转换为索引
   - links: 保持 source, target, value（注意是 links 不是 edges）

4. **布局配置**：
   - "force": 力导向布局（默认）
   - "circular": 环形布局
   - "none": 无布局（需要节点有 x, y 坐标）

5. **交互配置**：
   - roam: true（允许拖拽和缩放）
   - scaleLimit: 限制缩放范围

6. **样式配置**：
   - lineStyle.color: "source"（使用源节点颜色）
   - curveness: 0.3（边的弯曲度）

