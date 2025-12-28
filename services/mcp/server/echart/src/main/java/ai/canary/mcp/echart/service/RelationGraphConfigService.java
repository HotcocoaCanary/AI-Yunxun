package ai.canary.mcp.echart.service;

import ai.canary.mcp.echart.model.RelationGraphData;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RelationGraphConfigService {

    private final ObjectMapper objectMapper;

    public String generateConfig(RelationGraphData data) throws Exception {
        validateData(data);
        
        Map<String, Object> config = new HashMap<>();
        
        // Title
        if (data.getTitle() != null && !data.getTitle().isEmpty()) {
            Map<String, Object> title = new HashMap<>();
            title.put("text", data.getTitle());
            title.put("left", "center");
            config.put("title", title);
        }
        
        // Series
        Map<String, Object> series = new HashMap<>();
        series.put("type", "graph");
        series.put("layout", "force");
        series.put("roam", true);
        series.put("label", Map.of("show", true));
        series.put("edgeSymbol", List.of("circle", "arrow"));
        series.put("edgeSymbolSize", List.of(4, 10));
        
        // Nodes
        List<Map<String, Object>> nodes = data.getNodes().stream()
                .map(node -> {
                    Map<String, Object> nodeMap = new HashMap<>();
                    nodeMap.put("id", node.getId());
                    nodeMap.put("name", node.getName());
                    if (node.getCategory() != null) {
                        nodeMap.put("category", node.getCategory());
                    }
                    if (node.getValue() != null) {
                        nodeMap.put("value", node.getValue());
                    }
                    if (node.getProperties() != null && !node.getProperties().isEmpty()) {
                        nodeMap.putAll(node.getProperties());
                    }
                    return nodeMap;
                })
                .collect(Collectors.toList());
        series.put("data", nodes);
        
        // Links
        List<Map<String, Object>> links = data.getLinks().stream()
                .map(link -> {
                    Map<String, Object> linkMap = new HashMap<>();
                    linkMap.put("source", link.getSource());
                    linkMap.put("target", link.getTarget());
                    if (link.getName() != null) {
                        linkMap.put("name", link.getName());
                    }
                    if (link.getValue() != null) {
                        linkMap.put("value", link.getValue());
                    }
                    if (link.getProperties() != null && !link.getProperties().isEmpty()) {
                        linkMap.putAll(link.getProperties());
                    }
                    return linkMap;
                })
                .collect(Collectors.toList());
        series.put("links", links);
        
        // Force layout configuration
        Map<String, Object> force = new HashMap<>();
        force.put("repulsion", 4000);
        force.put("gravity", 0.02);
        force.put("edgeLength", 200);
        series.put("force", force);
        
        config.put("series", List.of(series));
        
        // Tooltip
        Map<String, Object> tooltip = new HashMap<>();
        tooltip.put("trigger", "item");
        config.put("tooltip", tooltip);
        
        return objectMapper.writeValueAsString(config);
    }

    private void validateData(RelationGraphData data) {
        if (data == null) {
            throw new IllegalArgumentException("RelationGraphData cannot be null");
        }
        if (data.getNodes() == null || data.getNodes().isEmpty()) {
            throw new IllegalArgumentException("Nodes cannot be null or empty");
        }
        if (data.getLinks() == null) {
            throw new IllegalArgumentException("Links cannot be null");
        }
        // Validate that all link sources and targets exist in nodes
        List<String> nodeIds = data.getNodes().stream()
                .map(RelationGraphData.Node::getId)
                .collect(Collectors.toList());
        for (RelationGraphData.Link link : data.getLinks()) {
            if (!nodeIds.contains(link.getSource())) {
                throw new IllegalArgumentException("Link source node not found: " + link.getSource());
            }
            if (!nodeIds.contains(link.getTarget())) {
                throw new IllegalArgumentException("Link target node not found: " + link.getTarget());
            }
        }
    }
}

