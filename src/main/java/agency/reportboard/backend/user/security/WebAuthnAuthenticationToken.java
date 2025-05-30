package agency.reportboard.backend.user.security;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

public class WebAuthnAuthenticationToken extends AbstractAuthenticationToken {
    
    private final Object principal;
    private final String credentialId;
    private final byte[] signature;
    
    // 인증 전 생성자
    public WebAuthnAuthenticationToken(String credentialId, byte[] signature) {
        super(null);
        this.principal = null;
        this.credentialId = credentialId;
        this.signature = signature;
        setAuthenticated(false);
    }
    
    // 인증 후 생성자
    public WebAuthnAuthenticationToken(Object principal, String credentialId, byte[] signature, 
                                     Collection<? extends GrantedAuthority> authorities) {
        super(authorities);
        this.principal = principal;
        this.credentialId = credentialId;
        this.signature = signature;
        setAuthenticated(true);
    }
    
    @Override
    public Object getCredentials() {
        return signature;
    }
    
    @Override
    public Object getPrincipal() {
        return principal;
    }
    
    public String getCredentialId() {
        return credentialId;
    }
    
    public byte[] getSignature() {
        return signature;
    }
}
