package yunxun.ai.canary.backend.setting.api;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import yunxun.ai.canary.backend.setting.handler.ConfigHandler;
import yunxun.ai.canary.backend.setting.model.dto.ConfigEntryDto;
import yunxun.ai.canary.backend.setting.model.dto.ConfigUpsertRequest;

import java.util.Map;

@RestController
@RequestMapping("/api/config")
@RequiredArgsConstructor
public class ConfigController {

    private final ConfigHandler configHandler;

    @GetMapping("/{scope}")
    public Map<String, ConfigEntryDto> getByScope(@PathVariable String scope) {
        return configHandler.getByScope(scope);
    }

    @PutMapping("/{scope}")
    public void upsert(@PathVariable String scope, @RequestBody ConfigUpsertRequest request) {
        configHandler.upsert(scope, request);
    }

    @PostMapping("/test")
    public Map<String, Object> testConnection() {
        return configHandler.testConnection();
    }
}
