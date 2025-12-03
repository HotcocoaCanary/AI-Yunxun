package yunxun.ai.canary.backend.db.mongo;

import org.springframework.data.mongodb.repository.MongoRepository;
import yunxun.ai.canary.backend.db.mongo.model.RawPaperDocument;

import java.util.List;

/**
 * Mongo repository for raw paper documents.
 */
public interface RawPaperDocumentRepository extends MongoRepository<RawPaperDocument, String> {

    List<RawPaperDocument> findByTopic(String topic);
}

