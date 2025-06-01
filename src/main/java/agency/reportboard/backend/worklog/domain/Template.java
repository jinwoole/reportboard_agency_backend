package agency.reportboard.backend.worklog.domain;

import agency.reportboard.backend.user.domain.User;
import jakarta.persistence.*;

@Entity
@Table(name = "templates")
public class Template {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String name;
    
    @Column(nullable = false, length = 500)
    private String content;
    
    @Enumerated(EnumType.STRING)
    private WorkCategory defaultCategory;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    // 기본 생성자
    public Template() {}
    
    public Template(String name, String content, WorkCategory defaultCategory, User user) {
        this.name = name;
        this.content = content;
        this.defaultCategory = defaultCategory;
        this.user = user;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getContent() {
        return content;
    }
    
    public void setContent(String content) {
        this.content = content;
    }
    
    public WorkCategory getDefaultCategory() {
        return defaultCategory;
    }
    
    public void setDefaultCategory(WorkCategory defaultCategory) {
        this.defaultCategory = defaultCategory;
    }
    
    public User getUser() {
        return user;
    }
    
    public void setUser(User user) {
        this.user = user;
    }
}
