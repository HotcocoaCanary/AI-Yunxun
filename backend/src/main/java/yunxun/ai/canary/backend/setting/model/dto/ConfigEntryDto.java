package yunxun.ai.canary.backend.setting.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConfigEntryDto {
    private String key;
    private String value;
    private Boolean encrypted;
}
