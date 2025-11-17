package yunxun.ai.canary.backend.service.mcp.tools;

import jakarta.annotation.Resource;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;
import yunxun.ai.canary.backend.model.dto.agent.AgentChartPayload;
import yunxun.ai.canary.backend.model.dto.graph.GraphChartRequest;
import yunxun.ai.canary.backend.model.dto.graph.GraphIngestionRequest;
import yunxun.ai.canary.backend.service.analysis.DataAnalysisService;

@Component
public class AnalyticsTool {

    @Resource
    private DataAnalysisService dataAnalysisService;

    @Tool(name = "graph_ingest_dataset", description = "将结构化数据写入 Neo4j 图数据库")
    public String ingestDataset(
            @ToolParam(description = "节点和关系 JSON") GraphIngestionRequest request
    ) {
        dataAnalysisService.ingest(request);
        return "图数据已写入";
    }

    @Tool(name = "graph_generate_chart", description = "根据 Cypher 查询生成 ECharts 配置")
    public AgentChartPayload generateChart(
            @ToolParam(description = "图表类型") String chartType,
            @ToolParam(description = "标题") String title,
            @ToolParam(description = "Cypher 查询") String cypher,
            @ToolParam(description = "横轴字段") String xField,
            @ToolParam(description = "纵轴字段") String yField
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
