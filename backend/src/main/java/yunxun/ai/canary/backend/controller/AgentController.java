package yunxun.ai.canary.backend.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;
import yunxun.ai.canary.backend.model.dto.agent.AgentChatRequest;
import yunxun.ai.canary.backend.model.dto.agent.AgentChatResponse;
import yunxun.ai.canary.backend.model.dto.agent.AgentConversationDetail;
import yunxun.ai.canary.backend.model.dto.agent.AgentConversationSummary;
import yunxun.ai.canary.backend.model.entity.agent.AgentConversation;
import yunxun.ai.canary.backend.service.agent.AgentOrchestratorService;
import yunxun.ai.canary.backend.repository.mongo.AgentConversationRepository;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/agent")
@RequiredArgsConstructor
public class AgentController {

    private final AgentOrchestratorService agentOrchestratorService;
    private final AgentConversationRepository conversationRepository;

    @PostMapping("/chat")
    public ResponseEntity<AgentChatResponse> chat(@Valid @RequestBody AgentChatRequest request) {
        return ResponseEntity.ok(agentOrchestratorService.chat(request));
    }

    @GetMapping("/tools")
    public ResponseEntity<List<Map<String, String>>> listTools() {
        return ResponseEntity.ok(List.of(
                Map.of("id", "crawler", "name", "Crawler", "description", "Fetch external papers and websites"),
                Map.of("id", "analysis", "name", "Analysis", "description", "Ingest graph data and build analytics"),
                Map.of("id", "rag", "name", "RAG", "description", "Use vector search to craft grounded answers")
        ));
    }

    @GetMapping("/conversations")
    public ResponseEntity<List<AgentConversationSummary>> listConversations() {
        List<AgentConversation> conversations = conversationRepository.findAll(Sort.by(Sort.Direction.DESC, "updatedAt"));
        List<AgentConversationSummary> response = conversations.stream()
                .map(conv -> new AgentConversationSummary(
                        conv.getId(),
                        conv.getTitle(),
                        conv.getUpdatedAt()
                ))
                .toList();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/conversations/{conversationId}")
    public ResponseEntity<AgentConversationDetail> getConversation(@PathVariable String conversationId) {
        return conversationRepository.findById(conversationId)
                .map(conv -> AgentConversationDetail.builder()
                        .id(conv.getId())
                        .title(conv.getTitle())
                        .history(conv.getHistory())
                        .enabledTools(conv.getEnabledTools())
                        .build())
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}


