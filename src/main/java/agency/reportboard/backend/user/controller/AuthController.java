package agency.reportboard.backend.user.controller;

import agency.reportboard.backend.common.dto.ApiResponse;
import agency.reportboard.backend.user.security.UserPrincipal;
import agency.reportboard.backend.user.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    
    private final UserService userService;
    private final SecureRandom random = new SecureRandom();
    
    // 임시로 챌린지를 저장할 메모리 캐시 (실제 환경에서는 Redis 등 사용)
    private final Map<String, ChallengeData> challengeStore = new ConcurrentHashMap<>();
    
    // 챌린지 만료 시간 (10분)
    private static final long CHALLENGE_EXPIRY_MS = 10 * 60 * 1000L;
    
    public AuthController(UserService userService) {
        this.userService = userService;
        
        // 만료된 챌린지 정리 작업 (5분마다 실행)
        scheduleCleanupTask();
    }
    
    // 챌린지 데이터 클래스
    private static class ChallengeData {
        final String challenge;
        final long timestamp;
        
        ChallengeData(String challenge) {
            this.challenge = challenge;
            this.timestamp = System.currentTimeMillis();
        }
        
        String getChallenge() {
            return challenge;
        }
        
        boolean isExpired() {
            return System.currentTimeMillis() - timestamp > CHALLENGE_EXPIRY_MS;
        }
    }
    
    // 만료된 챌린지 정리 스케줄러
    private void scheduleCleanupTask() {
        Thread cleanupThread = new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(5 * 60 * 1000L); // 5분마다 실행
                    challengeStore.entrySet().removeIf(entry -> entry.getValue().isExpired());
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        });
        cleanupThread.setDaemon(true);
        cleanupThread.start();
    }
    
    // Base64URL 인코딩 유틸리티
    private String base64UrlEncode(byte[] data) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(data);
    }
    
    // WebAuthn 등록 옵션 생성
    @PostMapping("/register-options")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getRegisterOptions(@RequestBody Map<String, String> request) {
        try {
            String username = request.get("username");
            String displayName = request.get("displayName");
            String email = request.get("email");
            
            if (username == null || displayName == null || email == null) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Missing required fields"));
            }
            
            // 사용자 중복 확인
            if (userService.findByUsername(username).isPresent()) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Username already exists"));
            }
            
            // 챌린지 생성
            byte[] challenge = new byte[32];
            random.nextBytes(challenge);
            String challengeStr = base64UrlEncode(challenge);
            
            // 사용자 ID 생성
            byte[] userId = username.getBytes(StandardCharsets.UTF_8);
            String userIdStr = base64UrlEncode(userId);
            
            // 챌린지 저장 (10분 후 만료)
            challengeStore.put(username + "_register", new ChallengeData(challengeStr));
            
            Map<String, Object> options = new HashMap<>();
            options.put("rp", Map.of(
                "name", "Report Board",
                "id", "localhost"
            ));
            options.put("user", Map.of(
                "id", userIdStr,
                "name", username,
                "displayName", displayName
            ));
            options.put("challenge", challengeStr);
            options.put("pubKeyCredParams", List.of(
                Map.of("type", "public-key", "alg", -7), // ES256
                Map.of("type", "public-key", "alg", -257) // RS256
            ));
            options.put("authenticatorSelection", Map.of(
                "authenticatorAttachment", "platform",
                "userVerification", "preferred",
                "residentKey", "preferred"
            ));
            options.put("attestation", "direct");
            options.put("timeout", 60000);
            
            return ResponseEntity.ok(ApiResponse.success(options));
            
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to generate registration options"));
        }
    }
    
    // WebAuthn 로그인 옵션 생성
    @PostMapping("/login-options")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getLoginOptions(@RequestBody Map<String, String> request) {
        try {
            String username = request.get("username");
            
            // 챌린지 생성
            byte[] challenge = new byte[32];
            random.nextBytes(challenge);
            String challengeStr = base64UrlEncode(challenge);
            
            // 챌린지 저장
            String challengeKey = username != null ? username + "_login" : "anonymous_login";
            challengeStore.put(challengeKey, new ChallengeData(challengeStr));
            
            Map<String, Object> options = new HashMap<>();
            options.put("challenge", challengeStr);
            options.put("timeout", 60000);
            options.put("rpId", "localhost");
            options.put("userVerification", "preferred");
            
            // 특정 사용자의 credential만 허용하거나 모든 credential 허용
            if (username != null && !username.trim().isEmpty()) {
                var credentials = userService.getCredentialsByUsername(username);
                if (!credentials.isEmpty()) {
                    List<Map<String, Object>> allowCredentials = new ArrayList<>();
                    for (var cred : credentials) {
                        Map<String, Object> credentialDescriptor = new HashMap<>();
                        credentialDescriptor.put("type", "public-key");
                        credentialDescriptor.put("id", base64UrlEncode(cred.getCredentialId().getBytes()));
                        allowCredentials.add(credentialDescriptor);
                    }
                    options.put("allowCredentials", allowCredentials);
                }
            }
            
            return ResponseEntity.ok(ApiResponse.success(options));
            
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to generate login options"));
        }
    }
    
    
    // WebAuthn 등록 처리
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<Map<String, Object>>> register(@RequestBody Map<String, String> request) {
        try {
            String username = request.get("username");
            String credentialId = request.get("credentialId");
            String publicKey = request.get("publicKey");
            // attestationObject와 clientDataJSON은 실제 WebAuthn 검증에서 사용됨
            // String attestationObject = request.get("attestationObject");
            // String clientDataJSON = request.get("clientDataJSON");
            
            if (username == null || credentialId == null || publicKey == null) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Missing required fields"));
            }
            
            // 저장된 챌린지 확인
            ChallengeData challengeData = challengeStore.remove(username + "_register");
            if (challengeData == null || challengeData.isExpired()) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Invalid or expired challenge"));
            }
            
            // 실제 환경에서는 여기서 attestation 검증을 수행해야 함
            // clientDataJSON에서 challenge를 추출하여 challengeData.getChallenge()와 비교
            String expectedChallenge = challengeData.getChallenge();
            // 현재는 간단히 credential 정보만 저장 (실제로는 attestation 검증 필요)
            // TODO: attestationObject와 clientDataJSON을 사용하여 실제 검증 수행
            System.out.println("Expected challenge for verification: " + expectedChallenge);
            
            var user = userService.registerUserWithCredential(username, credentialId, publicKey);
            
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("username", user.getUsername());
            responseData.put("displayName", user.getDisplayName());
            
            return ResponseEntity.ok(ApiResponse.success("Registration successful", responseData));
            
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }
    
    // WebAuthn 로그인 처리
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<Map<String, Object>>> login(@RequestBody Map<String, String> request, 
                                                                 HttpServletRequest httpRequest) {
        try {
            String credentialId = request.get("credentialId");
            // authenticatorData, signature, clientDataJSON은 실제 WebAuthn 검증에서 사용됨
            // String authenticatorData = request.get("authenticatorData");
            String signature = request.get("signature");
            // String clientDataJSON = request.get("clientDataJSON");
            
            if (credentialId == null || signature == null) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Missing credentials"));
            }
            
            // 저장된 챌린지 확인 (간단한 구현)
            // 실제 환경에서는 clientDataJSON에서 challenge를 추출하여 검증
            
            // credential 조회
            var credential = userService.findByCredentialId(credentialId);
            if (credential.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Invalid credentials"));
            }
            
            var user = credential.get().getUser();
            
            // 등록이 완료된 사용자만 로그인 허용
            if (!user.isRegistrationCompleted()) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("User registration not completed"));
            }
            
            // 실제 환경에서는 여기서 서명 검증을 수행해야 함
            // authenticatorData와 clientDataJSON을 사용하여 서명 검증
            
            // Spring Security 세션에 인증 정보 설정
            UserPrincipal userPrincipal = new UserPrincipal(user);
            Authentication authentication = new UsernamePasswordAuthenticationToken(
                userPrincipal, null, userPrincipal.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(authentication);
            
            // 세션에 인증 정보 저장
            HttpSession session = httpRequest.getSession(true);
            session.setAttribute("SPRING_SECURITY_CONTEXT", SecurityContextHolder.getContext());
            
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("username", user.getUsername());
            responseData.put("displayName", user.getDisplayName());
            
            return ResponseEntity.ok(ApiResponse.success("Login successful", responseData));
            
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Authentication failed"));
        }
    }
    
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getCurrentUser() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.getPrincipal() instanceof UserPrincipal) {
                UserPrincipal principal = (UserPrincipal) auth.getPrincipal();
                Map<String, Object> responseData = new HashMap<>();
                responseData.put("username", principal.getUsername());
                responseData.put("displayName", principal.getDisplayName());
                return ResponseEntity.ok(ApiResponse.success(responseData));
            }
            return ResponseEntity.status(401)
                    .body(ApiResponse.error("Not authenticated"));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(ApiResponse.error("Internal server error"));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<String>> logout(HttpServletRequest request) {
        try {
            // 세션 무효화
            HttpSession session = request.getSession(false);
            if (session != null) {
                session.invalidate();
            }
            
            // Security Context 초기화
            SecurityContextHolder.clearContext();
            
            return ResponseEntity.ok(ApiResponse.success("Logout successful"));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(ApiResponse.error("Logout failed"));
        }
    }
}
