package yunxun.ai.canary.backend.mcp.server.tool;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;
import yunxun.ai.canary.backend.db.mongo.model.RawPaperDocument;
import yunxun.ai.canary.backend.db.mongo.RawPaperDocumentRepository;

import java.time.Instant;

/**
 * MCP tools for basic MongoDB operations.
 * For now we expose a simple tool to save raw text into Mongo.
 */
@Component
public class MongoTool {

    private final RawPaperDocumentRepository rawPaperRepository;

    public MongoTool(RawPaperDocumentRepository rawPaperRepository) {
        this.rawPaperRepository = rawPaperRepository;
    }

    @Tool(
            name = "mongo_save_raw_text",
            description = "Save raw text into MongoDB for later graph processing"
    )
    public String saveRawText(
            @ToolParam(description = "High-level topic or intent of the text") String topic,
            @ToolParam(description = "Optional source identifier, such as a URL") String source,
            @ToolParam(description = "Raw text content to store") String content) {

        RawPaperDocument doc = new RawPaperDocument();
        doc.setTopic(topic);
        // Use source as a lightweight title; content is stored in summary field for now.
        doc.setTitle(source);
        doc.setSummary(content);
        doc.setSourceType("MCP_INGESTED");
        doc.setCreatedAt(Instant.now());

        RawPaperDocument saved = rawPaperRepository.save(doc);
        return saved.getId();
    }
}
