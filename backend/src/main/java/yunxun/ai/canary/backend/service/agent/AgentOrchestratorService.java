package yunxun.ai.canary.backend.service.agent;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import yunxun.ai.canary.backend.model.dto.agent.AgentChatRequest;
import yunxun.ai.canary.backend.model.dto.agent.AgentChatResponse;
import yunxun.ai.canary.backend.service.agent.llm.LlmService;

@Service
@RequiredArgsConstructor
public class AgentOrchestratorService {

    private final LlmService llmService;

    public AgentChatResponse chat(AgentChatRequest request) {
        String answer = llmService.chat(request.getMessage());
        return AgentChatResponse.builder()
                .conversationId(request.getConversationId())
                .answer(answer)
                .build();
    }
}