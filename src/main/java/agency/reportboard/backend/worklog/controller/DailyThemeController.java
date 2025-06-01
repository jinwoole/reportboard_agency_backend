package agency.reportboard.backend.worklog.controller;

import agency.reportboard.backend.worklog.dto.DailyThemeRequest;
import agency.reportboard.backend.worklog.dto.DailyThemeResponse;
import agency.reportboard.backend.worklog.service.DailyThemeService;
import agency.reportboard.backend.user.security.UserPrincipal;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/daily-themes")
public class DailyThemeController {
    
    private final DailyThemeService dailyThemeService;
    
    public DailyThemeController(DailyThemeService dailyThemeService) {
        this.dailyThemeService = dailyThemeService;
    }
    
    @PostMapping
    public ResponseEntity<DailyThemeResponse> createOrUpdateDailyTheme(
            @Valid @RequestBody DailyThemeRequest request,
            Authentication authentication) {
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        DailyThemeResponse response = dailyThemeService.createOrUpdateDailyTheme(request, principal.getUser());
        return ResponseEntity.ok(response);
    }
    
    @GetMapping
    public ResponseEntity<List<DailyThemeResponse>> getDailyThemes(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            Authentication authentication) {
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        List<DailyThemeResponse> themes = dailyThemeService.getDailyThemes(principal.getUser(), startDate, endDate);
        return ResponseEntity.ok(themes);
    }
    
    @GetMapping("/today")
    public ResponseEntity<DailyThemeResponse> getTodayTheme(Authentication authentication) {
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        return dailyThemeService.getTodayTheme(principal.getUser())
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/{date}")
    public ResponseEntity<DailyThemeResponse> getDailyTheme(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            Authentication authentication) {
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        return dailyThemeService.getDailyTheme(date, principal.getUser())
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDailyTheme(
            @PathVariable Long id,
            Authentication authentication) {
        try {
            UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
            dailyThemeService.deleteDailyTheme(id, principal.getUser());
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
