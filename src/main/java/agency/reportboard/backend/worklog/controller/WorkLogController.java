package agency.reportboard.backend.worklog.controller;

import agency.reportboard.backend.worklog.dto.*;
import agency.reportboard.backend.worklog.service.WorkLogService;
import agency.reportboard.backend.user.security.UserPrincipal;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
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
            @ModelAttribute WorkLogSearchRequest searchRequest,
            Authentication authentication) {
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
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
}
