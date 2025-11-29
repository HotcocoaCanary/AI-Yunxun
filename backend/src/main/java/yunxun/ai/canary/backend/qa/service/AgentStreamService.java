package yunxun.ai.canary.backend.qa.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import yunxun.ai.canary.backend.analytics.model.dto.ChartSpecDto;
import yunxun.ai.canary.backend.chat.model.dto.AgentQueryPayloadDto;
import yunxun.ai.canary.backend.chat.model.dto.ChatMessageDto;
import yunxun.ai.canary.backend.chat.service.InMemoryChatService;
import yunxun.ai.canary.backend.qa.service.llm.LlmService;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class AgentStreamService {

    private final InMemoryChatService chatService;
    private final LlmService llmService;

    public SseEmitter streamAnswer(AgentQueryPayloadDto payload) {
        SseEmitter emitter = new SseEmitter(0L);
        String sessionId = payload.getMemoryScope().getSessionId();
        chatService.appendMessage(sessionId, "user", payload.getQuestion());

        CompletableFuture.runAsync(() -> {
            try {
                send(emitter, Map.of("type", "answer_chunk",
                        "content", "?????????..."));
                TimeUnit.MILLISECONDS.sleep(100);

                String llmReply = llmService.chat(payload.getQuestion());
                ChatMessageDto assistant = chatService.appendMessage(sessionId, "assistant", llmReply);

                send(emitter, Map.of("type", "answer", "content", assistant.getContent()));
                send(emitter, Map.of("type", "end", "messageId", assistant.getId()));
                emitter.complete();
            } catch (Exception e) {
                tryCompleteWithError(emitter, e);
            }
        });
        return emitter;
    }

    private void send(SseEmitter emitter, Map<String, Object> data) throws IOException {
        emitter.send(SseEmitter.event()
                .id(UUID.randomUUID().toString())
                .data(data)
                .reconnectTime(1000L));
    }

    private void tryCompleteWithError(SseEmitter emitter, Exception e) {
        try {
            emitter.completeWithError(e);
        } catch (IllegalStateException ignored) {
        }
    }
}

