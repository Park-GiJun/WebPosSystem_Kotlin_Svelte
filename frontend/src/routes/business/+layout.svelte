<script>
  import { authStore } from '$lib/stores/auth.js';
  import { goto } from '$app/navigation';
  import { onMount } from 'svelte';

  onMount(() => {
    // 영업정보시스템 접근 권한 확인
    const unsubscribe = authStore.subscribe(auth => {
      if (auth.isAuthenticated) {
        // 본사 관리자 권한 확인 또는 메뉴 권한 확인
        const hasBusinessAccess = auth.user?.roles?.some(role => 
          ['SUPER_ADMIN', 'SYSTEM_ADMIN', 'HQ_MANAGER'].includes(role)
        ) || auth.hasPermission('BUSINESS', 'READ');
        
        if (!hasBusinessAccess) {
          goto('/dashboard');
        }
      }
    });

    return unsubscribe;
  });
</script>

<!-- 영업정보시스템도 동일한 레이아웃 사용 -->
<slot />
