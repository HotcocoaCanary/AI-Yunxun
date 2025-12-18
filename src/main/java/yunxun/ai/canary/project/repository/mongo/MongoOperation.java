package yunxun.ai.canary.project.repository.mongo;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import yunxun.ai.canary.project.repository.mongo.model.MongoDocument;

import java.util.List;
import java.util.Map;

public interface MongoOperation {

    Mono<String> saveDocument(String topic, String content, List<String> tags, Map<String, Object> source);

    Flux<MongoDocument> findByTopic(String topic, int limit);

    Mono<MongoDocument> findById(String id);

    Mono<Boolean> updateDocument(String id, Map<String, Object> patch);

    Mono<Boolean> deleteDocument(String id);

    Flux<MongoDocument> findAll(int limit);
}
