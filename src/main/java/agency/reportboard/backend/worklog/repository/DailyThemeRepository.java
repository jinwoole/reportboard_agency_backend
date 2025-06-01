package agency.reportboard.backend.worklog.repository;

import agency.reportboard.backend.worklog.domain.DailyTheme;
import agency.reportboard.backend.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface DailyThemeRepository extends JpaRepository<DailyTheme, Long> {
    
    Optional<DailyTheme> findByUserAndDate(User user, LocalDate date);
    
    List<DailyTheme> findByUserAndDateBetweenOrderByDateDesc(User user, LocalDate startDate, LocalDate endDate);
    
    List<DailyTheme> findByUserOrderByDateDesc(User user);
    
    boolean existsByUserAndDate(User user, LocalDate date);
}
