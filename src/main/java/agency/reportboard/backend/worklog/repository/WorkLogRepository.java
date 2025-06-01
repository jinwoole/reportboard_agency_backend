package agency.reportboard.backend.worklog.repository;

import agency.reportboard.backend.worklog.domain.WorkLog;
import agency.reportboard.backend.worklog.domain.WorkCategory;
import agency.reportboard.backend.worklog.domain.ImportanceLevel;
import agency.reportboard.backend.user.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface WorkLogRepository extends JpaRepository<WorkLog, Long> {
    
    Page<WorkLog> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);
    
    Page<WorkLog> findByUserAndCategoryOrderByCreatedAtDesc(User user, WorkCategory category, Pageable pageable);
    
    Page<WorkLog> findByUserAndImportanceOrderByCreatedAtDesc(User user, ImportanceLevel importance, Pageable pageable);
    
    Page<WorkLog> findByUserAndCreatedAtBetweenOrderByCreatedAtDesc(
        User user, 
        LocalDateTime startDate, 
        LocalDateTime endDate, 
        Pageable pageable
    );
    
    @Query("SELECT w FROM WorkLog w WHERE w.user = :user AND " +
           "LOWER(w.content) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "ORDER BY w.createdAt DESC")
    Page<WorkLog> findByUserAndContentContainingIgnoreCase(
        @Param("user") User user, 
        @Param("keyword") String keyword, 
        Pageable pageable
    );
    
    @Query("SELECT w FROM WorkLog w WHERE w.user = :user " +
           "AND (:categories IS NULL OR w.category IN :categories) " +
           "AND (:importanceLevels IS NULL OR w.importance IN :importanceLevels) " +
           "AND (:keyword IS NULL OR LOWER(w.content) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "AND (:startDate IS NULL OR w.createdAt >= :startDate) " +
           "AND (:endDate IS NULL OR w.createdAt <= :endDate) " +
           "ORDER BY w.createdAt DESC")
    Page<WorkLog> findBySearchCriteria(
        @Param("user") User user,
        @Param("categories") List<WorkCategory> categories,
        @Param("importanceLevels") List<ImportanceLevel> importanceLevels,
        @Param("keyword") String keyword,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate,
        Pageable pageable
    );
    
    List<WorkLog> findByUserAndCreatedAtBetween(User user, LocalDateTime startDate, LocalDateTime endDate);
    
    long countByUser(User user);
    
    long countByUserAndCategory(User user, WorkCategory category);
    
    long countByUserAndImportance(User user, ImportanceLevel importance);
}
