package agency.reportboard.backend.memo.repository;

import agency.reportboard.backend.memo.domain.Memo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemoRepository extends JpaRepository<Memo, Long> {
}
