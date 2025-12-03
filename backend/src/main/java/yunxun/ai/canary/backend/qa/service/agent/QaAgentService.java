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
 * ��������񣺸������ʴ������Э����ģ�ͺ� Neo4j ���ߡ�
 */
@SuppressWarnings("LossyEncoding")
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
     * ��ǰ�汾��Ϊ�˼����ӳ٣��߼���Ϊ��
     * - ֱ�Ӳ�ѯһ����ͼ����
     * - ��ͼ���ݺ��û�����һ�𽻸���ģ�ͣ��ô�ģ���ۺϻش�
     * ����ÿ��ֻ����һ�δ�ģ�ͣ�������ֵ��õ��³�ʱ��
     */
    public String answerWithNeo4jIfNeeded(String question) {
        // �򻯰棺ʼ�ղ�ѯһС����ͼ���ݡ��������Ը����������Ϊ����ϸ�� Cypher��
        String defaultCypher = "MATCH p = (a)-[r]->(m) RETURN p LIMIT 20";
        String graphJson = neo4jQueryService.runQueryAsJson(defaultCypher);

        String template = promptRegistry.getContent("qa-neo4j-answer");
        String prompt = template
                .replace("{{question}}", question)
                .replace("{{graph_json}}", graphJson);

        return llmService.chat(prompt);
    }

    /**
     * δ������ָ����滮-ִ�С����׶νṹ�����Ի���������߽ṹ��չ��
     */
    private AgentDecision decideAction(String question) {
        String plannerBase = promptRegistry.getContent("qa-neo4j-planner");
        String plannerPrompt = plannerBase + "\n\n�û����⣺\n" + question;

        String raw = llmService.chat(plannerPrompt);
        try {
            return objectMapper.readValue(raw, AgentDecision.class);
        } catch (JsonProcessingException e) {
            AgentDecision fallback = new AgentDecision();
            fallback.setAction("answer");
            fallback.setAnswer(raw);
            return fallback;
        }
    }

    @Setter
    @Getter
    public static class AgentDecision {
        private String action;
        private String answer;
        private String cypher;
    }
}
