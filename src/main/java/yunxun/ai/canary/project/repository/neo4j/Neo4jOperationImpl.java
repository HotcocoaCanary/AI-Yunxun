package yunxun.ai.canary.project.repository.neo4j;

import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;

@Service
public class Neo4jOperationImpl implements Neo4jOperation {

    private static final Pattern SAFE_NAME = Pattern.compile("^[A-Za-z][A-Za-z0-9_]*$");

    private final Neo4jClient neo4jClient;

    public Neo4jOperationImpl(Neo4jClient neo4jClient) {
        this.neo4jClient = neo4jClient;
    }

    @Override
    public Mono<Map<String, Object>> createNode(String label, Map<String, Object> properties) {
        return Mono.fromCallable(() -> {
            String safeLabel = requireSafe(label, "label");
            Map<String, Object> props = new HashMap<>(properties == null ? Map.of() : properties);
            props.putIfAbsent("id", UUID.randomUUID().toString());

            String cypher = "CREATE (n:" + safeLabel + ") SET n += $props RETURN n.id AS id, $label AS label, properties(n) AS properties";
            Map<String, Object> params = Map.of("props", props, "label", safeLabel);
            return neo4jClient.query(cypher).bindAll(params).fetch().one().orElse(null);
        });
    }

    @Override
    public Mono<Map<String, Object>> findNodeById(String id) {
        return Mono.fromCallable(() -> {
            String cypher = "MATCH (n) WHERE n.id = $id RETURN n.id AS id, head(labels(n)) AS label, properties(n) AS properties LIMIT 1";
            return neo4jClient.query(cypher).bindAll(Map.of("id", id)).fetch().one().orElse(null);
        });
    }

    @Override
    public Mono<Map<String, Object>> findNodeByProperty(String label, String property, String value) {
        return Mono.fromCallable(() -> {
            String safeLabel = requireSafe(label, "label");
            String safeProperty = requireSafe(property, "property");
            String cypher = "MATCH (n:" + safeLabel + ") WHERE n." + safeProperty + " = $value RETURN n.id AS id, $label AS label, properties(n) AS properties LIMIT 1";
            return neo4jClient.query(cypher).bindAll(Map.of("value", value, "label", safeLabel)).fetch().one().orElse(null);
        });
    }

    @Override
    public Mono<Boolean> updateNode(String id, Map<String, Object> patch) {
        return Mono.fromCallable(() -> {
            if (patch == null || patch.isEmpty()) {
                return false;
            }
            String cypher = "MATCH (n) WHERE n.id = $id SET n += $patch RETURN n.id AS id";
            Optional<Map<String, Object>> updated = neo4jClient.query(cypher).bindAll(Map.of("id", id, "patch", patch)).fetch().one();
            return updated.isPresent();
        });
    }

    @Override
    public Mono<Boolean> deleteNode(String id, boolean detach) {
        return Mono.fromCallable(() -> {
            String cypher = detach
                    ? "MATCH (n) WHERE n.id = $id DETACH DELETE n RETURN 1 AS deleted"
                    : "MATCH (n) WHERE n.id = $id DELETE n RETURN 1 AS deleted";
            Optional<Map<String, Object>> deleted = neo4jClient.query(cypher).bindAll(Map.of("id", id)).fetch().one();
            return deleted.isPresent();
        });
    }

    @Override
    public Mono<Map<String, Object>> createRelationship(String fromId, String toId, String type, Map<String, Object> properties) {
        return Mono.fromCallable(() -> {
            String safeType = requireSafe(type, "type");
            Map<String, Object> props = new HashMap<>(properties == null ? Map.of() : properties);
            props.putIfAbsent("id", UUID.randomUUID().toString());

            String cypher = "MATCH (a {id: $fromId}), (b {id: $toId}) "
                    + "CREATE (a)-[r:" + safeType + "]->(b) "
                    + "SET r += $props "
                    + "RETURN r.id AS id, $type AS type, a.id AS fromId, b.id AS toId, properties(r) AS properties";
            Map<String, Object> params = Map.of("fromId", fromId, "toId", toId, "props", props, "type", safeType);
            return neo4jClient.query(cypher).bindAll(params).fetch().one().orElse(null);
        });
    }

    @Override
    public Mono<Map<String, Object>> findRelationship(String id) {
        return Mono.fromCallable(() -> {
            String cypher = "MATCH (a)-[r]->(b) WHERE r.id = $id "
                    + "RETURN r.id AS id, type(r) AS type, a.id AS fromId, b.id AS toId, properties(r) AS properties LIMIT 1";
            return neo4jClient.query(cypher).bindAll(Map.of("id", id)).fetch().one().orElse(null);
        });
    }

    @Override
    public Mono<Boolean> updateRelationship(String id, Map<String, Object> patch) {
        return Mono.fromCallable(() -> {
            if (patch == null || patch.isEmpty()) {
                return false;
            }
            String cypher = "MATCH ()-[r]->() WHERE r.id = $id SET r += $patch RETURN r.id AS id";
            Optional<Map<String, Object>> updated = neo4jClient.query(cypher).bindAll(Map.of("id", id, "patch", patch)).fetch().one();
            return updated.isPresent();
        });
    }

    @Override
    public Mono<Boolean> deleteRelationship(String id) {
        return Mono.fromCallable(() -> {
            String cypher = "MATCH ()-[r]->() WHERE r.id = $id DELETE r RETURN 1 AS deleted";
            Optional<Map<String, Object>> deleted = neo4jClient.query(cypher).bindAll(Map.of("id", id)).fetch().one();
            return deleted.isPresent();
        });
    }

    @Override
    public Mono<Map<String, Object>> findPath(String fromId, String toId, int maxDepth, List<String> types) {
        return Mono.fromCallable(() -> {
            int depth = maxDepth <= 0 ? 4 : Math.min(maxDepth, 8);
            String relPattern = relationshipPattern(types, depth);
            String cypher = "MATCH (a {id: $fromId}), (b {id: $toId}) "
                    + "MATCH p = shortestPath((a)-" + relPattern + "-(b)) "
                    + "RETURN [n IN nodes(p) | {id: n.id, label: head(labels(n)), properties: properties(n)}] AS nodes, "
                    + "[r IN relationships(p) | {id: r.id, type: type(r), properties: properties(r)}] AS relationships "
                    + "LIMIT 1";
            return neo4jClient.query(cypher).bindAll(Map.of("fromId", fromId, "toId", toId)).fetch().one().orElse(null);
        });
    }

    @Override
    public Mono<Map<String, Object>> findNeighbors(String id, int depth, List<String> types) {
        return Mono.fromCallable(() -> {
            int d = depth <= 0 ? 1 : Math.min(depth, 3);
            String relPattern = relationshipPatternWithVar("rs", types, d);
            String cypher = "MATCH (a {id: $id})-" + relPattern + "-(b) "
                    + "WITH b, rs "
                    + "UNWIND rs AS r "
                    + "RETURN collect(distinct {id: b.id, label: head(labels(b)), properties: properties(b)}) AS nodes, "
                    + "collect(distinct {id: r.id, type: type(r), fromId: startNode(r).id, toId: endNode(r).id, properties: properties(r)}) AS relationships "
                    + "LIMIT 1";
            return neo4jClient.query(cypher).bindAll(Map.of("id", id)).fetch().one().orElse(null);
        });
    }

    @Override
    public Mono<List<Map<String, Object>>> fuzzySearch(String query, List<String> labels, int limit) {
        return Mono.fromCallable(() -> {
            int lim = limit <= 0 ? 10 : Math.min(limit, 20);
            String q = query == null ? "" : query;

            String labelPart = "";
            if (labels != null && !labels.isEmpty()) {
                List<String> safeLabels = new ArrayList<>();
                for (String label : labels) {
                    safeLabels.add(requireSafe(label, "label"));
                }
                labelPart = ":" + String.join("|", safeLabels);
            }

            String cypher = "MATCH (n" + labelPart + ") "
                    + "WHERE toLower(coalesce(n.name, '')) CONTAINS toLower($q) "
                    + "RETURN n.id AS id, head(labels(n)) AS label, properties(n) AS properties, 1.0 AS score "
                    + "LIMIT $limit";
            Collection<Map<String, Object>> rows = neo4jClient.query(cypher).bindAll(Map.of("q", q, "limit", lim)).fetch().all();
            return new ArrayList<>(rows);
        });
    }

    private static String relationshipPattern(List<String> types, int maxDepth) {
        String typePart = "";
        if (types != null && !types.isEmpty()) {
            List<String> safeTypes = new ArrayList<>();
            for (String type : types) {
                safeTypes.add(requireSafe(type, "type"));
            }
            typePart = ":" + String.join("|", safeTypes);
        }
        return "[" + typePart + "*1.." + maxDepth + "]";
    }

    private static String relationshipPatternWithVar(String var, List<String> types, int maxDepth) {
        String safeVar = requireSafe(var, "var");
        String typePart = "";
        if (types != null && !types.isEmpty()) {
            List<String> safeTypes = new ArrayList<>();
            for (String type : types) {
                safeTypes.add(requireSafe(type, "type"));
            }
            typePart = ":" + String.join("|", safeTypes);
        }
        return "[" + safeVar + typePart + "*1.." + maxDepth + "]";
    }

    private static String requireSafe(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " is required");
        }
        if (!SAFE_NAME.matcher(value).matches()) {
            throw new IllegalArgumentException(field + " contains unsafe characters: " + value);
        }
        return value;
    }
}
