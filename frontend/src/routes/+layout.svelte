<script>
  import '../app.css';
  import { page } from '$app/stores';
  import { authStore } from '$lib/stores/auth.js';
  import { goto } from '$app/navigation';
  import { onMount } from 'svelte';
  import ToastContainer from '$lib/components/Common/ToastContainer.svelte';

  onMount(() => {
    // 페이지 로드 시 토큰 확인
    authStore.checkAuth();
    
    // 인증이 필요한 페이지에서 로그인 상태 확인
    authStore.subscribe(auth => {
      if (!auth.isAuthenticated && $page.url.pathname !== '/login' && $page.url.pathname !== '/') {
        goto('/login');
      }
    });
  });
</script>

<main class="min-h-screen bg-gray-50">
  <slot />
</main>

<!-- 전역 토스트 컨테이너 -->
<ToastContainer />
