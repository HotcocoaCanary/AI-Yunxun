package yunxun.ai.canary.backend.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import yunxun.ai.canary.backend.model.dto.tools.McpToolStatusDto;
import yunxun.ai.canary.backend.model.dto.tools.McpToolToggleRequest;
import yunxun.ai.canary.backend.service.tools.InMemoryToolService;

import java.util.List;

@RestController
@RequestMapping("/api/tools")
@RequiredArgsConstructor
public class ToolController {

    private final InMemoryToolService toolService;

    @GetMapping
    public List<McpToolStatusDto> listTools() {
        return toolService.listTools();
    }

    @PostMapping("/toggle")
    public void toggle(@RequestBody McpToolToggleRequest request) {
        toolService.toggleTool(request.getName(), request.isEnabled());
    }
}
