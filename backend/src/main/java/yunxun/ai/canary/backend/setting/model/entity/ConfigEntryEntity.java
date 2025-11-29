package yunxun.ai.canary.backend.setting.model.entity;

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
@TableName("config_entry")
public class ConfigEntryEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * e.g. mcp / llm / db
     */
    private String scope;

    private String configKey;

    private String configValue;

    private Boolean encrypted;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
