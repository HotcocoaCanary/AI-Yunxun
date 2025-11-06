package yunxun.ai.canary.backend.repository.mysql;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import yunxun.ai.canary.backend.model.entity.mysql.NodeRegister;

import java.util.Optional;

@Repository
public interface NodeRegisterRepository extends JpaRepository<NodeRegister, Long> {

    Optional<NodeRegister> findByLabelAndDeletedFalse(String label);

    @Transactional
    @Modifying
    @Query("update NodeRegister n set n.count = :count where n.label = :label and n.deleted = false")
    int updateCountByLabel(String label, Long count);

    @Transactional
    @Modifying
    @Query("update NodeRegister n set n.deleted = true where n.label = :label and n.deleted = false")
    int softDeleteByLabel(String label);
}
