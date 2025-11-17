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
import yunxun.ai.canary.backend.model.entity.document.PaperDocument;
import yunxun.ai.canary.backend.service.analysis.DataAnalysisService;
import yunxun.ai.canary.backend.service.crawler.PaperStorageService;
import yunxun.ai.canary.backend.service.crawler.WebCrawlerService;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AgentOrchestratorService {

    private static final String DEFAULT_PLAN_PROMPT = """
            你是一个科研智能体，需要根据用户问题列出3步以内的行动计划，返回 JSON 数组，元素包含:
            {
              "id": "step-1",
              "title": "描述",
              "tool": "crawler|analysis|rag",
              "objective": "该步骤目标"
            }
            """;

    private final WebCrawlerService webCrawlerService;
    private final PaperStorageService paperStorageService;
    private final DataAnalysisService dataAnalysisService;
    private final LlmService llmService;
    private final RagPipelineService ragPipelineService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public AgentChatResponse handleChat(AgentChatRequest request) {
        List<AgentPlanStep> plan = buildPlan(request);
        List<String> usedTools = new ArrayList<>();
        List<PaperDocument> documents = new ArrayList<>();
        List<AgentChartPayload> chartPayloads = new ArrayList<>();
        AgentGraphPayload graphPayload = null;

        for (AgentPlanStep step : plan) {
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
                .conversationId(Optional.ofNullable(request.getConversationId()).orElse(UUID.randomUUID().toString()))
                .answer(answer)
                .plan(plan)
                .charts(chartPayloads)
                .graph(graphPayload)
                .documents(snippets)
                .usedTools(usedTools)
                .build();
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
                .title("论文数量分布")
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
            context.append("最新文献：\n");
            for (PaperDocument doc : documents) {
                context.append("- ").append(doc.getTitle()).append(" (").append(doc.getSource()).append(")\n");
                if (doc.getSummary() != null) {
                    context.append("  摘要：").append(doc.getSummary()).append("\n");
                }
            }
        }
        if (!CollectionUtils.isEmpty(charts)) {
            context.append("\n图表洞察：\n");
            for (AgentChartPayload chart : charts) {
                context.append("- ").append(chart.getTitle()).append(" 类型: ").append(chart.getChartType()).append("\n");
            }
        }
        if (!CollectionUtils.isEmpty(ragDocs)) {
            context.append("\n向量检索片段：\n");
            for (Document doc : ragDocs) {
                String snippet = Optional.ofNullable(doc.getContent()).orElse("");
                context.append("- ").append(doc.getMetadata().getOrDefault("title", "未命名")).append("\n");
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
                .title("检索最新论文")
                .tool("crawler")
                .objective("检索问题相关的最新论文数据")
                .status("running")
                .build());
        steps.add(AgentPlanStep.builder()
                .id("plan-2")
                .title("整理向量上下文与图谱数据")
                .tool("analysis")
                .objective("构建知识图谱并生成图表数据")
                .status("pending")
                .build());
        steps.add(AgentPlanStep.builder()
                .id("plan-3")
                .title("生成综合回答")
                .tool("rag")
                .objective("结合图谱和向量上下文回答用户问题")
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
