package yunxun.ai.canary.backend.mcp.client.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import yunxun.ai.canary.backend.mcp.client.service.McpChatService;

/**
 * REST controller that exposes a simple chat endpoint for the frontend.
 * <p>
 * This matches the frontend expectation:
 *   POST /api/chat  { "message": "..." }  ->  { "reply": "..." }
 */
@RestController
@Validated
@RequestMapping(path = "/api/chat", produces = MediaType.APPLICATION_JSON_VALUE)
public class McpChatController {

    private final McpChatService chatService;

    public McpChatController(McpChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ChatResponse chat(@Valid @RequestBody ChatRequest request) {
        McpChatService.ChatResult result = chatService.chat(request.getMessage());
        ChatResponse response = new ChatResponse();
        response.setReply(result.replyText());
        response.setGraphJson(result.graphJson());
        response.setChartJson(result.chartJson());
        return response;
    }

    public static class ChatRequest {

        @NotBlank
        private String message;

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }

    public static class ChatResponse {

        private String reply;
        /**
         * Optional graph data JSON string produced by the model,
         * following the convention in {@link McpChatService.ChatResult#graphJson()}.
         */
        private String graphJson;
        /**
         * Optional chart JSON string produced by the model / chart MCP tool,
         * typically a serialized ChartResponse object understood by the frontend.
         */
        private String chartJson;

        public String getReply() {
            return reply;
        }

        public void setReply(String reply) {
            this.reply = reply;
        }

        public String getGraphJson() {
            return graphJson;
        }

        public void setGraphJson(String graphJson) {
            this.graphJson = graphJson;
        }

        public String getChartJson() {
            return chartJson;
        }

        public void setChartJson(String chartJson) {
            this.chartJson = chartJson;
        }
    }
}
