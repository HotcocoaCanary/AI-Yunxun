package yunxun.ai.canary.project.service.llm;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Objects;

public class NoopLlmClient implements LlmClient {

    private final String message;

    public NoopLlmClient(String message) {
        this.message = Objects.requireNonNullElse(message, "LLM not configured");
    }

    @Override
    public Mono<String> complete(List<LlmMessage> messages) {
        return Mono.just(message);
    }

    @Override
    public Flux<String> stream(List<LlmMessage> messages) {
        return Flux.just(message);
    }
}

