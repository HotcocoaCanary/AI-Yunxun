package yunxun.ai.canary.backend.handler;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import yunxun.ai.canary.backend.model.dto.config.ConfigEntryDto;
import yunxun.ai.canary.backend.model.dto.config.ConfigUpsertRequest;
import yunxun.ai.canary.backend.service.setting.ConfigService;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class ConfigHandler {

    private final ConfigService configService;

    public Map<String, ConfigEntryDto> getByScope(String scope) {
        return configService.getByScope(scope);
    }

    public void upsert(String scope, ConfigUpsertRequest request) {
        configService.upsert(scope, request);
    }

    public Map<String, Object> testConnection() {
        // TODO: real connectivity checks
        return Map.of("success", true, "message", "connection test placeholder");
    }
}
