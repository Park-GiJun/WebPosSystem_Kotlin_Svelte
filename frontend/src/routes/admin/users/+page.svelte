<script>
  import { goto } from '$app/navigation';
  import { onMount } from 'svelte';

  onMount(() => {
    // 기본 사용자 관리 페이지는 내부 사용자 관리로 리다이렉트
    console.log('📍 사용자 관리 메인 페이지 - 내부 사용자로 리다이렉트');
    goto('/admin/users/internal', { replaceState: true });
  });
</script>

<svelte:head>
  <title>사용자 관리 - WebPos</title>
</svelte:head>

<div class="min-h-screen flex items-center justify-center">
  <div class="text-center">
    <div class="animate-spin rounded-full h-12 w-12 border-b-2 border-primary-600 mx-auto"></div>
    <p class="mt-4 text-gray-600">내부 사용자 관리로 이동 중...</p>
  </div>
</div>
