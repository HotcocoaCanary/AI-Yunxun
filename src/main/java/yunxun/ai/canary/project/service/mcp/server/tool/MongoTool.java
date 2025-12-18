package yunxun.ai.canary.project.service.mcp.server.tool;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;
import yunxun.ai.canary.project.repository.mongo.MongoOperation;
import yunxun.ai.canary.project.repository.mongo.model.MongoDocument;
import yunxun.ai.canary.project.service.mcp.server.tool.model.ToolResponse;
import yunxun.ai.canary.project.service.mcp.server.tool.model.ToolResponses;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * MongoDB CRUD 操作工具集
 * 提供文档的增删改查（CRUD）操作，用于存储和管理论文原始文档数据
 */
@Component
public class MongoTool {

    private static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(5);

    private final MongoOperation mongoOperation;

    public MongoTool(MongoOperation mongoOperation) {
        this.mongoOperation = mongoOperation;
    }

    @Tool(name = "mongo_save_document", description = "保存文档到 MongoDB（用于记忆/知识条目/对话片段等）")
    public ToolResponse mongoSaveDocument(
            @ToolParam(required = true, description = "文档主题") String topic,
            @ToolParam(required = true, description = "文档内容") String content,
            @ToolParam(required = false, description = "标签列表") List<String> tags,
            @ToolParam(required = false, description = "来源信息（type/url/title 等）") Map<String, Object> source) {
        Instant startedAt = Instant.now();
        String traceId = UUID.randomUUID().toString();
        try {
            if (topic == null || topic.isBlank()) {
                return ToolResponses.error("INVALID_ARGUMENT", "topic 不能为空", null, traceId, startedAt);
            }
            if (content == null || content.isBlank()) {
                return ToolResponses.error("INVALID_ARGUMENT", "content 不能为空", null, traceId, startedAt);
            }
            String id = mongoOperation.saveDocument(topic, content, tags, source).block(DEFAULT_TIMEOUT);
            return ToolResponses.ok(Map.of("id", id), traceId, startedAt);
        }
        catch (Exception ex) {
            return ToolResponses.error("DB_ERROR", ex.getMessage(), null, traceId, startedAt);
        }
    }

    @Tool(name = "mongo_find_by_topic", description = "按主题检索 MongoDB 文档")
    public ToolResponse mongoFindByTopic(
            @ToolParam(required = true, description = "主题") String topic,
            @ToolParam(required = false, description = "返回条数，默认 5，最大 20") Integer limit) {
        Instant startedAt = Instant.now();
        String traceId = UUID.randomUUID().toString();
        try {
            if (topic == null || topic.isBlank()) {
                return ToolResponses.error("INVALID_ARGUMENT", "topic 不能为空", null, traceId, startedAt);
            }
            int lim = normalizeLimit(limit, 5);
            List<MongoDocument> items = mongoOperation.findByTopic(topic, lim).collectList().block(DEFAULT_TIMEOUT);
            return ToolResponses.ok(Map.of("items", items == null ? List.of() : items), traceId, startedAt);
        }
        catch (Exception ex) {
            return ToolResponses.error("DB_ERROR", ex.getMessage(), null, traceId, startedAt);
        }
    }

    @Tool(name = "mongo_find_by_id", description = "按 ID 获取 MongoDB 文档")
    public ToolResponse mongoFindById(@ToolParam(required = true, description = "文档 ID") String id) {
        Instant startedAt = Instant.now();
        String traceId = UUID.randomUUID().toString();
        try {
            if (id == null || id.isBlank()) {
                return ToolResponses.error("INVALID_ARGUMENT", "id 不能为空", null, traceId, startedAt);
            }
            MongoDocument doc = mongoOperation.findById(id).block(DEFAULT_TIMEOUT);
            if (doc == null) {
                return ToolResponses.error("NOT_FOUND", "未找到文档: " + id, null, traceId, startedAt);
            }
            return ToolResponses.ok(doc, traceId, startedAt);
        }
        catch (Exception ex) {
            return ToolResponses.error("DB_ERROR", ex.getMessage(), null, traceId, startedAt);
        }
    }

    @Tool(name = "mongo_update_document", description = "更新 MongoDB 文档（patch 方式）")
    public ToolResponse mongoUpdateDocument(
            @ToolParam(required = true, description = "文档 ID") String id,
            @ToolParam(required = true, description = "patch 对象，可包含 topic/content/tags/source") Map<String, Object> patch) {
        Instant startedAt = Instant.now();
        String traceId = UUID.randomUUID().toString();
        try {
            if (id == null || id.isBlank()) {
                return ToolResponses.error("INVALID_ARGUMENT", "id 不能为空", null, traceId, startedAt);
            }
            boolean updated = Boolean.TRUE.equals(mongoOperation.updateDocument(id, patch).block(DEFAULT_TIMEOUT));
            return ToolResponses.ok(Map.of("updated", updated), traceId, startedAt);
        }
        catch (Exception ex) {
            return ToolResponses.error("DB_ERROR", ex.getMessage(), null, traceId, startedAt);
        }
    }

    @Tool(name = "mongo_delete_document", description = "删除 MongoDB 文档")
    public ToolResponse mongoDeleteDocument(@ToolParam(required = true, description = "文档 ID") String id) {
        Instant startedAt = Instant.now();
        String traceId = UUID.randomUUID().toString();
        try {
            if (id == null || id.isBlank()) {
                return ToolResponses.error("INVALID_ARGUMENT", "id 不能为空", null, traceId, startedAt);
            }
            boolean deleted = Boolean.TRUE.equals(mongoOperation.deleteDocument(id).block(DEFAULT_TIMEOUT));
            return ToolResponses.ok(Map.of("deleted", deleted), traceId, startedAt);
        }
        catch (Exception ex) {
            return ToolResponses.error("DB_ERROR", ex.getMessage(), null, traceId, startedAt);
        }
    }

    @Tool(name = "mongo_find_all", description = "查询所有 MongoDB 文档（调试用，生产建议禁用）")
    public ToolResponse mongoFindAll(@ToolParam(required = false, description = "返回条数，默认 20，最大 50") Integer limit) {
        Instant startedAt = Instant.now();
        String traceId = UUID.randomUUID().toString();
        try {
            int lim = normalizeLimit(limit, 20, 50);
            List<MongoDocument> items = mongoOperation.findAll(lim).collectList().block(DEFAULT_TIMEOUT);
            return ToolResponses.ok(Map.of("items", items == null ? List.of() : items), traceId, startedAt);
        }
        catch (Exception ex) {
            return ToolResponses.error("DB_ERROR", ex.getMessage(), null, traceId, startedAt);
        }
    }

    private static int normalizeLimit(Integer limit, int defaultValue) {
        return normalizeLimit(limit, defaultValue, 20);
    }

    private static int normalizeLimit(Integer limit, int defaultValue, int max) {
        int lim = limit == null ? defaultValue : limit;
        if (lim <= 0) {
            return defaultValue;
        }
        return Math.min(lim, max);
    }
}
