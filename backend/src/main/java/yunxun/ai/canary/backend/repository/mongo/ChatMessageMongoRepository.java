package yunxun.ai.canary.backend.repository.mongo;

import org.springframework.data.mongodb.repository.MongoRepository;
import yunxun.ai.canary.backend.model.entity.chat.ChatMessageDoc;

import java.util.List;

public interface ChatMessageMongoRepository extends MongoRepository<ChatMessageDoc, String> {

    List<ChatMessageDoc> findBySessionIdOrderByCreatedAtAsc(Long sessionId);
}
