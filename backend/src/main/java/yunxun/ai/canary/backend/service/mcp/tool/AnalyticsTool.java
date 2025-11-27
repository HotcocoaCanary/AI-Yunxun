package yunxun.ai.canary.backend.service.mcp.tool;

import jakarta.annotation.Resource;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;
import yunxun.ai.canary.backend.model.dto.agent.AgentChartPayload;
import yunxun.ai.canary.backend.model.dto.graph.GraphChartRequest;
import yunxun.ai.canary.backend.model.dto.graph.GraphIngestionRequest;
import yunxun.ai.canary.backend.service.pipeline.connectors.analytics.DataAnalysisService;

@Component
public class AnalyticsTool {

    @Resource
    private DataAnalysisService dataAnalysisService;

    @Tool(name = "graph_ingest_dataset", description = "Ingest structured data into Neo4j graph database")
    public String ingestDataset(
            @ToolParam(description = "Nodes and relationships payload") GraphIngestionRequest request
    ) {
        dataAnalysisService.ingest(request);
        return "Graph data ingested";
    }

    @Tool(name = "graph_generate_chart", description = "Generate ECharts option from Cypher query result")
    public AgentChartPayload generateChart(
            @ToolParam(description = "chart type") String chartType,
            @ToolParam(description = "title") String title,
            @ToolParam(description = "cypher query") String cypher,
            @ToolParam(description = "x axis field") String xField,
            @ToolParam(description = "y axis field") String yField
    ) {
        GraphChartRequest chartRequest = GraphChartRequest.builder()
                .chartType(chartType)
                .title(title)
                .cypher(cypher)
                .xField(xField)
                .yField(yField)
                .build();
        return dataAnalysisService.generateChart(chartRequest);
    }
}
