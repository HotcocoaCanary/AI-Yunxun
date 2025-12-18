package yunxun.ai.canary.project.repository.mongo;

import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import yunxun.ai.canary.project.repository.mongo.model.MongoDocument;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
public class MongoOperationImpl implements MongoOperation {

    private final MongoDocumentRepository repository;

    public MongoOperationImpl(MongoDocumentRepository repository) {
        this.repository = repository;
    }

    @Override
    public Mono<String> saveDocument(String topic, String content, List<String> tags, Map<String, Object> source) {
        MongoDocument doc = new MongoDocument();
        doc.setTopic(topic);
        doc.setContent(content);
        doc.setTags(tags);
        doc.setSource(source);
        return repository.save(doc).map(MongoDocument::getId);
    }

    @Override
    public Flux<MongoDocument> findByTopic(String topic, int limit) {
        if (limit <= 0) {
            return Flux.empty();
        }
        return repository.findByTopic(topic).take(limit);
    }

    @Override
    public Mono<MongoDocument> findById(String id) {
        return repository.findById(id);
    }

    @Override
    public Mono<Boolean> updateDocument(String id, Map<String, Object> patch) {
        if (patch == null || patch.isEmpty()) {
            return Mono.just(false);
        }
        return repository.findById(id)
                .flatMap(existing -> {
                    if (patch.containsKey("topic")) {
                        existing.setTopic(Objects.toString(patch.get("topic"), null));
                    }
                    if (patch.containsKey("content")) {
                        existing.setContent(Objects.toString(patch.get("content"), null));
                    }
                    if (patch.containsKey("tags")) {
                        @SuppressWarnings("unchecked")
                        List<String> tags = (List<String>) patch.get("tags");
                        existing.setTags(tags);
                    }
                    if (patch.containsKey("source")) {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> source = (Map<String, Object>) patch.get("source");
                        existing.setSource(source);
                    }
                    return repository.save(existing);
                })
                .map(saved -> true)
                .defaultIfEmpty(false);
    }

    @Override
    public Mono<Boolean> deleteDocument(String id) {
        return repository.deleteById(id).thenReturn(true);
    }

    @Override
    public Flux<MongoDocument> findAll(int limit) {
        if (limit <= 0) {
            return Flux.empty();
        }
        return repository.findAll().take(limit);
    }
}

