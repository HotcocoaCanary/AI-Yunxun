package yunxun.ai.canary.backend.graph.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.neo4j.driver.types.Node;
import org.neo4j.driver.types.Path;
import org.neo4j.driver.types.Relationship;
import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class Neo4jQueryService {

    private final Neo4jClient neo4jClient;
    private final ObjectMapper objectMapper;

    public Neo4jQueryService(Neo4jClient neo4jClient, ObjectMapper objectMapper) {
        this.neo4jClient = neo4jClient;
        this.objectMapper = objectMapper;
    }

    /**
     * 执行只读 Cypher 查询并以 JSON 字符串形式返回结果。
     * 这是供业务服务和 MCP 工具复用的统一入口。
     */
    public String runQueryAsJson(String cypher) {
        List<Map<String, Object>> rawRows = neo4jClient.query(cypher).fetch().all().stream().toList();
        List<Map<String, Object>> rows = new ArrayList<>();
        for (Map<String, Object> row : rawRows) {
            rows.add(convertRow(row));
        }
        try {
            return objectMapper.writeValueAsString(rows);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("序列化 Neo4j 查询结果为 JSON 时出错", e);
        }
    }

    private Map<String, Object> convertRow(Map<String, Object> row) {
        Map<String, Object> converted = new LinkedHashMap<>();
        for (Map.Entry<String, Object> entry : row.entrySet()) {
            converted.put(entry.getKey(), convertValue(entry.getValue()));
        }
        return converted;
    }

    private Object convertValue(Object value) {
        if (value instanceof Node node) {
            Map<String, Object> nodeMap = new LinkedHashMap<>();
            nodeMap.put("elementId", node.elementId());
            nodeMap.put("labels", node.labels());
            nodeMap.put("properties", node.asMap());
            return nodeMap;
        }
        if (value instanceof Relationship rel) {
            Map<String, Object> relMap = new LinkedHashMap<>();
            relMap.put("elementId", rel.elementId());
            relMap.put("type", rel.type());
            relMap.put("startElementId", rel.startNodeElementId());
            relMap.put("endElementId", rel.endNodeElementId());
            relMap.put("properties", rel.asMap());
            return relMap;
        }
        if (value instanceof Path path) {
            List<Map<String, Object>> segments = new ArrayList<>();
            for (Path.Segment seg : path) {
                Map<String, Object> segMap = new LinkedHashMap<>();
                segMap.put("start", convertValue(seg.start()));
                segMap.put("relationship", convertValue(seg.relationship()));
                segMap.put("end", convertValue(seg.end()));
                segments.add(segMap);
            }
            return segments;
        }
        if (value instanceof Iterable<?> iterable) {
            List<Object> list = new ArrayList<>();
            for (Object v : iterable) {
                list.add(convertValue(v));
            }
            return list;
        }
        if (value instanceof Map<?, ?> map) {
            Map<String, Object> m = new LinkedHashMap<>();
            map.forEach((k, v) -> m.put(String.valueOf(k), convertValue(v)));
            return m;
        }
        return value;
    }
}
