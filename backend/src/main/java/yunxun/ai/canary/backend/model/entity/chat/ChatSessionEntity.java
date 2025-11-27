package yunxun.ai.canary.backend.model.entity.chat;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("chat_session")
public class ChatSessionEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Long parentId;

    /**
     * folder | dialog
     */
    private String type;

    private String title;

    /**
     * materialized path like 1/5/23
     */
    private String path;

    private Boolean isArchived;

    /**
     * JSON string for extra data
     */
    private String metadata;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
