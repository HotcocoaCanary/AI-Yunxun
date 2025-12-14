package yunxun.ai.canary.backend.mcp.server.tool;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;
import yunxun.ai.canary.backend.db.mongo.model.RawPaperDocument;
import yunxun.ai.canary.backend.db.mongo.RawPaperDocumentRepository;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

/**
 * MongoDB 操作工具集
 * 提供文档的增删改查（CRUD）操作，用于存储和管理论文原始文档数据
 */
@Component
public class MongoTool {

    private final RawPaperDocumentRepository rawPaperRepository;
    private final ObjectMapper objectMapper;

    public MongoTool(RawPaperDocumentRepository rawPaperRepository, ObjectMapper objectMapper) {
        this.rawPaperRepository = rawPaperRepository;
        this.objectMapper = objectMapper;
    }

    /**
     * 保存原始文本到 MongoDB
     */
    @Tool(
            name = "mongo_save_document",
            description = "保存原始文本文档到 MongoDB，用于后续的图谱处理"
    )
    public String saveDocument(
            @ToolParam(description = "文档主题或分类") String topic,
            @ToolParam(description = "文档标题（可选）") String title,
            @ToolParam(description = "文档摘要或内容") String summary,
            @ToolParam(description = "来源类型，如 URL 或其他标识（可选）") String sourceType) {

        RawPaperDocument doc = new RawPaperDocument();
        doc.setTopic(topic);
        doc.setTitle(title != null ? title : "");
        doc.setSummary(summary);
        doc.setSourceType(sourceType != null ? sourceType : "MCP_INGESTED");
        doc.setCreatedAt(Instant.now());

        RawPaperDocument saved = rawPaperRepository.save(doc);
        return "文档已保存，ID: " + saved.getId();
    }

    /**
     * 根据主题查询文档
     */
    @Tool(
            name = "mongo_find_by_topic",
            description = "根据主题查询 MongoDB 中的文档列表"
    )
    public String findByTopic(
            @ToolParam(description = "文档主题") String topic) {
        try {
            List<RawPaperDocument> documents = rawPaperRepository.findByTopic(topic);
            if (documents.isEmpty()) {
                return "未找到主题为 '" + topic + "' 的文档";
            }
            return objectMapper.writeValueAsString(documents);
        } catch (JsonProcessingException e) {
            return "查询失败: " + e.getMessage();
        }
    }

    /**
     * 根据 ID 查询文档
     */
    @Tool(
            name = "mongo_find_by_id",
            description = "根据文档 ID 查询 MongoDB 中的文档"
    )
    public String findById(
            @ToolParam(description = "文档 ID") String id) {
        try {
            return rawPaperRepository.findById(id)
                    .map(doc -> {
                        try {
                            return objectMapper.writeValueAsString(doc);
                        } catch (JsonProcessingException e) {
                            return "序列化失败: " + e.getMessage();
                        }
                    })
                    .orElse("未找到 ID 为 '" + id + "' 的文档");
        } catch (Exception e) {
            return "查询失败: " + e.getMessage();
        }
    }

    /**
     * 更新文档
     */
    @Tool(
            name = "mongo_update_document",
            description = "更新 MongoDB 中的文档信息"
    )
    public String updateDocument(
            @ToolParam(description = "文档 ID") String id,
            @ToolParam(description = "新的主题（可选）") String topic,
            @ToolParam(description = "新的标题（可选）") String title,
            @ToolParam(description = "新的摘要（可选）") String summary) {
        return rawPaperRepository.findById(id)
                .map(doc -> {
                    if (topic != null && !topic.isBlank()) {
                        doc.setTopic(topic);
                    }
                    if (title != null && !title.isBlank()) {
                        doc.setTitle(title);
                    }
                    if (summary != null && !summary.isBlank()) {
                        doc.setSummary(summary);
                    }
                    rawPaperRepository.save(doc);
                    return "文档已更新，ID: " + id;
                })
                .orElse("未找到 ID 为 '" + id + "' 的文档");
    }

    /**
     * 删除文档
     */
    @Tool(
            name = "mongo_delete_document",
            description = "根据文档 ID 删除 MongoDB 中的文档"
    )
    public String deleteDocument(
            @ToolParam(description = "文档 ID") String id) {
        if (rawPaperRepository.existsById(id)) {
            rawPaperRepository.deleteById(id);
            return "文档已删除，ID: " + id;
        } else {
            return "未找到 ID 为 '" + id + "' 的文档";
        }
    }

    /**
     * 查询所有文档
     */
    @Tool(
            name = "mongo_find_all",
            description = "查询 MongoDB 中的所有文档"
    )
    public String findAll() {
        try {
            List<RawPaperDocument> documents = rawPaperRepository.findAll();
            return objectMapper.writeValueAsString(documents);
        } catch (JsonProcessingException e) {
            return "查询失败: " + e.getMessage();
        }
    }
}
