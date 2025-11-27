package yunxun.ai.canary.backend.model.dto.config;

import lombok.Data;

import java.util.Map;

@Data
public class ConfigUpsertRequest {
    private Map<String, String> values;
    private Boolean encrypted;
}
