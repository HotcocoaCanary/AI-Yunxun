package yunxun.ai.canary.project.service.chat;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import yunxun.ai.canary.project.service.llm.LlmClient;
import yunxun.ai.canary.project.service.llm.LlmMessage;

import java.util.List;
import java.util.Map;

@Service
public class ToolPlanner {

    private final LlmClient llmClient;
    private final ObjectMapper objectMapper;

    public ToolPlanner(LlmClient llmClient, ObjectMapper objectMapper) {
        this.llmClient = llmClient;
        this.objectMapper = objectMapper;
    }

    public Mono<PlannerDecision> decideNext(String userMessage, ToolContext context) {
        String prompt = buildPlannerPrompt(userMessage, context);
        List<LlmMessage> messages = List.of(
                new LlmMessage("system", systemPrompt()),
                new LlmMessage("user", prompt)
        );

        return llmClient.complete(messages)
                .subscribeOn(Schedulers.boundedElastic())
                .map(this::parseDecision)
                .onErrorReturn(new PlannerDecision("final", null, Map.of()));
    }

    private String buildPlannerPrompt(String userMessage, ToolContext context) {
        StringBuilder sb = new StringBuilder();
        sb.append("用户问题：\n").append(userMessage == null ? "" : userMessage).append("\n\n");
        sb.append("上下文（已执行工具）：\n");
        if (context.traces().isEmpty()) {
            sb.append("- 无\n");
        }
        else {
            for (ToolTrace trace : context.traces()) {
                sb.append("- ").append(trace.name()).append(" (ok=").append(trace.result() != null && trace.result().ok()).append(")\n");
            }
        }
        sb.append("\n可用工具：\n").append(toolList()).append("\n");
        sb.append("\n请输出 JSON：{action: tool|final, name?, args?}（不要输出其它内容）");
        return sb.toString();
    }

    private PlannerDecision parseDecision(String text) {
        if (text == null) {
            return new PlannerDecision("final", null, Map.of());
        }
        String json = extractJsonObject(text);
        try {
            JsonNode node = objectMapper.readTree(json);
            String action = node.path("action").asText("final");
            String name = node.path("name").isMissingNode() ? null : node.path("name").asText(null);
            Map<String, Object> args = node.has("args")
                    ? objectMapper.convertValue(node.get("args"), new TypeReference<Map<String, Object>>() {})
                    : Map.of();
            return new PlannerDecision(action, name, args);
        }
        catch (Exception ignored) {
            return new PlannerDecision("final", null, Map.of());
        }
    }

    private static String extractJsonObject(String text) {
        int start = text.indexOf('{');
        int end = text.lastIndexOf('}');
        if (start >= 0 && end > start) {
            return text.substring(start, end + 1);
        }
        return "{\"action\":\"final\"}";
    }

    private static String systemPrompt() {
        return """
                你是一个“工具调度器”，负责决定是否需要调用工具来帮助回答用户问题。
                规则：
                - 你只能输出 JSON，不能输出任何额外文字。
                - 如果需要调用工具：输出 {\"action\":\"tool\",\"name\":\"工具名\",\"args\":{...}}
                - 如果不需要调用工具：输出 {\"action\":\"final\"}
                """;
    }

    private static String toolList() {
        return """
                - mongo_save_document(topic, content, tags?, source?)
                - mongo_find_by_topic(topic, limit?)
                - mongo_find_by_id(id)
                - mongo_update_document(id, patch)
                - mongo_delete_document(id)
                - web_search(query, maxResults?, language?, recencyDays?)
                - echart_generate(chartType, title?, data?, mapping?, options?, graph?)
                - neo4j_create_node(label, properties)
                - neo4j_find_node(id? | label+property+value)
                - neo4j_update_node(id, patch)
                - neo4j_delete_node(id, detach?)
                - neo4j_create_relationship(fromId, toId, type, properties?)
                - neo4j_find_relationship(id)
                - neo4j_update_relationship(id, patch)
                - neo4j_delete_relationship(id)
                - neo4j_find_path(fromId, toId, maxDepth?, types?)
                - neo4j_find_neighbors(id, depth?, types?)
                - neo4j_fuzzy_search(query, labels?, limit?)
                """;
    }
}
