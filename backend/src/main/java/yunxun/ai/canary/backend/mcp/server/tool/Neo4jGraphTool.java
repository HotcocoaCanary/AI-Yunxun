package yunxun.ai.canary.backend.mcp.server.tool;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;
import yunxun.ai.canary.backend.db.neo4j.Neo4jGraphService;

import java.util.Map;

/**
 * MCP tool wrapper for Neo4jGraphService.
 * This class only exposes methods as MCP tools and delegates all logic
 * to the db.neo4j.Neo4jGraphService.
 */
@Component
public class Neo4jGraphTool {

    private final Neo4jGraphService graphService;

    public Neo4jGraphTool(Neo4jGraphService graphService) {
        this.graphService = graphService;
    }

    // ===== Node CRUD =====

    @Tool(name = "neo4j_create_node", description = "Create a node with a label and properties")
    public String createNode(
            @ToolParam(description = "Node label") String label,
            @ToolParam(description = "Properties to set on the node") Map<String, Object> properties) {
        return graphService.createNode(label, properties);
    }

    @Tool(name = "neo4j_delete_node", description = "Delete nodes by label and optional property filter")
    public String deleteNode(
            @ToolParam(description = "Node label") String label,
            @ToolParam(description = "Property key to filter by (optional)") String propertyKey,
            @ToolParam(description = "Property value to filter by (optional)") String propertyValue) {
        return graphService.deleteNode(label, propertyKey, propertyValue);
    }

    @Tool(name = "neo4j_find_node", description = "Find nodes by label and optional property filter, returning JSON")
    public String findNode(
            @ToolParam(description = "Node label") String label,
            @ToolParam(description = "Property key to filter by (optional)") String propertyKey,
            @ToolParam(description = "Property value to filter by (optional)") String propertyValue,
            @ToolParam(description = "Maximum number of nodes to return") Integer limit) {
        return graphService.findNode(label, propertyKey, propertyValue, limit);
    }

    @Tool(name = "neo4j_update_node", description = "Update properties of nodes that match a simple filter")
    public String updateNode(
            @ToolParam(description = "Node label") String label,
            @ToolParam(description = "Property key to filter by (optional)") String propertyKey,
            @ToolParam(description = "Property value to filter by (optional)") String propertyValue,
            @ToolParam(description = "Properties to merge into the node") Map<String, Object> properties) {
        return graphService.updateNode(label, propertyKey, propertyValue, properties);
    }

    // ===== Relationship CRUD =====

    @Tool(name = "neo4j_create_relationship", description = "Create a relationship between two nodes")
    public String createRelationship(
            @ToolParam(description = "Start node label") String startLabel,
            @ToolParam(description = "Start node property key (optional)") String startKey,
            @ToolParam(description = "Start node property value (optional)") String startValue,
            @ToolParam(description = "End node label") String endLabel,
            @ToolParam(description = "End node property key (optional)") String endKey,
            @ToolParam(description = "End node property value (optional)") String endValue,
            @ToolParam(description = "Relationship type") String type,
            @ToolParam(description = "Relationship properties") Map<String, Object> properties) {
        return graphService.createRelationship(startLabel, startKey, startValue,
                endLabel, endKey, endValue, type, properties);
    }

    @Tool(name = "neo4j_delete_relationship", description = "Delete relationships by type and endpoint filters")
    public String deleteRelationship(
            @ToolParam(description = "Start node label") String startLabel,
            @ToolParam(description = "Start node property key (optional)") String startKey,
            @ToolParam(description = "Start node property value (optional)") String startValue,
            @ToolParam(description = "End node label") String endLabel,
            @ToolParam(description = "End node property key (optional)") String endKey,
            @ToolParam(description = "End node property value (optional)") String endValue,
            @ToolParam(description = "Relationship type") String type) {
        return graphService.deleteRelationship(startLabel, startKey, startValue,
                endLabel, endKey, endValue, type);
    }

    @Tool(name = "neo4j_find_relationship", description = "Find relationships by type and endpoint filters, returning JSON")
    public String findRelationship(
            @ToolParam(description = "Start node label") String startLabel,
            @ToolParam(description = "Start node property key (optional)") String startKey,
            @ToolParam(description = "Start node property value (optional)") String startValue,
            @ToolParam(description = "End node label") String endLabel,
            @ToolParam(description = "End node property key (optional)") String endKey,
            @ToolParam(description = "End node property value (optional)") String endValue,
            @ToolParam(description = "Relationship type") String type,
            @ToolParam(description = "Maximum number of relationships to return") Integer limit) {
        return graphService.findRelationship(startLabel, startKey, startValue,
                endLabel, endKey, endValue, type, limit);
    }

    @Tool(name = "neo4j_update_relationship", description = "Update properties of relationships that match simple filters")
    public String updateRelationship(
            @ToolParam(description = "Start node label") String startLabel,
            @ToolParam(description = "Start node property key (optional)") String startKey,
            @ToolParam(description = "Start node property value (optional)") String startValue,
            @ToolParam(description = "End node label") String endLabel,
            @ToolParam(description = "End node property key (optional)") String endKey,
            @ToolParam(description = "End node property value (optional)") String endValue,
            @ToolParam(description = "Relationship type") String type,
            @ToolParam(description = "Properties to merge into the relationship") Map<String, Object> properties) {
        return graphService.updateRelationship(startLabel, startKey, startValue,
                endLabel, endKey, endValue, type, properties);
    }
}

