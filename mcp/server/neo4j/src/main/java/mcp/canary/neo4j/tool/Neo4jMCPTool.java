package mcp.canary.neo4j.tool;

import jakarta.annotation.Resource;
import mcp.canary.neo4j.service.Neo4jService;
import org.springaicommunity.mcp.annotation.McpTool;
import org.springaicommunity.mcp.annotation.McpToolParam;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Component
public class Neo4jMCPTool {

    @Resource
    public Neo4jService neo4jService;

    private static final String SCHEMA = """
            call apoc.meta.data() yield label, property, type, other, unique, index, elementType
            where elementType = 'node' and not label starts with '_'
            with label,
                collect(case when type <> 'RELATIONSHIP' then [property, type + case when unique then " unique" else "" end + case when index then " indexed" else "" end] end) as attributes,
                collect(case when type = 'RELATIONSHIP' then [property, head(other)] end) as relationships
            RETURN label, apoc.map.fromPairs(attributes) as attributes, apoc.map.fromPairs(relationships) as relationships
            """;

    @McpTool(name = "get-neo4j-schema", description = "列出 Neo4j 数据库中的所有节点类型、它们的属性以及它们与其他节点类型之间的关系")
    public List<Map<String, Object>> neo4jSchema() {
        return neo4jService.executeQuery(SCHEMA, Collections.emptyMap());
    }

    @McpTool(name = "read-neo4j-cypher", description = "在 neo4j 数据库上执行 Cypher 查询")
    public List<Map<String, Object>> neo4jRead(@McpToolParam(description = "Cypher 读取要执行的查询") String query) {
        if (neo4jService.isWriteQuery(query)) {
            throw new IllegalArgumentException("读取查询只允许使用 MATCH 查询。");
        }
        return neo4jService.executeQuery(query, Collections.emptyMap());
    }

    @McpTool(name = "write-neo4j-cypher", description = "在 Neo4j 数据库上执行写入 Cypher 查询")
    public List<Map<String, Object>> neo4jWrite(@McpToolParam(description = "编写 Cypher 查询语句并执行") String query) {
        if (!neo4jService.isWriteQuery(query)) {
            throw new IllegalArgumentException("仅允许对写入查询执行写入操作。");
        }
        return neo4jService.executeQuery(query, Collections.emptyMap());
    }

}
