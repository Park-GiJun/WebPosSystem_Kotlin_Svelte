<script>
  import { authStore } from '$lib/stores/auth.js';
  import { goto } from '$app/navigation';
  import { onMount } from 'svelte';

  onMount(() => {
    // 슈퍼어드민 권한 확인
    const unsubscribe = authStore.subscribe(auth => {
      if (!auth.isAuthenticated) {
        goto('/login');
      } else if (auth.isAuthenticated && !auth.user?.roles?.includes('SUPER_ADMIN')) {
        // 권한이 없으면 시스템 선택 페이지로 리다이렉트
        goto('/system-select');
      }
    });

    return unsubscribe;
  });
</script>

<slot />
