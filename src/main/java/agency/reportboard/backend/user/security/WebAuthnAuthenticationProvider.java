package agency.reportboard.backend.user.security;

import agency.reportboard.backend.user.domain.UserCredential;
import agency.reportboard.backend.user.service.UserService;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class WebAuthnAuthenticationProvider implements AuthenticationProvider {
    
    private final UserService userService;
    private final CustomUserDetailsService userDetailsService;
    
    public WebAuthnAuthenticationProvider(UserService userService, CustomUserDetailsService userDetailsService) {
        this.userService = userService;
        this.userDetailsService = userDetailsService;
    }
    
    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        WebAuthnAuthenticationToken token = (WebAuthnAuthenticationToken) authentication;
        
        try {
            // 크리덴셜 조회
            Optional<UserCredential> credentialOpt = userService.findByCredentialId(token.getCredentialId());
            if (credentialOpt.isEmpty()) {
                throw new BadCredentialsException("Credential not found");
            }
            
            UserCredential credential = credentialOpt.get();
            
            // 간단한 서명 검증 (실제 구현에서는 더 정교한 검증이 필요)
            if (!validateSignature(token, credential)) {
                throw new BadCredentialsException("Invalid signature");
            }
            
            // 사용자 인증 정보 로드
            UserDetails userDetails = userDetailsService.loadUserByUsername(credential.getUser().getUsername());
            
            return new WebAuthnAuthenticationToken(
                    userDetails,
                    token.getCredentialId(),
                    token.getSignature(),
                    userDetails.getAuthorities()
            );
            
        } catch (Exception e) {
            throw new BadCredentialsException("Authentication failed", e);
        }
    }
    
    private boolean validateSignature(WebAuthnAuthenticationToken token, UserCredential credential) {
        // 간단한 검증 로직 (테스트용)
        // 실제로는 WebAuthn 표준에 따른 완전한 검증이 필요
        return token.getSignature() != null && token.getSignature().length > 0;
    }
    
    @Override
    public boolean supports(Class<?> authentication) {
        return WebAuthnAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
