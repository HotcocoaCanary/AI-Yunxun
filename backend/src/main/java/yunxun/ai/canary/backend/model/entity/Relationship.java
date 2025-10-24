package yunxun.ai.canary.backend.model.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.RelationshipProperties;
import org.springframework.data.neo4j.core.schema.TargetNode;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 知识图谱关系
 */
@RelationshipProperties
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Relationship {
    
    @Id
    private Long id;
    
    @TargetNode
    private KnowledgeEntity target;
    
    private String source; // 源实体名称
    
    private String type; // CO_AUTHOR, CITE, RELATED_TO, etc.
    
    private String description;
    
    private Double confidence;
    
    private List<String> sourcePapers;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
}
