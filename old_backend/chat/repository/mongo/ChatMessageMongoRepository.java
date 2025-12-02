package yunxun.ai.canary.backend.chat.repository.mongo;

import org.springframework.data.mongodb.repository.MongoRepository;
import yunxun.ai.canary.backend.chat.model.entity.ChatMessageDoc;

import java.util.List;

public interface ChatMessageMongoRepository extends MongoRepository<ChatMessageDoc, String> {

    List<ChatMessageDoc> findBySessionIdOrderByCreatedAtAsc(Long sessionId);
}
