// 전역 변수
let currentUser = null;
let currentPage = 0;
let totalPages = 0;
let isLoading = false;
let searchFilters = {
    categories: [],
    importanceLevels: [],
    keyword: '',
    startDate: null,
    endDate: null
};

// DOM이 로드되면 초기화
document.addEventListener('DOMContentLoaded', async () => {
    await initializeApp();
});

// 앱 초기화
async function initializeApp() {
    try {
        // 시간 표시 시작
        updateDateTime();
        setInterval(updateDateTime, 1000);
        
        // 사용자 정보 로드
        await loadCurrentUser();
        
        // 이벤트 리스너 설정
        setupEventListeners();
        
        // 초기 데이터 로드
        await Promise.all([
            loadTodayTheme(),
            loadWorkLogs(),
            loadStats(),
            loadTemplates()
        ]);
        
    } catch (error) {
        console.error('앱 초기화 실패:', error);
        // 인증 실패 시 로그인 페이지로 리다이렉트
        window.location.href = '/index.html';
    }
}

// 현재 사용자 정보 로드
async function loadCurrentUser() {
    const response = await fetch('/api/auth/me');
    if (!response.ok) {
        throw new Error('사용자 정보 로드 실패');
    }
    
    currentUser = await response.json();
    document.getElementById('user-display-name').textContent = currentUser.displayName;
}

// 이벤트 리스너 설정
function setupEventListeners() {
    // 사이드바 토글 (모바일)
    document.getElementById('sidebar-toggle').addEventListener('click', toggleSidebar);
    document.getElementById('sidebar-overlay').addEventListener('click', closeSidebar);
    
    // 로그아웃
    document.getElementById('logout-btn').addEventListener('click', logout);
    
    // 오늘의 테마 저장
    document.getElementById('save-theme-btn').addEventListener('click', saveTodayTheme);
    
    // 빠른 추가 폼
    document.getElementById('quick-add-form').addEventListener('submit', handleQuickAdd);
    
    // 검색
    document.getElementById('search-input').addEventListener('input', debounce(handleSearch, 300));
    
    // 필터
    document.querySelectorAll('.category-filter, .importance-filter').forEach(checkbox => {
        checkbox.addEventListener('change', handleFilterChange);
    });
    
    document.getElementById('start-date').addEventListener('change', handleDateFilterChange);
    document.getElementById('end-date').addEventListener('change', handleDateFilterChange);
    document.getElementById('clear-filters').addEventListener('click', clearFilters);
    
    // 더 보기
    document.getElementById('load-more').addEventListener('click', loadMoreWorkLogs);
    
    // 템플릿 추가
    document.getElementById('add-template').addEventListener('click', showTemplateModal);
}

// 사이드바 토글
function toggleSidebar() {
    const sidebar = document.getElementById('sidebar');
    const overlay = document.getElementById('sidebar-overlay');
    
    sidebar.classList.toggle('sidebar-hidden');
    overlay.classList.toggle('hidden');
}

function closeSidebar() {
    const sidebar = document.getElementById('sidebar');
    const overlay = document.getElementById('sidebar-overlay');
    
    sidebar.classList.add('sidebar-hidden');
    overlay.classList.add('hidden');
}

// 로그아웃
async function logout() {
    try {
        await fetch('/api/auth/logout', { method: 'POST' });
        window.location.href = '/index.html';
    } catch (error) {
        console.error('로그아웃 실패:', error);
        window.location.href = '/index.html';
    }
}

// 오늘의 테마 로드
async function loadTodayTheme() {
    try {
        const response = await fetch('/api/daily-themes/today');
        
        if (response.ok) {
            const theme = await response.json();
            document.getElementById('today-theme-input').value = theme.theme;
        } else {
            // 오늘의 테마가 없는 경우
            document.getElementById('today-theme-input').value = '';
        }
    } catch (error) {
        console.error('오늘의 테마 로드 실패:', error);
    }
}

// 오늘의 테마 저장
async function saveTodayTheme() {
    const themeInput = document.getElementById('today-theme-input');
    const theme = themeInput.value.trim();
    
    if (!theme) {
        alert('테마를 입력해주세요.');
        return;
    }
    
    try {
        const response = await fetch('/api/daily-themes', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                theme: theme,
                date: new Date().toISOString().split('T')[0]
            })
        });
        
        if (response.ok) {
            showAlert('오늘의 테마가 저장되었습니다!', 'success');
        } else {
            throw new Error('테마 저장 실패');
        }
    } catch (error) {
        console.error('테마 저장 실패:', error);
        showAlert('테마 저장에 실패했습니다.', 'error');
    }
}

// 빠른 추가 폼 처리
async function handleQuickAdd(event) {
    event.preventDefault();
    
    const formData = {
        content: document.getElementById('content-input').value.trim(),
        category: document.getElementById('category-select').value,
        importance: document.getElementById('importance-select').value,
        referenceUrl: document.getElementById('reference-url').value.trim() || null,
        referenceTitle: document.getElementById('reference-title').value.trim() || null
    };
    
    if (!formData.content || !formData.category || !formData.importance) {
        showAlert('모든 필수 항목을 입력해주세요.', 'error');
        return;
    }
    
    try {
        const response = await fetch('/api/worklogs', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(formData)
        });
        
        if (response.ok) {
            // 폼 초기화
            document.getElementById('quick-add-form').reset();
            
            // 목록 새로고침
            currentPage = 0;
            await loadWorkLogs(true);
            await loadStats();
            
            showAlert('업무 로그가 추가되었습니다!', 'success');
        } else {
            throw new Error('업무 로그 추가 실패');
        }
    } catch (error) {
        console.error('업무 로그 추가 실패:', error);
        showAlert('업무 로그 추가에 실패했습니다.', 'error');
    }
}

// 업무 로그 목록 로드
async function loadWorkLogs(reset = false) {
    if (isLoading) return;
    
    isLoading = true;
    document.getElementById('loading').classList.remove('hidden');
    
    try {
        const params = new URLSearchParams();
        params.append('page', reset ? 0 : currentPage);
        params.append('size', 20);
        
        // null이 아닌 값들만 추가
        if (searchFilters.keyword && searchFilters.keyword.trim()) {
            params.append('keyword', searchFilters.keyword.trim());
        }
        if (searchFilters.startDate) {
            params.append('startDate', searchFilters.startDate);
        }
        if (searchFilters.endDate) {
            params.append('endDate', searchFilters.endDate);
        }
        
        // 배열 파라미터 처리
        searchFilters.categories.forEach(cat => params.append('categories', cat));
        searchFilters.importanceLevels.forEach(imp => params.append('importanceLevels', imp));
        
        const response = await fetch(`/api/worklogs?${params}`);
        
        if (!response.ok) {
            throw new Error('업무 로그 로드 실패');
        }
        
        const data = await response.json();
        
        if (reset) {
            document.getElementById('worklog-list').innerHTML = '';
            currentPage = 0;
        }
        
        renderWorkLogs(data.content);
        
        totalPages = data.totalPages;
        currentPage = data.number + 1;
        
        // 더 보기 버튼 표시/숨김
        const loadMoreBtn = document.getElementById('load-more');
        if (currentPage < totalPages) {
            loadMoreBtn.classList.remove('hidden');
        } else {
            loadMoreBtn.classList.add('hidden');
        }
        
    } catch (error) {
        console.error('업무 로그 로드 실패:', error);
        showAlert('업무 로그를 불러오는데 실패했습니다.', 'error');
    } finally {
        isLoading = false;
        document.getElementById('loading').classList.add('hidden');
    }
}

// 업무 로그 렌더링
function renderWorkLogs(workLogs) {
    const container = document.getElementById('worklog-list');
    
    workLogs.forEach(workLog => {
        const workLogElement = createWorkLogElement(workLog);
        container.appendChild(workLogElement);
    });
}

// 업무 로그 엘리먼트 생성
function createWorkLogElement(workLog) {
    const div = document.createElement('div');
    div.className = `bg-white rounded-lg shadow-sm border border-gray-200 p-4 importance-${workLog.importance.toLowerCase()}`;
    div.dataset.id = workLog.id;
    
    const categoryClass = `category-${workLog.category.toLowerCase()}`;
    const importanceText = getImportanceText(workLog.importance);
    const categoryText = getCategoryText(workLog.category);
    
    div.innerHTML = `
        <div class="flex items-start justify-between">
            <div class="flex-1">
                <div class="flex items-center space-x-2 mb-2">
                    <span class="px-2 py-1 rounded-full text-xs font-medium ${categoryClass}">
                        ${categoryText}
                    </span>
                    <span class="text-xs text-gray-500">${importanceText}</span>
                    <span class="text-xs text-gray-400">${formatDateTime(workLog.createdAt)}</span>
                </div>
                <p class="text-gray-900 mb-2">${escapeHtml(workLog.content)}</p>
                ${workLog.referenceUrl ? `
                    <a href="${workLog.referenceUrl}" target="_blank" class="inline-flex items-center text-sm text-blue-600 hover:text-blue-800">
                        <i class="fas fa-external-link-alt mr-1"></i>
                        ${workLog.referenceTitle || '참조 링크'}
                    </a>
                ` : ''}
                ${workLog.dailyTheme ? `
                    <div class="mt-2">
                        <span class="text-xs text-gray-500">테마: ${escapeHtml(workLog.dailyTheme.theme)}</span>
                    </div>
                ` : ''}
            </div>
            <div class="flex items-center space-x-2 ml-4">
                <button onclick="editWorkLog(${workLog.id})" class="text-gray-400 hover:text-gray-600">
                    <i class="fas fa-edit"></i>
                </button>
                <button onclick="deleteWorkLog(${workLog.id})" class="text-gray-400 hover:text-red-600">
                    <i class="fas fa-trash"></i>
                </button>
            </div>
        </div>
    `;
    
    return div;
}

// 더 많은 업무 로그 로드
async function loadMoreWorkLogs() {
    await loadWorkLogs(false);
}

// 검색 처리
function handleSearch(event) {
    searchFilters.keyword = event.target.value.trim();
    currentPage = 0;
    loadWorkLogs(true);
}

// 필터 변경 처리
function handleFilterChange() {
    // 카테고리 필터
    const categoryFilters = document.querySelectorAll('.category-filter:checked');
    searchFilters.categories = Array.from(categoryFilters).map(cb => cb.value);
    
    // 중요도 필터
    const importanceFilters = document.querySelectorAll('.importance-filter:checked');
    searchFilters.importanceLevels = Array.from(importanceFilters).map(cb => cb.value);
    
    currentPage = 0;
    loadWorkLogs(true);
}

// 날짜 필터 변경 처리
function handleDateFilterChange() {
    const startDate = document.getElementById('start-date').value;
    const endDate = document.getElementById('end-date').value;
    
    searchFilters.startDate = startDate || null;
    searchFilters.endDate = endDate || null;
    
    currentPage = 0;
    loadWorkLogs(true);
}

// 필터 초기화
function clearFilters() {
    // 체크박스 초기화
    document.querySelectorAll('.category-filter, .importance-filter').forEach(cb => cb.checked = false);
    
    // 날짜 필터 초기화
    document.getElementById('start-date').value = '';
    document.getElementById('end-date').value = '';
    
    // 검색어 초기화
    document.getElementById('search-input').value = '';
    
    // 필터 객체 초기화
    searchFilters = {
        categories: [],
        importanceLevels: [],
        keyword: '',
        startDate: null,
        endDate: null
    };
    
    currentPage = 0;
    loadWorkLogs(true);
}

// 통계 로드
async function loadStats() {
    try {
        const response = await fetch('/api/worklogs/stats');
        
        if (!response.ok) {
            throw new Error('통계 로드 실패');
        }
        
        const stats = await response.json();
        renderStats(stats);
        
    } catch (error) {
        console.error('통계 로드 실패:', error);
    }
}

// 통계 렌더링
function renderStats(stats) {
    const container = document.getElementById('stats-container');
    container.innerHTML = `
        <div class="flex justify-between">
            <span class="text-gray-600">전체 기록:</span>
            <span class="font-medium">${stats.totalCount}</span>
        </div>
        <div class="flex justify-between">
            <span class="text-gray-600">핵심 업무:</span>
            <span class="font-medium text-red-600">${stats.criticalCount}</span>
        </div>
        <div class="flex justify-between">
            <span class="text-gray-600">중요 업무:</span>
            <span class="font-medium text-orange-600">${stats.importantCount}</span>
        </div>
        <div class="flex justify-between">
            <span class="text-gray-600">실행/구현:</span>
            <span class="font-medium">${stats.implementationCount}</span>
        </div>
        <div class="flex justify-between">
            <span class="text-gray-600">계획/설계:</span>
            <span class="font-medium">${stats.planningCount}</span>
        </div>
        <div class="flex justify-between">
            <span class="text-gray-600">소통/협업:</span>
            <span class="font-medium">${stats.collaborationCount}</span>
        </div>
    `;
}

// 템플릿 로드
async function loadTemplates() {
    try {
        const response = await fetch('/api/templates');
        
        if (!response.ok) {
            throw new Error('템플릿 로드 실패');
        }
        
        const templates = await response.json();
        renderTemplates(templates);
        
    } catch (error) {
        console.error('템플릿 로드 실패:', error);
    }
}

// 템플릿 렌더링
function renderTemplates(templates) {
    const container = document.getElementById('template-list');
    container.innerHTML = '';
    
    templates.forEach(template => {
        const div = document.createElement('div');
        div.className = 'p-2 border border-gray-200 rounded text-xs cursor-pointer hover:bg-gray-50';
        div.innerHTML = `
            <div class="font-medium">${escapeHtml(template.name)}</div>
            <div class="text-gray-500 truncate">${escapeHtml(template.content)}</div>
        `;
        div.onclick = () => useTemplate(template);
        container.appendChild(div);
    });
}

// 템플릿 사용
function useTemplate(template) {
    document.getElementById('content-input').value = template.content;
    if (template.defaultCategory) {
        document.getElementById('category-select').value = template.defaultCategory;
    }
    // 포커스를 중요도 선택으로 이동
    document.getElementById('importance-select').focus();
}

// 업무 로그 삭제
async function deleteWorkLog(id) {
    if (!confirm('이 업무 로그를 삭제하시겠습니까?')) {
        return;
    }
    
    try {
        const response = await fetch(`/api/worklogs/${id}`, {
            method: 'DELETE'
        });
        
        if (response.ok) {
            // DOM에서 해당 엘리먼트 제거
            const element = document.querySelector(`[data-id="${id}"]`);
            if (element) {
                element.remove();
            }
            
            // 통계 업데이트
            await loadStats();
            
            showAlert('업무 로그가 삭제되었습니다.', 'success');
        } else {
            throw new Error('삭제 실패');
        }
    } catch (error) {
        console.error('업무 로그 삭제 실패:', error);
        showAlert('업무 로그 삭제에 실패했습니다.', 'error');
    }
}

// 업무 로그 편집 (간단한 프롬프트로 구현)
async function editWorkLog(id) {
    const element = document.querySelector(`[data-id="${id}"]`);
    const currentContent = element.querySelector('p').textContent;
    
    const newContent = prompt('업무 내용을 수정하세요:', currentContent);
    if (!newContent || newContent === currentContent) {
        return;
    }
    
    try {
        // 현재 데이터를 서버에서 가져와서 업데이트
        const getResponse = await fetch(`/api/worklogs/${id}`);
        if (!getResponse.ok) {
            throw new Error('업무 로그 조회 실패');
        }
        
        const workLog = await getResponse.json();
        workLog.content = newContent;
        
        const updateResponse = await fetch(`/api/worklogs/${id}`, {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(workLog)
        });
        
        if (updateResponse.ok) {
            // DOM 업데이트
            element.querySelector('p').textContent = newContent;
            showAlert('업무 로그가 수정되었습니다.', 'success');
        } else {
            throw new Error('수정 실패');
        }
    } catch (error) {
        console.error('업무 로그 수정 실패:', error);
        showAlert('업무 로그 수정에 실패했습니다.', 'error');
    }
}

// 템플릿 모달 표시 (간단한 프롬프트로 구현)
async function showTemplateModal() {
    const name = prompt('템플릿 이름을 입력하세요:');
    if (!name) return;
    
    const content = prompt('템플릿 내용을 입력하세요:');
    if (!content) return;
    
    try {
        const response = await fetch('/api/templates', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                name: name,
                content: content,
                defaultCategory: null
            })
        });
        
        if (response.ok) {
            await loadTemplates();
            showAlert('템플릿이 추가되었습니다!', 'success');
        } else {
            throw new Error('템플릿 추가 실패');
        }
    } catch (error) {
        console.error('템플릿 추가 실패:', error);
        showAlert('템플릿 추가에 실패했습니다.', 'error');
    }
}

// 유틸리티 함수들
function debounce(func, wait) {
    let timeout;
    return function executedFunction(...args) {
        const later = () => {
            clearTimeout(timeout);
            func(...args);
        };
        clearTimeout(timeout);
        timeout = setTimeout(later, wait);
    };
}

function escapeHtml(text) {
    const map = {
        '&': '&amp;',
        '<': '&lt;',
        '>': '&gt;',
        '"': '&quot;',
        "'": '&#039;'
    };
    return text.replace(/[&<>"']/g, m => map[m]);
}

function formatDateTime(dateTimeString) {
    const date = new Date(dateTimeString);
    const now = new Date();
    const diff = now - date;
    const diffDays = Math.floor(diff / (1000 * 60 * 60 * 24));
    
    if (diffDays === 0) {
        return date.toLocaleTimeString('ko-KR', { 
            hour: '2-digit', 
            minute: '2-digit' 
        });
    } else if (diffDays === 1) {
        return '어제';
    } else if (diffDays < 7) {
        return `${diffDays}일 전`;
    } else {
        return date.toLocaleDateString('ko-KR');
    }
}

function getImportanceText(importance) {
    const map = {
        'CRITICAL': '⭐⭐⭐ 핵심',
        'IMPORTANT': '⭐⭐ 중요',
        'NORMAL': '⭐ 보통'
    };
    return map[importance] || importance;
}

function getCategoryText(category) {
    const map = {
        'IMPLEMENTATION': '🔨 실행/구현',
        'PLANNING': '📋 계획/설계',
        'COLLABORATION': '💬 소통/협업',
        'LEARNING': '📚 학습/연구',
        'ANALYSIS': '🔍 분석/검토',
        'PROBLEM_SOLVING': '🛠️ 문제해결',
        'DOCUMENTATION': '📊 보고/문서화'
    };
    return map[category] || category;
}

function showAlert(message, type = 'info') {
    // 간단한 알림 구현 (나중에 더 예쁜 모달로 교체 가능)
    const alertClass = type === 'success' ? 'bg-green-100 text-green-800' : 
                     type === 'error' ? 'bg-red-100 text-red-800' : 
                     'bg-blue-100 text-blue-800';
    
    const alertDiv = document.createElement('div');
    alertDiv.className = `fixed top-4 right-4 z-50 p-4 rounded-md ${alertClass} max-w-sm`;
    alertDiv.textContent = message;
    
    document.body.appendChild(alertDiv);
    
    setTimeout(() => {
        alertDiv.remove();
    }, 3000);
}

// 현재 날짜/시간 업데이트
function updateDateTime() {
    const now = new Date();
    const options = {
        year: 'numeric',
        month: 'long',
        day: 'numeric',
        hour: '2-digit',
        minute: '2-digit',
        second: '2-digit'
    };
    document.getElementById('currentDateTime').textContent = now.toLocaleDateString('ko-KR', options);
}
