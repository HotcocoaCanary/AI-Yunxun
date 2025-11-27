package yunxun.ai.canary.backend.service.mcp.tool;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

@Component
public class GraphTool {

    @Tool(name = "kg_query", description = "Query knowledge graph with Cypher")
    public String query(@ToolParam(description = "cypher query") String cypher) {
        return "Graph result for cypher: " + cypher;
    }
}
