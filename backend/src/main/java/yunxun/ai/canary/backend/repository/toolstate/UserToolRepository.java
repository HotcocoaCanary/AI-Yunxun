package yunxun.ai.canary.backend.repository.toolstate;

import org.springframework.data.mongodb.repository.MongoRepository;
import yunxun.ai.canary.backend.model.entity.toolstate.UserToolDoc;

import java.util.Optional;

public interface UserToolRepository extends MongoRepository<UserToolDoc, String> {

    Optional<UserToolDoc> findByUserId(Long userId);
}
