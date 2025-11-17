package yunxun.ai.canary.backend.model.dto.crawler;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CrawlTaskRequest {
    private String query;
    private String source; // arxiv / cnki / custom
    private List<String> urls;
    private int maxResults;
}
