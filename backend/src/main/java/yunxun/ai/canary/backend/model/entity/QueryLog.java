package yunxun.ai.canary.backend.model.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

/**
 * 查询日志实体
 */
@Entity
@Table(name = "query_logs")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class QueryLog {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "user_id")
    private Long userId;
    
    @Column(name = "query_text", columnDefinition = "TEXT")
    private String queryText;
    
    @Column(name = "query_type")
    private String queryType; // NATURAL_LANGUAGE, CYPHER, RAG
    
    @Column(name = "response", columnDefinition = "TEXT")
    private String response;
    
    @Column(name = "execution_time")
    private Long executionTime; // 毫秒
    
    @Column(name = "success")
    private Boolean success;
    
    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
