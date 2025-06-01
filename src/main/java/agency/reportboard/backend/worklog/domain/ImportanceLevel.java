package agency.reportboard.backend.worklog.domain;

public enum ImportanceLevel {
    NORMAL("⭐ 보통", 1),
    IMPORTANT("⭐⭐ 중요", 2),
    CRITICAL("⭐⭐⭐ 핵심", 3);
    
    private final String displayName;
    private final int level;
    
    ImportanceLevel(String displayName, int level) {
        this.displayName = displayName;
        this.level = level;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public int getLevel() {
        return level;
    }
}
