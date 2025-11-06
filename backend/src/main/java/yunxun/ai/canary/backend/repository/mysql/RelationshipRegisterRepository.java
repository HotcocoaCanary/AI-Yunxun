package yunxun.ai.canary.backend.repository.mysql;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import yunxun.ai.canary.backend.model.entity.mysql.RelationshipRegister;

import java.util.Optional;

@Repository
public interface RelationshipRegisterRepository extends JpaRepository<RelationshipRegister, Long> {

    Optional<RelationshipRegister> findByLabelAndDeletedFalse(String label);

    @Transactional
    @Modifying
    @Query("update RelationshipRegister r set r.count = :count where r.label = :label and r.deleted = false")
    int updateCountByLabel(String label, Long count);

    @Transactional
    @Modifying
    @Query("update RelationshipRegister r set r.deleted = true where r.label = :label and r.deleted = false")
    int softDeleteByLabel(String label);
}
