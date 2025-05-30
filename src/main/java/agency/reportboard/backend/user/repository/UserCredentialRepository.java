package agency.reportboard.backend.user.repository;

import agency.reportboard.backend.user.domain.UserCredential;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserCredentialRepository extends JpaRepository<UserCredential, Long> {
    
    Optional<UserCredential> findByCredentialId(String credentialId);
}
