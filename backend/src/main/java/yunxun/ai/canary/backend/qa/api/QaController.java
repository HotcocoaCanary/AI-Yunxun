package yunxun.ai.canary.backend.qa.api;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import yunxun.ai.canary.backend.qa.model.dto.QaChatRequest;
import yunxun.ai.canary.backend.qa.model.dto.QaChatResponse;
import yunxun.ai.canary.backend.qa.service.agent.QaAgentService;
import yunxun.ai.canary.backend.qa.service.llm.LlmService;

@RestController
@RequestMapping("/api/qa")
@RequiredArgsConstructor
public class QaController {

    private final LlmService llmService;
    private final QaAgentService qaAgentService;

    @PostMapping("/chat")
    public QaChatResponse chat(@Valid @RequestBody QaChatRequest request) {
        String answer = llmService.chat(request.getQuestion());
        QaChatResponse response = new QaChatResponse();
        response.setAnswer(answer);
        return response;
    }

    /**
     * 智能问答接口：
     * - 大模型会根据问题自动判断是否需要访问 Neo4j 工具
     * - 如需要则查询图数据库，再给出最终回答
     */
    @PostMapping("/chat/auto")
    public QaChatResponse chatAuto(@Valid @RequestBody QaChatRequest request) {
        String answer = qaAgentService.answerWithNeo4jIfNeeded(request.getQuestion());
        QaChatResponse response = new QaChatResponse();
        response.setAnswer(answer);
        return response;
    }
}
