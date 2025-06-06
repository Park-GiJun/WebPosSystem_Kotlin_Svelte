<script>
  import { page } from '$app/stores';
  import { goto } from '$app/navigation';
</script>

<svelte:head>
  <title>오류 - WebPos</title>
</svelte:head>

<div class="min-h-screen bg-gray-50 flex flex-col justify-center py-12 sm:px-6 lg:px-8">
  <div class="text-center">
    <h1 class="text-6xl font-bold text-gray-900 mb-4">500</h1>
    <h2 class="text-2xl font-semibold text-gray-700 mb-4">서버 오류가 발생했습니다</h2>
    <p class="text-gray-600 mb-8">요청을 처리하는 중 문제가 발생했습니다.</p>
    
    {#if $page.error?.message}
      <div class="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded-lg mb-6 max-w-md mx-auto">
        <p class="text-sm">{$page.error.message}</p>
      </div>
    {/if}
    
    <div class="space-x-4">
      <button
        type="button"
        class="btn btn-primary"
        on:click={() => goto('/system-select')}
      >
        시스템 선택으로 돌아가기
      </button>
      
      <button
        type="button"
        class="btn btn-secondary"
        on:click={() => window.location.reload()}
      >
        페이지 새로고침
      </button>
    </div>
  </div>
</div>
