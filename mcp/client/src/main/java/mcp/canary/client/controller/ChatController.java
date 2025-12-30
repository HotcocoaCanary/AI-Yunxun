package mcp.canary.client.controller;

import mcp.canary.client.dto.ChatRequest;
import mcp.canary.client.dto.ChatResponse;
import mcp.canary.client.service.ChatService;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

/**
 * 对话接口：SSE 流式输出。
 */
@RestController
@RequestMapping("/api")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping(value = "/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<ChatResponse>> chat(@RequestBody ChatRequest request) {
        String message = request != null ? request.message() : null;
        return chatService.streamChat(message == null ? "" : message)
                .map(resp -> ServerSentEvent.<ChatResponse>builder(resp)
                        .event(resp.type())
                        .build());
    }
}



