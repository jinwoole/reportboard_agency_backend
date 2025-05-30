package agency.reportboard.backend.user.controller;

import agency.reportboard.backend.user.security.UserPrincipal;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class ProtectedController {
    
    @GetMapping("/protected")
    public ResponseEntity<?> getProtectedResource() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        if (auth != null && auth.isAuthenticated() && auth.getPrincipal() instanceof UserPrincipal) {
            UserPrincipal principal = (UserPrincipal) auth.getPrincipal();
            return ResponseEntity.ok(Map.of(
                "message", "보호된 리소스에 성공적으로 접근했습니다!",
                "user", principal.getUsername(),
                "displayName", principal.getDisplayName()
            ));
        }
        
        return ResponseEntity.status(401).body(Map.of("error", "인증이 필요합니다"));
    }
    
    @GetMapping("/public")
    public ResponseEntity<?> getPublicResource() {
        return ResponseEntity.ok(Map.of("message", "누구나 접근 가능한 공개 리소스입니다"));
    }
}
