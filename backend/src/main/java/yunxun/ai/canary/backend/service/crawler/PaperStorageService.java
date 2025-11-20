package yunxun.ai.canary.backend.service.crawler;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import yunxun.ai.canary.backend.algo.rag.RagPipelineService;
import yunxun.ai.canary.backend.model.dto.crawler.CrawlResult;
import yunxun.ai.canary.backend.model.entity.document.PaperDocument;
import yunxun.ai.canary.backend.repository.mongo.PaperDocumentRepository;

import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

@Service
public class PaperStorageService {

    private final PaperDocumentRepository paperDocumentRepository;
    private final RagPipelineService ragPipelineService;

    public PaperStorageService(PaperDocumentRepository paperDocumentRepository,
                               @Lazy RagPipelineService ragPipelineService) {
        this.paperDocumentRepository = paperDocumentRepository;
        this.ragPipelineService = ragPipelineService;
    }

    public PaperDocument saveResult(CrawlResult result) {
        PaperDocument document = paperDocumentRepository.findByUrl(result.getUrl())
                .orElseGet(PaperDocument::new);

        document.setTitle(result.getTitle());
        document.setSummary(result.getSummary());
        document.setAuthors(result.getAuthors());
        document.setSource(result.getSource());
        document.setUrl(result.getUrl());
        document.setRawContent(result.getRawContent());
        document.setMetadata(result.getMetadata());
        if (result.getPublishedAt() != null) {
            document.setPublishedDate(result.getPublishedAt().atZone(ZoneOffset.UTC).toLocalDate());
        }

        PaperDocument saved = paperDocumentRepository.save(document);

        ragPipelineService.ingestDocument(
                saved.getId(),
                saved.getTitle(),
                buildContentBlock(saved),
                saved.getMetadata()
        );

        return saved;
    }

    public List<PaperDocument> saveResults(List<CrawlResult> results) {
        List<PaperDocument> documents = new ArrayList<>();
        if (CollectionUtils.isEmpty(results)) {
            return documents;
        }
        for (CrawlResult result : results) {
            documents.add(saveResult(result));
        }
        return documents;
    }

    private String buildContentBlock(PaperDocument document) {
        StringBuilder builder = new StringBuilder();
        builder.append("标题：").append(document.getTitle()).append("\n");
        if (!CollectionUtils.isEmpty(document.getAuthors())) {
            builder.append("作者：").append(String.join("、", document.getAuthors())).append("\n");
        }
        if (document.getSummary() != null) {
            builder.append("摘要：").append(document.getSummary()).append("\n");
        }
        if (document.getRawContent() != null) {
            builder.append("\n原文：").append(document.getRawContent());
        }
        return builder.toString();
    }
}
