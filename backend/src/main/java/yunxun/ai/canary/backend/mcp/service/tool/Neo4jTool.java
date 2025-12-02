package yunxun.ai.canary.backend.mcp.service.tool;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;
import yunxun.ai.canary.backend.graph.service.Neo4jQueryService;

@Component
public class Neo4jTool {

    private final Neo4jQueryService neo4jQueryService;

    public Neo4jTool(Neo4jQueryService neo4jQueryService) {
        this.neo4jQueryService = neo4jQueryService;
    }

    @Tool(
            name = "neo4j_run_cypher",
            description = "在 Neo4j 上执行只读 Cypher 查询并以 JSON 返回结果"
    )
    public String runCypher(@ToolParam(description = "要执行的 Cypher 查询语句") String cypher) {
        // MCP 工具只做一层转发，实际查询与序列化由 Neo4jQueryService 负责
        return neo4jQueryService.runQueryAsJson(cypher);
    }
}
