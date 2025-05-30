package agency.reportboard.backend.user.dto;

public class UserRegistrationRequest {
    private String username;
    private String displayName;
    
    public UserRegistrationRequest() {}
    
    public UserRegistrationRequest(String username, String displayName) {
        this.username = username;
        this.displayName = displayName;
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
}
