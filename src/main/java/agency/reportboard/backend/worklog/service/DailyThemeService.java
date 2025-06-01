package agency.reportboard.backend.worklog.service;

import agency.reportboard.backend.worklog.domain.DailyTheme;
import agency.reportboard.backend.worklog.dto.DailyThemeRequest;
import agency.reportboard.backend.worklog.dto.DailyThemeResponse;
import agency.reportboard.backend.worklog.repository.DailyThemeRepository;
import agency.reportboard.backend.user.domain.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class DailyThemeService {
    
    private final DailyThemeRepository dailyThemeRepository;
    
    public DailyThemeService(DailyThemeRepository dailyThemeRepository) {
        this.dailyThemeRepository = dailyThemeRepository;
    }
    
    public DailyThemeResponse createOrUpdateDailyTheme(DailyThemeRequest request, User user) {
        Optional<DailyTheme> existingTheme = dailyThemeRepository.findByUserAndDate(user, request.getDate());
        
        DailyTheme dailyTheme;
        if (existingTheme.isPresent()) {
            // 기존 테마 업데이트
            dailyTheme = existingTheme.get();
            dailyTheme.setTheme(request.getTheme());
        } else {
            // 새 테마 생성
            dailyTheme = new DailyTheme(request.getTheme(), request.getDate(), user);
        }
        
        DailyTheme savedTheme = dailyThemeRepository.save(dailyTheme);
        return DailyThemeResponse.from(savedTheme);
    }
    
    @Transactional(readOnly = true)
    public Optional<DailyThemeResponse> getDailyTheme(LocalDate date, User user) {
        return dailyThemeRepository.findByUserAndDate(user, date)
            .map(DailyThemeResponse::from);
    }
    
    @Transactional(readOnly = true)
    public List<DailyThemeResponse> getDailyThemes(User user, LocalDate startDate, LocalDate endDate) {
        List<DailyTheme> themes;
        if (startDate != null && endDate != null) {
            themes = dailyThemeRepository.findByUserAndDateBetweenOrderByDateDesc(user, startDate, endDate);
        } else {
            themes = dailyThemeRepository.findByUserOrderByDateDesc(user);
        }
        
        return themes.stream()
            .map(DailyThemeResponse::from)
            .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public Optional<DailyThemeResponse> getTodayTheme(User user) {
        return getDailyTheme(LocalDate.now(), user);
    }
    
    public void deleteDailyTheme(Long id, User user) {
        DailyTheme dailyTheme = dailyThemeRepository.findById(id)
            .filter(theme -> theme.getUser().getId().equals(user.getId()))
            .orElseThrow(() -> new RuntimeException("DailyTheme not found or access denied"));
        
        dailyThemeRepository.delete(dailyTheme);
    }
    
    @Transactional(readOnly = true)
    public boolean existsByDate(LocalDate date, User user) {
        return dailyThemeRepository.existsByUserAndDate(user, date);
    }
}
