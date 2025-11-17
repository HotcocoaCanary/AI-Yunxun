package yunxun.ai.canary.backend.service.agent;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.ai.document.Document;
import yunxun.ai.canary.backend.algo.llm.LlmService;
import yunxun.ai.canary.backend.algo.rag.RagPipelineService;
import yunxun.ai.canary.backend.model.dto.agent.*;
import yunxun.ai.canary.backend.model.dto.crawler.CrawlResult;
import yunxun.ai.canary.backend.model.dto.crawler.CrawlTaskRequest;
import yunxun.ai.canary.backend.model.dto.graph.GraphChartRequest;
import yunxun.ai.canary.backend.model.dto.graph.GraphIngestionRequest;
import yunxun.ai.canary.backend.model.entity.agent.AgentConversation;
import yunxun.ai.canary.backend.model.entity.document.PaperDocument;
import yunxun.ai.canary.backend.repository.mongo.AgentConversationRepository;
import yunxun.ai.canary.backend.service.analysis.DataAnalysisService;
import yunxun.ai.canary.backend.service.crawler.PaperStorageService;
import yunxun.ai.canary.backend.service.crawler.WebCrawlerService;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AgentOrchestratorService {

            private static final String DEFAULT_PLAN_PROMPT = """
            You are a research agent. Create an action plan with at most 3 steps in JSON array form with fields id/title/tool/objective.
            {
              "id": "step-1",
              "title": "Describe the action",
              "tool": "crawler|analysis|rag",
              "objective": "Goal of this step"
            }
            """;



    private static final List<String> DEFAULT_TOOLS = List.of("crawler", "analysis", "rag");
    private static final int MAX_HISTORY_ENTRIES = 40;

    private final WebCrawlerService webCrawlerService;
    private final PaperStorageService paperStorageService;
    private final DataAnalysisService dataAnalysisService;
    private final LlmService llmService;
    private final RagPipelineService ragPipelineService;
    private final AgentConversationRepository agentConversationRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public AgentChatResponse handleChat(AgentChatRequest request) {
        AgentConversation conversation = loadConversation(request.getConversationId());
        List<String> activeTools = resolveEnabledTools(request, conversation);

        List<AgentPlanStep> plan = buildPlan(request);
        List<String> usedTools = new ArrayList<>();
        List<PaperDocument> documents = new ArrayList<>();
        List<AgentChartPayload> chartPayloads = new ArrayList<>();
        AgentGraphPayload graphPayload = null;

        List<AgentMessage> conversationHistory = new ArrayList<>(Optional.ofNullable(conversation.getHistory()).orElseGet(ArrayList::new));
        conversationHistory.add(new AgentMessage("user", request.getMessage(), Instant.now()));

        for (AgentPlanStep step : plan) {
            if (!activeTools.contains(step.getTool())) {
                step.setStatus("skipped");
                continue;
            }
            switch (step.getTool()) {
                case "crawler" -> {
                    usedTools.add("crawler");
                    documents.addAll(executeCrawlerStep(request, step));
                    step.setStatus("completed");
                }
                case "analysis" -> {
                    usedTools.add("analysis");
                    chartPayloads.addAll(executeAnalysisStep(step));
                    graphPayload = dataAnalysisService.buildGraphView("MATCH (start)-[rel]->(end) RETURN start,rel,end LIMIT 50");
                    step.setStatus("completed");
                }
                case "rag" -> {
                    usedTools.add("rag");
                    step.setStatus("completed");
                }
                default -> step.setStatus("skipped");
            }
        }

        List<Document> ragDocuments = ragPipelineService.similaritySearch(request.getMessage(), 4, 0.65d);
        String context = buildContext(documents, chartPayloads, ragDocuments);
        String answer = llmService.answerQuestion(
                request.getMessage(),
                context,
                request.getPreference() != null ? request.getPreference().getAnswerStyle() : null,
                0.2d
        );

        conversationHistory.add(new AgentMessage("assistant", answer, Instant.now()));
        conversation.setHistory(trimHistory(conversationHistory));
        conversation.setEnabledTools(activeTools);
        if (!StringUtils.hasText(conversation.getTitle())) {
            conversation.setTitle(request.getMessage().length() > 30
                    ? request.getMessage().substring(0, 30) + "..."
                    : request.getMessage());
        }
        agentConversationRepository.save(conversation);

        List<AgentDocumentSnippet> snippets = documents.stream()
                .map(doc -> AgentDocumentSnippet.builder()
                        .documentId(doc.getId())
                        .title(doc.getTitle())
                        .summary(doc.getSummary())
                        .source(doc.getSource())
                        .url(doc.getUrl())
                        .build())
                .collect(Collectors.toList());

        return AgentChatResponse.builder()
                .conversationId(conversation.getId())
                .answer(answer)
                .plan(plan)
                .charts(chartPayloads)
                .graph(graphPayload)
                .documents(snippets)
                .usedTools(usedTools)
                .build();
    }

    private AgentConversation loadConversation(String conversationId) {
        if (StringUtils.hasText(conversationId)) {
            return agentConversationRepository.findById(conversationId)
                    .orElseGet(() -> AgentConversation.builder()
                            .id(conversationId)
                            .history(new ArrayList<>())
                            .enabledTools(new ArrayList<>(DEFAULT_TOOLS))
                            .build());
        }
        return AgentConversation.builder()
                .id(UUID.randomUUID().toString())
                .history(new ArrayList<>())
                .enabledTools(new ArrayList<>(DEFAULT_TOOLS))
                .build();
    }

    private List<String> resolveEnabledTools(AgentChatRequest request, AgentConversation conversation) {
        if (!CollectionUtils.isEmpty(request.getEnabledTools())) {
            return new ArrayList<>(request.getEnabledTools());
        }
        if (!CollectionUtils.isEmpty(conversation.getEnabledTools())) {
            return new ArrayList<>(conversation.getEnabledTools());
        }
        return new ArrayList<>(DEFAULT_TOOLS);
    }

    private List<AgentMessage> trimHistory(List<AgentMessage> history) {
        if (history.size() <= MAX_HISTORY_ENTRIES) {
            return history;
        }
        return new ArrayList<>(history.subList(history.size() - MAX_HISTORY_ENTRIES, history.size()));
    }

    private List<PaperDocument> executeCrawlerStep(AgentChatRequest request, AgentPlanStep step) {
        List<PaperDocument> documents = new ArrayList<>();
        CrawlTaskRequest taskRequest = CrawlTaskRequest.builder()
                .query(Optional.ofNullable(request.getMessage()).orElse("ai"))
                .source("arxiv")
                .maxResults(5)
                .build();
        List<CrawlResult> results = webCrawlerService.crawlArxiv(taskRequest.getQuery(), taskRequest.getMaxResults());
        documents.addAll(paperStorageService.saveResults(results));
        if (!CollectionUtils.isEmpty(taskRequest.getUrls())) {
            for (String url : taskRequest.getUrls()) {
                webCrawlerService.crawlGenericUrl(url).ifPresent(result -> documents.add(paperStorageService.saveResult(result)));
            }
        }
        return documents;
    }

    private List<AgentChartPayload> executeAnalysisStep(AgentPlanStep step) {
        GraphIngestionRequest ingestionRequest = GraphIngestionRequest.builder().build();
        dataAnalysisService.ingest(ingestionRequest);
        GraphChartRequest chartRequest = GraphChartRequest.builder()
                .chartType("bar")
                .title("Paper distribution by year")
                .cypher("MATCH (p:Paper)-[:PUBLISHED_IN]->(y:Year) RETURN y.name as label, COUNT(p) as count ORDER BY count DESC LIMIT 10")
                .xField("label")
                .yField("count")
                .seriesField("count")
                .build();
        return List.of(dataAnalysisService.generateChart(chartRequest));
    }

    private String buildContext(List<PaperDocument> documents, List<AgentChartPayload> charts, List<Document> ragDocs) {
        StringBuilder context = new StringBuilder();
        if (!CollectionUtils.isEmpty(documents)) {
            context.append("Latest papers:\n");
            for (PaperDocument doc : documents) {
                context.append("- ").append(doc.getTitle()).append(" (").append(doc.getSource()).append(")\n");
                if (doc.getSummary() != null) {
                    context.append("  Summary: ").append(doc.getSummary()).append("\n");
                }
            }
        }
        if (!CollectionUtils.isEmpty(charts)) {
            context.append("\nChart insights:\n");
            for (AgentChartPayload chart : charts) {
                context.append("- ").append(chart.getTitle()).append(" [").append(chart.getChartType()).append("]\n");
            }
        }
        if (!CollectionUtils.isEmpty(ragDocs)) {
            context.append("\nVector snippets:\n");
            for (Document doc : ragDocs) {
                String snippet = Optional.ofNullable(doc.getText()).orElse("");
                context.append("- ").append(doc.getMetadata().getOrDefault("title", "untitled")).append("\n");
                context.append("  ")
                        .append(snippet, 0, Math.min(snippet.length(), 300))
                        .append("...\n");
            }
        }
        return context.toString();
    }

    private List<AgentPlanStep> buildPlan(AgentChatRequest request) {
        try {
            String planJson = llmService.completion(DEFAULT_PLAN_PROMPT, request.getMessage(), 0.1d);
            JsonNode node = objectMapper.readTree(extractJson(planJson));
            if (node.isArray()) {
                List<AgentPlanStep> steps = new ArrayList<>();
                for (JsonNode stepNode : node) {
                    AgentPlanStep step = objectMapper.convertValue(stepNode, AgentPlanStep.class);
                    step.setStatus("pending");
                    steps.add(step);
                }
                if (!steps.isEmpty()) {
                    steps.get(0).setStatus("running");
                }
                return steps;
            }
        } catch (JsonProcessingException e) {
            log.warn("plan parse failed: {}", e.getMessage());
        }
        return fallbackPlan(request.getMessage());
    }

    private List<AgentPlanStep> fallbackPlan(String message) {
        List<AgentPlanStep> steps = new ArrayList<>();
        steps.add(AgentPlanStep.builder()
                .id("plan-1")
                .title("Crawl the freshest papers")
                .tool("crawler")
                .objective("Collect the latest research related to the user question")
                .status("running")
                .build());
        steps.add(AgentPlanStep.builder()
                .id("plan-2")
                .title("Summarize structured insights")
                .tool("analysis")
                .objective("Build graphs/charts to highlight the key findings")
                .status("pending")
                .build());
        steps.add(AgentPlanStep.builder()
                .id("plan-3")
                .title("Generate the final answer")
                .tool("rag")
                .objective("Compose a response using the retrieved documents and graph context")
                .status("pending")
                .build());
        return steps;
    }

    private String extractJson(String raw) {
        int start = raw.indexOf('[');
        int end = raw.lastIndexOf(']');
        if (start >= 0 && end >= start) {
            return raw.substring(start, end + 1);
        }
        return raw;
    }
}
