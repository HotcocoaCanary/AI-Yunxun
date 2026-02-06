# Tool Methods Test Cases

## Neo4jMCPToolTest

| 用例编号 | 测试工具 | 输入条件 | 预期结果 |
| --- | --- | --- | --- |
| NEO4J-TOOL-001 | get-neo4j-schema | exchange 非空 | 记录 INFO 日志，执行 schema 查询并返回结果列表 |
| NEO4J-TOOL-002 | get-neo4j-schema | exchange 为空 | 不记录日志，执行 schema 查询并返回结果列表 |
| NEO4J-TOOL-003 | read-neo4j-cypher | query 含 CREATE/MERGE/DELETE/SET 关键字 | 抛出 IllegalArgumentException，Neo4jService 不被调用 |
| NEO4J-TOOL-004 | read-neo4j-cypher | query 为只读 + exchange 非空 | 记录 INFO 日志，先执行查询再执行去重，返回结果列表 |
| NEO4J-TOOL-005 | read-neo4j-cypher | query 为只读 + exchange 为空 | 不记录日志，先执行查询再执行去重，返回结果列表 |
| NEO4J-TOOL-006 | write-neo4j-cypher | query 为写入 + exchange 非空 | 记录 INFO 日志，执行写入并返回统计信息 |
| NEO4J-TOOL-007 | write-neo4j-cypher | query 为写入 + exchange 为空 | 不记录日志，执行写入并返回统计信息 |

## GraphEChartMCPToolTest

| 用例编号 | 测试工具 | 输入条件 | 预期结果 |
| --- | --- | --- | --- |
| ECHART-TOOL-001 | generate_graph_chart | title 非空、layout=circular、nodes/edges/categories 非空、exchange 非空 | 生成含 title 的 option，layout 为 circular，类别映射正确，记录 INFO 日志 |
| ECHART-TOOL-002 | generate_graph_chart | title 为空、layout 为空、nodes 非空、categories 为空、exchange 为空 | title 不生成，layout 默认 force，categories 由 node.categoryName 自动生成 |
| ECHART-TOOL-003 | generate_graph_chart | title 为空白、nodes 为空、categories 为空列表 | title 不生成，data/categories 为空 |
| ECHART-TOOL-004 | generate_graph_chart | nodes 为空列表、categories 为空 | data/categories 为空，正常返回 |
| ECHART-TOOL-005 | generate_graph_chart | nodes 包含 null、exchange 非空 | 抛出 RuntimeException，记录 ERROR 日志 |
| ECHART-TOOL-006 | generate_graph_chart | exchange 的 loggingNotification 抛异常 | 日志异常被吞掉，仍返回 JsonNode |
