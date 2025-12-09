package yunxun.ai.canary.backend.mcp.client.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

/**
 * High-level chat service that delegates to Spring AI's {@link ChatClient}.
 * <p>
 * Responsibilities:
 * <ul>
 *     <li>Drive MCP tool calls via the model (Neo4j graph, chart service, etc.).</li>
 *     <li>Extract structured results (graph JSON / chart JSON) for the frontend.</li>
 *     <li>Ask the model to write a natural-language answer based on these results.</li>
 * </ul>
 */
@Service
public class McpChatService {

    /**
     * Result returned to the REST layer.
     *
     * @param replyText natural-language answer for the user
     * @param graphJson optional graph JSON (nodes/edges) for GraphPanel
     * @param chartJson optional chart JSON (ChartResponse) for ChartPanel
     */
    public record ChatResult(String replyText, String graphJson, String chartJson) {
    }

    private final ChatClient chatClient;

    public McpChatService(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    /**
     * Execute a single-turn chat call using the underlying model and tools.
     * <p>
     * Phase 1: let the model call MCP tools and output only structured JSON markers:
     * <ul>
     *     <li>GRAPH_JSON: {"nodes":[...], "edges":[...]}</li>
     *     <li>CHART_JSON: { ChartResponse ... }</li>
     * </ul>
     * Phase 2: feed the extracted JSON back to the model so it can answer
     * in natural language, without exposing raw JSON to the user.
     */
    public ChatResult chat(String message) {
        if (message == null || message.isBlank()) {
            throw new IllegalArgumentException("message must not be blank");
        }

        // ===== Phase 1: tools + JSON only =====
        String toolsRaw = chatClient.prompt()
                .system("""
                        你是“云寻”知识助手，当前阶段的任务是：
                        - 根据用户问题，调用可用的 MCP 工具（特别是 Neo4j 图谱工具和图表服务工具）；
                        - 最终【只输出结构化 JSON 标记】，不要输出任何自然语言解释。

                        约定的输出标记：
                        1) 若问题涉及实体/关系图谱（例如“Bob 认识谁？”），
                           请查询 Neo4j 图谱并输出一行：
                           GRAPH_JSON: {"nodes":[...], "edges":[...]}
                           其中：
                             - nodes: [{ "id": string, "label": string, "type": string }]
                             - edges: [{ "id": string, "source": string, "target": string, "label": string }]

                        2) 若问题涉及数值分析或趋势图（例如“近 10 年考研人数变化趋势，画一个柱状图”），
                           请调用图表相关 MCP 工具（如 generate_chart），
                           并输出一行：
                           CHART_JSON: { ...ChartResponse JSON... }

                        3) 若一个问题同时需要图谱和图表，你可以输出最多两行：
                           - 一行以 GRAPH_JSON: 开头
                           - 一行以 CHART_JSON: 开头

                        非常重要：
                        - 不要输出任何自然语言解释；
                        - 不要输出除 GRAPH_JSON/CHART_JSON 以外的内容；
                        - 每个标记占一行，后面紧跟合法的 JSON。
                        """)
                .user(message)
                .call()
                .content();

        String graphJson = extractMarkerJson(toolsRaw, "GRAPH_JSON:");
        String chartJson = extractMarkerJson(toolsRaw, "CHART_JSON:");

        // ===== Phase 2: natural-language answer based on tool results =====
        String replyText;
        if (graphJson != null || chartJson != null) {
            StringBuilder userPrompt = new StringBuilder();
            userPrompt.append("用户原始问题: ").append(message).append("\n");
            if (graphJson != null) {
                userPrompt.append("图谱 JSON: ").append(graphJson).append("\n");
            }
            if (chartJson != null) {
                userPrompt.append("图表 JSON: ").append(chartJson).append("\n");
            }

            replyText = chatClient.prompt()
                    .system("""
                            你是“云寻”知识助手。
                            你已经通过工具获得了结构化结果（图谱 JSON、图表 JSON），
                            现在需要基于这些结果用自然语言回答用户问题。

                            要求：
                            - 用清晰、简洁的中文回答；
                            - 如果有图谱 JSON，可以简要说明有哪些实体及其关系；
                            - 如果有图表 JSON，可以结合其中的数据描述整体趋势和结论；
                            - 不要输出任何 JSON 文本；
                            - 不要输出 GRAPH_JSON 或 CHART_JSON 之类的标记。
                            """)
                    .user(userPrompt.toString())
                    .call()
                    .content();
        } else {
            // Fallback: no structured output, just do a normal QA call
            replyText = chatClient.prompt()
                    .system("""
                            你是“云寻”知识助手，能够根据需要调用可用的 MCP 工具
                            （例如 MongoDB、Neo4j 图谱、图表服务等）来完成任务。
                            在需要读取或写入数据、生成图表时优先通过工具，而不是凭空编造。
                            回答时用自然语言回答用户问题，不要输出任何 GRAPH_JSON 或 CHART_JSON 标记。
                            """)
                    .user(message)
                    .call()
                    .content();
        }

        return new ChatResult(replyText, graphJson, chartJson);
    }

    /**
     * Extract the JSON payload that follows a given marker in the model output.
     *
     * @param raw    full model output
     * @param marker marker prefix such as "GRAPH_JSON:" or "CHART_JSON:"
     * @return trimmed JSON string, or {@code null} if not found
     */
    private String extractMarkerJson(String raw, String marker) {
        if (raw == null || raw.isEmpty()) {
            return null;
        }
        int idx = raw.indexOf(marker);
        if (idx < 0) {
            return null;
        }
        String after = raw.substring(idx + marker.length()).trim();
        return after.isEmpty() ? null : after;
    }
}

