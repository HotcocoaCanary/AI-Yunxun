package ai.canary.mcp.neo4j.tool;

import ai.canary.mcp.neo4j.service.Neo4jService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springaicommunity.mcp.annotation.McpTool;
import org.springaicommunity.mcp.annotation.McpToolParam;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class QueryGraphTool {

    private final Neo4jService neo4jService;
    private final ObjectMapper objectMapper;

    @McpTool(name = "query_node_with_relationships", description = "Query a node and all its relationships. Returns the node and all connected nodes and relationships.")
    public String queryNodeWithRelationships(
            @McpToolParam(description = "The node ID to query", required = true) String nodeId) {
        try {
            List<Map<String, Object>> results = neo4jService.queryNodeWithRelationships(nodeId);
            return objectMapper.writeValueAsString(results);
        } catch (JsonProcessingException e) {
            return "{\"error\": \"Failed to serialize results: " + e.getMessage() + "\"}";
        } catch (Exception e) {
            return "{\"error\": \"Failed to query node: " + e.getMessage() + "\"}";
        }
    }

    @McpTool(name = "find_path", description = "Find the shortest path between two nodes. Returns the path as a list of nodes and relationships.")
    public String findPath(
            @McpToolParam(description = "The starting node ID", required = true) String startNodeId,
            @McpToolParam(description = "The ending node ID", required = true) String endNodeId) {
        try {
            List<Map<String, Object>> results = neo4jService.findPath(startNodeId, endNodeId);
            return objectMapper.writeValueAsString(results);
        } catch (JsonProcessingException e) {
            return "{\"error\": \"Failed to serialize results: " + e.getMessage() + "\"}";
        } catch (Exception e) {
            return "{\"error\": \"Failed to find path: " + e.getMessage() + "\"}";
        }
    }

    @McpTool(name = "match_pattern", description = "Match a graph pattern in Neo4j. The pattern should be a Cypher pattern like '(a:Person)-[:KNOWS]->(b:Person)'. Returns matching subgraphs.")
    public String matchPattern(
            @McpToolParam(description = "The graph pattern to match (e.g., '(a:Person)-[:KNOWS]->(b:Person)')", required = true) String pattern) {
        try {
            List<Map<String, Object>> results = neo4jService.matchPattern(pattern);
            return objectMapper.writeValueAsString(results);
        } catch (JsonProcessingException e) {
            return "{\"error\": \"Failed to serialize results: " + e.getMessage() + "\"}";
        } catch (Exception e) {
            return "{\"error\": \"Failed to match pattern: " + e.getMessage() + "\"}";
        }
    }
}

