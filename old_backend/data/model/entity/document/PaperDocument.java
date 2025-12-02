package yunxun.ai.canary.backend.data.model.entity.document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "paper_documents")
public class PaperDocument {

    @Id
    private String id;

    private String title;
    private String summary;
    private List<String> authors;
    private String source;
    private String url;
    private String vectorId;
    private Map<String, Object> metadata;
    private String rawContent;
    private LocalDate publishedDate;

    @CreatedDate
    private Instant createdAt;
}
