package yunxun.ai.canary.backend.controller.setting;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import yunxun.ai.canary.backend.handler.ConfigHandler;
import yunxun.ai.canary.backend.model.dto.config.ConfigEntryDto;
import yunxun.ai.canary.backend.model.dto.config.ConfigUpsertRequest;

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
