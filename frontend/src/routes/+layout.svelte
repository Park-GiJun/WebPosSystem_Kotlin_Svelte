<script>
  import '../app.css';
  import { page } from '$app/stores';
  import { authStore } from '$lib/stores/auth.js';
  import { goto } from '$app/navigation';
  import { onMount } from 'svelte';
  import ToastContainer from '$lib/components/Common/ToastContainer.svelte';

  onMount(async () => {
    // 페이지 로드 시 토큰 확인
    await authStore.checkAuth();
    
    // 인증이 필요한 페이지에서 로그인 상태 확인
    authStore.subscribe(auth => {
      const currentPath = $page.url.pathname;
      const publicPaths = ['/login', '/'];
      const systemSelectPath = '/system-select';
      
      // 로그인이 필요한 페이지에서 인증되지 않은 경우
      if (!auth.isAuthenticated && !publicPaths.includes(currentPath)) {
        console.log('🔒 인증되지 않은 접근, 로그인 페이지로 이동:', currentPath);
        goto('/login?error=unauthorized');
      }
      // 로그인은 되었지만 시스템 선택이 필요한 경우
      else if (auth.isAuthenticated && 
               !publicPaths.includes(currentPath) && 
               currentPath !== systemSelectPath &&
               !currentPath.startsWith('/admin') &&
               !currentPath.startsWith('/business') &&
               !currentPath.startsWith('/pos')) {
        console.log('🎯 시스템 선택이 필요함:', currentPath);
        goto('/system-select');
      }
    });
  });
</script>

<main class="min-h-screen bg-gray-50">
  <slot />
</main>

<!-- 전역 토스트 컨테이너 -->
<ToastContainer />
