package yunxun.ai.canary.backend.qa.api;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import yunxun.ai.canary.backend.qa.model.dto.QaChatRequest;
import yunxun.ai.canary.backend.qa.model.dto.QaChatResponse;
import yunxun.ai.canary.backend.qa.service.llm.LlmService;

@RestController
@RequestMapping("/api/qa")
@RequiredArgsConstructor
public class QaController {

    private final LlmService llmService;

    @PostMapping("/chat")
    public QaChatResponse chat(@Valid @RequestBody QaChatRequest request) {
        String answer = llmService.chat(request.getQuestion());
        QaChatResponse response = new QaChatResponse();
        response.setAnswer(answer);
        return response;
    }
}

