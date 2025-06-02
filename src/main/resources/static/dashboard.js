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
    
    // 글자 수 카운터
    const contentInput = document.getElementById('content-input');
    if (contentInput) {
        contentInput.addEventListener('input', updateCharCount);
    }
    
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

// 빠른 추가 폼 처리
async function handleQuickAdd(event) {
    event.preventDefault();
    
    const formData = {
        content: document.getElementById('content-input').value.trim(),
        category: document.getElementById('category-select').value,
        importance: document.getElementById('importance-select').value,
        referenceUrl: document.getElementById('reference-url').value.trim() || null,
        memo: document.getElementById('memo').value.trim() || null
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
                ${workLog.memo ? `
                    <p class="text-gray-600 text-sm mb-2 italic">
                        <i class="fas fa-sticky-note mr-1"></i> ${escapeHtml(workLog.memo)}
                    </p>
                ` : ''}
                ${workLog.referenceUrl ? `
                    <a href="${workLog.referenceUrl}" target="_blank" class="inline-flex items-center text-sm text-blue-600 hover:text-blue-800">
                        <i class="fas fa-external-link-alt mr-1"></i>
                        ${workLog.memo || '참조 링크'}
                    </a>
                ` : ''}
                ${workLog.dailyTheme ? `
                    <div class="mt-2">
                        <span class="text-xs text-gray-500">테마: ${escapeHtml(workLog.dailyTheme.theme)}</span>
                    </div>
                ` : ''}
            </div>
            <div class="flex items-center space-x-2 ml-4">
                <button class="worklog-edit-btn text-gray-400 hover:text-gray-600">
                    <i class="fas fa-edit"></i>
                </button>
                <button class="worklog-delete-btn text-gray-400 hover:text-red-600">
                    <i class="fas fa-trash"></i>
                </button>
            </div>
        </div>
    `;
    return div;
}

// 업무로그 삭제/수정 버튼 이벤트 위임 (동적 항목 포함)
document.getElementById('worklog-list').addEventListener('click', function(e) {
    const editBtn = e.target.closest('.edit-worklog-btn');
    const deleteBtn = e.target.closest('.delete-worklog-btn');
    const worklogDiv = e.target.closest('[data-id]');
    if (!worklogDiv) return;
    const worklogId = worklogDiv.dataset.id;
    if (editBtn) {
        if (worklogId) editWorkLog(worklogId);
        e.preventDefault();
        e.stopPropagation();
        return;
    }
    if (deleteBtn) {
        if (worklogId) deleteWorkLog(worklogId);
        e.preventDefault();
        e.stopPropagation();
        return;
    }
});

// 더 많은 업무 로그 로드
async function loadMoreWorkLogs() {
    await loadWorkLogs(false);
}

// 검색 처리
function handleSearch(event) {
    const keyword = event.target.value.trim();
    searchFilters.keyword = keyword;
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

// 통계 로드 (최근 7일 추이, 스트릭, 주간 기록)
async function loadStats() {
    try {
        // 1. 최근 7일 추이
        const weeklyRes = await fetch('/api/worklogs/stats/weekly');
        const weekly = await weeklyRes.json();
        // 2. 스트릭
        const streakRes = await fetch('/api/worklogs/stats/streak');
        const streak = (await streakRes.json()).streak;
        // 3. 이번 주 기록 수
        const weekCountRes = await fetch('/api/worklogs/stats/week-count');
        const weekCount = (await weekCountRes.json()).weekCount;
        renderStats(weekly, streak, weekCount);
    } catch (error) {
        console.error('통계 로드 실패:', error);
    }
}

// 통계 렌더링 (차트 + 스트릭 + 주간 기록)
function renderStats(weekly, streak, weekCount) {
    const container = document.getElementById('stats-container');
    container.innerHTML = `
        <div class="mb-4">
            <canvas id="weeklyChart" height="80"></canvas>
        </div>
        <div class="flex justify-between items-center mb-2">
            <span class="text-gray-600">연속 기록 일수</span>
            <span class="font-bold text-blue-600 text-lg"><i class="fas fa-fire text-orange-500 mr-1"></i>${streak}일</span>
        </div>
        <div class="flex justify-between items-center">
            <span class="text-gray-600">이번 주 기록 수</span>
            <span class="font-bold text-green-600 text-lg">${weekCount}건</span>
        </div>
    `;
    // Chart.js 라인차트
    if (window.weeklyChartObj) window.weeklyChartObj.destroy();
    const ctx = document.getElementById('weeklyChart').getContext('2d');
    window.weeklyChartObj = new Chart(ctx, {
        type: 'line',
        data: {
            labels: Object.keys(weekly).map(d => d.slice(5)),
            datasets: [{
                label: '최근 7일 기록',
                data: Object.values(weekly),
                borderColor: '#6366f1',
                backgroundColor: 'rgba(99,102,241,0.1)',
                tension: 0.3,
                pointRadius: 4,
                pointBackgroundColor: '#6366f1',
                fill: true
            }]
        },
        options: {
            plugins: { legend: { display: false } },
            scales: {
                x: { grid: { display: false } },
                y: { beginAtZero: true, ticks: { stepSize: 1, precision: 0 } }
            }
        }
    });
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

// 템플릿 렌더링 및 관리
function renderTemplates(templates) {
    const container = document.getElementById('template-list');
    container.innerHTML = '';
    const management = document.getElementById('template-actions');
    if (management) management.innerHTML = '';

    templates.forEach(template => {
        // 템플릿 빠른 선택
        const div = document.createElement('div');
        div.className = 'p-2 border border-gray-200 rounded text-xs cursor-pointer hover:bg-gray-50 flex justify-between items-center';
        div.dataset.id = template.id;
        div.innerHTML = `
            <div class="flex-1">
                <div class="font-medium">${escapeHtml(template.name)}</div>
                <div class="text-gray-500 truncate">${escapeHtml(template.content)}</div>
                <div class="text-gray-400 mt-1">
                    ${template.defaultCategory ? getCategoryText(template.defaultCategory) : ''}
                    ${template.defaultImportance ? ' / ' + getImportanceText(template.defaultImportance) : ''}
                </div>
            </div>
            <div class="ml-2 flex flex-col gap-1">
                <button class="template-edit-btn text-blue-500 hover:underline text-xs" data-id="${template.id}">수정</button>
                <button class="template-delete-btn text-red-500 hover:underline text-xs" data-id="${template.id}">삭제</button>
            </div>
        `;
        div.onclick = e => {
            if (e.target.tagName === 'BUTTON') return;
            useTemplate(template);
        };
        container.appendChild(div);
        // 관리 패널에도 추가
        if (management) {
            const row = document.createElement('div');
            row.className = 'flex items-center justify-between border-b py-1';
            row.innerHTML = `
                <span class="font-medium">${escapeHtml(template.name)}</span>
                <span>
                    <button class="template-edit-btn text-blue-500 hover:underline text-xs mr-2" data-id="${template.id}">수정</button>
                    <button class="template-delete-btn text-red-500 hover:underline text-xs" data-id="${template.id}">삭제</button>
                </span>
            `;
            management.appendChild(row);
        }
    });
}

// 템플릿 수정/삭제 이벤트 위임
// template-list와 template-actions 모두에 이벤트 위임 적용
['template-list', 'template-actions'].forEach(containerId => {
    const container = document.getElementById(containerId);
    if (!container) return;
    container.addEventListener('click', async function(e) {
        const editBtn = e.target.closest('.template-edit-btn');
        const deleteBtn = e.target.closest('.template-delete-btn');
        if (editBtn) {
            const id = editBtn.dataset.id;
            if (id) window.editTemplate(id);
            e.preventDefault();
            e.stopPropagation();
            return;
        }
        if (deleteBtn) {
            const id = deleteBtn.dataset.id;
            if (id) window.deleteTemplate(id);
            e.preventDefault();
            e.stopPropagation();
            return;
        }
    });
});

// 템플릿 사용
function useTemplate(template) {
    document.getElementById('content-input').value = template.content;
    if (template.defaultCategory) {
        document.getElementById('category-select').value = template.defaultCategory;
    }
    if (template.defaultImportance) {
        document.getElementById('importance-select').value = template.defaultImportance;
    }
    document.getElementById('importance-select').focus();
}

// 템플릿 추가/수정 모달 → 폼 값 기반으로 동작
window.showTemplateModal = async function(templateId) {
    // 현재 입력 폼 값 읽기
    const content = document.getElementById('content-input').value.trim();
    const category = document.getElementById('category-select').value || null;
    const importance = document.getElementById('importance-select').value || null;
    const body = {
        name: templateId ? prompt('템플릿 설명:') : prompt('템플릿 이름:'),
        content,
        defaultCategory: category,
        defaultImportance: importance
    };
    if (!body.name) return;
    try {
        let response;
        if (templateId) {
            response = await fetch(`/api/templates/${templateId}`, {
                method: 'PUT',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(body)
            });
        } else {
            response = await fetch('/api/templates', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(body)
            });
        }
        if (response.ok) {
            await loadTemplates();
            showAlert('템플릿이 저장되었습니다!', 'success');
        } else {
            throw new Error('템플릿 저장 실패');
        }
    } catch (error) {
        showAlert('템플릿 저장에 실패했습니다.', 'error');
    }
}

// 템플릿 수정
window.editTemplate = function(id) {
    // id가 PointerEvent 등 객체가 아닌지 체크
    if (typeof id !== 'string' && typeof id !== 'number') return;
    showTemplateModal(id);
}

// 템플릿 삭제
window.deleteTemplate = async function(id) {
    if (!confirm('이 템플릿을 삭제하시겠습니까?')) return;
    try {
        const res = await fetch(`/api/templates/${id}`, { method: 'DELETE' });
        if (res.ok) {
            await loadTemplates();
            showAlert('템플릿이 삭제되었습니다.', 'success');
        } else {
            throw new Error('삭제 실패');
        }
    } catch (e) {
        showAlert('템플릿 삭제에 실패했습니다.', 'error');
    }
}

// 글자 수 카운터 업데이트
function updateCharCount(event) {
    const input = event.target;
    const currentLength = input.value.length;
    const maxLength = input.getAttribute('maxlength');
    const counterElement = document.getElementById('char-count');
    
    if (counterElement) {
        counterElement.textContent = currentLength;
        
        // 길이에 따라 스타일 변경
        if (currentLength > maxLength * 0.9) {
            counterElement.classList.add('text-red-500');
            counterElement.classList.remove('text-gray-400');
        } else {
            counterElement.classList.remove('text-red-500');
            counterElement.classList.add('text-gray-400');
        }
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

// 업무 로그 목록에서 삭제/수정 버튼 이벤트 위임

document.addEventListener('click', async function(e) {
    // 삭제 버튼
    if (e.target.closest('.worklog-delete-btn')) {
        const div = e.target.closest('[data-id]');
        if (!div) return;
        const id = div.dataset.id;
        if (!id) return;
        if (!confirm('이 업무 로그를 삭제하시겠습니까?')) return;
        try {
            const response = await fetch(`/api/worklogs/${id}`, { method: 'DELETE' });
            if (response.ok) {
                div.remove();
                await loadStats();
                showAlert('업무 로그가 삭제되었습니다.', 'success');
            } else {
                throw new Error('삭제 실패');
            }
        } catch (error) {
            showAlert('업무 로그 삭제에 실패했습니다.', 'error');
        }
        e.preventDefault();
        return;
    }
    // 수정 버튼
    if (e.target.closest('.worklog-edit-btn')) {
        const div = e.target.closest('[data-id]');
        if (!div) return;
        const id = div.dataset.id;
        if (!id) return;
        const currentContent = div.querySelector('p').textContent;
        const newContent = prompt('업무 내용을 수정하세요:', currentContent);
        if (!newContent || newContent === currentContent) return;
        try {
            const getResponse = await fetch(`/api/worklogs/${id}`);
            if (!getResponse.ok) throw new Error('업무 로그 조회 실패');
            const workLog = await getResponse.json();
            workLog.content = newContent;
            const updateResponse = await fetch(`/api/worklogs/${id}`, {
                method: 'PUT',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(workLog)
            });
            if (updateResponse.ok) {
                div.querySelector('p').textContent = newContent;
                showAlert('업무 로그가 수정되었습니다.', 'success');
            } else {
                throw new Error('수정 실패');
            }
        } catch (error) {
            showAlert('업무 로그 수정에 실패했습니다.', 'error');
        }
        e.preventDefault();
        return;
    }
});

// 템플릿 추가 버튼 이벤트 연결 (입력폼 값 기반)
const addTemplateBtn = document.getElementById('add-template');
if (addTemplateBtn) {
    addTemplateBtn.onclick = () => {
        window.showTemplateModal(undefined);
    };
}
