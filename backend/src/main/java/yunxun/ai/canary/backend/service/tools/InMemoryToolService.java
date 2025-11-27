package yunxun.ai.canary.backend.service.tools;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;
import yunxun.ai.canary.backend.model.dto.tools.McpToolStatusDto;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class InMemoryToolService {

    private final Map<String, McpToolStatusDto> registry = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        registry.put("web_search", McpToolStatusDto.builder()
                .name("web_search")
                .displayName("Web Search")
                .enabled(true)
                .description("在线检索最新数据")
                .tags(List.of("search", "sync"))
                .build());
        registry.put("web_crawl", McpToolStatusDto.builder()
                .name("web_crawl")
                .displayName("Web Crawl")
                .enabled(true)
                .description("爬取指定站点或 URL")
                .tags(List.of("crawler"))
                .build());
        registry.put("kg_query", McpToolStatusDto.builder()
                .name("kg_query")
                .displayName("图谱查询")
                .enabled(true)
                .description("从 Neo4j 读取子图")
                .tags(List.of("neo4j"))
                .build());
        registry.put("rag_answer", McpToolStatusDto.builder()
                .name("rag_answer")
                .displayName("RAG 答复")
                .enabled(true)
                .description("结合向量搜索生成答案")
                .tags(List.of("rag"))
                .build());
    }

    public List<McpToolStatusDto> listTools() {
        return new ArrayList<>(registry.values());
    }

    public void toggleTool(String name, boolean enabled) {
        McpToolStatusDto tool = registry.get(name);
        if (tool != null) {
            tool.setEnabled(enabled);
        }
    }
}
