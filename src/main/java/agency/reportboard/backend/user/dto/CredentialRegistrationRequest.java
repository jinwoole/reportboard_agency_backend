package agency.reportboard.backend.user.dto;

public class CredentialRegistrationRequest {
    private String credentialId;
    private String publicKey; // Base64 encoded
    private String username;
    
    public CredentialRegistrationRequest() {}
    
    public CredentialRegistrationRequest(String credentialId, String publicKey, String username) {
        this.credentialId = credentialId;
        this.publicKey = publicKey;
        this.username = username;
    }
    
    public String getCredentialId() {
        return credentialId;
    }
    
    public void setCredentialId(String credentialId) {
        this.credentialId = credentialId;
    }
    
    public String getPublicKey() {
        return publicKey;
    }
    
    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
}
