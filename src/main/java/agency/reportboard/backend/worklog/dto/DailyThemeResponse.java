package agency.reportboard.backend.worklog.dto;

import agency.reportboard.backend.worklog.domain.DailyTheme;

import java.time.LocalDate;

public class DailyThemeResponse {
    
    private Long id;
    private String theme;
    private LocalDate date;
    private int workLogCount;
    
    // 기본 생성자
    public DailyThemeResponse() {}
    
    // DailyTheme에서 변환하는 정적 메서드
    public static DailyThemeResponse from(DailyTheme dailyTheme) {
        DailyThemeResponse response = new DailyThemeResponse();
        response.setId(dailyTheme.getId());
        response.setTheme(dailyTheme.getTheme());
        response.setDate(dailyTheme.getDate());
        response.setWorkLogCount(dailyTheme.getWorkLogs().size());
        return response;
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
    
    public int getWorkLogCount() {
        return workLogCount;
    }
    
    public void setWorkLogCount(int workLogCount) {
        this.workLogCount = workLogCount;
    }
}
