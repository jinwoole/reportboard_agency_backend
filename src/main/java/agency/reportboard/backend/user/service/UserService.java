package agency.reportboard.backend.user.service;

import agency.reportboard.backend.user.domain.User;
import agency.reportboard.backend.user.domain.UserCredential;
import agency.reportboard.backend.user.dto.UserRegistrationRequest;
import agency.reportboard.backend.user.dto.CredentialRegistrationRequest;
import agency.reportboard.backend.user.repository.UserRepository;
import agency.reportboard.backend.user.repository.UserCredentialRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Base64;
import java.util.Optional;

@Service
@Transactional
public class UserService {
    
    private final UserRepository userRepository;
    private final UserCredentialRepository credentialRepository;
    
    @Autowired
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
}
