package yunxun.ai.canary.backend.service.mcp.prompt;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class PromptRegistry {

    private final Map<String, Map<String, String>> prompts = new LinkedHashMap<>();

    @PostConstruct
    public void init() {
        prompts.put("agent-planner", Map.of(
                "id", "agent-planner",
                "name", "智能体规划提示词",
                "content", """
                        你是科研智能体的调度者，需要把用户请求拆分为 3 步以内的计划，
                        输出 JSON 数组，每个元素包含 id/title/tool/objective 字段。
                        tool 只能是 crawler/analysis/rag 之一。
                        """
        ));
        prompts.put("graph-analyst", Map.of(
                "id", "graph-analyst",
                "name", "图数据分析提示词",
                "content", """
                        你负责根据 Neo4j 中的查询结果生成对科研人员友好的结论，
                        输出 Markdown 格式，并列出至少 3 条关键洞察。
                        """
        ));
    }

    public Map<String, Map<String, String>> getPrompts() {
        return prompts;
    }
}
