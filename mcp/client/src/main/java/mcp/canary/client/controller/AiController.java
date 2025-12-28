package mcp.canary.client.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AiController {
    @GetMapping("/chat")
    public String chat(String msg) {
        return "hello, " + msg;
    }
}