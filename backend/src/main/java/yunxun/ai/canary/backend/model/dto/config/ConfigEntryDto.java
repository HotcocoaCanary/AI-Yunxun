package yunxun.ai.canary.backend.model.dto.config;

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
