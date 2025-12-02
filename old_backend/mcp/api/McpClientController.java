package yunxun.ai.canary.backend.mcp.api;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import yunxun.ai.canary.backend.mcp.model.dto.McpChatRequest;
import yunxun.ai.canary.backend.mcp.service.server.McpClientService;

@RestController
@RequestMapping("/api/mcp")
@RequiredArgsConstructor
public class McpClientController {

    private final McpClientService mcpClientService;

    @PostMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream(@RequestBody McpChatRequest request) {
        return mcpClientService.streamChat(request);
    }
}
