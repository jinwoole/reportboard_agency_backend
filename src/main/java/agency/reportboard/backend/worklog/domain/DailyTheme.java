package agency.reportboard.backend.worklog.domain;

import agency.reportboard.backend.user.domain.User;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "daily_themes")
public class DailyTheme {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String theme;
    
    @Column(nullable = false)
    private LocalDate date;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @OneToMany(mappedBy = "dailyTheme", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<WorkLog> workLogs = new ArrayList<>();
    
    // 기본 생성자
    public DailyTheme() {}
    
    public DailyTheme(String theme, LocalDate date, User user) {
        this.theme = theme;
        this.date = date;
        this.user = user;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getTheme() {
        return theme;
    }
    
    public void setTheme(String theme) {
        this.theme = theme;
    }
    
    public LocalDate getDate() {
        return date;
    }
    
    public void setDate(LocalDate date) {
        this.date = date;
    }
    
    public User getUser() {
        return user;
    }
    
    public void setUser(User user) {
        this.user = user;
    }
    
    public List<WorkLog> getWorkLogs() {
        return workLogs;
    }
    
    public void setWorkLogs(List<WorkLog> workLogs) {
        this.workLogs = workLogs;
    }
}
