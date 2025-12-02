package yunxun.ai.canary.backend.setting.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import yunxun.ai.canary.backend.setting.model.entity.UserToolDoc;

import java.util.Optional;

public interface UserToolRepository extends MongoRepository<UserToolDoc, String> {

    Optional<UserToolDoc> findByUserId(Long userId);
}
