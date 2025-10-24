# 智能查询API文档

## 概述

智能查询系统为用户提供了基于自然语言的学术知识图谱查询接口。用户只需要输入自然语言查询，系统会自动分析意图、执行相应的操作，并返回图谱数据和可视化分析结果。

## 核心特性

- **自然语言理解**: 自动分析用户查询意图
- **智能路由**: 根据意图类型执行不同的查询策略
- **图谱可视化**: 自动生成图谱可视化数据
- **数据分析**: 提供多种图表和分析报告
- **隐藏复杂性**: 用户无需了解MCP工具、Cypher查询等技术细节

## API接口

### 1. 智能查询接口

**POST** `/api/intelligent/query`

用户输入自然语言查询，系统自动分析并返回结果。

#### 请求参数

```json
{
    "query": "查找与机器学习相关的所有论文"
}
```

#### 响应格式

```json
{
    "success": true,
    "message": "查询成功",
    "data": {
        "originalQuery": "查找与机器学习相关的所有论文",
        "intent": "GRAPH_QUERY",
        "confidence": 0.85,
        "cypherQuery": "MATCH (e:KnowledgeEntity)-[r]->(target:KnowledgeEntity) WHERE e.name CONTAINS '机器学习' RETURN e, r, target LIMIT 20",
        "graphResults": [...],
        "visualizationData": {
            "nodes": [...],
            "edges": [...],
            "type": "graph"
        },
        "analysisReport": "# 分析报告\n\n## 数据概览\n...",
        "executionTime": 1500
    }
}
```

#### 查询类型

系统支持以下查询类型：

1. **GRAPH_QUERY** - 图谱查询
   - 示例: "查找与机器学习相关的所有论文"
   - 返回: 图谱节点和关系数据

2. **DATA_ANALYSIS** - 数据分析
   - 示例: "分析深度学习领域的发展趋势"
   - 返回: 统计图表和分析报告

3. **LITERATURE_REVIEW** - 文献综述
   - 示例: "总结自然语言处理的最新进展"
   - 返回: 文献综述和趋势图表

4. **TREND_ANALYSIS** - 趋势分析
   - 示例: "机器学习领域近5年的发展趋势"
   - 返回: 时间序列数据和趋势图表

5. **CRAWL_DATA** - 数据爬取
   - 示例: "爬取人工智能相关的论文"
   - 返回: 爬取任务状态

### 2. 查询建议接口

**GET** `/api/intelligent/suggestions`

获取查询建议和示例。

#### 响应格式

```json
{
    "success": true,
    "message": "获取建议成功",
    "data": {
        "examples": [
            "查找与机器学习相关的所有论文",
            "分析深度学习领域的发展趋势",
            "总结自然语言处理的最新进展",
            "显示人工智能领域的合作网络",
            "比较不同算法的性能表现"
        ],
        "categories": [
            "图谱查询",
            "数据分析",
            "文献综述",
            "趋势分析",
            "合作网络"
        ],
        "tips": [
            "使用具体的关键词可以获得更精确的结果",
            "可以指定时间范围，如'近5年'、'2020-2024年'",
            "可以指定领域，如'计算机科学'、'人工智能'",
            "可以询问关系，如'谁与谁合作'、'什么引用了什么'"
        ]
    }
}
```

### 3. 查询历史接口

**GET** `/api/intelligent/history?page=0&size=10`

获取用户的查询历史记录。

#### 请求参数

- `page`: 页码（默认0）
- `size`: 每页大小（默认10）

#### 响应格式

```json
{
    "success": true,
    "message": "获取历史成功",
    "data": {
        "queries": [...],
        "total": 25,
        "page": 0,
        "size": 10
    }
}
```

### 4. 系统状态接口

**GET** `/api/intelligent/status`

获取系统运行状态和统计信息。

#### 响应格式

```json
{
    "success": true,
    "message": "获取状态成功",
    "data": {
        "system": "running",
        "version": "1.0.0",
        "uptime": 1234567890,
        "statistics": {
            "totalQueries": 150,
            "totalPapers": 5000,
            "totalEntities": 2000,
            "totalRelationships": 8000
        }
    }
}
```

## 查询示例

### 图谱查询示例

```bash
curl -X POST http://localhost:8080/api/intelligent/query \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <token>" \
  -d '{"query": "查找与深度学习相关的所有论文"}'
```

### 数据分析示例

```bash
curl -X POST http://localhost:8080/api/intelligent/query \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <token>" \
  -d '{"query": "分析机器学习领域近3年的发展趋势"}'
```

### 文献综述示例

```bash
curl -X POST http://localhost:8080/api/intelligent/query \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <token>" \
  -d '{"query": "总结自然语言处理的最新研究进展"}'
```

## 响应数据结构

### 图谱可视化数据

```json
{
    "visualizationData": {
        "nodes": [
            {
                "id": "node1",
                "label": "机器学习",
                "type": "Concept",
                "size": 30,
                "color": "#45B7D1"
            }
        ],
        "edges": [
            {
                "source": "node1",
                "target": "node2",
                "label": "RELATED_TO",
                "weight": 1
            }
        ],
        "type": "graph",
        "layout": "force"
    }
}
```

### 图表数据

```json
{
    "chartData": {
        "barChart": {
            "type": "bar",
            "title": "实体类型分布",
            "data": [
                {"name": "Person", "value": 50},
                {"name": "Organization", "value": 30},
                {"name": "Concept", "value": 100}
            ]
        },
        "lineChart": {
            "type": "line",
            "title": "趋势分析",
            "data": [...],
            "xAxis": "date",
            "yAxis": "frequency"
        }
    }
}
```

## 错误处理

### 常见错误码

- `400`: 请求参数错误
- `401`: 未授权访问
- `500`: 服务器内部错误

### 错误响应格式

```json
{
    "success": false,
    "message": "错误描述",
    "error": "详细错误信息"
}
```

## 使用建议

1. **查询优化**: 使用具体的关键词和领域信息可以获得更精确的结果
2. **时间范围**: 可以指定时间范围来限制查询结果
3. **关系查询**: 可以询问实体之间的关系，如合作、引用等
4. **组合查询**: 可以组合多个条件进行复杂查询

## 技术架构

### 服务层

- `IntelligentQueryService`: 智能查询服务
- `NaturalLanguageProcessor`: 自然语言处理器
- `GraphAnalysisService`: 图谱分析服务
- `ChartGenerationService`: 图表生成服务
- `DataCrawlingService`: 数据爬取服务

### 数据流

1. 用户输入自然语言查询
2. 自然语言处理器分析查询意图
3. 根据意图类型路由到相应的服务
4. 执行图谱查询、数据分析等操作
5. 生成可视化数据和图表
6. 返回综合结果给用户

这种设计将复杂的MCP工具和数据库操作对用户隐藏，提供了简洁易用的自然语言查询接口。
