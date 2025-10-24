package yunxun.ai.canary.backend.model.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import yunxun.ai.canary.backend.model.entity.Paper;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 论文数据传输对象
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaperDTO {
    
    private String id;
    private String title;
    private String arxivId;
    private String doi;
    private List<String> authors;
    private String abstractText;
    private List<String> keywords;
    private List<String> categories;
    private String source;
    private String pdfUrl;
    private LocalDateTime publishedDate;
    private LocalDateTime crawledAt;
    private String language;
    private Integer citationCount;
    private String venue;
    private String volume;
    private String issue;
    private String pages;
    private String processingStatus;
    private LocalDateTime processedAt;
    
    /**
     * 从实体转换为DTO
     */
    public static PaperDTO fromEntity(Paper paper) {
        return PaperDTO.builder()
                .id(paper.getId())
                .title(paper.getTitle())
                .arxivId(paper.getArxivId())
                .doi(paper.getDoi())
                .authors(paper.getAuthors())
                .abstractText(paper.getAbstractText())
                .keywords(paper.getKeywords())
                .categories(paper.getCategories())
                .source(paper.getSource())
                .pdfUrl(paper.getPdfUrl())
                .publishedDate(paper.getPublishedDate())
                .crawledAt(paper.getCrawledAt())
                .language(paper.getLanguage())
                .citationCount(paper.getCitationCount())
                .venue(paper.getVenue())
                .volume(paper.getVolume())
                .issue(paper.getIssue())
                .pages(paper.getPages())
                .processingStatus(paper.getProcessingStatus() != null ? paper.getProcessingStatus().name() : null)
                .processedAt(paper.getProcessedAt())
                .build();
    }
}
