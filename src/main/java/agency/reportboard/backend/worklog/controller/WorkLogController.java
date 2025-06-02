package agency.reportboard.backend.worklog.controller;

import agency.reportboard.backend.worklog.dto.*;
import agency.reportboard.backend.worklog.service.WorkLogService;
import agency.reportboard.backend.user.security.UserPrincipal;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/worklogs")
public class WorkLogController {
    
    private final WorkLogService workLogService;
    
    public WorkLogController(WorkLogService workLogService) {
        this.workLogService = workLogService;
    }
    
    @PostMapping
    public ResponseEntity<WorkLogResponse> createWorkLog(
            @Valid @RequestBody WorkLogCreateRequest request,
            Authentication authentication) {
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        WorkLogResponse response = workLogService.createWorkLog(request, principal.getUser());
        return ResponseEntity.ok(response);
    }
    
    @GetMapping
    public ResponseEntity<Page<WorkLogResponse>> getWorkLogs(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) String[] categories,
            @RequestParam(required = false) String[] importanceLevels,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection,
            Authentication authentication) {
        
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        
        // SearchRequest 객체 생성
        WorkLogSearchRequest searchRequest = new WorkLogSearchRequest();
        
        // 날짜 파라미터 처리
        if (startDate != null && !startDate.isEmpty() && !startDate.equals("null")) {
            try {
                searchRequest.setStartDate(java.time.LocalDate.parse(startDate));
            } catch (Exception e) {
                // 날짜 파싱 에러 무시
            }
        }
        
        if (endDate != null && !endDate.isEmpty() && !endDate.equals("null")) {
            try {
                searchRequest.setEndDate(java.time.LocalDate.parse(endDate));
            } catch (Exception e) {
                // 날짜 파싱 에러 무시
            }
        }
        
        // 카테고리 파라미터 처리
        if (categories != null && categories.length > 0) {
            java.util.Set<agency.reportboard.backend.worklog.domain.WorkCategory> categorySet = 
                new java.util.HashSet<>();
            for (String category : categories) {
                if (category != null && !category.isEmpty() && !category.equals("null")) {
                    try {
                        categorySet.add(agency.reportboard.backend.worklog.domain.WorkCategory.valueOf(category.toUpperCase()));
                    } catch (IllegalArgumentException e) {
                        // 잘못된 카테고리 무시
                    }
                }
            }
            searchRequest.setCategories(categorySet);
        }
        
        // 중요도 파라미터 처리
        if (importanceLevels != null && importanceLevels.length > 0) {
            java.util.Set<agency.reportboard.backend.worklog.domain.ImportanceLevel> importanceSet = 
                new java.util.HashSet<>();
            for (String importance : importanceLevels) {
                if (importance != null && !importance.isEmpty() && !importance.equals("null")) {
                    try {
                        importanceSet.add(agency.reportboard.backend.worklog.domain.ImportanceLevel.valueOf(importance.toUpperCase()));
                    } catch (IllegalArgumentException e) {
                        // 잘못된 중요도 무시
                    }
                }
            }
            searchRequest.setImportanceLevels(importanceSet);
        }
        
        // 키워드 처리
        if (keyword != null && !keyword.isEmpty() && !keyword.equals("null")) {
            searchRequest.setKeyword(keyword);
        }
        
        // 페이징 정보 설정
        searchRequest.setPage(page);
        searchRequest.setSize(size);
        searchRequest.setSortBy(sortBy);
        searchRequest.setSortDirection(sortDirection);
        
        Page<WorkLogResponse> workLogs = workLogService.getWorkLogs(principal.getUser(), searchRequest);
        return ResponseEntity.ok(workLogs);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<WorkLogResponse> getWorkLog(
            @PathVariable Long id,
            Authentication authentication) {
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        return workLogService.getWorkLog(id, principal.getUser())
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<WorkLogResponse> updateWorkLog(
            @PathVariable Long id,
            @Valid @RequestBody WorkLogCreateRequest request,
            Authentication authentication) {
        try {
            UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
            WorkLogResponse response = workLogService.updateWorkLog(id, request, principal.getUser());
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteWorkLog(
            @PathVariable Long id,
            Authentication authentication) {
        try {
            UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
            workLogService.deleteWorkLog(id, principal.getUser());
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats(Authentication authentication) {
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        
        long totalCount = workLogService.getTotalCount(principal.getUser());
        
        return ResponseEntity.ok(Map.of(
            "totalCount", totalCount,
            "implementationCount", workLogService.getCountByCategory(principal.getUser(), 
                agency.reportboard.backend.worklog.domain.WorkCategory.IMPLEMENTATION),
            "planningCount", workLogService.getCountByCategory(principal.getUser(), 
                agency.reportboard.backend.worklog.domain.WorkCategory.PLANNING),
            "collaborationCount", workLogService.getCountByCategory(principal.getUser(), 
                agency.reportboard.backend.worklog.domain.WorkCategory.COLLABORATION),
            "criticalCount", workLogService.getCountByImportance(principal.getUser(), 
                agency.reportboard.backend.worklog.domain.ImportanceLevel.CRITICAL),
            "importantCount", workLogService.getCountByImportance(principal.getUser(), 
                agency.reportboard.backend.worklog.domain.ImportanceLevel.IMPORTANT)
        ));
    }

    @GetMapping("/stats/weekly")
    public Map<String, Long> getWeeklyStats(@AuthenticationPrincipal UserPrincipal principal) {
        return workLogService.getWeeklyStats(principal.getUser().getId());
    }

    @GetMapping("/stats/streak")
    public Map<String, Integer> getStreak(@AuthenticationPrincipal UserPrincipal principal) {
        int streak = workLogService.getStreakCount(principal.getUser().getId());
        return Map.of("streak", streak);
    }

    @GetMapping("/stats/week-count")
    public Map<String, Long> getWeekCount(@AuthenticationPrincipal UserPrincipal principal) {
        long count = workLogService.getWeekCount(principal.getUser().getId());
        return Map.of("weekCount", count);
    }
}
