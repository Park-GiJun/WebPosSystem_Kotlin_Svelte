<script>
  import { AlertTriangle, RefreshCw, Home } from 'lucide-svelte';
  
  export let title = 'API를 찾을 수 없습니다';
  export let message = '요청하신 API 엔드포인트가 존재하지 않거나 아직 구현되지 않았습니다.';
  export let showHomeButton = true;
  export let showRefreshButton = true;
  export let onRetry = null;
  export let onHome = null;

  function handleRetry() {
    if (onRetry) {
      onRetry();
    } else {
      window.location.reload();
    }
  }

  function handleHome() {
    if (onHome) {
      onHome();
    } else {
      window.location.href = '/';
    }
  }
</script>

<div class="min-h-[400px] flex items-center justify-center p-8">
  <div class="text-center max-w-md mx-auto">
    <!-- 아이콘 -->
    <div class="mb-6">
      <div class="w-20 h-20 mx-auto bg-orange-100 rounded-full flex items-center justify-center">
        <AlertTriangle class="w-10 h-10 text-orange-600" />
      </div>
    </div>

    <!-- 제목 -->
    <h2 class="text-2xl font-bold text-gray-900 mb-4">
      {title}
    </h2>

    <!-- 메시지 -->
    <p class="text-gray-600 mb-8 leading-relaxed">
      {message}
    </p>

    <!-- 버튼들 -->
    <div class="flex flex-col sm:flex-row gap-3 justify-center">
      {#if showRefreshButton}
        <button
          type="button"
          class="btn btn-primary flex items-center justify-center"
          on:click={handleRetry}
        >
          <RefreshCw class="w-4 h-4 mr-2" />
          다시 시도
        </button>
      {/if}

      {#if showHomeButton}
        <button
          type="button"
          class="btn btn-secondary flex items-center justify-center"
          on:click={handleHome}
        >
          <Home class="w-4 h-4 mr-2" />
          홈으로
        </button>
      {/if}
    </div>

    <!-- 개발 정보 -->
    <div class="mt-8 p-4 bg-blue-50 border border-blue-200 rounded-lg text-left">
      <h4 class="text-sm font-medium text-blue-900 mb-2">개발자 정보</h4>
      <ul class="text-xs text-blue-700 space-y-1">
        <li>• 이 페이지는 데모 모드에서 실행 중입니다</li>
        <li>• API가 구현되면 실제 데이터로 대체됩니다</li>
        <li>• 일부 기능은 더미 데이터로 동작할 수 있습니다</li>
      </ul>
    </div>
  </div>
</div>
