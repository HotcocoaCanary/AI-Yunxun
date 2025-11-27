package yunxun.ai.canary.backend.repository.data;

import org.springframework.data.mongodb.repository.MongoRepository;
import yunxun.ai.canary.backend.model.entity.data.DataResourceDoc;

import java.util.List;
import java.util.Optional;

public interface DataResourceRepository extends MongoRepository<DataResourceDoc, String> {

    List<DataResourceDoc> findByOwnerId(Long ownerId);

    List<DataResourceDoc> findByVisibility(String visibility);

    Optional<DataResourceDoc> findByIdAndOwnerId(String id, Long ownerId);
}
