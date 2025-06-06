<script>
  import { createEventDispatcher } from 'svelte';
  import { X, ShoppingCart, Clock, Printer } from 'lucide-svelte';

  export let tabs = []; // { id, title, path, closeable, orderCount, timestamp }
  export let activeTabId = null;

  const dispatch = createEventDispatcher();

  function switchTab(tabId) {
    dispatch('tab-switch', { tabId });
  }

  function closeTab(tabId, event) {
    event.stopPropagation();
    dispatch('tab-close', { tabId });
  }

  function printTab(tabId, event) {
    event.stopPropagation();
    dispatch('tab-print', { tabId });
  }

  // 현재 시간 표시
  let currentTime = new Date();
  setInterval(() => {
    currentTime = new Date();
  }, 1000);
</script>

{#if tabs.length > 0}
  <div class="bg-gradient-to-r from-green-600 to-green-700 shadow-lg">
    <div class="flex items-center px-3 py-2">
      <!-- 시스템 식별자 및 시간 -->
      <div class="flex items-center mr-4 space-x-4">
        <div class="flex items-center px-3 py-1 bg-green-500/30 rounded border border-green-400/50">
          <ShoppingCart size="16" class="text-green-200 mr-2" />
          <span class="text-xs font-semibold text-green-200 uppercase tracking-wider">POS</span>
        </div>
        <div class="flex items-center text-green-200">
          <Clock size="14" class="mr-1" />
          <span class="text-xs font-mono">{currentTime.toLocaleTimeString('ko-KR', { hour12: false })}</span>
        </div>
      </div>

      <!-- 탭 목록 -->
      <div class="flex overflow-x-auto scrollbar-hide flex-1">
        {#each tabs as tab}
          <button
            type="button"
            class="pos-tab group relative whitespace-nowrap"
            class:active={activeTabId === tab.id}
            on:click={() => switchTab(tab.id)}
          >
            <span class="flex items-center space-x-2">
              <span class="font-medium">{tab.title}</span>
              {#if tab.orderCount}
                <span class="px-1.5 py-0.5 bg-orange-400 text-orange-900 text-xs rounded-full font-bold">
                  {tab.orderCount}
                </span>
              {/if}
              {#if tab.timestamp}
                <span class="text-xs text-green-300">
                  {new Date(tab.timestamp).toLocaleTimeString('ko-KR', { hour: '2-digit', minute: '2-digit' })}
                </span>
              {/if}
              <div class="flex items-center space-x-1 opacity-0 group-hover:opacity-100 transition-opacity duration-200">
                {#if tab.printable}
                  <button
                    type="button"
                    class="p-1 rounded hover:bg-green-400/50 transition-colors duration-200"
                    on:click={(e) => printTab(tab.id, e)}
                    title="인쇄"
                  >
                    <Printer size="10" class="text-green-200" />
                  </button>
                {/if}
                {#if tab.closeable}
                  <button
                    type="button"
                    class="p-1 rounded hover:bg-green-400/50 transition-colors duration-200"
                    on:click={(e) => closeTab(tab.id, e)}
                  >
                    <X size="10" class="text-green-200" />
                  </button>
                {/if}
              </div>
            </span>
          </button>
        {/each}
      </div>

      <!-- 빠른 작업 버튼 -->
      <div class="flex items-center space-x-1 ml-4">
        <button 
          class="pos-action-btn" 
          title="새 주문"
        >
          <span class="text-sm">🛒</span>
        </button>
        <button 
          class="pos-action-btn" 
          title="결제"
        >
          <span class="text-sm">💳</span>
        </button>
        <button 
          class="pos-action-btn" 
          title="영수증 출력"
        >
          <Printer size="14" class="text-green-200" />
        </button>
      </div>
    </div>

    <!-- 상태 표시줄 -->
    <div class="px-3 py-1 bg-green-500/20 border-t border-green-500/30">
      <div class="flex justify-between items-center text-xs text-green-200">
        <div class="flex items-center space-x-4">
          <span>활성 주문: {tabs.reduce((sum, tab) => sum + (tab.orderCount || 0), 0)}</span>
          <span>•</span>
          <span>오늘 매출: ₩847,200</span>
        </div>
        <div class="flex items-center space-x-2">
          <span class="w-2 h-2 bg-green-400 rounded-full animate-pulse"></span>
          <span>온라인</span>
        </div>
      </div>
    </div>
  </div>
{/if}

<style>
  .scrollbar-hide {
    -ms-overflow-style: none;
    scrollbar-width: none;
  }
  .scrollbar-hide::-webkit-scrollbar {
    display: none;
  }

  .pos-tab {
    @apply px-3 py-2 text-sm text-green-100 hover:text-white transition-all duration-200 border-r relative;
    border-color: rgba(34, 197, 94, 0.3);
  }

  .pos-tab:hover {
    background-color: rgba(34, 197, 94, 0.3);
    transform: translateY(-1px);
  }

  .pos-tab.active {
    @apply text-white shadow-inner;
    background-color: rgba(34, 197, 94, 0.5);
  }

  .pos-tab.active::after {
    content: '';
    position: absolute;
    bottom: 0;
    left: 0;
    right: 0;
    height: 2px;
    background: rgb(134, 239, 172);
  }

  .pos-action-btn {
    @apply p-2 rounded transition-colors duration-200;
    background-color: rgba(34, 197, 94, 0.3);
    border: 1px solid rgba(74, 222, 128, 0.5);
  }

  .pos-action-btn:hover {
    background-color: rgba(34, 197, 94, 0.3);
    border-color: rgb(134, 239, 172);
  }
</style>
