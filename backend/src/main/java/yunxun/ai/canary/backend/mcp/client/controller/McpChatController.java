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
 * REST 控制器，为前端提供简单的聊天接口
 * <p>
 * 匹配前端期望的接口格式：
 *   POST /api/chat  { "message": "..." }  ->  { "reply": "...", "graphJson": "...", "chartJson": "..." }
 */
@RestController
@Validated
@RequestMapping(path = "/api/chat", produces = MediaType.APPLICATION_JSON_VALUE)
public class McpChatController {

    private final McpChatService chatService;

    public McpChatController(McpChatService chatService) {
        this.chatService = chatService;
    }

    /**
     * 处理聊天请求
     * 接收用户消息，返回自然语言回答、图谱数据和图表数据
     */
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ChatResponse chat(@Valid @RequestBody ChatRequest request) {
        McpChatService.ChatResult result = chatService.chat(request.getMessage());
        ChatResponse response = new ChatResponse();
        response.setReply(result.replyText());
        response.setGraphJson(result.graphJson());
        response.setChartJson(result.chartJson());
        return response;
    }

    /**
     * 聊天请求体
     */
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

    /**
     * 聊天响应体
     */
    public static class ChatResponse {

        /** 自然语言回答 */
        private String reply;
        
        /**
         * 可选的图谱数据 JSON 字符串，由模型生成
         * 遵循 {@link McpChatService.ChatResult#graphJson()} 的约定
         * 格式：{"nodes":[...], "edges":[...]}
         */
        private String graphJson;
        
        /**
         * 可选的图表数据 JSON 字符串，由模型或图表 MCP 工具生成
         * 通常是前端可理解的序列化 ChartResponse 对象
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
