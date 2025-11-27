package yunxun.ai.canary.backend.service.graph;

import org.springframework.stereotype.Service;
import yunxun.ai.canary.backend.model.dto.graph.GraphDataDto;
import yunxun.ai.canary.backend.model.dto.graph.GraphEdgeDto;
import yunxun.ai.canary.backend.model.dto.graph.GraphNodeDto;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class GraphMockService {

    public GraphDataDto overview() {
        List<GraphNodeDto> nodes = new ArrayList<>();
        List<GraphEdgeDto> edges = new ArrayList<>();
        nodes.add(GraphNodeDto.builder().id("neo4j").label("Neo4j").type("tech").build());
        nodes.add(GraphNodeDto.builder().id("mongo").label("MongoDB").type("tech").build());
        nodes.add(GraphNodeDto.builder().id("mcp").label("MCP Tool").type("module").build());
        nodes.add(GraphNodeDto.builder().id("agent").label("Agent").type("service").build());
        nodes.add(GraphNodeDto.builder().id("rag").label("RAG").type("pipeline").build());

        edges.add(GraphEdgeDto.builder().id("e1").source("agent").target("mcp").type("uses").build());
        edges.add(GraphEdgeDto.builder().id("e2").source("agent").target("rag").type("invokes").build());
        edges.add(GraphEdgeDto.builder().id("e3").source("rag").target("neo4j").type("reads").build());
        edges.add(GraphEdgeDto.builder().id("e4").source("rag").target("mongo").type("reads").build());

        return GraphDataDto.builder().nodes(nodes).edges(edges).build();
    }

    public GraphDataDto expand(String nodeId, int limit) {
        List<GraphNodeDto> nodes = new ArrayList<>();
        List<GraphEdgeDto> edges = new ArrayList<>();
        for (int i = 0; i < Math.min(limit, 5); i++) {
            String id = nodeId + "-child-" + i;
            nodes.add(GraphNodeDto.builder().id(id).label("节点 " + i).type("entity").build());
            edges.add(GraphEdgeDto.builder()
                    .id(UUID.randomUUID().toString())
                    .source(nodeId)
                    .target(id)
                    .type("related")
                    .build());
        }
        return GraphDataDto.builder().nodes(nodes).edges(edges).build();
    }
}
