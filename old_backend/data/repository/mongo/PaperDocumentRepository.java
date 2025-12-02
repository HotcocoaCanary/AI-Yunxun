package yunxun.ai.canary.backend.data.repository.mongo;

import org.springframework.data.mongodb.repository.MongoRepository;
import yunxun.ai.canary.backend.data.model.entity.document.PaperDocument;

import java.util.List;
import java.util.Optional;

public interface PaperDocumentRepository extends MongoRepository<PaperDocument, String> {
    Optional<PaperDocument> findByUrl(String url);
    List<PaperDocument> findTop10BySourceOrderByCreatedAtDesc(String source);
}
