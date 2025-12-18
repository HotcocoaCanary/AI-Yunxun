package yunxun.ai.canary.project.repository.mongo;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;
import yunxun.ai.canary.project.repository.mongo.model.MongoDocument;

public interface MongoDocumentRepository extends ReactiveMongoRepository<MongoDocument, String> {

    Flux<MongoDocument> findByTopic(String topic);
}

