package yunxun.ai.canary.backend.mcp.service.tool;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import yunxun.ai.canary.backend.qa.model.dto.QaChatRequest;
import yunxun.ai.canary.backend.qa.model.dto.QaChatResponse;

@Component
public class LlmTool {

    private final RestClient restClient;

    public LlmTool(@Value("${server.port:8080}") int serverPort) {
        this.restClient = RestClient.builder()
                .baseUrl("http://localhost:" + serverPort)
                .build();
    }

    @Tool(name = "llm_answer", description = "调用本地大模型（Ollama）进行问答")
    public String answer(@ToolParam(description = "用户问题") String question) {
        QaChatRequest request = new QaChatRequest();
        request.setQuestion(question);

        QaChatResponse response = restClient
                .post()
                .uri("/api/qa/chat")
                .body(request)
                .retrieve()
                .body(QaChatResponse.class);

        return response != null ? response.getAnswer() : "";
    }
}
