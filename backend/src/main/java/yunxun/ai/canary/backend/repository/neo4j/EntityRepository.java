package yunxun.ai.canary.backend.repository.neo4j;

import yunxun.ai.canary.backend.model.entity.KnowledgeEntity;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 实体数据访问层
 */
@Repository
public interface EntityRepository extends Neo4jRepository<KnowledgeEntity, Long> {
    
    Optional<KnowledgeEntity> findByName(String name);
    
    List<KnowledgeEntity> findByType(String type);
    
    @Query("MATCH (e:KnowledgeEntity) WHERE e.name CONTAINS $name RETURN e")
    List<KnowledgeEntity> findByNameContaining(@Param("name") String name);
    
    @Query("MATCH (e:KnowledgeEntity) WHERE e.type = $type AND e.confidence >= $minConfidence RETURN e")
    List<KnowledgeEntity> findByTypeAndConfidenceGreaterThanEqual(@Param("type") String type, @Param("minConfidence") Double minConfidence);
    
    @Query("MATCH (e:KnowledgeEntity)-[r]->(target:KnowledgeEntity) WHERE e.name = $name RETURN e, r, target")
    List<KnowledgeEntity> findEntitiesWithRelationships(@Param("name") String name);
    
    @Query("MATCH (e:KnowledgeEntity) WHERE e.name = $name RETURN e, [(e)-[r]->(target) | {relationship: r, target: target}]")
    Optional<KnowledgeEntity> findByNameWithRelationships(@Param("name") String name);
}
