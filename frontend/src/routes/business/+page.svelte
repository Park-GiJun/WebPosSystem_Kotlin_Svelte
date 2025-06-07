<script>
  import { onMount } from 'svelte';
  import { goto } from '$app/navigation';
  import { authStore } from '$lib/stores/auth.js';
  import { Building, Store, Monitor, BarChart, Users } from 'lucide-svelte';

  $: user = $authStore.user;

  onMount(() => {
    // 사용자 권한에 따라 적절한 기본 페이지로 리디렉션
    if (user?.roles) {
      if (user.roles.includes('SUPER_ADMIN') || user.roles.includes('SYSTEM_ADMIN')) {
        goto('/admin/organizations');
      } else if (user.roles.includes('HQ_MANAGER')) {
        goto('/business/headquarters/stores');
      } else if (user.roles.includes('STORE_MANAGER')) {
        goto('/business/pos');
      } else {
        goto('/business/reports'); // 기본값
      }
    }
  });
</script>

<svelte:head>
  <title>영업정보시스템 - WebPos</title>
</svelte:head>

<div class="h-full flex items-center justify-center">
  <div class="text-center">
    <div class="animate-spin rounded-full h-12 w-12 border-b-2 border-primary-600 mx-auto"></div>
    <p class="mt-4 text-gray-600">적절한 페이지로 이동중...</p>
  </div>
</div>
