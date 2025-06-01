package agency.reportboard.backend.worklog.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public class DailyThemeRequest {
    
    @NotBlank(message = "테마는 필수입니다")
    private String theme;
    
    @NotNull(message = "날짜는 필수입니다")
    private LocalDate date;
    
    // 기본 생성자
    public DailyThemeRequest() {}
    
    public DailyThemeRequest(String theme, LocalDate date) {
        this.theme = theme;
        this.date = date;
    }
    
    // Getters and Setters
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
}
