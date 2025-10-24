package yunxun.ai.canary.backend.repository;

import yunxun.ai.canary.backend.model.entity.QueryLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 查询日志数据访问层
 */
@Repository
public interface QueryLogRepository extends JpaRepository<QueryLog, Long> {
    
    List<QueryLog> findByUserId(Long userId);
    
    List<QueryLog> findByUserIdOrderByCreatedAtDesc(Long userId);
    
    List<QueryLog> findBySuccess(Boolean success);
    
    @Query("SELECT q FROM QueryLog q WHERE q.createdAt BETWEEN :startDate AND :endDate")
    List<QueryLog> findByDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT q FROM QueryLog q WHERE q.userId = :userId AND q.createdAt BETWEEN :startDate AND :endDate")
    List<QueryLog> findByUserIdAndDateRange(@Param("userId") Long userId, 
                                           @Param("startDate") LocalDateTime startDate, 
                                           @Param("endDate") LocalDateTime endDate);
}
