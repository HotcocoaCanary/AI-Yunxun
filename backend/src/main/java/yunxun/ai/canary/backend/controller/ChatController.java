package yunxun.ai.canary.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import yunxun.ai.canary.backend.model.dto.chat.AgentQueryPayloadDto;
import yunxun.ai.canary.backend.model.dto.chat.ChatMessageDto;
import yunxun.ai.canary.backend.model.dto.chat.ChatNodeCreateRequest;
import yunxun.ai.canary.backend.model.dto.chat.ChatNodeRenameRequest;
import yunxun.ai.canary.backend.model.dto.chat.ChatTreeNodeDto;
import yunxun.ai.canary.backend.service.agent.AgentStreamService;
import yunxun.ai.canary.backend.service.chat.InMemoryChatService;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final InMemoryChatService chatService;
    private final AgentStreamService agentStreamService;
    private final ObjectMapper objectMapper;

    @GetMapping("/tree")
    public List<ChatTreeNodeDto> getTree() {
        return chatService.getTree();
    }

    @PostMapping("/node")
    public ChatTreeNodeDto createNode(@RequestBody ChatNodeCreateRequest request) {
        return chatService.createNode(request);
    }

    @PutMapping("/node/{id}")
    public ChatTreeNodeDto renameNode(@PathVariable String id, @RequestBody ChatNodeRenameRequest request) {
        return chatService.renameNode(id, request);
    }

    @DeleteMapping("/node/{id}")
    public void deleteNode(@PathVariable String id) {
        chatService.deleteNode(id);
    }

    @GetMapping("/session/{sessionId}/messages")
    public List<ChatMessageDto> getMessages(@PathVariable String sessionId) {
        return chatService.getMessages(sessionId);
    }

    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream(@RequestParam("payload") String payload) throws IOException {
        AgentQueryPayloadDto query = objectMapper.readValue(payload, AgentQueryPayloadDto.class);
        return agentStreamService.streamAnswer(query);
    }
}
