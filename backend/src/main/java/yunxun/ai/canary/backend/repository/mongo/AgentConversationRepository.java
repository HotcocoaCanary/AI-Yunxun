package yunxun.ai.canary.backend.repository.mongo;

import org.springframework.data.mongodb.repository.MongoRepository;
import yunxun.ai.canary.backend.model.entity.agent.AgentConversation;

public interface AgentConversationRepository extends MongoRepository<AgentConversation, String> {
}
