package yunxun.ai.canary.backend.service.agent.llm;

import org.springframework.stereotype.Service;

@Service
public class RagPipelineService {

    private final LlmService llmService;

    public RagPipelineService(LlmService llmService) {
        this.llmService = llmService;
    }

    public String answer(String question) {
        return llmService.chat(question);
    }
}
