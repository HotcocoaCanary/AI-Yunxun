package yunxun.ai.canary.backend.model.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import yunxun.ai.canary.backend.model.entity.KnowledgeEntity;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 知识实体数据传输对象
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KnowledgeEntityDTO {
    
    private Long id;
    private String name;
    private String type;
    private String description;
    private String properties;
    private Double confidence;
    private List<String> sourcePapers;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    /**
     * 从实体转换为DTO
     */
    public static KnowledgeEntityDTO fromEntity(KnowledgeEntity entity) {
        return KnowledgeEntityDTO.builder()
                .id(entity.getId())
                .name(entity.getName())
                .type(entity.getType())
                .description(entity.getDescription())
                .properties(entity.getProperties())
                .confidence(entity.getConfidence())
                .sourcePapers(entity.getSourcePapers())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
