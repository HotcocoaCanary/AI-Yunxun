package yunxun.ai.canary.backend.db.mongo;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;
import yunxun.ai.canary.backend.db.mongo.model.RawPaperDocument;

/**
 * Reactive Mongo repository for raw paper documents.
 */
public interface RawPaperDocumentRepository extends ReactiveMongoRepository<RawPaperDocument, String> {

    Flux<RawPaperDocument> findByTopic(String topic);
}
