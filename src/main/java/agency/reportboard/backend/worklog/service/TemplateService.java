package agency.reportboard.backend.worklog.service;

import agency.reportboard.backend.worklog.domain.Template;
import agency.reportboard.backend.worklog.dto.TemplateRequest;
import agency.reportboard.backend.worklog.dto.TemplateResponse;
import agency.reportboard.backend.worklog.repository.TemplateRepository;
import agency.reportboard.backend.user.domain.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class TemplateService {
    
    private final TemplateRepository templateRepository;
    
    public TemplateService(TemplateRepository templateRepository) {
        this.templateRepository = templateRepository;
    }
    
    public TemplateResponse createTemplate(TemplateRequest request, User user) {
        if (templateRepository.existsByUserAndName(user, request.getName())) {
            throw new RuntimeException("Template with this name already exists");
        }
        
        Template template = new Template(
            request.getName(),
            request.getContent(),
            request.getDefaultCategory(),
            user
        );
        
        Template savedTemplate = templateRepository.save(template);
        return TemplateResponse.from(savedTemplate);
    }
    
    @Transactional(readOnly = true)
    public List<TemplateResponse> getTemplates(User user) {
        return templateRepository.findByUserOrderByNameAsc(user)
            .stream()
            .map(TemplateResponse::from)
            .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public Optional<TemplateResponse> getTemplate(Long id, User user) {
        return templateRepository.findById(id)
            .filter(template -> template.getUser().getId().equals(user.getId()))
            .map(TemplateResponse::from);
    }
    
    public TemplateResponse updateTemplate(Long id, TemplateRequest request, User user) {
        Template template = templateRepository.findById(id)
            .filter(t -> t.getUser().getId().equals(user.getId()))
            .orElseThrow(() -> new RuntimeException("Template not found or access denied"));
        
        // 이름 중복 체크 (자기 자신 제외)
        if (!template.getName().equals(request.getName()) && 
            templateRepository.existsByUserAndName(user, request.getName())) {
            throw new RuntimeException("Template with this name already exists");
        }
        
        template.setName(request.getName());
        template.setContent(request.getContent());
        template.setDefaultCategory(request.getDefaultCategory());
        
        Template savedTemplate = templateRepository.save(template);
        return TemplateResponse.from(savedTemplate);
    }
    
    public void deleteTemplate(Long id, User user) {
        Template template = templateRepository.findById(id)
            .filter(t -> t.getUser().getId().equals(user.getId()))
            .orElseThrow(() -> new RuntimeException("Template not found or access denied"));
        
        templateRepository.delete(template);
    }
}
