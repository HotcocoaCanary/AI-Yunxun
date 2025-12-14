package yunxun.ai.canary.backend.db.mongo.model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

/**
 * Raw paper-like content stored in MongoDB.
 * This is the source text for later triple extraction and graph ingestion.
 */
@Setter
@Getter
@Document(collection = "raw_papers")
public class RawPaperDocument {

    @Id
    private String id;

    private String topic;
    private String title;
    private String summary;
    private String sourceType; // e.g. "LLM_SYNTHESIZED" or "CRAWLED"
    private Instant createdAt;

}

