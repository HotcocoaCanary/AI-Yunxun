package yunxun.ai.canary.backend.qa.service.agent;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Service;
import yunxun.ai.canary.backend.graph.service.Neo4jQueryService;
import yunxun.ai.canary.backend.mcp.service.prompt.PromptRegistry;
import yunxun.ai.canary.backend.qa.service.llm.LlmService;

/**
 * 简单的智能体服务：
 * - 先让大模型判断是否需要访问 Neo4j 工具
 * - 如需要则执行 Cypher 并将结果再交给大模型生成最终回答
 */
@Service
public class QaAgentService {

    private final LlmService llmService;
    private final Neo4jQueryService neo4jQueryService;
    private final ObjectMapper objectMapper;
    private final PromptRegistry promptRegistry;

    public QaAgentService(LlmService llmService,
                          Neo4jQueryService neo4jQueryService,
                          ObjectMapper objectMapper,
                          PromptRegistry promptRegistry) {
        this.llmService = llmService;
        this.neo4jQueryService = neo4jQueryService;
        this.objectMapper = objectMapper;
        this.promptRegistry = promptRegistry;
    }

    /**
     * 根据问题自动决定是否调用 Neo4j，再给出最终自然语言回答。
     */
    public String answerWithNeo4jIfNeeded(String question) {
        AgentDecision decision = decideAction(question);

        if ("answer".equalsIgnoreCase(decision.getAction())
                && decision.getAnswer() != null
                && !decision.getAnswer().isBlank()) {
            return decision.getAnswer();
        }

        if ("neo4j_query".equalsIgnoreCase(decision.getAction())
                && decision.getCypher() != null
                && !decision.getCypher().isBlank()) {
            String graphJson = neo4jQueryService.runQueryAsJson(decision.getCypher());

            // 使用 PromptRegistry 中定义的 qa-neo4j-answer 模板
            String template = promptRegistry.getContent("qa-neo4j-answer");
            String prompt = template
                    .replace("{{question}}", question)
                    .replace("{{graph_json}}", graphJson);

            return llmService.chat(prompt);
        }

        // 回退策略：如果决策结果不完整，就直接当作普通问答处理
        return llmService.chat(question);
    }

    /**
     * 让大模型作为“规划器”，决定是直接回答还是先查询 Neo4j。
     */
    private AgentDecision decideAction(String question) {
        // 使用 PromptRegistry 中的 qa-neo4j-planner 提示词
        String plannerBase = promptRegistry.getContent("qa-neo4j-planner");
        String plannerPrompt = plannerBase + "\n\n用户问题：\n" + question;

        String raw = llmService.chat(plannerPrompt);
        try {
            return objectMapper.readValue(raw, AgentDecision.class);
        } catch (JsonProcessingException e) {
            // 如果解析失败，简单回退为“直接回答”
            AgentDecision fallback = new AgentDecision();
            fallback.setAction("answer");
            fallback.setAnswer(raw);
            return fallback;
        }
    }

    /**
     * 大模型决策结构，用于解析 JSON。
     */
    @Setter
    @Getter
    public static class AgentDecision {
        private String action;
        private String answer;
        private String cypher;

    }
}
