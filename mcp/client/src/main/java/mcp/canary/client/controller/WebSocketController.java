package mcp.canary.client.controller;

import mcp.canary.client.model.ToolLogEvent;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

/**
 * WebSocket 推送：监听 {@link ToolLogEvent} 并广播到前端订阅者。
 */
@Controller
public class WebSocketController {

    private final SimpMessagingTemplate messagingTemplate;

    public WebSocketController(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @EventListener
    public void onToolLogEvent(ToolLogEvent event) {
        messagingTemplate.convertAndSend("/topic/tool-logs", event);
    }
}



