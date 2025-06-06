<script>
  import { authStore } from '$lib/stores/auth.js';
  import { goto } from '$app/navigation';
  import { onMount } from 'svelte';

  onMount(() => {
    // POS 시스템 접근 권한 확인
    const unsubscribe = authStore.subscribe(auth => {
      if (auth.isAuthenticated) {
        // 매장 관련 권한 확인 또는 메뉴 권한 확인
        const hasPosAccess = auth.user?.roles?.some(role => 
          ['SUPER_ADMIN', 'SYSTEM_ADMIN', 'STORE_MANAGER', 'USER'].includes(role)
        ) || auth.hasPermission('POS', 'READ');
        
        if (!hasPosAccess) {
          goto('/dashboard');
        }
      }
    });

    return unsubscribe;
  });
</script>

<!-- POS시스템도 동일한 레이아웃 사용 -->
<slot />
