package yunxun.ai.canary.backend.mcp.client.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

/**
 * High-level chat service that delegates to Spring AI's {@link ChatClient}.
 * <p>
 * The ChatClient is configured to use the local Ollama model and can
 * transparently call any tools that are registered via Spring AI / MCP.
 */
@Service
public class McpChatService {

    public record ChatResult(String replyText, String graphJson) {
    }

    private final ChatClient chatClient;

    public McpChatService(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    /**
     * Execute a single-turn chat call using the underlying model and tools.
     * <p>
     * 为了在前端展示图谱，这里约定：
     * 如果大模型在回答中需要返回图谱数据，请在回答中包含一段形如
     * GRAPH_JSON: { ... }
     * 的 JSON（单行或紧跟在标记后面），服务会尝试解析这一段并单独返回给前端。
     */
    public ChatResult chat(String message) {
        if (message == null || message.isBlank()) {
            throw new IllegalArgumentException("message must not be blank");
        }

        // ========= 第一步：让模型 + MCP 只返回 GRAPH_JSON =========
        String graphRaw = chatClient.prompt()
                .system("""
                        你是“云寻”知识助手，当前阶段的任务是：
                        1) 根据用户问题，调用可用的 MCP 工具（特别是 Neo4j 图谱相关工具）查询到相关图谱数据。
                        2) 最终【只输出一行】以 `GRAPH_JSON:` 开头的 JSON，格式示例：
                           GRAPH_JSON: {"nodes":[...], "edges":[...]}
                           其中：
                           - nodes: [{ "id": string, "label": string, "type": string }]
                           - edges: [{ "id": string, "source": string, "target": string, "label": string }]

                        不要输出任何自然语言解释，不要多行说明，只返回这一行 GRAPH_JSON: ...。
                        """)
                .user(message)
                .call()
                .content();

        String graphJson = null;
        int marker = graphRaw.lastIndexOf("GRAPH_JSON:");
        if (marker >= 0) {
            String after = graphRaw.substring(marker + "GRAPH_JSON:".length()).trim();
            if (!after.isEmpty()) {
                graphJson = after;
            }
        }

        // ========= 第二步：将图谱 JSON + 原问题交给模型，让它用自然语言回答 =========
        String replyText;
        if (graphJson != null) {
            replyText = chatClient.prompt()
                    .system("""
                            你是“云寻”知识助手。
                            现在你已经通过工具得到了一个图谱 JSON（nodes/edges），请基于它用自然语言回答用户问题。
                            要求：
                            - 用清晰的中文直接回答问题；
                            - 可以简要说明是谁与谁存在什么关系；
                            - 不要输出任何 JSON 或 GRAPH_JSON 标记。
                            """)
                    .user("""
                            用户原始问题: %s
                            图谱 JSON: %s
                            """.formatted(message, graphJson))
                    .call()
                    .content();
        } else {
            // 兜底：如果第一阶段没有拿到 GRAPH_JSON，就走一次普通的问答逻辑
            replyText = chatClient.prompt()
                    .system("""
                            你是“云寻”知识助手，能够根据需要调用可用的 MCP 工具
                            （例如 MongoDB、Neo4j 图谱、图表服务等）来完成任务。
                            在需要读取或写入数据、生成图表时优先通过工具，而不是凭空编造。
                            回答时用自然语言回答用户问题，不要输出任何 GRAPH_JSON 标记。
                            """)
                    .user(message)
                    .call()
                    .content();
        }

        return new ChatResult(replyText, graphJson);
    }
}
