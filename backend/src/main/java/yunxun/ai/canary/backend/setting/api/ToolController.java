package yunxun.ai.canary.backend.setting.api;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import yunxun.ai.canary.backend.setting.handler.ToolHandler;
import yunxun.ai.canary.backend.setting.model.dto.tools.McpToolStatusDto;
import yunxun.ai.canary.backend.setting.model.dto.tools.McpToolToggleRequest;

import java.util.List;

@RestController
@RequestMapping("/api/tools")
@RequiredArgsConstructor
public class ToolController {

    private final ToolHandler toolHandler;

    @GetMapping
    public List<McpToolStatusDto> listTools() {
        return toolHandler.list(null);
    }

    @PostMapping("/toggle")
    public void toggle(@RequestBody McpToolToggleRequest request) {
        toolHandler.toggle(null, request.getName(), request.isEnabled());
    }
}

