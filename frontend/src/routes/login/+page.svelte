<script>
  import { onMount } from 'svelte';
  import { goto } from '$app/navigation';
  import { authStore } from '$lib/stores/auth.js';
  import { toastStore } from '$lib/stores/toast.js';
  import LoadingSpinner from '$lib/components/Common/LoadingSpinner.svelte';

  let username = '';
  let password = '';
  let isLoading = false;
  let rememberMe = false;

  // 인증 상태 구독
  let isAuthenticated = false;
  authStore.subscribe(state => {
    isAuthenticated = state.isAuthenticated;
  });

  onMount(() => {
    // 이미 로그인된 경우 대시보드로 리다이렉트
    if (isAuthenticated) {
      goto('/dashboard');
    }

    // URL에서 에러 메시지 확인
    const urlParams = new URLSearchParams(window.location.search);
    const error = urlParams.get('error');
    if (error === 'unauthorized') {
      toastStore.error('로그인이 필요합니다.');
    }
  });

  async function handleLogin() {
    if (!username.trim() || !password.trim()) {
      toastStore.error('사용자명과 비밀번호를 입력해주세요.');
      return;
    }

    isLoading = true;

    try {
      console.log('🔐 로그인 시도:', { username, rememberMe });
      
      const result = await authStore.login({
        username: username.trim(),
        password: password.trim()
      });

      if (result.success) {
        toastStore.success(`환영합니다, ${result.user.username}님!`);
        
        // Remember Me 처리
        if (rememberMe && typeof localStorage !== 'undefined') {
          localStorage.setItem('rememberMe', 'true');
          localStorage.setItem('rememberedUsername', username.trim());
        } else if (typeof localStorage !== 'undefined') {
          localStorage.removeItem('rememberMe');
          localStorage.removeItem('rememberedUsername');
        }

        // 잠시 후 대시보드로 이동
        setTimeout(() => {
          goto('/dashboard');
        }, 1000);
      }
    } catch (error) {
      console.error('❌ 로그인 오류:', error);
      
      let errorMessage = '로그인에 실패했습니다.';
      
      if (error.message.includes('사용자명') || error.message.includes('패스워드')) {
        errorMessage = '사용자명 또는 비밀번호가 올바르지 않습니다.';
      } else if (error.message.includes('잠겨')) {
        errorMessage = '계정이 잠겨있습니다. 관리자에게 문의하세요.';
      } else if (error.message.includes('비활성')) {
        errorMessage = '비활성화된 계정입니다. 관리자에게 문의하세요.';
      } else if (error.message.includes('인증')) {
        errorMessage = '이메일 인증이 필요합니다.';
      }
      
      toastStore.error(errorMessage);
      password = ''; // 비밀번호 필드 초기화
    } finally {
      isLoading = false;
    }
  }

  function handleKeyPress(event) {
    if (event.key === 'Enter') {
      handleLogin();
    }
  }

  function goToRegister() {
    goto('/register');
  }

  // Remember Me 기능 - 페이지 로드 시 저장된 사용자명 복원
  onMount(() => {
    if (typeof localStorage !== 'undefined') {
      const savedRememberMe = localStorage.getItem('rememberMe') === 'true';
      const savedUsername = localStorage.getItem('rememberedUsername');
      
      if (savedRememberMe && savedUsername) {
        username = savedUsername;
        rememberMe = true;
      }
    }
  });
</script>

<svelte:head>
  <title>로그인 - WebPos</title>
</svelte:head>

<div class="min-h-screen bg-gradient-to-br from-blue-50 to-indigo-100 flex items-center justify-center p-4">
  <div class="max-w-md w-full">
    <!-- 로고 및 제목 -->
    <div class="text-center mb-8">
      <div class="inline-flex items-center justify-center w-16 h-16 bg-indigo-600 rounded-full mb-4">
        <svg class="w-8 h-8 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 15v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2zm10-10V7a4 4 0 00-8 0v4h8z"></path>
        </svg>
      </div>
      <h1 class="text-3xl font-bold text-gray-900 mb-2">WebPos</h1>
      <p class="text-gray-600">통합 POS 관리 시스템</p>
    </div>

    <!-- 로그인 폼 -->
    <div class="bg-white rounded-xl shadow-lg p-8">
      <h2 class="text-2xl font-semibold text-gray-900 mb-6 text-center">로그인</h2>
      
      <form on:submit|preventDefault={handleLogin} class="space-y-6">
        <!-- 사용자명 입력 -->
        <div>
          <label for="username" class="block text-sm font-medium text-gray-700 mb-2">
            사용자명
          </label>
          <input
            id="username"
            type="text"
            bind:value={username}
            on:keypress={handleKeyPress}
            disabled={isLoading}
            class="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500 transition-colors disabled:opacity-50"
            placeholder="사용자명을 입력하세요"
            autocomplete="username"
            required
          />
        </div>

        <!-- 비밀번호 입력 -->
        <div>
          <label for="password" class="block text-sm font-medium text-gray-700 mb-2">
            비밀번호
          </label>
          <input
            id="password"
            type="password"
            bind:value={password}
            on:keypress={handleKeyPress}
            disabled={isLoading}
            class="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500 transition-colors disabled:opacity-50"
            placeholder="비밀번호를 입력하세요"
            autocomplete="current-password"
            required
          />
        </div>

        <!-- Remember Me 체크박스 -->
        <div class="flex items-center">
          <input
            id="remember-me"
            type="checkbox"
            bind:checked={rememberMe}
            disabled={isLoading}
            class="h-4 w-4 text-indigo-600 focus:ring-indigo-500 border-gray-300 rounded disabled:opacity-50"
          />
          <label for="remember-me" class="ml-2 block text-sm text-gray-700">
            로그인 상태 유지
          </label>
        </div>

        <!-- 로그인 버튼 -->
        <button
          type="submit"
          disabled={isLoading || !username.trim() || !password.trim()}
          class="w-full flex justify-center items-center py-3 px-4 border border-transparent rounded-lg shadow-sm text-sm font-medium text-white bg-indigo-600 hover:bg-indigo-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
        >
          {#if isLoading}
            <LoadingSpinner size="sm" />
            <span class="ml-2">로그인 중...</span>
          {:else}
            로그인
          {/if}
        </button>
      </form>

      <!-- 추가 링크 -->
      <div class="mt-6 text-center space-y-2">
        <button
          on:click={goToRegister}
          class="text-sm text-indigo-600 hover:text-indigo-500 transition-colors"
        >
          계정이 없으신가요? 회원가입
        </button>
        
        <div class="text-xs text-gray-500">
          <p>문의사항이 있으시면 관리자에게 연락하세요.</p>
        </div>
      </div>
    </div>

    <!-- 시스템 정보 -->
    <div class="mt-8 text-center text-sm text-gray-500">
      <p>WebPos v1.0 | © 2025 All rights reserved</p>
    </div>
  </div>
</div>

<style>
  /* 추가 스타일링 */
  .gradient-bg {
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  }
  
  input:focus {
    box-shadow: 0 0 0 3px rgba(99, 102, 241, 0.1);
  }
  
  button:focus {
    box-shadow: 0 0 0 3px rgba(99, 102, 241, 0.1);
  }
</style>
