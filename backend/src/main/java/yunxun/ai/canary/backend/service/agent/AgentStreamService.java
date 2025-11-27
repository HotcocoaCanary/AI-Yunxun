package yunxun.ai.canary.backend.service.agent;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import yunxun.ai.canary.backend.model.dto.chart.ChartSpecDto;
import yunxun.ai.canary.backend.model.dto.chat.AgentQueryPayloadDto;
import yunxun.ai.canary.backend.model.dto.graph.GraphDataDto;
import yunxun.ai.canary.backend.service.chat.InMemoryChatService;
import yunxun.ai.canary.backend.service.graph.GraphMockService;

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
    private final GraphMockService graphMockService;

    public SseEmitter streamAnswer(AgentQueryPayloadDto payload) {
        SseEmitter emitter = new SseEmitter(0L);
        String sessionId = payload.getMemoryScope().getSessionId();
        chatService.appendMessage(sessionId, "user", payload.getQuestion());

        CompletableFuture.runAsync(() -> {
            try {
                // 1) 文本片段
                send(emitter, Map.of("type", "answer_chunk",
                        "content", "正在读取历史上下文与 Neo4j 子图..."));
                TimeUnit.MILLISECONDS.sleep(200);

                // 2) 图谱更新
                GraphDataDto graph = graphMockService.overview();
                send(emitter, Map.of("type", "graph_update", "graph", graph));
                TimeUnit.MILLISECONDS.sleep(200);

                // 3) 图表更新
                List<ChartSpecDto> charts = List.of(ChartSpecDto.builder()
                        .id("chart-1")
                        .title("最新抓取数据分布")
                        .type("bar")
                        .xField("date")
                        .yField("value")
                        .data(List.of(
                                Map.of("date", "2025-01-01", "value", 12),
                                Map.of("date", "2025-01-02", "value", 18),
                                Map.of("date", "2025-01-03", "value", 21)
                        ))
                        .build());
                send(emitter, Map.of("type", "chart_update", "charts", charts));

                // 4) 完整文本
                String finalText = """
                        已根据当前会话记忆和可用 MCP 工具生成答案：
                        - 数据已更新并写入 Mongo + Neo4j
                        - 返回了本次引用的子图与概要图表
                        """;
                send(emitter, Map.of("type", "answer_chunk", "content", finalText));
                var message = chatService.appendMessage(sessionId, "assistant", finalText);
                chatService.attachGraph(sessionId, message.getId(), graph);
                chatService.attachCharts(sessionId, message.getId(), charts);

                send(emitter, Map.of("type", "done"));
                emitter.complete();
            } catch (Exception ex) {
                try {
                    send(emitter, Map.of("type", "error", "message", ex.getMessage()));
                } catch (IOException ignored) {
                    // ignore secondary errors
                }
                emitter.completeWithError(ex);
            }
        });
        return emitter;
    }

    private void send(SseEmitter emitter, Object payload) throws IOException {
        emitter.send(SseEmitter.event()
                .data(payload)
                .id(UUID.randomUUID().toString())
                .name("message")
                .reconnectTime(1000)
                .comment("at " + Instant.now()));
    }
}
