<script>
  import { onMount } from 'svelte';
  import { authStore } from '$lib/stores/auth.js';

  let authState = {};
  let isLoading = false;
  let debugInfo = {};

  // 인증 상태 구독
  $: authState = $authStore;

  onMount(() => {
    // 백엔드 연결 테스트
    testBackendConnection();
    
    // 디버그 정보 수집
    collectDebugInfo();
  });

  function collectDebugInfo() {
    debugInfo = {
      timestamp: new Date().toISOString(),
      userAgent: navigator.userAgent,
      url: window.location.href,
      localStorage: {
        hasToken: !!localStorage.getItem('auth_token'),
        tokenLength: localStorage.getItem('auth_token')?.length || 0
      }
    };
  }

  function testBackendConnection() {
    console.group('🔌 백엔드 연결 테스트');
    fetch('/api/v1/health')
      .then(response => {
        console.log('💚 Health Check:', response.status);
        return response.json();
      })
      .then(data => console.log('💚 Health 응답:', data))
      .catch(error => console.error('❌ Health Check 실패:', error));
    console.groupEnd();
  }

  async function testLoginFlow() {
    isLoading = true;
    console.log('🧪 로그인 플로우 테스트 시작');
    
    try {
      const result = await authStore.login({
        username: 'admin',
        password: 'admin'
      });
      
      console.log('🧪 로그인 결과:', result);
    } catch (error) {
      console.error('🧪 로그인 테스트 실패:', error);
    } finally {
      isLoading = false;
    }
  }

  async function testMenusOnly() {
    isLoading = true;
    console.log('🧪 메뉴 전용 테스트 시작');
    
    const token = localStorage.getItem('auth_token');
    if (!token) {
      console.error('🧪 토큰이 없습니다');
      isLoading = false;
      return;
    }

    try {
      const response = await fetch('/api/v1/permissions/my-menus', {
        headers: {
          'Authorization': `Bearer ${token}`,
        },
      });

      console.log('🧪 메뉴 응답 상태:', response.status);
      
      if (response.ok) {
        const data = await response.json();
        console.log('🧪 메뉴 데이터:', data);
      } else {
        const errorText = await response.text();
        console.error('🧪 메뉴 오류:', errorText);
      }
    } catch (error) {
      console.error('🧪 메뉴 테스트 실패:', error);
    } finally {
      isLoading = false;
    }
  }

  async function checkHealth() {
    console.log('🧪 헬스 체크 시작');
    
    try {
      const response = await fetch('/api/v1/health');
      console.log('🧪 헬스 응답:', response.status);
      
      if (response.ok) {
        const data = await response.json();
        console.log('🧪 헬스 데이터:', data);
      }
    } catch (error) {
      console.error('🧪 헬스 체크 실패:', error);
    }
  }
</script>

<svelte:head>
  <title>디버그 페이지</title>
</svelte:head>

<div class="container mx-auto p-6 max-w-4xl">
  <div class="bg-white rounded-lg shadow-lg p-6">
    <h1 class="text-3xl font-bold text-gray-900 mb-6">🔧 디버그 대시보드</h1>
    
    <!-- 시스템 정보 -->
    <div class="grid grid-cols-1 md:grid-cols-2 gap-6 mb-8">
      <div class="bg-gray-50 p-4 rounded-lg">
        <h2 class="text-lg font-semibold text-gray-800 mb-3">📊 시스템 정보</h2>
        <div class="space-y-2 text-sm">
          <div><strong>타임스탬프:</strong> {debugInfo.timestamp}</div>
          <div><strong>URL:</strong> {debugInfo.url}</div>
          <div><strong>토큰 존재:</strong> {debugInfo.localStorage?.hasToken ? '✅' : '❌'}</div>
          <div><strong>토큰 길이:</strong> {debugInfo.localStorage?.tokenLength || 0}자</div>
        </div>
      </div>

      <div class="bg-blue-50 p-4 rounded-lg">
        <h2 class="text-lg font-semibold text-blue-800 mb-3">🔐 인증 상태</h2>
        <div class="space-y-2 text-sm">
          <div><strong>인증됨:</strong> {authState.isAuthenticated ? '✅' : '❌'}</div>
          <div><strong>사용자:</strong> {authState.user?.username || 'N/A'}</div>
          <div><strong>역할:</strong> {authState.user?.roles?.join(', ') || 'N/A'}</div>
          <div><strong>메뉴 수:</strong> {authState.menus?.length || 0}개</div>
          <div><strong>권한 수:</strong> {authState.permissions?.length || 0}개</div>
        </div>
      </div>
    </div>

    <!-- 메뉴 상세 정보 -->
    {#if authState.menus?.length > 0}
      <div class="bg-green-50 p-4 rounded-lg mb-6">
        <h2 class="text-lg font-semibold text-green-800 mb-3">📋 메뉴 상세</h2>
        <div class="grid grid-cols-1 md:grid-cols-3 gap-4 text-sm">
          <div>
            <h3 class="font-medium text-red-700 mb-2">🛡️ ADMIN 메뉴</h3>
            <ul class="space-y-1">
              {#each authState.menus.filter(m => m.menu_code?.startsWith('ADMIN')) as menu}
                <li class="text-xs">• {menu.menu_name} ({menu.menu_code})</li>
              {/each}
            </ul>
          </div>
          
          <div>
            <h3 class="font-medium text-blue-700 mb-2">🏢 BUSINESS 메뉴</h3>
            <ul class="space-y-1">
              {#each authState.menus.filter(m => m.menu_code?.startsWith('BUSINESS')) as menu}
                <li class="text-xs">• {menu.menu_name} ({menu.menu_code})</li>
              {/each}
            </ul>
          </div>
          
          <div>
            <h3 class="font-medium text-green-700 mb-2">🛒 POS 메뉴</h3>
            <ul class="space-y-1">
              {#each authState.menus.filter(m => m.menu_code?.startsWith('POS')) as menu}
                <li class="text-xs">• {menu.menu_name} ({menu.menu_code})</li>
              {/each}
            </ul>
          </div>
        </div>
      </div>
    {/if}

    <!-- 테스트 버튼들 -->
    <div class="space-y-4">
      <h2 class="text-lg font-semibold text-gray-800">🧪 테스트 도구</h2>
      <div class="flex flex-wrap gap-3">
        <button 
          class="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 disabled:opacity-50"
          on:click={checkHealth}
          disabled={isLoading}
        >
          🏥 헬스 체크
        </button>
        
        <button 
          class="px-4 py-2 bg-green-600 text-white rounded-lg hover:bg-green-700 disabled:opacity-50"
          on:click={testLoginFlow}
          disabled={isLoading}
        >
          🔐 로그인 테스트
        </button>
        
        <button 
          class="px-4 py-2 bg-purple-600 text-white rounded-lg hover:bg-purple-700 disabled:opacity-50"
          on:click={testMenusOnly}
          disabled={isLoading}
        >
          📋 메뉴 로드 테스트
        </button>
        
        <button 
          class="px-4 py-2 bg-gray-600 text-white rounded-lg hover:bg-gray-700"
          on:click={collectDebugInfo}
        >
          🔄 정보 새로고침
        </button>
      </div>
    </div>

    {#if isLoading}
      <div class="mt-4 p-4 bg-yellow-50 border border-yellow-200 rounded-lg">
        <div class="flex items-center">
          <div class="animate-spin rounded-full h-4 w-4 border-2 border-yellow-600 border-t-transparent mr-2"></div>
          <span class="text-yellow-800">테스트 진행 중...</span>
        </div>
      </div>
    {/if}

    <!-- 로그 확인 안내 -->
    <div class="mt-6 p-4 bg-gray-100 rounded-lg">
      <p class="text-gray-700 text-sm">
        <strong>📝 로그 확인:</strong> 브라우저 개발자 도구 콘솔(F12)에서 상세한 디버깅 정보를 확인할 수 있습니다.
      </p>
    </div>
  </div>
</div>
