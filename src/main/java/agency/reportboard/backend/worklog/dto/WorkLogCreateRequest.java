package agency.reportboard.backend.worklog.dto;

import agency.reportboard.backend.worklog.domain.WorkCategory;
import agency.reportboard.backend.worklog.domain.ImportanceLevel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.URL;

public class WorkLogCreateRequest {
    
    @NotBlank(message = "내용은 필수입니다")
    @Size(max = 500, message = "내용은 500자를 초과할 수 없습니다")
    private String content;
    
    @NotNull(message = "카테고리는 필수입니다")
    private WorkCategory category;
    
    @NotNull(message = "중요도는 필수입니다")
    private ImportanceLevel importance;
    
    @URL(message = "올바른 URL 형식이어야 합니다")
    private String referenceUrl;
    
    @Size(max = 500, message = "메모는 500자를 초과할 수 없습니다")
    private String memo;
    
    private Long dailyThemeId;
    
    // 기본 생성자
    public WorkLogCreateRequest() {}
    
    // Getters and Setters
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
    
    public Long getDailyThemeId() {
        return dailyThemeId;
    }
    
    public void setDailyThemeId(Long dailyThemeId) {
        this.dailyThemeId = dailyThemeId;
    }
}
