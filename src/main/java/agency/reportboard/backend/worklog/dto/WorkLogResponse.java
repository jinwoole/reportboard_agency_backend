package agency.reportboard.backend.worklog.dto;

import agency.reportboard.backend.worklog.domain.WorkLog;
import agency.reportboard.backend.worklog.domain.WorkCategory;
import agency.reportboard.backend.worklog.domain.ImportanceLevel;

import java.time.LocalDateTime;

public class WorkLogResponse {
    
    private Long id;
    private String content;
    private WorkCategory category;
    private ImportanceLevel importance;
    private String referenceUrl;
    private String memo;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private DailyThemeResponse dailyTheme;
    
    // 기본 생성자
    public WorkLogResponse() {}
    
    // WorkLog에서 변환하는 정적 메서드
    public static WorkLogResponse from(WorkLog workLog) {
        WorkLogResponse response = new WorkLogResponse();
        response.setId(workLog.getId());
        response.setContent(workLog.getContent());
        response.setCategory(workLog.getCategory());
        response.setImportance(workLog.getImportance());
        response.setReferenceUrl(workLog.getReferenceUrl());
        response.setMemo(workLog.getMemo());
        response.setCreatedAt(workLog.getCreatedAt());
        response.setUpdatedAt(workLog.getUpdatedAt());
        
        if (workLog.getDailyTheme() != null) {
            response.setDailyTheme(DailyThemeResponse.from(workLog.getDailyTheme()));
        }
        
        return response;
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
    
    public DailyThemeResponse getDailyTheme() {
        return dailyTheme;
    }
    
    public void setDailyTheme(DailyThemeResponse dailyTheme) {
        this.dailyTheme = dailyTheme;
    }
}
