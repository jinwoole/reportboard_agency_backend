package agency.reportboard.backend.user.domain;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false)
    private String username;
    
    @Column(nullable = false)
    private String displayName;
    
    @Column(nullable = false)
    private boolean registrationCompleted = false;
    
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<UserCredential> credentials = new ArrayList<>();
    
    // WorkLog 관련 관계 추가
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<agency.reportboard.backend.worklog.domain.WorkLog> workLogs = new ArrayList<>();
    
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<agency.reportboard.backend.worklog.domain.DailyTheme> dailyThemes = new ArrayList<>();
    
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<agency.reportboard.backend.worklog.domain.Template> templates = new ArrayList<>();
    
    // 기본 생성자
    public User() {}
    
    public User(String username, String displayName) {
        this.username = username;
        this.displayName = displayName;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
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
    
    public boolean isRegistrationCompleted() {
        return registrationCompleted;
    }
    
    public void setRegistrationCompleted(boolean registrationCompleted) {
        this.registrationCompleted = registrationCompleted;
    }
    
    public List<UserCredential> getCredentials() {
        return credentials;
    }
    
    public void setCredentials(List<UserCredential> credentials) {
        this.credentials = credentials;
    }
}
