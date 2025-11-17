package yunxun.ai.canary.backend.service.analysis;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.neo4j.driver.types.Node;
import org.neo4j.driver.types.Relationship;
import yunxun.ai.canary.backend.model.dto.agent.AgentChartPayload;
import yunxun.ai.canary.backend.model.dto.agent.AgentGraphPayload;
import yunxun.ai.canary.backend.model.dto.graph.GraphChartRequest;
import yunxun.ai.canary.backend.model.dto.graph.GraphIngestionRequest;
import yunxun.ai.canary.backend.model.dto.graph.GraphNodeInput;
import yunxun.ai.canary.backend.model.dto.graph.GraphRelationshipInput;
import yunxun.ai.canary.backend.model.entity.graph.BaseNode;
import yunxun.ai.canary.backend.model.entity.graph.BaseRelationship;
import yunxun.ai.canary.backend.repository.graph.GraphRepository;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DataAnalysisService {

    private final GraphRepository graphRepository;

    public void ingest(GraphIngestionRequest request) {
        if (!CollectionUtils.isEmpty(request.getNodes())) {
            for (GraphNodeInput nodeInput : request.getNodes()) {
                BaseNode node = new BaseNode(nodeInput.getLabel()) {};
                node.setId(nodeInput.getId());
                node.setProperties(Optional.ofNullable(nodeInput.getProperties()).orElseGet(HashMap::new));
                graphRepository.createOrUpdateNode(node);
            }
        }
        if (!CollectionUtils.isEmpty(request.getRelationships())) {
            for (GraphRelationshipInput relInput : request.getRelationships()) {
                BaseNode start = new BaseNode(relInput.getStartLabel()) {{ setId(relInput.getStartId()); }};
                BaseNode end = new BaseNode(relInput.getEndLabel()) {{ setId(relInput.getEndId()); }};
                BaseRelationship relationship = new BaseRelationship(relInput.getLabel(), start, end) {};
                relationship.setId(relInput.getId());
                relationship.setProperties(Optional.ofNullable(relInput.getProperties()).orElseGet(HashMap::new));
                graphRepository.createOrUpdateRelationship(relationship);
            }
        }
    }

    public AgentChartPayload generateChart(GraphChartRequest request) {
        List<Map<String, Object>> rows = graphRepository.runCustomQuery(request.getCypher(), Collections.emptyMap());
        Map<String, Object> options = buildEchartsOptions(request, rows);
        return AgentChartPayload.builder()
                .chartType(request.getChartType())
                .title(request.getTitle())
                .options(options)
                .build();
    }

    public AgentGraphPayload buildGraphView(String cypher) {
        List<Map<String, Object>> rows = graphRepository.runCustomQuery(cypher, Collections.emptyMap());
        List<Map<String, Object>> nodes = new ArrayList<>();
        List<Map<String, Object>> links = new ArrayList<>();
        for (Map<String, Object> row : rows) {
            Object startNode = row.get("start");
            Object endNode = row.get("end");
            Object rel = row.get("rel");
            if (startNode instanceof Node nodeValue) {
                nodes.add(flattenNode(nodeValue));
            }
            if (endNode instanceof Node nodeValue) {
                nodes.add(flattenNode(nodeValue));
            }
            if (rel instanceof Relationship relationship) {
                Map<String, Object> link = new HashMap<>();
                link.put("id", relationship.id());
                link.put("source", relationship.startNodeId());
                link.put("target", relationship.endNodeId());
                link.put("type", relationship.type());
                link.put("properties", relationship.asMap());
                links.add(link);
            }
        }
        nodes = nodes.stream()
                .collect(Collectors.toMap(node -> node.get("id"), node -> node, (first, second) -> first))
                .values()
                .stream()
                .toList();
        return AgentGraphPayload.builder()
                .nodes(nodes)
                .links(links)
                .build();
    }

    private Map<String, Object> flattenNode(Node node) {
        Map<String, Object> flattened = new HashMap<>();
        flattened.put("id", node.id());
        flattened.put("labels", node.labels());
        flattened.put("properties", node.asMap());
        return flattened;
    }

    private Map<String, Object> buildEchartsOptions(GraphChartRequest request, List<Map<String, Object>> rows) {
        Map<String, Object> option = new HashMap<>();
        option.put("title", Map.of("text", request.getTitle()));
        List<Object> categories = rows.stream()
                .map(row -> row.get(request.getXField()))
                .distinct()
                .toList();
        option.put("xAxis", Map.of("type", "category", "data", categories));
        List<Number> values = rows.stream()
                .map(row -> (Number) row.get(request.getYField()))
                .toList();
        Map<String, Object> series = new HashMap<>();
        series.put("type", request.getChartType());
        series.put("name", Optional.ofNullable(request.getSeriesField()).orElse("value"));
        series.put("data", values);
        option.put("series", List.of(series));
        option.put("tooltip", Map.of("trigger", "axis"));
        return option;
    }
}
