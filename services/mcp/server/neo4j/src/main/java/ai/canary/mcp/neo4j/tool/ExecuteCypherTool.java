package ai.canary.mcp.neo4j.tool;

import ai.canary.mcp.neo4j.service.Neo4jService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springaicommunity.mcp.annotation.McpToolParam;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class ExecuteCypherTool {

    private final Neo4jService neo4jService;
    private final ObjectMapper objectMapper;

    @Tool(name = "execute_cypher", description = "Execute a Cypher query statement against Neo4j database. Supports CREATE, MATCH, SET, MERGE, DELETE operations. Returns query results as JSON.")
    public String executeCypher(
            @McpToolParam(description = "The Cypher query statement to execute (e.g., 'MATCH (n) RETURN n LIMIT 10', 'CREATE (n:Person {name: $name})', etc.)", required = true) String cypher) {
        try {
            List<Map<String, Object>> results = neo4jService.executeCypher(cypher);
            return objectMapper.writeValueAsString(results);
        } catch (JsonProcessingException e) {
            return "{\"error\": \"Failed to serialize results: " + e.getMessage() + "\"}";
        } catch (Exception e) {
            return "{\"error\": \"Failed to execute Cypher: " + e.getMessage() + "\"}";
        }
    }
}

