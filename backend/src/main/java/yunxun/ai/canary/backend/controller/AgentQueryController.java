package yunxun.ai.canary.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import yunxun.ai.canary.backend.model.dto.chat.AgentQueryPayloadDto;
import yunxun.ai.canary.backend.service.agent.AgentStreamService;

import java.io.IOException;

@RestController
@RequestMapping("/api/agent")
@RequiredArgsConstructor
public class AgentQueryController {

    private final AgentStreamService agentStreamService;
    private final ObjectMapper objectMapper;

    @GetMapping(value = "/query", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter query(@RequestParam("payload") String payload) throws IOException {
        AgentQueryPayloadDto query = objectMapper.readValue(payload, AgentQueryPayloadDto.class);
        return agentStreamService.streamAnswer(query);
    }
}
