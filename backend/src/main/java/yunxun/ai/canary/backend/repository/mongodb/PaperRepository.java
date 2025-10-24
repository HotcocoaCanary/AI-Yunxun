package yunxun.ai.canary.backend.repository.mongodb;

import yunxun.ai.canary.backend.model.entity.Paper;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 论文数据访问层
 */
@Repository
public interface PaperRepository extends MongoRepository<Paper, String> {
    
    Optional<Paper> findByArxivId(String arxivId);
    
    List<Paper> findBySource(String source);
    
    List<Paper> findByProcessingStatus(Paper.ProcessingStatus status);
    
    @Query("{ 'title': { $regex: ?0, $options: 'i' } }")
    List<Paper> findByTitleContaining(String title);
    
    @Query("{ 'authors': { $in: ?0 } }")
    List<Paper> findByAuthorsIn(List<String> authors);
    
    @Query("{ 'keywords': { $in: ?0 } }")
    List<Paper> findByKeywordsIn(List<String> keywords);
    
    @Query("{ 'categories': { $in: ?0 } }")
    List<Paper> findByCategoriesIn(List<String> categories);
    
    @Query("{ 'abstractText': { $regex: ?0, $options: 'i' } }")
    List<Paper> findByAbstractContaining(String abstractText);
}
