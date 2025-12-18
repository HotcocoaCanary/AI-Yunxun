package yunxun.ai.canary.project.app;

import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import yunxun.ai.canary.project.app.dto.ChatRequest;
import yunxun.ai.canary.project.app.dto.ChatResponse;
import yunxun.ai.canary.project.app.dto.ChatStreamEvent;
import yunxun.ai.canary.project.service.chat.ChatOrchestrator;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final ChatOrchestrator chatOrchestrator;

    public ChatController(ChatOrchestrator chatOrchestrator) {
        this.chatOrchestrator = chatOrchestrator;
    }

    @PostMapping
    public Mono<ChatResponse> chat(@RequestBody ChatRequest request) {
        return chatOrchestrator.chat(request);
    }

    @PostMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<ChatStreamEvent>> chatStream(@RequestBody ChatRequest request) {
        return chatOrchestrator.chatStream(request);
    }
}

