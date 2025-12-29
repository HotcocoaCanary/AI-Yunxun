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
            调用 apoc.meta.data() 返回 label、property、type、other、unique、index、elementType
            其中 elementType = 'node' 且 label 不以 '_' 开头
            使用 label，
            收集所有 type 不等于 'RELATIONSHIP' 的属性，格式为 [property, type + 如果 unique 为真则添加 " unique" 否则为空 + 如果 index 为真则添加 " indexed" 否则为空]，并将结果命名为 attributes，
            收集所有 type 等于 'RELATIONSHIP' 的属性，格式为 [property, head(other)]，并将结果命名为 relationships
            返回 label、apoc.map.fromPairs(attributes) 作为 attributes、apoc.map.fromPairs(relationships) 作为 relationships
            """;

    @McpTool(name = "get-neo4j-schema", description = "列出 Neo4j 数据库中的所有节点类型、它们的属性以及它们与其他节点类型之间的关系")
    public List<Map<String, Object>> neo4jSchema() {
        return neo4jService.executeQuery(SCHEMA, Collections.emptyMap());
    }

    @McpTool(name = "read-neo4j-cypher", description = "在 neo4j 数据库上执行 Cypher 查询。")
    public List<Map<String, Object>> neo4jRead(@McpToolParam(description = "Cypher 读取要执行的查询") String query) {
        if (neo4jService.isWriteQuery(query)) {
            throw new IllegalArgumentException("读取查询只允许使用 MATCH 查询。");
        }
        return neo4jService.executeQuery(query, Collections.emptyMap());
    }

    @McpTool(name = "write-neo4j-cypher", description = "在 Neo4j 数据库上执行写入 Cypher 查询。")
    public List<Map<String, Object>> neo4jWrite(@McpToolParam(description = "编写 Cypher 查询语句并执行") String query) {
        if (!neo4jService.isWriteQuery(query)) {
            throw new IllegalArgumentException("仅允许对写入查询执行写入操作。");
        }
        return neo4jService.executeQuery(query, Collections.emptyMap());
    }

}
