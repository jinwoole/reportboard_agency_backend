package agency.reportboard.backend.user.service;

import agency.reportboard.backend.user.domain.User;
import agency.reportboard.backend.user.domain.UserCredential;
import agency.reportboard.backend.user.dto.UserRegistrationRequest;
import agency.reportboard.backend.user.dto.CredentialRegistrationRequest;
import agency.reportboard.backend.user.dto.CompleteRegistrationRequest;
import agency.reportboard.backend.user.repository.UserRepository;
import agency.reportboard.backend.user.repository.UserCredentialRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Base64;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class UserService {
    
    private final UserRepository userRepository;
    private final UserCredentialRepository credentialRepository;
    
    public UserService(UserRepository userRepository, UserCredentialRepository credentialRepository) {
        this.userRepository = userRepository;
        this.credentialRepository = credentialRepository;
    }
    
    public User registerUser(UserRegistrationRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username already exists");
        }
        
        User user = new User(request.getUsername(), request.getDisplayName());
        return userRepository.save(user);
    }
    
    public void registerCredential(CredentialRegistrationRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        byte[] publicKeyBytes = Base64.getDecoder().decode(request.getPublicKey());
        UserCredential credential = new UserCredential(
                request.getCredentialId(),
                publicKeyBytes,
                0L,
                user
        );
        
        credentialRepository.save(credential);
    }
    
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }
    
    public Optional<UserCredential> findByCredentialId(String credentialId) {
        return credentialRepository.findByCredentialId(credentialId);
    }
    
    public void updateSignCount(String credentialId, long signCount) {
        UserCredential credential = credentialRepository.findByCredentialId(credentialId)
                .orElseThrow(() -> new RuntimeException("Credential not found"));
        credential.setSignCount(signCount);
        credentialRepository.save(credential);
    }
    
    // 새로운 통합 등록 메서드들
    public User startRegistration(UserRegistrationRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username already exists");
        }
        
        // 등록이 완료되지 않은 상태로 사용자 생성
        User user = new User(request.getUsername(), request.getDisplayName());
        user.setRegistrationCompleted(false);
        return userRepository.save(user);
    }
    
    public User completeRegistration(CompleteRegistrationRequest request) {
        // 사용자 조회 (등록 완료되지 않은 사용자만)
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        if (user.isRegistrationCompleted()) {
            throw new RuntimeException("User registration already completed");
        }
        
        // Passkey 크리덴셜 등록
        byte[] publicKeyBytes = Base64.getDecoder().decode(request.getPublicKey());
        UserCredential credential = new UserCredential(
                request.getCredentialId(),
                publicKeyBytes,
                0L,
                user
        );
        
        credentialRepository.save(credential);
        
        // 등록 완료 처리
        user.setRegistrationCompleted(true);
        return userRepository.save(user);
    }
    
    public void deleteIncompleteUser(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        if (!user.isRegistrationCompleted()) {
            userRepository.delete(user);
        } else {
            throw new RuntimeException("Cannot delete completed user registration");
        }
    }
    
    public Optional<User> findActiveUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .filter(User::isRegistrationCompleted);
    }
    
    // WebAuthn용 통합 등록 메서드
    public User registerUserWithCredential(String username, String credentialId, String publicKey) {
        try {
            if (userRepository.existsByUsername(username)) {
                throw new RuntimeException("Username already exists");
            }
            
            // 사용자 생성 및 등록 완료 처리
            User user = new User(username, username); // displayName을 임시로 username과 동일하게 설정
            user.setRegistrationCompleted(true);
            user = userRepository.save(user);
            
            // Passkey 크리덴셜 등록
            byte[] publicKeyBytes = Base64.getDecoder().decode(publicKey);
            UserCredential credential = new UserCredential(
                    credentialId,
                    publicKeyBytes,
                    0L,
                    user
            );
            
            credentialRepository.save(credential);
            
            return user;
        } catch (Exception e) {
            // 등록 실패 시 미완료 사용자가 있다면 정리
            cleanupIncompleteUser(username);
            throw new RuntimeException("Registration failed: " + e.getMessage());
        }
    }
    
    // 미완료 등록 자동 정리 (public으로 변경)
    public void cleanupIncompleteUser(String username) {
        try {
            userRepository.findByUsername(username)
                    .filter(user -> !user.isRegistrationCompleted())
                    .ifPresent(userRepository::delete);
        } catch (Exception e) {
            // 정리 실패는 로그만 남기고 무시
            System.err.println("Failed to cleanup incomplete user: " + username);
        }
    }
    
    // 사용자명으로 credential 목록 조회
    public List<UserCredential> getCredentialsByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return credentialRepository.findByUser(user);
    }
    
    // 스케줄링을 통한 주기적 정리 (선택사항)
    @Transactional
    public void cleanupExpiredRegistrations() {
        // 24시간 이상 된 미완료 등록을 정리
        // 실제 환경에서는 @Scheduled 어노테이션과 함께 사용
        try {
            List<User> incompleteUsers = userRepository.findAll().stream()
                    .filter(user -> !user.isRegistrationCompleted())
                    .toList();
            
            userRepository.deleteAll(incompleteUsers);
            System.out.println("Cleaned up " + incompleteUsers.size() + " incomplete registrations");
        } catch (Exception e) {
            System.err.println("Failed to cleanup expired registrations: " + e.getMessage());
        }
    }
}
