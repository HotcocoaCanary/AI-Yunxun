package yunxun.ai.canary.backend.setting.handler;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import yunxun.ai.canary.backend.setting.model.dto.tools.McpToolStatusDto;
import yunxun.ai.canary.backend.setting.service.UserToolService;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ToolHandler {

    private final UserToolService userToolService;

    public List<McpToolStatusDto> list(Long userId) {
        return userToolService.listTools(userId);
    }

    public void toggle(Long userId, String name, boolean enabled) {
        userToolService.setEnabled(userId, name, enabled);
    }
}
