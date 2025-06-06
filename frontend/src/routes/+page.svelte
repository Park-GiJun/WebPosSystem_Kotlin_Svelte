<script>
  import { onMount } from 'svelte';
  import { goto } from '$app/navigation';
  import { authStore } from '$lib/stores/auth.js';

  onMount(() => {
    // 이미 로그인된 경우 시스템 선택 페이지로 리다이렉트
    const unsubscribe = authStore.subscribe(auth => {
      if (auth.isAuthenticated) {
        goto('/system-select');
      } else {
        goto('/login');
      }
    });

    return unsubscribe;
  });
</script>

<div class="flex items-center justify-center min-h-screen">
  <div class="text-center">
    <div class="animate-spin rounded-full h-12 w-12 border-b-2 border-primary-600 mx-auto"></div>
    <p class="mt-4 text-gray-600">로딩 중...</p>
  </div>
</div>
