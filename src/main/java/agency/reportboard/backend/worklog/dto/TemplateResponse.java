package agency.reportboard.backend.worklog.dto;

import agency.reportboard.backend.worklog.domain.Template;
import agency.reportboard.backend.worklog.domain.WorkCategory;
import agency.reportboard.backend.worklog.domain.ImportanceLevel;

public class TemplateResponse {
    
    private Long id;
    private String name;
    private String content;
    private WorkCategory defaultCategory;
    private ImportanceLevel defaultImportance;
    
    // 기본 생성자
    public TemplateResponse() {}
    
    // Template에서 변환하는 정적 메서드
    public static TemplateResponse from(Template template) {
        TemplateResponse response = new TemplateResponse();
        response.setId(template.getId());
        response.setName(template.getName());
        response.setContent(template.getContent());
        response.setDefaultCategory(template.getDefaultCategory());
        response.setDefaultImportance(template.getDefaultImportance());
        return response;
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
    
    public ImportanceLevel getDefaultImportance() {
        return defaultImportance;
    }
    
    public void setDefaultImportance(ImportanceLevel defaultImportance) {
        this.defaultImportance = defaultImportance;
    }
}
