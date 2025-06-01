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
    
    // Use native query to avoid enum conversion issues
    @Query(value = "SELECT * FROM work_logs w WHERE w.user_id = :userId AND " +
                   "LOWER(w.content) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
                   "ORDER BY w.created_at DESC",
           nativeQuery = true)
    Page<WorkLog> findByUserAndContentContainingIgnoreCase(
        @Param("userId") Long userId, 
        @Param("keyword") String keyword, 
        Pageable pageable
    );
    
    // Separate methods for better enum handling
    @Query("SELECT w FROM WorkLog w WHERE w.user = :user " +
           "AND w.category IN :categories " +
           "ORDER BY w.createdAt DESC")
    Page<WorkLog> findByUserAndCategoriesIn(
        @Param("user") User user,
        @Param("categories") List<WorkCategory> categories,
        Pageable pageable
    );
    
    @Query("SELECT w FROM WorkLog w WHERE w.user = :user " +
           "AND w.importance IN :importanceLevels " +
           "ORDER BY w.createdAt DESC")
    Page<WorkLog> findByUserAndImportanceLevelsIn(
        @Param("user") User user,
        @Param("importanceLevels") List<ImportanceLevel> importanceLevels,
        Pageable pageable
    );
    
    List<WorkLog> findByUserAndCreatedAtBetween(User user, LocalDateTime startDate, LocalDateTime endDate);
    
    long countByUser(User user);
    
    long countByUserAndCategory(User user, WorkCategory category);
    
    long countByUserAndImportance(User user, ImportanceLevel importance);
}
