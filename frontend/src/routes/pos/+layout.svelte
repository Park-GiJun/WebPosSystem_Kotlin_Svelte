<script>
  import { authStore } from '$lib/stores/auth.js';
  import { goto } from '$app/navigation';
  import { onMount } from 'svelte';

  onMount(() => {
    // POS 시스템 접근 권한 확인
    const unsubscribe = authStore.subscribe(auth => {
      if (auth.isAuthenticated) {
        const hasPosAccess = auth.user?.roles?.some(role => 
          ['SUPER_ADMIN', 'SYSTEM_ADMIN', 'STORE_MANAGER', 'USER'].includes(role)
        );
        
        if (!hasPosAccess) {
          // 권한이 없으면 대시보드로 리다이렉트
          goto('/dashboard');
        }
      }
    });

    return unsubscribe;
  });
</script>

<slot />
