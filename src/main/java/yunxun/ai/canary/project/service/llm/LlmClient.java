package yunxun.ai.canary.project.service.llm;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public interface LlmClient {

    Mono<String> complete(List<LlmMessage> messages);

    Flux<String> stream(List<LlmMessage> messages);
}

