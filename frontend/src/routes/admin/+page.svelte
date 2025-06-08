<script>
  import { goto } from '$app/navigation';
  import { onMount } from 'svelte';
  import { authStore } from '$lib/stores/auth.js';
  import { tabStore } from '$lib/stores/tabs.js';

  onMount(() => {
    // 슈퍼어드민은 기본적으로 사용자 관리 페이지로 이동
    const unsubscribe = authStore.subscribe(auth => {
      if (auth.isAuthenticated && auth.user?.roles?.includes('SUPER_ADMIN')) {
        console.log('🔐 Admin 루트 페이지 - 사용자 관리로 리다이렉트');
        
        // 사용자 관리 탭 생성
        tabStore.openTab({
          id: 'admin-ADMIN_USERS',
          title: '사용자 관리',
          path: '/admin/users',
          system: 'admin',
          closeable: true,
          secure: true,
          priority: 'HIGH'
        });
        
        goto('/admin/users');
      }
    });

    return unsubscribe;
  });
</script>

<svelte:head>
  <title>슈퍼어드민 - WebPos</title>
</svelte:head>

<div class="min-h-screen flex items-center justify-center">
  <div class="text-center">
    <div class="animate-spin rounded-full h-12 w-12 border-b-2 border-red-600 mx-auto"></div>
    <p class="mt-4 text-gray-600">시스템을 초기화하고 있습니다...</p>
  </div>
</div>
