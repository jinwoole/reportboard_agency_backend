package agency.reportboard.backend.worklog.dto;

import agency.reportboard.backend.worklog.domain.WorkCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class TemplateRequest {
    
    @NotBlank(message = "템플릿 이름은 필수입니다")
    @Size(max = 100, message = "템플릿 이름은 100자를 초과할 수 없습니다")
    private String name;
    
    @NotBlank(message = "템플릿 내용은 필수입니다")
    @Size(max = 500, message = "템플릿 내용은 500자를 초과할 수 없습니다")
    private String content;
    
    private WorkCategory defaultCategory;
    
    // 기본 생성자
    public TemplateRequest() {}
    
    public TemplateRequest(String name, String content, WorkCategory defaultCategory) {
        this.name = name;
        this.content = content;
        this.defaultCategory = defaultCategory;
    }
    
    // Getters and Setters
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
}
