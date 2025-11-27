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
@TableName("chat_message_ref")
public class ChatMessageRefEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long sessionId;

    /**
     * Mongo ObjectId stored as string
     */
    private String messageId;

    private String role;

    private Long seq;

    private LocalDateTime createdAt;
}
