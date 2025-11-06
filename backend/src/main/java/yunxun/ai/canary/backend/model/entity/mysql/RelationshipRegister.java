package yunxun.ai.canary.backend.model.entity.mysql;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "relationship_register")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RelationshipRegister {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 关系标签（如 PURCHASED、FRIEND_WITH）
     */
    @Column(nullable = false, unique = true, length = 128)
    private String label;

    /**
     * 当前关系数量
     */
    @Column(nullable = false)
    private Long count;

    /**
     * 是否已删除（标记删除法）
     */
    @Column(nullable = false)
    private Boolean deleted = false;

    /**
     * 注册时间
     */
    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    /**
     * 最后更新时间
     */
    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
