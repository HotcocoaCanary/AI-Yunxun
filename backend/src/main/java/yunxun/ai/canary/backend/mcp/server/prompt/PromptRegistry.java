package yunxun.ai.canary.backend.mcp.server.prompt;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 统一管理系统内用到的 Prompt 模板：
 * - 智能体规划提示词
 * - 图数据库问答提示词
 * - 通用问答系统提示词等
 * <p>
 * 后续也可以将这些 Prompt 暴露给 MCP Server 作为 prompt 能力的一部分。
 */
@Getter
@Service
public class PromptRegistry {

    /**
     * key: promptId
     * value: 包含 id / name / content 三个字段的 Map
     */
    private final Map<String, Map<String, String>> prompts = new LinkedHashMap<>();

    @PostConstruct
    public void init() {
        // 通用问答的系统提示词
        prompts.put("qa-system", Map.of(
                "id", "qa-system",
                "name", "通用问答系统提示词",
                "content", "你是智能数据分析助手，请提供简洁、安全、准确的回答。"
        ));

        // 决定是否需要访问 Neo4j 的规划提示词
        prompts.put("qa-neo4j-planner", Map.of(
                "id", "qa-neo4j-planner",
                "name", "Neo4j 查询规划提示词",
                "content", """
                        你可以访问一个 Neo4j 图数据库工具，它可以根据给定的 Cypher 查询返回 JSON 结果。
                        请根据用户问题判断是否需要查询图数据库。

                        如果不需要查询图数据库，请只输出严格的 JSON（不要加多余文本）：
                        {"action":"answer","answer":"这里是你的自然语言回答"}

                        如果需要查询图数据库，请只输出严格的 JSON（不要加多余文本）：
                        {"action":"neo4j_query","cypher":"这里是要执行的 Cypher 语句"}

                        注意：
                        - 只能选择一个 action（answer 或 neo4j_query）
                        - 必须是合法 JSON，键名使用双引号
                        """
        ));

        // 基于 Neo4j 查询结果生成最终回答的提示词
        prompts.put("qa-neo4j-answer", Map.of(
                "id", "qa-neo4j-answer",
                "name", "Neo4j 查询结果解读提示词",
                "content", """
                        用户问题：
                        {{question}}

                        下面是从 Neo4j 图数据库中查询到的 JSON 数据：
                        {{graph_json}}

                        请基于这些图数据，用清晰的中文回答用户问题。
                        要求：
                        - 不要复述原始 JSON
                        - 尽量给出结构化、易读的结论
                        - 如有必要，可以简要说明关系结构（例如谁认识谁、谁购买了什么）
                        """
        ));
    }

    public Map<String, String> getPrompt(String id) {
        return prompts.get(id);
    }

    public String getContent(String id) {
        Map<String, String> prompt = prompts.get(id);
        return prompt != null ? prompt.getOrDefault("content", "") : "";
    }
}
