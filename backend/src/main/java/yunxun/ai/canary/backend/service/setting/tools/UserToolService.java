package yunxun.ai.canary.backend.service.setting.tools;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import yunxun.ai.canary.backend.model.dto.tools.McpToolStatusDto;
import yunxun.ai.canary.backend.model.entity.toolstate.UserToolDoc;
import yunxun.ai.canary.backend.repository.toolstate.UserToolRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserToolService {

    private final UserToolRepository repository;

    private static final List<McpToolStatusDto> BUILTIN = List.of(
            McpToolStatusDto.builder().name("web_search").displayName("Web Search").enabled(true).description("Search the web").build(),
            McpToolStatusDto.builder().name("web_crawl").displayName("Web Crawl").enabled(true).description("Crawl url").build(),
            McpToolStatusDto.builder().name("kg_query").displayName("Graph Query").enabled(true).description("Query graph").build(),
            McpToolStatusDto.builder().name("rag_answer").displayName("RAG Answer").enabled(true).description("RAG answer").build(),
            McpToolStatusDto.builder().name("llm_answer").displayName("LLM Answer").enabled(true).description("LLM direct answer").build()
    );

    public List<McpToolStatusDto> listTools(Long userId) {
        UserToolDoc doc = repository.findByUserId(userId).orElseGet(() -> repository.findByUserId(null).orElse(null));
        if (doc == null) {
            return BUILTIN;
        }
        return doc.getTools().stream()
                .map(t -> McpToolStatusDto.builder()
                        .name(t.getName())
                        .displayName(Optional.ofNullable(t.getDisplayName()).orElse(t.getName()))
                        .description(t.getDescription())
                        .enabled(t.isEnabled())
                        .build())
                .collect(Collectors.toList());
    }

    public void setEnabled(Long userId, String name, boolean enabled) {
        UserToolDoc doc = repository.findByUserId(userId).orElseGet(() -> {
            UserToolDoc created = UserToolDoc.builder()
                    .userId(userId)
                    .tools(new ArrayList<>())
                    .build();
            return repository.save(created);
        });
        List<UserToolDoc.ToolState> tools = new ArrayList<>(doc.getTools());
        boolean updated = false;
        for (int i = 0; i < tools.size(); i++) {
            UserToolDoc.ToolState state = tools.get(i);
            if (state.getName().equals(name)) {
                tools.set(i, UserToolDoc.ToolState.builder()
                        .name(name)
                        .enabled(enabled)
                        .displayName(state.getDisplayName())
                        .description(state.getDescription())
                        .build());
                updated = true;
                break;
            }
        }
        if (!updated) {
            tools.add(UserToolDoc.ToolState.builder().name(name).enabled(enabled).build());
        }
        doc.setTools(tools);
        repository.save(doc);
    }

    /**
     * Retrieve enabled tool names for current user (fallback to global).
     */
    public List<String> enabledToolNames(Long userId) {
        return listTools(userId).stream()
                .filter(McpToolStatusDto::isEnabled)
                .map(McpToolStatusDto::getName)
                .toList();
    }
}
