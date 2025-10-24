package yunxun.ai.canary.backend.model.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Property;
import org.springframework.data.neo4j.core.schema.GeneratedValue;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 知识图谱实体节点
 */
@Node("KnowledgeEntity")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class KnowledgeEntity {
    
    @Id
    @GeneratedValue
    private Long id;
    
    @Property("name")
    private String name;
    
    @Property("type")
    private String type; // Person, Organization, Concept, Method, etc.
    
    @Property("description")
    private String description;
    
    @Property("properties")
    private String properties; // JSON格式的额外属性
    
    @Property("confidence")
    private Double confidence;
    
    @Property("source_papers")
    private List<String> sourcePapers;
    
    @Property("created_at")
    private LocalDateTime createdAt;
    
    @Property("updated_at")
    private LocalDateTime updatedAt;
}