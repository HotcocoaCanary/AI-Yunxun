package yunxun.ai.canary.project.service.chat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import yunxun.ai.canary.project.app.dto.ChatError;
import yunxun.ai.canary.project.app.dto.ChatRequest;
import yunxun.ai.canary.project.app.dto.ChatResponse;
import yunxun.ai.canary.project.app.dto.ChatStreamEvent;
import yunxun.ai.canary.project.service.llm.LlmClient;
import yunxun.ai.canary.project.service.llm.LlmMessage;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class ChatOrchestrator {

    private static final int MAX_TOOL_STEPS = 3;

    private final LlmClient llmClient;
    private final ToolPlanner toolPlanner;
    private final ToolExecutor toolExecutor;
    private final ObjectMapper objectMapper;

    public ChatOrchestrator(LlmClient llmClient, ToolPlanner toolPlanner, ToolExecutor toolExecutor, ObjectMapper objectMapper) {
        this.llmClient = llmClient;
        this.toolPlanner = toolPlanner;
        this.toolExecutor = toolExecutor;
        this.objectMapper = objectMapper;
    }

    public Mono<ChatResponse> chat(ChatRequest request) {
        String traceId = UUID.randomUUID().toString();
        String sessionId = request.sessionId() == null || request.sessionId().isBlank()
                ? UUID.randomUUID().toString()
                : request.sessionId();
        String userMessage = request.message() == null ? "" : request.message();

        return planAndExecuteTools(userMessage)
                .onErrorReturn(ToolContext.empty())
                .flatMap(ctx -> llmClient.complete(answerMessages(userMessage, ctx))
                        .subscribeOn(Schedulers.boundedElastic())
                        .map(answer -> new ChatResponse(traceId, sessionId, answer, ctx.sources(), ctx.charts())))
                .onErrorResume(ex -> Mono.just(new ChatResponse(
                        traceId,
                        sessionId,
                        "处理失败: " + ex.getMessage(),
                        List.of(),
                        List.of()
                )));
    }

    public Flux<ServerSentEvent<ChatStreamEvent>> chatStream(ChatRequest request) {
        String traceId = UUID.randomUUID().toString();
        String sessionId = request.sessionId() == null || request.sessionId().isBlank()
                ? UUID.randomUUID().toString()
                : request.sessionId();
        String userMessage = request.message() == null ? "" : request.message();

        Mono<ToolContext> contextMono = planAndExecuteTools(userMessage)
                .onErrorReturn(ToolContext.empty())
                .cache();

        Flux<ServerSentEvent<ChatStreamEvent>> toolEvents = contextMono.flatMapMany(ctx ->
                Flux.fromIterable(ctx.traces())
                        .concatMap(trace -> Flux.just(
                                sse(traceId, "tool_call", Map.of("name", trace.name(), "args", trace.args())),
                                sse(traceId, "tool_result", Map.of("name", trace.name(), "result", trace.result()))
                        )));

        Flux<ServerSentEvent<ChatStreamEvent>> tokenEvents = contextMono.flatMapMany(ctx -> {
            StringBuilder answerBuilder = new StringBuilder();
            return llmClient.stream(answerMessages(userMessage, ctx))
                    .subscribeOn(Schedulers.boundedElastic())
                    .map(token -> {
                        answerBuilder.append(token);
                        return sse(traceId, "token", token);
                    })
                    .concatWith(Mono.fromSupplier(() -> {
                        ChatResponse finalResp = new ChatResponse(traceId, sessionId, answerBuilder.toString(), ctx.sources(), ctx.charts());
                        return sse(traceId, "final", finalResp);
                    }))
                    .onErrorResume(ex -> Flux.just(sse(traceId, "error", new ChatError("LLM_ERROR", ex.getMessage()))));
        });

        return Flux.concat(toolEvents, tokenEvents);
    }

    private static ServerSentEvent<ChatStreamEvent> sse(String traceId, String type, Object data) {
        return ServerSentEvent.<ChatStreamEvent>builder()
                .event(type)
                .data(new ChatStreamEvent(traceId, type, data))
                .build();
    }

    private Mono<ToolContext> planAndExecuteTools(String userMessage) {
        return planAndExecuteTools(userMessage, ToolContext.empty(), 0);
    }

    private Mono<ToolContext> planAndExecuteTools(String userMessage, ToolContext ctx, int step) {
        if (step >= MAX_TOOL_STEPS) {
            return Mono.just(ctx);
        }
        if (userMessage == null || userMessage.isBlank()) {
            return Mono.just(ctx);
        }

        return toolPlanner.decideNext(userMessage, ctx)
                .flatMap(decision -> {
                    if (decision.isFinal()) {
                        return Mono.just(ctx);
                    }
                    return Mono.fromCallable(() -> toolExecutor.execute(decision.name(), decision.args()))
                            .subscribeOn(Schedulers.boundedElastic())
                            .map(result -> {
                                ToolTrace trace = new ToolTrace(decision.name(), decision.args(), result);
                                ToolContext next = applyToolEffects(ctx.withTrace(trace), trace);
                                return next;
                            })
                            .flatMap(next -> planAndExecuteTools(userMessage, next, step + 1));
                });
    }

    private ToolContext applyToolEffects(ToolContext ctx, ToolTrace trace) {
        if (trace == null || trace.result() == null || !trace.result().ok()) {
            return ctx;
        }

        Object data = trace.result().data();
        if (data == null) {
            return ctx;
        }

        try {
            return switch (trace.name()) {
                case "mongo_save_document" -> applyMongoSave(ctx, data);
                case "mongo_find_by_topic" -> applyMongoFindList(ctx, data);
                case "mongo_find_by_id" -> applyMongoFindOne(ctx, data);
                case "web_search" -> applyWebSearch(ctx, data);
                case "echart_generate" -> applyEChart(ctx, data);
                default -> ctx;
            };
        }
        catch (Exception ignored) {
            return ctx;
        }
    }

    @SuppressWarnings("unchecked")
    private ToolContext applyMongoSave(ToolContext ctx, Object data) {
        if (data instanceof Map<?, ?> map) {
            Object id = map.get("id");
            if (id != null) {
                return ctx.withSource(new yunxun.ai.canary.project.app.dto.ChatSource("mongo", null, null, id.toString()));
            }
        }
        return ctx;
    }

    private ToolContext applyMongoFindList(ToolContext ctx, Object data) {
        if (!(data instanceof Map<?, ?> map)) {
            return ctx;
        }
        Object items = map.get("items");
        if (!(items instanceof List<?> list)) {
            return ctx;
        }
        ToolContext next = ctx;
        for (Object item : list) {
            if (item instanceof yunxun.ai.canary.project.repository.mongo.model.MongoDocument doc) {
                if (doc.getId() != null) {
                    next = next.withSource(new yunxun.ai.canary.project.app.dto.ChatSource("mongo", null, null, doc.getId()));
                }
            }
        }
        return next;
    }

    private ToolContext applyMongoFindOne(ToolContext ctx, Object data) {
        if (data instanceof yunxun.ai.canary.project.repository.mongo.model.MongoDocument doc) {
            if (doc.getId() != null) {
                return ctx.withSource(new yunxun.ai.canary.project.app.dto.ChatSource("mongo", null, null, doc.getId()));
            }
        }
        return ctx;
    }

    @SuppressWarnings("unchecked")
    private ToolContext applyWebSearch(ToolContext ctx, Object data) {
        Object items = null;
        if (data instanceof Map<?, ?> map) {
            items = map.get("items");
        }
        else if (data instanceof List<?> list) {
            items = list;
        }
        if (!(items instanceof List<?> list)) {
            return ctx;
        }

        ToolContext next = ctx;
        for (Object item : list) {
            if (item instanceof Map<?, ?> m) {
                Object url = m.get("url");
                Object title = m.get("title");
                if (url != null) {
                    next = next.withSource(new yunxun.ai.canary.project.app.dto.ChatSource("web", title == null ? null : title.toString(), url.toString(), null));
                }
            }
        }
        return next;
    }

    @SuppressWarnings("unchecked")
    private ToolContext applyEChart(ToolContext ctx, Object data) {
        if (data instanceof Map<?, ?> map) {
            Object option = map.get("option");
            if (option != null) {
                return ctx.withChart(new yunxun.ai.canary.project.app.dto.ChatChart("echarts", objectMapper.valueToTree(option)));
            }
        }
        return ctx;
    }

    private List<LlmMessage> answerMessages(String userMessage, ToolContext ctx) {
        String toolResultsJson = "";
        if (!ctx.traces().isEmpty()) {
            try {
                toolResultsJson = objectMapper.writeValueAsString(ctx.traces());
            }
            catch (JsonProcessingException ignored) {
                toolResultsJson = "";
            }
        }
        return List.of(
                new LlmMessage("system", answerSystemPrompt(toolResultsJson)),
                new LlmMessage("user", userMessage == null ? "" : userMessage)
        );
    }

    private static String answerSystemPrompt(String toolResultsJson) {
        StringBuilder sb = new StringBuilder();
        sb.append("你是 AI-Yunxun 智能问答助手，请用中文回答用户问题。\n");
        sb.append("- 回答尽量简洁清晰，可使用 Markdown。\n");
        sb.append("- 如果问题需要澄清，请先提出 1~2 个关键澄清问题。\n");
        if (toolResultsJson != null && !toolResultsJson.isBlank()) {
            sb.append("- 以下是已执行工具的结果（JSON），请结合这些信息回答：\n");
            sb.append(toolResultsJson).append("\n");
            sb.append("- 若回答引用了搜索结果，请在末尾列出来源链接。\n");
        }
        return sb.toString();
    }
}
