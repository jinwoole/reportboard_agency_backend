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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Optional;

@Service
@Transactional
public class WorkLogService {
    
    private final WorkLogRepository workLogRepository;
    private final DailyThemeRepository dailyThemeRepository;
    
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
        workLog.setReferenceTitle(request.getReferenceTitle());
        
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
        workLog.setReferenceTitle(request.getReferenceTitle());
        
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
