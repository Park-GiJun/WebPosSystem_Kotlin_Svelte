// WebPos Swagger UI 커스텀 기능

// Swagger UI가 로드된 후 실행되는 함수
window.addEventListener('DOMContentLoaded', function() {
    // Swagger UI 완전 로딩 대기
    const checkSwaggerUI = setInterval(() => {
        if (window.ui && document.querySelector('.swagger-ui')) {
            clearInterval(checkSwaggerUI);
            initializeCustomFeatures();
        }
    }, 100);
});

function initializeCustomFeatures() {
    console.log('🚀 WebPos Swagger UI 커스텀 기능 초기화 중...');
    
    // 1. 커스텀 헤더 추가
    addCustomHeader();
    
    // 2. 유용한 정보 패널 추가
    addInfoPanel();
    
    // 3. 단축키 지원
    addKeyboardShortcuts();
    
    // 4. API 테스트 도우미
    addTestHelper();
    
    // 5. 다크모드 토글 추가
    addDarkModeToggle();
    
    // 6. 즐겨찾기 기능
    addBookmarkFeature();
    
    console.log('✅ WebPos Swagger UI 커스텀 기능 초기화 완료');
}

// 커스텀 헤더 추가
function addCustomHeader() {
    const headerHtml = `
        <div id="webpos-custom-header" style="
            background: linear-gradient(90deg, #2E8B57, #3CB371);
            color: white;
            padding: 10px 20px;
            text-align: center;
            font-weight: bold;
            border-bottom: 3px solid #20B2AA;
            box-shadow: 0 2px 10px rgba(0,0,0,0.1);
        ">
            🏪 WebPos API Documentation
            <span style="margin-left: 20px; font-size: 0.9em; opacity: 0.9;">
                📚 Complete API Reference for Modern POS System
            </span>
        </div>
    `;
    
    const body = document.body;
    body.insertAdjacentHTML('afterbegin', headerHtml);
}

// 유용한 정보 패널 추가
function addInfoPanel() {
    const infoPanelHtml = `
        <div id="webpos-info-panel" style="
            background: #f8f9fa;
            border: 1px solid #dee2e6;
            border-radius: 8px;
            padding: 20px;
            margin: 20px;
            font-family: 'Segoe UI', sans-serif;
        ">
            <h3 style="color: #2E8B57; margin-top: 0;">🚀 빠른 시작 가이드</h3>
            <div style="display: grid; grid-template-columns: repeat(auto-fit, minmax(250px, 1fr)); gap: 15px;">
                <div>
                    <h4>🔐 인증</h4>
                    <p>API 테스트를 위해 먼저 로그인 후 JWT 토큰을 획득하세요.</p>
                    <code>POST /api/v1/auth/login</code>
                </div>
                <div>
                    <h4>📋 기본 정보</h4>
                    <p>• Base URL: <code>${window.location.origin}</code></p>
                    <p>• Content-Type: <code>application/json</code></p>
                    <p>• Auth: <code>Bearer {token}</code></p>
                </div>
                <div>
                    <h4>🛠️ 개발 도구</h4>
                    <p>• <a href="/actuator/health" target="_blank">헬스체크</a></p>
                    <p>• <a href="/actuator/metrics" target="_blank">메트릭</a></p>
                    <p>• <a href="/actuator/info" target="_blank">앱 정보</a></p>
                </div>
            </div>
            <div style="margin-top: 15px; padding: 10px; background: #e7f3ff; border-radius: 4px;">
                💡 <strong>Tip:</strong> 각 API 그룹은 상단 탭으로 구분되어 있습니다. 
                <kbd>Ctrl</kbd> + <kbd>F</kbd>로 특정 API를 검색할 수 있습니다.
            </div>
        </div>
    `;
    
    const wrapper = document.querySelector('.swagger-ui .wrapper');
    if (wrapper) {
        wrapper.insertAdjacentHTML('afterbegin', infoPanelHtml);
    }
}

// 단축키 지원
function addKeyboardShortcuts() {
    document.addEventListener('keydown', function(e) {
        // Ctrl+/ : 도움말 표시
        if (e.ctrlKey && e.key === '/') {
            e.preventDefault();
            showHelp();
        }
        
        // Ctrl+E : 모든 섹션 펼치기
        if (e.ctrlKey && e.key === 'e') {
            e.preventDefault();
            expandAllSections();
        }
        
        // Ctrl+R : 모든 섹션 접기
        if (e.ctrlKey && e.key === 'r') {
            e.preventDefault();
            collapseAllSections();
        }
        
        // Ctrl+T : 상단으로 이동
        if (e.ctrlKey && e.key === 't') {
            e.preventDefault();
            window.scrollTo({ top: 0, behavior: 'smooth' });
        }
    });
}

// 다크모드 토글
function addDarkModeToggle() {
    const toggleHtml = `
        <button id="dark-mode-toggle" style="
            position: fixed;
            top: 20px;
            right: 20px;
            z-index: 9999;
            background: #2E8B57;
            color: white;
            border: none;
            border-radius: 50%;
            width: 50px;
            height: 50px;
            cursor: pointer;
            font-size: 20px;
            box-shadow: 0 4px 12px rgba(0,0,0,0.15);
            transition: all 0.3s ease;
        " title="다크모드 토글">
            🌙
        </button>
    `;
    
    document.body.insertAdjacentHTML('beforeend', toggleHtml);
    
    const toggle = document.getElementById('dark-mode-toggle');
    toggle.addEventListener('click', function() {
        document.body.classList.toggle('dark-mode');
        const isDark = document.body.classList.contains('dark-mode');
        this.innerHTML = isDark ? '☀️' : '🌙';
        localStorage.setItem('webpos-dark-mode', isDark);
    });
    
    // 저장된 다크모드 설정 로드
    if (localStorage.getItem('webpos-dark-mode') === 'true') {
        toggle.click();
    }
}

// 즐겨찾기 기능
function addBookmarkFeature() {
    // 각 API 엔드포인트에 즐겨찾기 버튼 추가
    const observer = new MutationObserver(function(mutations) {
        mutations.forEach(function(mutation) {
            if (mutation.addedNodes.length) {
                addBookmarkButtons();
            }
        });
    });
    
    observer.observe(document.body, { childList: true, subtree: true });
    
    // 초기 즐겨찾기 버튼 추가
    setTimeout(addBookmarkButtons, 1000);
}

function addBookmarkButtons() {
    const summaries = document.querySelectorAll('.opblock-summary:not(.bookmark-added)');
    summaries.forEach(summary => {
        const bookmarkBtn = document.createElement('button');
        bookmarkBtn.innerHTML = '⭐';
        bookmarkBtn.className = 'bookmark-btn';
        bookmarkBtn.style.cssText = `
            background: none;
            border: none;
            font-size: 16px;
            cursor: pointer;
            margin-left: 10px;
            opacity: 0.6;
            transition: opacity 0.3s ease;
        `;
        
        const path = summary.querySelector('.opblock-summary-path')?.textContent;
        const method = summary.querySelector('.opblock-summary-method')?.textContent;
        
        if (path && method) {
            const bookmarkKey = `${method}-${path}`;
            const isBookmarked = localStorage.getItem(`bookmark-${bookmarkKey}`) === 'true';
            
            if (isBookmarked) {
                bookmarkBtn.style.opacity = '1';
            }
            
            bookmarkBtn.addEventListener('click', function(e) {
                e.stopPropagation();
                toggleBookmark(bookmarkKey, bookmarkBtn);
            });
            
            summary.appendChild(bookmarkBtn);
            summary.classList.add('bookmark-added');
        }
    });
}

// API 테스트 도우미
function addTestHelper() {
    // Try it out 버튼에 이벤트 리스너 추가
    document.addEventListener('click', function(e) {
        if (e.target.classList.contains('try-out__btn')) {
            setTimeout(() => {
                addSampleData(e.target);
            }, 100);
        }
    });
}

function addSampleData(button) {
    const operationWrapper = button.closest('.opblock');
    if (!operationWrapper) return;
    
    // 샘플 데이터 버튼 추가
    const parametersWrapper = operationWrapper.querySelector('.parameters-container');
    if (parametersWrapper && !parametersWrapper.querySelector('.sample-data-btn')) {
        const sampleBtn = document.createElement('button');
        sampleBtn.className = 'btn sample-data-btn';
        sampleBtn.innerHTML = '📝 샘플 데이터 입력';
        sampleBtn.style.cssText = `
            background: #17a2b8;
            color: white;
            margin: 10px 5px;
            padding: 5px 10px;
            border: none;
            border-radius: 4px;
            cursor: pointer;
        `;
        
        sampleBtn.addEventListener('click', function() {
            fillSampleData(operationWrapper);
        });
        
        parametersWrapper.appendChild(sampleBtn);
    }
}

function fillSampleData(operationWrapper) {
    const inputs = operationWrapper.querySelectorAll('input, textarea');
    inputs.forEach(input => {
        if (input.placeholder && !input.value) {
            input.value = input.placeholder;
            input.dispatchEvent(new Event('input', { bubbles: true }));
        }
    });
    
    showNotification('📝 샘플 데이터가 입력되었습니다', 'success');
}

function toggleBookmark(key, button) {
    const isBookmarked = localStorage.getItem(`bookmark-${key}`) === 'true';
    const newState = !isBookmarked;
    
    localStorage.setItem(`bookmark-${key}`, newState);
    button.style.opacity = newState ? '1' : '0.6';
    
    showNotification(
        newState ? '⭐ 즐겨찾기에 추가되었습니다' : '✨ 즐겨찾기에서 제거되었습니다',
        'info'
    );
}

// 유틸리티 함수들
function showHelp() {
    const helpHtml = `
        <div id="help-modal" style="
            position: fixed;
            top: 0;
            left: 0;
            width: 100%;
            height: 100%;
            background: rgba(0,0,0,0.8);
            z-index: 10000;
            display: flex;
            justify-content: center;
            align-items: center;
        ">
            <div style="
                background: white;
                padding: 30px;
                border-radius: 12px;
                max-width: 600px;
                max-height: 80vh;
                overflow-y: auto;
            ">
                <h2 style="color: #2E8B57; margin-top: 0;">⌨️ 단축키 도움말</h2>
                <table style="width: 100%; border-collapse: collapse;">
                    <tr><td><kbd>Ctrl</kbd> + <kbd>/</kbd></td><td>이 도움말 표시</td></tr>
                    <tr><td><kbd>Ctrl</kbd> + <kbd>E</kbd></td><td>모든 섹션 펼치기</td></tr>
                    <tr><td><kbd>Ctrl</kbd> + <kbd>R</kbd></td><td>모든 섹션 접기</td></tr>
                    <tr><td><kbd>Ctrl</kbd> + <kbd>T</kbd></td><td>상단으로 이동</td></tr>
                    <tr><td><kbd>Ctrl</kbd> + <kbd>F</kbd></td><td>페이지 내 검색</td></tr>
                </table>
                <h3 style="color: #2E8B57;">🎯 기능</h3>
                <ul>
                    <li>⭐ 각 API에 즐겨찾기 버튼 클릭으로 북마크</li>
                    <li>📝 "샘플 데이터 입력" 버튼으로 빠른 테스트</li>
                    <li>🌙 우상단 버튼으로 다크모드 토글</li>
                    <li>🎨 반응형 디자인 및 커스텀 스타일</li>
                </ul>
                <button onclick="document.getElementById('help-modal').remove()" style="
                    background: #2E8B57;
                    color: white;
                    border: none;
                    padding: 10px 20px;
                    border-radius: 6px;
                    cursor: pointer;
                    float: right;
                    margin-top: 20px;
                ">닫기</button>
            </div>
        </div>
    `;
    
    document.body.insertAdjacentHTML('beforeend', helpHtml);
}

function expandAllSections() {
    const expandButtons = document.querySelectorAll('.expand-operation');
    expandButtons.forEach(button => {
        if (!button.classList.contains('expanded')) {
            button.click();
        }
    });
    showNotification('📖 모든 섹션이 펼쳐졌습니다', 'info');
}

function collapseAllSections() {
    const expandButtons = document.querySelectorAll('.expand-operation.expanded');
    expandButtons.forEach(button => {
        button.click();
    });
    showNotification('📑 모든 섹션이 접혔습니다', 'info');
}

function showNotification(message, type = 'info') {
    const notification = document.createElement('div');
    notification.style.cssText = `
        position: fixed;
        top: 80px;
        right: 20px;
        z-index: 9999;
        padding: 12px 20px;
        border-radius: 6px;
        color: white;
        font-weight: 500;
        box-shadow: 0 4px 12px rgba(0,0,0,0.15);
        transform: translateX(100%);
        transition: transform 0.3s ease;
        max-width: 300px;
    `;
    
    const colors = {
        success: '#28a745',
        error: '#dc3545',
        warning: '#ffc107',
        info: '#17a2b8'
    };
    
    notification.style.background = colors[type] || colors.info;
    notification.textContent = message;
    
    document.body.appendChild(notification);
    
    // 애니메이션으로 표시
    setTimeout(() => {
        notification.style.transform = 'translateX(0)';
    }, 10);
    
    // 3초 후 자동 제거
    setTimeout(() => {
        notification.style.transform = 'translateX(100%)';
        setTimeout(() => {
            if (notification.parentNode) {
                notification.parentNode.removeChild(notification);
            }
        }, 300);
    }, 3000);
}

// 페이지 로드 완료 시 추가 기능 초기화
setTimeout(initializeCustomFeatures, 2000);
