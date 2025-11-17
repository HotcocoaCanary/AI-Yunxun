package yunxun.ai.canary.backend.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import yunxun.ai.canary.backend.model.dto.agent.AgentChatRequest;
import yunxun.ai.canary.backend.model.dto.agent.AgentChatResponse;
import yunxun.ai.canary.backend.service.agent.AgentOrchestratorService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/agent")
@RequiredArgsConstructor
public class AgentController {

    private final AgentOrchestratorService agentOrchestratorService;

    @PostMapping("/chat")
    public ResponseEntity<AgentChatResponse> chat(@Valid @RequestBody AgentChatRequest request) {
        return ResponseEntity.ok(agentOrchestratorService.handleChat(request));
    }

    @GetMapping("/tools")
    public ResponseEntity<List<Map<String, String>>> listTools() {
        return ResponseEntity.ok(List.of(
                Map.of("id", "crawler", "name", "数据爬取工具", "description", "检索并拉取外部论文数据"),
                Map.of("id", "analysis", "name", "数据分析工具", "description", "整理图数据库并生成图表数据"),
                Map.of("id", "rag", "name", "上下文问答工具", "description", "结合向量检索生成回答")
        ));
    }
}
