package yunxun.ai.canary.backend.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import yunxun.ai.canary.backend.model.dto.chat.ChatMessageAppendRequest;
import yunxun.ai.canary.backend.model.dto.chat.ChatMessageDto;
import yunxun.ai.canary.backend.model.dto.chat.ChatNodeCreateRequest;
import yunxun.ai.canary.backend.model.dto.chat.ChatNodeRenameRequest;
import yunxun.ai.canary.backend.model.dto.chat.ChatTreeNodeDto;
import yunxun.ai.canary.backend.model.dto.mcp.McpChatRequest;
import yunxun.ai.canary.backend.service.agent.chat.ChatHistoryService;
import yunxun.ai.canary.backend.service.mcp.server.McpClientService;

import java.util.List;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatHistoryService chatHistoryService;
    private final McpClientService mcpClientService;

    @GetMapping("/sessions")
    public List<ChatTreeNodeDto> getSessions(@RequestParam(required = false) Long userId) {
        return chatHistoryService.getTree(resolveUserId(userId));
    }

    @PostMapping("/sessions")
    public ChatTreeNodeDto createSession(@RequestParam(required = false) Long userId,
                                         @RequestBody ChatNodeCreateRequest request) {
        return chatHistoryService.createNode(request, resolveUserId(userId));
    }

    @PatchMapping("/sessions/{id}")
    public ChatTreeNodeDto renameSession(@PathVariable Long id,
                                         @RequestParam(required = false) Long userId,
                                         @RequestBody ChatNodeRenameRequest request) {
        return chatHistoryService.renameNode(id, request, resolveUserId(userId));
    }

    @DeleteMapping("/sessions/{id}")
    public void deleteSession(@PathVariable Long id, @RequestParam(required = false) Long userId) {
        chatHistoryService.deleteNode(id, resolveUserId(userId));
    }

    @GetMapping("/messages")
    public List<ChatMessageDto> getMessages(@RequestParam Long sessionId) {
        return chatHistoryService.getMessages(sessionId);
    }

    @PostMapping("/messages")
    public ChatMessageDto appendMessage(@RequestBody ChatMessageAppendRequest request) {
        return chatHistoryService.appendMessage(request);
    }

    @PostMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream(@RequestBody McpChatRequest request) {
        return mcpClientService.streamChat(request);
    }

    private Long resolveUserId(Long userIdParam) {
        if (userIdParam != null) {
            return userIdParam;
        }
        Object principal = org.springframework.security.core.context.SecurityContextHolder.getContext()
                .getAuthentication() != null ? org.springframework.security.core.context.SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal() : null;
        if (principal instanceof Long) {
            return (Long) principal;
        }
        return 1L; // fallback for testing
    }
}
