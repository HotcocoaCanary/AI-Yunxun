package yunxun.ai.canary.backend.data.service.connectors;

import org.springframework.stereotype.Service;
import yunxun.ai.canary.backend.data.model.dto.crawler.CrawlResult;
import yunxun.ai.canary.backend.data.model.dto.crawler.CrawlTaskRequest;
import yunxun.ai.canary.backend.qa.service.llm.RagPipelineService;

@Service
public class PaperStorageService {

    private final RagPipelineService ragPipelineService;

    public PaperStorageService(RagPipelineService ragPipelineService) {
        this.ragPipelineService = ragPipelineService;
    }

    public CrawlResult store(CrawlTaskRequest request) {
        // placeholder: process and index content
        String answer = ragPipelineService.answer(String.join(" ", request.getUrls()));
        return CrawlResult.builder()
                .summary(answer)
                .build();
    }
}
