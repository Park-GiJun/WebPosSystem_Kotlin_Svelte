<script>
  import { onMount } from 'svelte';
  import { goto } from '$app/navigation';

  onMount(() => {
    // POS 시스템 진입 시 기본적으로 판매 페이지로 리다이렉트
    goto('/pos/sales');
  });
</script>

<svelte:head>
  <title>POS 시스템 - WebPos</title>
</svelte:head>

<div class="flex items-center justify-center min-h-screen">
  <div class="text-center">
    <div class="animate-spin rounded-full h-12 w-12 border-b-2 border-green-600 mx-auto"></div>
    <p class="mt-4 text-gray-600">POS 시스템으로 이동 중...</p>
  </div>
</div>
