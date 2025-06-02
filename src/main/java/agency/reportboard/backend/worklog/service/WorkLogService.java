package agency.reportboard.backend.worklog.service;

import agency.reportboard.backend.worklog.domain.WorkLog;
import agency.reportboard.backend.worklog.domain.DailyTheme;
import agency.reportboard.backend.worklog.domain.WorkCategory;
import agency.reportboard.backend.worklog.domain.ImportanceLevel;
import agency.reportboard.backend.worklog.dto.WorkLogCreateRequest;
import agency.reportboard.backend.worklog.dto.WorkLogResponse;
import agency.reportboard.backend.worklog.dto.WorkLogSearchRequest;
import agency.reportboard.backend.worklog.repository.WorkLogRepository;
import agency.reportboard.backend.worklog.repository.DailyThemeRepository;
import agency.reportboard.backend.user.domain.User;
import agency.reportboard.backend.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

@Service
@Transactional
public class WorkLogService {
    
    private final WorkLogRepository workLogRepository;
    private final DailyThemeRepository dailyThemeRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    public WorkLogService(WorkLogRepository workLogRepository, DailyThemeRepository dailyThemeRepository) {
        this.workLogRepository = workLogRepository;
        this.dailyThemeRepository = dailyThemeRepository;
    }
    
    public WorkLogResponse createWorkLog(WorkLogCreateRequest request, User user) {
        WorkLog workLog = new WorkLog(
            request.getContent(),
            request.getCategory(),
            request.getImportance(),
            user
        );
        
        workLog.setReferenceUrl(request.getReferenceUrl());
        workLog.setMemo(request.getMemo());
        
        // 일일 테마 설정
        if (request.getDailyThemeId() != null) {
            Optional<DailyTheme> dailyTheme = dailyThemeRepository.findById(request.getDailyThemeId());
            if (dailyTheme.isPresent() && dailyTheme.get().getUser().getId().equals(user.getId())) {
                workLog.setDailyTheme(dailyTheme.get());
            }
        }
        
        WorkLog savedWorkLog = workLogRepository.save(workLog);
        return WorkLogResponse.from(savedWorkLog);
    }
    
    @Transactional(readOnly = true)
    public Page<WorkLogResponse> getWorkLogs(User user, WorkLogSearchRequest searchRequest) {
        Pageable pageable = createPageable(searchRequest);
        
        // 날짜 범위 설정 (LocalDate를 LocalDateTime으로 변환)
        LocalDateTime startDateTime = searchRequest.getStartDate() != null 
            ? searchRequest.getStartDate().atStartOfDay() 
            : null;
        LocalDateTime endDateTime = searchRequest.getEndDate() != null 
            ? searchRequest.getEndDate().atTime(23, 59, 59) 
            : null;
        
        Page<WorkLog> workLogs;
        
        // 단순한 검색 조건 처리
        if (hasOnlyKeyword(searchRequest)) {
            workLogs = workLogRepository.findByUserAndContentContainingIgnoreCase(
                user.getId(), 
                searchRequest.getKeyword(), 
                pageable
            );
        } else if (hasOnlyCategories(searchRequest)) {
            workLogs = workLogRepository.findByUserAndCategoriesIn(
                user, 
                new ArrayList<>(searchRequest.getCategories()), 
                pageable
            );
        } else if (hasOnlyImportanceLevels(searchRequest)) {
            workLogs = workLogRepository.findByUserAndImportanceLevelsIn(
                user, 
                new ArrayList<>(searchRequest.getImportanceLevels()), 
                pageable
            );
        } else if (hasOnlyDateRange(searchRequest)) {
            workLogs = workLogRepository.findByUserAndCreatedAtBetweenOrderByCreatedAtDesc(
                user, 
                startDateTime, 
                endDateTime, 
                pageable
            );
        } else {
            // 복잡한 검색의 경우 메모리에서 필터링
            workLogs = workLogRepository.findByUserOrderByCreatedAtDesc(user, pageable);
        }
        
        return workLogs.map(WorkLogResponse::from);
    }
    
    @Transactional(readOnly = true)
    public Optional<WorkLogResponse> getWorkLog(Long id, User user) {
        return workLogRepository.findById(id)
            .filter(workLog -> workLog.getUser().getId().equals(user.getId()))
            .map(WorkLogResponse::from);
    }
    
    public WorkLogResponse updateWorkLog(Long id, WorkLogCreateRequest request, User user) {
        WorkLog workLog = workLogRepository.findById(id)
            .filter(log -> log.getUser().getId().equals(user.getId()))
            .orElseThrow(() -> new RuntimeException("WorkLog not found or access denied"));
        
        workLog.setContent(request.getContent());
        workLog.setCategory(request.getCategory());
        workLog.setImportance(request.getImportance());
        workLog.setReferenceUrl(request.getReferenceUrl());
        workLog.setMemo(request.getMemo());
        
        // 일일 테마 업데이트
        if (request.getDailyThemeId() != null) {
            Optional<DailyTheme> dailyTheme = dailyThemeRepository.findById(request.getDailyThemeId());
            if (dailyTheme.isPresent() && dailyTheme.get().getUser().getId().equals(user.getId())) {
                workLog.setDailyTheme(dailyTheme.get());
            }
        } else {
            workLog.setDailyTheme(null);
        }
        
        WorkLog savedWorkLog = workLogRepository.save(workLog);
        return WorkLogResponse.from(savedWorkLog);
    }
    
    public void deleteWorkLog(Long id, User user) {
        WorkLog workLog = workLogRepository.findById(id)
            .filter(log -> log.getUser().getId().equals(user.getId()))
            .orElseThrow(() -> new RuntimeException("WorkLog not found or access denied"));
        
        workLogRepository.delete(workLog);
    }
    
    @Transactional(readOnly = true)
    public long getTotalCount(User user) {
        return workLogRepository.countByUser(user);
    }
    
    @Transactional(readOnly = true)
    public long getCountByCategory(User user, WorkCategory category) {
        return workLogRepository.countByUserAndCategory(user, category);
    }
    
    @Transactional(readOnly = true)
    public long getCountByImportance(User user, ImportanceLevel importance) {
        return workLogRepository.countByUserAndImportance(user, importance);
    }
    
    // 최근 7일간 날짜별 기록 개수
    public Map<String, Long> getWeeklyStats(Long userId) {
        LocalDate today = LocalDate.now();
        LocalDate start = today.minusDays(6);
        Map<String, Long> result = new LinkedHashMap<>();
        User user = userRepository.findById(userId).orElseThrow();
        for (int i = 0; i < 7; i++) {
            LocalDate date = start.plusDays(i);
            long count = workLogRepository.countByUserAndCreatedAtBetween(
                user,
                date.atStartOfDay(),
                date.plusDays(1).atStartOfDay()
            );
            result.put(date.toString(), count);
        }
        return result;
    }

    // 연속 기록 일수(스트릭)
    public int getStreakCount(Long userId) {
        LocalDate today = LocalDate.now();
        int streak = 0;
        User user = userRepository.findById(userId).orElseThrow();
        for (int i = 0; i < 365; i++) {
            LocalDate date = today.minusDays(i);
            long count = workLogRepository.countByUserAndCreatedAtBetween(
                user,
                date.atStartOfDay(),
                date.plusDays(1).atStartOfDay()
            );
            if (count > 0) {
                streak++;
            } else {
                break;
            }
        }
        return streak;
    }

    // 이번 주 기록 수
    public long getWeekCount(Long userId) {
        LocalDate today = LocalDate.now();
        WeekFields weekFields = WeekFields.of(Locale.getDefault());
        LocalDate startOfWeek = today.with(weekFields.dayOfWeek(), 1);
        User user = userRepository.findById(userId).orElseThrow();
        return workLogRepository.countByUserAndCreatedAtBetween(
            user,
            startOfWeek.atStartOfDay(),
            today.plusDays(1).atStartOfDay()
        );
    }
    
    private Pageable createPageable(WorkLogSearchRequest searchRequest) {
        Sort.Direction direction = searchRequest.getSortDirection().equalsIgnoreCase("ASC") 
            ? Sort.Direction.ASC 
            : Sort.Direction.DESC;
        Sort sort = Sort.by(direction, searchRequest.getSortBy());
        return PageRequest.of(searchRequest.getPage(), searchRequest.getSize(), sort);
    }
    
    private boolean hasOnlyKeyword(WorkLogSearchRequest searchRequest) {
        return searchRequest.getKeyword() != null && !searchRequest.getKeyword().trim().isEmpty() &&
               (searchRequest.getCategories() == null || searchRequest.getCategories().isEmpty()) &&
               (searchRequest.getImportanceLevels() == null || searchRequest.getImportanceLevels().isEmpty()) &&
               searchRequest.getStartDate() == null && searchRequest.getEndDate() == null;
    }
    
    private boolean hasOnlyCategories(WorkLogSearchRequest searchRequest) {
        return (searchRequest.getCategories() != null && !searchRequest.getCategories().isEmpty()) &&
               (searchRequest.getKeyword() == null || searchRequest.getKeyword().trim().isEmpty()) &&
               (searchRequest.getImportanceLevels() == null || searchRequest.getImportanceLevels().isEmpty()) &&
               searchRequest.getStartDate() == null && searchRequest.getEndDate() == null;
    }
    
    private boolean hasOnlyImportanceLevels(WorkLogSearchRequest searchRequest) {
        return (searchRequest.getImportanceLevels() != null && !searchRequest.getImportanceLevels().isEmpty()) &&
               (searchRequest.getKeyword() == null || searchRequest.getKeyword().trim().isEmpty()) &&
               (searchRequest.getCategories() == null || searchRequest.getCategories().isEmpty()) &&
               searchRequest.getStartDate() == null && searchRequest.getEndDate() == null;
    }
    
    private boolean hasOnlyDateRange(WorkLogSearchRequest searchRequest) {
        return (searchRequest.getStartDate() != null || searchRequest.getEndDate() != null) &&
               (searchRequest.getKeyword() == null || searchRequest.getKeyword().trim().isEmpty()) &&
               (searchRequest.getCategories() == null || searchRequest.getCategories().isEmpty()) &&
               (searchRequest.getImportanceLevels() == null || searchRequest.getImportanceLevels().isEmpty());
    }
}
