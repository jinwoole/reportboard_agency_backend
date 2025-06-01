package agency.reportboard.backend.worklog.dto;

import agency.reportboard.backend.worklog.domain.WorkCategory;
import agency.reportboard.backend.worklog.domain.ImportanceLevel;

import java.time.LocalDate;
import java.util.Set;

public class WorkLogSearchRequest {
    
    private LocalDate startDate;
    private LocalDate endDate;
    private Set<WorkCategory> categories;
    private Set<ImportanceLevel> importanceLevels;
    private String keyword;
    private int page = 0;
    private int size = 20;
    private String sortBy = "createdAt";
    private String sortDirection = "DESC";
    
    // 기본 생성자
    public WorkLogSearchRequest() {}
    
    // Getters and Setters
    public LocalDate getStartDate() {
        return startDate;
    }
    
    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }
    
    public LocalDate getEndDate() {
        return endDate;
    }
    
    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }
    
    public Set<WorkCategory> getCategories() {
        return categories;
    }
    
    public void setCategories(Set<WorkCategory> categories) {
        this.categories = categories;
    }
    
    public Set<ImportanceLevel> getImportanceLevels() {
        return importanceLevels;
    }
    
    public void setImportanceLevels(Set<ImportanceLevel> importanceLevels) {
        this.importanceLevels = importanceLevels;
    }
    
    public String getKeyword() {
        return keyword;
    }
    
    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }
    
    public int getPage() {
        return page;
    }
    
    public void setPage(int page) {
        this.page = page;
    }
    
    public int getSize() {
        return size;
    }
    
    public void setSize(int size) {
        this.size = size;
    }
    
    public String getSortBy() {
        return sortBy;
    }
    
    public void setSortBy(String sortBy) {
        this.sortBy = sortBy;
    }
    
    public String getSortDirection() {
        return sortDirection;
    }
    
    public void setSortDirection(String sortDirection) {
        this.sortDirection = sortDirection;
    }
}
