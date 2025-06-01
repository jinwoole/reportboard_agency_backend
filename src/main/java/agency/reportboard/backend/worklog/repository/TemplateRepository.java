package agency.reportboard.backend.worklog.repository;

import agency.reportboard.backend.worklog.domain.Template;
import agency.reportboard.backend.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TemplateRepository extends JpaRepository<Template, Long> {
    
    List<Template> findByUserOrderByNameAsc(User user);
    
    boolean existsByUserAndName(User user, String name);
}
