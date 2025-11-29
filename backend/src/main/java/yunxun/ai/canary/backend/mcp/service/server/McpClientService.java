package yunxun.ai.canary.backend.mcp.service.server;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import yunxun.ai.canary.backend.chat.model.dto.ChatMessageAppendRequest;
import yunxun.ai.canary.backend.chat.model.dto.ChatMessageDto;
import yunxun.ai.canary.backend.mcp.model.dto.McpChatRequest;
import yunxun.ai.canary.backend.chat.service.ChatHistoryService;
import yunxun.ai.canary.backend.qa.service.llm.LlmService;

import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
public class McpClientService {

    private final ChatHistoryService chatHistoryService;
    private final LlmService llmService;

    public SseEmitter streamChat(McpChatRequest request) {
        SseEmitter emitter = new SseEmitter(0L);
        CompletableFuture.runAsync(() -> {
            try {
                String userMsg = Optional.ofNullable(request.getMessages())
                        .filter(list -> !list.isEmpty())
                        .map(list -> list.get(list.size() - 1).getContent())
                        .orElse("Hello, how can I help you?");

                String llmReply = llmService.chat(userMsg);

                ChatMessageAppendRequest appendRequest = new ChatMessageAppendRequest();
                appendRequest.setSessionId(request.getSessionId());
                appendRequest.setRole("assistant");
                appendRequest.setContent(llmReply);

                ChatMessageDto saved = chatHistoryService.appendMessage(appendRequest);

                emitter.send(SseEmitter.event().name("markdown").data(saved.getContent()));
                emitter.send(SseEmitter.event().name("end").data(saved.getId()));
                emitter.complete();
            } catch (IOException e) {
                emitter.completeWithError(e);
            }
        });
        return emitter;
    }
}


