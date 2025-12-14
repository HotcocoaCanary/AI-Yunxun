package yunxun.ai.canary.backend.mcp.client.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import yunxun.ai.canary.backend.mcp.client.model.ToolCallInfo;
import yunxun.ai.canary.backend.mcp.client.service.McpChatService;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * REST 控制器，为前端提供简单的聊天接口
 * <p>
 * 匹配前端期望的接口格式：
 *   POST /api/chat  { "message": "..." }  ->  { "reply": "...", "graphJson": "...", "chartJson": "..." }
 */
@RestController
@Validated
@RequestMapping(path = "/api/chat")
public class McpChatController {

    private final McpChatService chatService;
    private final ObjectMapper objectMapper;

    public McpChatController(McpChatService chatService, ObjectMapper objectMapper) {
        this.chatService = chatService;
        this.objectMapper = objectMapper;
    }

    /**
     * 处理聊天请求（响应式）
     * 接收用户消息，返回自然语言回答、图谱数据和图表数据
     */
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ChatResponse> chat(@Valid @RequestBody Mono<ChatRequest> requestMono) {
        return requestMono
                .doOnNext(request -> System.out.println("收到聊天请求: " + request.getMessage()))
                .flatMap(request -> chatService.chatReactive(request.getMessage()))
                .map(result -> {
                    ChatResponse response = new ChatResponse();
                    response.setReply(result.replyText());
                    response.setGraphJson(result.graphJson());
                    response.setChartJson(result.chartJson());
                    response.setToolCalls(result.toolCalls());
                    System.out.println("聊天请求处理完成，回复长度: " + (result.replyText() != null ? result.replyText().length() : 0));
                    return response;
                })
                .doOnError(e -> {
                    System.err.println("处理聊天请求时出错: " + e.getMessage());
                    e.printStackTrace();
                });
    }

    /**
     * 流式聊天请求（Server-Sent Events）
     * 接收用户消息，流式返回自然语言回答、工具调用信息、图谱数据和图表数据
     */
    @PostMapping(path = "/stream", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> chatStream(@Valid @RequestBody Mono<ChatRequest> requestMono) {
        return requestMono
                .doOnNext(request -> System.out.println("收到流式聊天请求: " + request.getMessage()))
                .flatMapMany(request -> chatService.chatStream(request.getMessage()))
                .doOnError(e -> {
                    System.err.println("流式聊天请求处理出错: " + e.getMessage());
                    e.printStackTrace();
                })
                .map(event -> {
                    try {
                        // 使用 LinkedHashMap 支持 null 值
                        Map<String, Object> eventMap = new LinkedHashMap<>();
                        eventMap.put("type", event.type().name().toLowerCase()); // 转换为小写
                        
                        if (event.toolCall() != null) {
                            Map<String, Object> toolCallMap = new LinkedHashMap<>();
                            toolCallMap.put("toolGroup", event.toolCall().toolGroup());
                            toolCallMap.put("toolName", event.toolCall().toolName());
                            toolCallMap.put("args", event.toolCall().args());
                            eventMap.put("toolCall", toolCallMap);
                        } else {
                            eventMap.put("toolCall", null);
                        }
                        
                        eventMap.put("content", event.content() != null ? event.content() : "");
                        eventMap.put("graphJson", event.graphJson() != null ? event.graphJson() : "");
                        eventMap.put("chartJson", event.chartJson() != null ? event.chartJson() : "");
                        
                        String json = objectMapper.writeValueAsString(eventMap);
                        System.out.println("发送SSE事件 - 类型: " + event.type().name().toLowerCase() + ", JSON长度: " + json.length());
                        if (event.type() == yunxun.ai.canary.backend.mcp.client.service.McpChatService.StreamEventType.CONTENT) {
                            System.out.println("SSE事件内容预览: " + (event.content() != null ? event.content().substring(0, Math.min(50, event.content().length())) : "null"));
                        }
                        
                        return ServerSentEvent.<String>builder()
                                .event(event.type().name().toLowerCase())
                                .data(json)
                                .build();
                    } catch (Exception e) {
                        System.err.println("序列化SSE事件时出错: " + e.getMessage());
                        e.printStackTrace();
                        return ServerSentEvent.<String>builder()
                                .event("error")
                                .data("{\"error\": \"" + e.getMessage().replace("\"", "\\\"") + "\"}")
                                .build();
                    }
                });
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

        /**
         * 工具调用列表
         */
        private List<ToolCallInfo> toolCalls;

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

        public List<ToolCallInfo> getToolCalls() {
            return toolCalls != null ? toolCalls : new ArrayList<>();
        }

        public void setToolCalls(List<ToolCallInfo> toolCalls) {
            this.toolCalls = toolCalls;
        }
    }
}
