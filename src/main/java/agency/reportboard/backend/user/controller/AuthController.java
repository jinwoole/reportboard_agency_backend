package agency.reportboard.backend.user.controller;

import agency.reportboard.backend.user.dto.UserRegistrationRequest;
import agency.reportboard.backend.user.dto.CredentialRegistrationRequest;
import agency.reportboard.backend.user.security.UserPrincipal;
import agency.reportboard.backend.user.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    
    private final UserService userService;
    
    public AuthController(UserService userService) {
        this.userService = userService;
    }
    
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody UserRegistrationRequest request) {
        try {
            userService.registerUser(request);
            return ResponseEntity.ok(Map.of("message", "User registered successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    @PostMapping("/register-credential")
    public ResponseEntity<?> registerCredential(@RequestBody CredentialRegistrationRequest request) {
        try {
            userService.registerCredential(request);
            return ResponseEntity.ok(Map.of("message", "Credential registered successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> request) {
        try {
            String credentialId = request.get("credentialId");
            String signature = request.get("signature");
            
            if (credentialId == null || signature == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Missing credentials"));
            }
            
            // 간단한 인증 처리
            var credential = userService.findByCredentialId(credentialId);
            if (credential.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Invalid credentials"));
            }
            
            var user = credential.get().getUser();
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Login successful");
            response.put("username", user.getUsername());
            response.put("displayName", user.getDisplayName());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Authentication failed"));
        }
    }
    
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof UserPrincipal) {
            UserPrincipal principal = (UserPrincipal) auth.getPrincipal();
            Map<String, Object> response = new HashMap<>();
            response.put("username", principal.getUsername());
            response.put("displayName", principal.getDisplayName());
            return ResponseEntity.ok(response);
        }
        return ResponseEntity.status(401).build();
    }
}
