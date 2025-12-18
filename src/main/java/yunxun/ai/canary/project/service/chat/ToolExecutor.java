package yunxun.ai.canary.project.service.chat;

import org.springframework.stereotype.Service;
import yunxun.ai.canary.project.service.mcp.server.tool.EChartGenerateTool;
import yunxun.ai.canary.project.service.mcp.server.tool.MongoTool;
import yunxun.ai.canary.project.service.mcp.server.tool.Neo4jTool;
import yunxun.ai.canary.project.service.mcp.server.tool.WebSearchTool;
import yunxun.ai.canary.project.service.mcp.server.tool.model.ToolResponse;
import yunxun.ai.canary.project.service.mcp.server.tool.model.ToolResponses;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class ToolExecutor {

    private final MongoTool mongoTool;
    private final Neo4jTool neo4jTool;
    private final EChartGenerateTool eChartGenerateTool;
    private final Optional<WebSearchTool> webSearchTool;

    public ToolExecutor(
            MongoTool mongoTool,
            Neo4jTool neo4jTool,
            EChartGenerateTool eChartGenerateTool,
            Optional<WebSearchTool> webSearchTool) {
        this.mongoTool = mongoTool;
        this.neo4jTool = neo4jTool;
        this.eChartGenerateTool = eChartGenerateTool;
        this.webSearchTool = webSearchTool;
    }

    public ToolResponse execute(String name, Map<String, Object> args) {
        String traceId = UUID.randomUUID().toString();
        Instant startedAt = Instant.now();
        if (name == null || name.isBlank()) {
            return ToolResponses.error("INVALID_ARGUMENT", "tool name 不能为空", null, traceId, startedAt);
        }
        Map<String, Object> safeArgs = args == null ? Map.of() : args;

        return switch (name) {
            case "mongo_save_document" -> mongoTool.mongoSaveDocument(
                    asString(safeArgs.get("topic")),
                    asString(safeArgs.get("content")),
                    asStringList(safeArgs.get("tags")),
                    asMap(safeArgs.get("source"))
            );
            case "mongo_find_by_topic" -> mongoTool.mongoFindByTopic(
                    asString(safeArgs.get("topic")),
                    asInt(safeArgs.get("limit"))
            );
            case "mongo_find_by_id" -> mongoTool.mongoFindById(asString(safeArgs.get("id")));
            case "mongo_update_document" -> mongoTool.mongoUpdateDocument(
                    asString(safeArgs.get("id")),
                    asMap(safeArgs.get("patch"))
            );
            case "mongo_delete_document" -> mongoTool.mongoDeleteDocument(asString(safeArgs.get("id")));
            case "mongo_find_all" -> mongoTool.mongoFindAll(asInt(safeArgs.get("limit")));

            case "neo4j_create_node" -> neo4jTool.neo4jCreateNode(
                    asString(safeArgs.get("label")),
                    asMap(safeArgs.get("properties"))
            );
            case "neo4j_find_node" -> neo4jTool.neo4jFindNode(
                    asString(safeArgs.get("id")),
                    asString(safeArgs.get("label")),
                    asString(safeArgs.get("property")),
                    asString(safeArgs.get("value"))
            );
            case "neo4j_update_node" -> neo4jTool.neo4jUpdateNode(asString(safeArgs.get("id")), asMap(safeArgs.get("patch")));
            case "neo4j_delete_node" -> neo4jTool.neo4jDeleteNode(asString(safeArgs.get("id")), asBoolean(safeArgs.get("detach")));
            case "neo4j_create_relationship" -> neo4jTool.neo4jCreateRelationship(
                    asString(safeArgs.get("fromId")),
                    asString(safeArgs.get("toId")),
                    asString(safeArgs.get("type")),
                    asMap(safeArgs.get("properties"))
            );
            case "neo4j_find_relationship" -> neo4jTool.neo4jFindRelationship(asString(safeArgs.get("id")));
            case "neo4j_update_relationship" -> neo4jTool.neo4jUpdateRelationship(asString(safeArgs.get("id")), asMap(safeArgs.get("patch")));
            case "neo4j_delete_relationship" -> neo4jTool.neo4jDeleteRelationship(asString(safeArgs.get("id")));
            case "neo4j_find_path" -> neo4jTool.neo4jFindPath(
                    asString(safeArgs.get("fromId")),
                    asString(safeArgs.get("toId")),
                    asInt(safeArgs.get("maxDepth")),
                    asStringList(safeArgs.get("types"))
            );
            case "neo4j_find_neighbors" -> neo4jTool.neo4jFindNeighbors(
                    asString(safeArgs.get("id")),
                    asInt(safeArgs.get("depth")),
                    asStringList(safeArgs.get("types"))
            );
            case "neo4j_fuzzy_search" -> neo4jTool.neo4jFuzzySearch(
                    asString(safeArgs.get("query")),
                    asStringList(safeArgs.get("labels")),
                    asInt(safeArgs.get("limit"))
            );

            case "web_search" -> webSearchTool
                    .map(tool -> tool.webSearch(
                            asString(safeArgs.get("query")),
                            asInt(safeArgs.get("maxResults")),
                            asString(safeArgs.get("language")),
                            asInt(safeArgs.get("recencyDays"))
                    ))
                    .orElseGet(() -> ToolResponses.error("UPSTREAM_ERROR", "web.search.enabled 未开启或 WebSearchTool 未加载", null, traceId, startedAt));

            case "echart_generate" -> eChartGenerateTool.echartGenerate(
                    asString(safeArgs.get("chartType")),
                    asString(safeArgs.get("title")),
                    asMapList(safeArgs.get("data")),
                    asMap(safeArgs.get("mapping")),
                    asMap(safeArgs.get("options")),
                    asMap(safeArgs.get("graph"))
            );

            default -> ToolResponses.error("INVALID_ARGUMENT", "未知工具: " + name, null, traceId, startedAt);
        };
    }

    private static String asString(Object value) {
        return value == null ? null : value.toString();
    }

    private static Integer asInt(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number n) {
            return n.intValue();
        }
        try {
            return Integer.parseInt(value.toString());
        }
        catch (NumberFormatException ignored) {
            return null;
        }
    }

    private static Boolean asBoolean(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Boolean b) {
            return b;
        }
        return Boolean.parseBoolean(value.toString());
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> asMap(Object value) {
        if (value == null) {
            return Map.of();
        }
        if (value instanceof Map<?, ?> map) {
            return (Map<String, Object>) map;
        }
        return Map.of();
    }

    @SuppressWarnings("unchecked")
    private static List<String> asStringList(Object value) {
        if (value == null) {
            return List.of();
        }
        if (value instanceof List<?> list) {
            return (List<String>) list;
        }
        return List.of(value.toString());
    }

    @SuppressWarnings("unchecked")
    private static List<Map<String, Object>> asMapList(Object value) {
        if (value == null) {
            return List.of();
        }
        if (value instanceof List<?> list) {
            return (List<Map<String, Object>>) list;
        }
        return List.of();
    }
}

