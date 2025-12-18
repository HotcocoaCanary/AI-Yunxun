package yunxun.ai.canary.project.repository.neo4j;

import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

public interface Neo4jOperation {

    Mono<Map<String, Object>> createNode(String label, Map<String, Object> properties);

    Mono<Map<String, Object>> findNodeById(String id);

    Mono<Map<String, Object>> findNodeByProperty(String label, String property, String value);

    Mono<Boolean> updateNode(String id, Map<String, Object> patch);

    Mono<Boolean> deleteNode(String id, boolean detach);

    Mono<Map<String, Object>> createRelationship(String fromId, String toId, String type, Map<String, Object> properties);

    Mono<Map<String, Object>> findRelationship(String id);

    Mono<Boolean> updateRelationship(String id, Map<String, Object> patch);

    Mono<Boolean> deleteRelationship(String id);

    Mono<Map<String, Object>> findPath(String fromId, String toId, int maxDepth, List<String> types);

    Mono<Map<String, Object>> findNeighbors(String id, int depth, List<String> types);

    Mono<List<Map<String, Object>>> fuzzySearch(String query, List<String> labels, int limit);
}
