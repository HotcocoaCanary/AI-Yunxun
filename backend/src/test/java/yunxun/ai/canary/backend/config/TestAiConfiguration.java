package yunxun.ai.canary.backend.config;

import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.Filter;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@TestConfiguration
public class TestAiConfiguration {

    @Bean
    @Primary
    public ChatModel stubChatModel() {
        return new ChatModel() {
            @Override
            public ChatResponse call(Prompt prompt) {
                Generation generation = new Generation(new AssistantMessage("test-response"));
                return new ChatResponse(List.of(generation));
            }

            @Override
            public Flux<ChatResponse> stream(Prompt prompt) {
                return Flux.just(call(prompt));
            }
        };
    }

    @Bean
    @Primary
    public VectorStore stubVectorStore() {
        return new InMemoryVectorStore();
    }

    private static class InMemoryVectorStore implements VectorStore {

        private final Map<String, Document> documents = new ConcurrentHashMap<>();

        @Override
        public void add(List<Document> docs) {
            for (Document document : docs) {
                String id = document.getId();
                if (id == null) {
                    id = UUID.randomUUID().toString();
                }
                documents.put(id, document);
            }
        }

        @Override
        public void delete(List<String> ids) {
            if (ids == null) {
                return;
            }
            ids.forEach(documents::remove);
        }

        @Override
        public void delete(Filter.Expression expression) {
            documents.clear();
        }

        @Override
        public List<Document> similaritySearch(SearchRequest request) {
            return new ArrayList<>(documents.values());
        }
    }
}
