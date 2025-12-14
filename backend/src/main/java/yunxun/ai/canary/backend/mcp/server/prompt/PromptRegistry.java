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

        // 网络搜索数据收集提示词
        prompts.put("web-search-data-collection", Map.of(
                "id", "web-search-data-collection",
                "name", "网络搜索数据收集提示词",
                "content", """
                        你需要从网络搜索中收集数据，用于生成图表。
                        
                        搜索策略：
                        1. 根据用户问题确定需要搜索的关键词和数据范围
                        2. 搜索相关的统计数据、报告、官方数据等
                        3. 提取关键数据点（时间、数值、分类等）
                        
                        数据格式要求：
                        将搜索到的数据整理为 JSON 数组格式，每个元素包含：
                        - 时间序列数据：{"year": "2015", "count": 150} 或 {"date": "2020-01", "value": 100}
                        - 分类数据：{"category": "类别名", "value": 数值} 或 {"name": "名称", "count": 数量}
                        
                        字段命名建议：
                        - X 轴（类别/时间）：year, date, time, name, category, label
                        - Y 轴（数值）：count, value, amount, quantity, number
                        
                        示例输出格式：
                        [
                          {"year": "2015", "count": 150},
                          {"year": "2016", "count": 170},
                          {"year": "2017", "count": 190}
                        ]
                        
                        注意事项：
                        - 确保数据准确可靠，优先使用官方或权威来源
                        - 数据点数量建议在 5-50 之间，过多会影响图表可读性
                        - 如果无法获取准确数据，如实说明，不要编造数据
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
