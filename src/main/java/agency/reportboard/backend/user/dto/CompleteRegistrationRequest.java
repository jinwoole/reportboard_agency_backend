package agency.reportboard.backend.user.dto;

public class CompleteRegistrationRequest {
    private String username;
    private String displayName;
    private String credentialId;
    private String publicKey; // Base64 encoded
    
    public CompleteRegistrationRequest() {}
    
    public CompleteRegistrationRequest(String username, String displayName, String credentialId, String publicKey) {
        this.username = username;
        this.displayName = displayName;
        this.credentialId = credentialId;
        this.publicKey = publicKey;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
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
}
