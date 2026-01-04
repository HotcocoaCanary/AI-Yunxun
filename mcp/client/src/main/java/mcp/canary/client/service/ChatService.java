package mcp.canary.client.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import mcp.canary.client.dto.ChatResponse;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Optional;

/**
 * 对话服务：集成智谱 AI，并通过 MCP ToolCallbacks 支持工具调用。
 */
@Service
public class ChatService {

    private final ChatClient chatClient;
    private final MCPClientService mcpClientService;
    private final ObjectMapper objectMapper;

    public ChatService(ChatClient chatClient, MCPClientService mcpClientService, ObjectMapper objectMapper) {
        this.chatClient = chatClient;
        this.mcpClientService = mcpClientService;
        this.objectMapper = objectMapper;
    }

    public Flux<ChatResponse> streamChat(String message) {
        ToolCallback[] toolCallbacks = mcpClientService.getToolCallbacks();

        StringBuilder full = new StringBuilder();

        Flux<ChatResponse> streaming = chatClient
                .prompt()
                .user(message)
                .toolCallbacks(toolCallbacks)
                .stream()
                .content()
                .doOnNext(full::append)
                .map(chunk -> new ChatResponse("text", chunk));

        Mono<ChatResponse> chartEvent = Mono.fromSupplier(() -> tryExtractEchartsOption(full.toString()))
                .flatMap(opt -> opt.map(json -> Mono.just(new ChatResponse("chart", json))).orElseGet(Mono::empty));

        return Flux.concat(
                Flux.just(new ChatResponse("status", "thinking")),
                streaming,
                chartEvent,
                Flux.just(new ChatResponse("status", "done"))
        );
    }

    private Optional<String> tryExtractEchartsOption(String content) {
        if (content == null) {
            return Optional.empty();
        }
        String trimmed = content.trim();
        if (!(trimmed.startsWith("{") && trimmed.endsWith("}"))) {
            return Optional.empty();
        }
        try {
            JsonNode node = objectMapper.readTree(trimmed);
            // 轻量判定：ECharts option 通常至少包含 series
            if (node.has("series")) {
                return Optional.of(objectMapper.writeValueAsString(node));
            }
            return Optional.empty();
        } catch (Exception e) {
            return Optional.empty();
        }
    }
}





