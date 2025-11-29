package yunxun.ai.canary.backend.qa.repository.mongo;

import org.springframework.data.mongodb.repository.MongoRepository;
import yunxun.ai.canary.backend.qa.model.entity.AgentConversation;

public interface AgentConversationRepository extends MongoRepository<AgentConversation, String> {
}
