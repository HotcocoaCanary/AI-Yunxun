package yunxun.ai.canary.backend.model.dto.crawler;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CrawlResult {
    private String title;
    private String summary;
    private List<String> authors;
    private String source;
    private String url;
    private Instant publishedAt;
    private String rawContent;
    private Map<String, Object> metadata;
}
