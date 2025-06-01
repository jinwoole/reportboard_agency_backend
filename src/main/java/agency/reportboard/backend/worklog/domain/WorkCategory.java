package agency.reportboard.backend.worklog.domain;

public enum WorkCategory {
    IMPLEMENTATION("🔨 실행/구현"),
    PLANNING("📋 계획/설계"),
    COLLABORATION("💬 소통/협업"),
    LEARNING("📚 학습/연구"),
    ANALYSIS("🔍 분석/검토"),
    PROBLEM_SOLVING("🛠️ 문제해결"),
    DOCUMENTATION("📊 보고/문서화");
    
    private final String displayName;
    
    WorkCategory(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
}
