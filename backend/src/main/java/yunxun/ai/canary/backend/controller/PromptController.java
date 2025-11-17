package yunxun.ai.canary.backend.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import yunxun.ai.canary.backend.service.prompt.PromptRegistry;

@RestController
@RequestMapping("/api/prompts")
@RequiredArgsConstructor
public class PromptController {

    private final PromptRegistry promptRegistry;

    @GetMapping
    public ResponseEntity<Object> list() {
        return ResponseEntity.ok(promptRegistry.getPrompts().values());
    }
}
