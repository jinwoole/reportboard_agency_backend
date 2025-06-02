package agency.reportboard.backend.worklog.domain;

import agency.reportboard.backend.user.domain.User;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "work_logs")
public class WorkLog {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 500)
    private String content;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private WorkCategory category;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ImportanceLevel importance;
    
    @Column
    private String referenceUrl;
    
    @Column
    private String memo;
    
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    @Column
    private LocalDateTime updatedAt;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "daily_theme_id")
    private DailyTheme dailyTheme;
    
    // 기본 생성자
    public WorkLog() {}
    
    public WorkLog(String content, WorkCategory category, ImportanceLevel importance, User user) {
        this.content = content;
        this.category = category;
        this.importance = importance;
        this.user = user;
        this.createdAt = LocalDateTime.now();
    }
    
    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getContent() {
        return content;
    }
    
    public void setContent(String content) {
        this.content = content;
    }
    
    public WorkCategory getCategory() {
        return category;
    }
    
    public void setCategory(WorkCategory category) {
        this.category = category;
    }
    
    public ImportanceLevel getImportance() {
        return importance;
    }
    
    public void setImportance(ImportanceLevel importance) {
        this.importance = importance;
    }
    
    public String getReferenceUrl() {
        return referenceUrl;
    }
    
    public void setReferenceUrl(String referenceUrl) {
        this.referenceUrl = referenceUrl;
    }
    
    public String getMemo() {
        return memo;
    }
    
    public void setMemo(String memo) {
        this.memo = memo;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    public User getUser() {
        return user;
    }
    
    public void setUser(User user) {
        this.user = user;
    }
    
    public DailyTheme getDailyTheme() {
        return dailyTheme;
    }
    
    public void setDailyTheme(DailyTheme dailyTheme) {
        this.dailyTheme = dailyTheme;
    }
}
