package yunxun.ai.canary.backend.qa.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;
import yunxun.ai.canary.backend.qa.model.entity.AgentConversation;
import yunxun.ai.canary.backend.qa.repository.mongo.AgentConversationRepository;

import java.util.Optional;
import java.util.concurrent.Future;

@Slf4j
@Service
@RequiredArgsConstructor
public class AgentConversationService {

    private final AgentConversationRepository repository;

    public Optional<AgentConversation> findById(String conversationId) {
        try {
            return repository.findById(conversationId);
        } catch (Exception ex) {
            log.warn("load conversation {} failed: {}", conversationId, ex.getMessage());
            return Optional.empty();
        }
    }

    @Async("conversationTaskExecutor")
    public Future<Void> saveAsync(AgentConversation conversation) {
        try {
            repository.save(conversation);
        } catch (Exception ex) {
            log.warn("save conversation {} failed: {}", conversation.getId(), ex.getMessage());
        }
        return AsyncResult.forValue(null);
    }
}
