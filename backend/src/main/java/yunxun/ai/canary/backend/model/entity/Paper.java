package yunxun.ai.canary.backend.model.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 论文实体 - 存储在MongoDB
 */
@Document(collection = "papers")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Paper {
    
    @Id
    private String id;
    
    @Indexed
    private String title;
    
    @Indexed
    private String arxivId;
    
    private String doi;
    
    private List<String> authors;
    
    private String abstractText;
    
    private String fullText;
    
    private List<String> keywords;
    
    private List<String> categories;
    
    private String source; // arxiv, cnki, google_scholar
    
    private String pdfUrl;
    
    private LocalDateTime publishedDate;
    
    private LocalDateTime crawledAt;
    
    private String language;
    
    private Integer citationCount;
    
    private List<String> references;
    
    private String venue; // 会议或期刊名称
    
    private String volume;
    
    private String issue;
    
    private String pages;
    
    // 向量化相关
    private List<Double> embedding;
    
    // 处理状态
    private ProcessingStatus processingStatus = ProcessingStatus.PENDING;
    
    private LocalDateTime processedAt;
    
    public enum ProcessingStatus {
        PENDING,    // 待处理
        PROCESSING, // 处理中
        COMPLETED,  // 已完成
        FAILED      // 处理失败
    }
}
