<script>
  import { createEventDispatcher } from 'svelte';
  import { X, ShoppingCart, Clock, Printer, Plus, DollarSign, Activity, Zap } from 'lucide-svelte';

  export let tabs = [];
  export let activeTabId = null;

  const dispatch = createEventDispatcher();

  function switchTab(tabId) {
    dispatch('tab-switch', { tabId });
  }

  function closeTab(tabId, event) {
    event.preventDefault();
    event.stopPropagation();
    console.log('🗑️ POS 탭 닫기 클릭:', tabId);
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

  // 통계 계산
  $: totalOrders = tabs.reduce((sum, tab) => sum + (tab.orderCount || 0), 0);
  $: todaySales = 847200; // 임시 데이터
</script>

{#if tabs.length > 0}
  <div class="bg-gradient-to-r from-emerald-600 via-emerald-650 to-emerald-700 border-b border-emerald-800/50 shadow-lg backdrop-blur-sm">
    <!-- 시스템 헤더 -->
    <div class="px-6 py-2 border-b border-emerald-500/20">
      <div class="flex items-center justify-between">
        <div class="flex items-center space-x-4">
          <!-- 시스템 식별자 -->
          <div class="flex items-center space-x-2 px-3 py-1.5 bg-emerald-500/20 rounded-full border border-emerald-400/30 backdrop-blur-sm">
            <ShoppingCart size="16" class="text-emerald-200" />
            <span class="text-sm font-bold text-emerald-100 uppercase tracking-wider">POS 시스템</span>
          </div>

          <!-- 실시간 정보 -->
          <div class="hidden md:flex items-center space-x-4 text-emerald-200/90">
            <div class="flex items-center space-x-1.5">
              <Clock size="14" />
              <span class="text-sm font-mono font-semibold">
                {currentTime.toLocaleTimeString('ko-KR', { hour12: false })}
              </span>
            </div>
            
            <div class="flex items-center space-x-1.5">
              <Activity size="14" class="text-emerald-300" />
              <span class="text-sm">주문 {totalOrders}건</span>
            </div>

            <div class="flex items-center space-x-1.5">
              <DollarSign size="14" class="text-emerald-300" />
              <span class="text-sm font-semibold">₩{todaySales.toLocaleString()}</span>
            </div>
          </div>
        </div>

        <!-- 빠른 액션 버튼들 -->
        <div class="flex items-center space-x-2">
          <button 
            class="pos-action-btn group" 
            title="새 주문 받기"
          >
            <Plus size="14" class="group-hover:scale-110 transition-transform" />
            <span class="hidden sm:inline text-sm font-medium">새 주문</span>
          </button>
          
          <button 
            class="pos-action-btn group" 
            title="영수증 출력"
          >
            <Printer size="14" class="group-hover:scale-110 transition-transform" />
            <span class="hidden sm:inline text-sm font-medium">출력</span>
          </button>

          <button 
            class="pos-action-btn group" 
            title="빠른 결제"
          >
            <Zap size="14" class="group-hover:scale-110 transition-transform" />
            <span class="hidden sm:inline text-sm font-medium">결제</span>
          </button>
        </div>
      </div>
    </div>

    <!-- 탭 목록 -->
    <div class="px-4 py-2">
      <div class="flex overflow-x-auto scrollbar-hide space-x-1">
        {#each tabs as tab}
          <button
            type="button"
            class="pos-tab group relative whitespace-nowrap flex-shrink-0"
            class:active={activeTabId === tab.id}
            on:click={() => switchTab(tab.id)}
          >
            <div class="flex items-center space-x-2 px-4 py-2.5">
              <!-- 탭 제목 -->
              <span class="font-medium text-sm" title="ID: {tab.id}">
                {tab.title || 'Untitled'}
              </span>
              
              <!-- 주문 수량 배지 -->
              {#if tab.orderCount}
                <span class="px-2 py-0.5 bg-orange-400 text-orange-900 text-xs rounded-full font-bold border border-orange-300 shadow-sm">
                  {tab.orderCount}
                </span>
              {/if}
              
              <!-- 시간 표시 -->
              {#if tab.timestamp}
                <span class="text-xs text-emerald-300/80 font-mono">
                  {new Date(tab.timestamp).toLocaleTimeString('ko-KR', { hour: '2-digit', minute: '2-digit' })}
                </span>
              {/if}
              
              <!-- 액션 버튼들 -->
              <div class="flex items-center space-x-1 opacity-70 group-hover:opacity-100 transition-opacity duration-150">
                {#if tab.printable}
                  <button
                    type="button"
                    class="p-1 rounded-md hover:bg-emerald-400/40 transition-all duration-200 hover:scale-110"
                    on:click={(e) => printTab(tab.id, e)}
                    title="영수증 인쇄"
                  >
                    <Printer size="12" class="text-emerald-200 hover:text-white" />
                  </button>
                {/if}
                
                {#if tab.closeable}
                  <button
                    type="button"
                    class="p-1.5 ml-1 rounded-md hover:bg-emerald-400/60 active:bg-emerald-400/80 transition-all duration-200 hover:scale-110 active:scale-95 flex items-center justify-center min-w-[24px] min-h-[24px] relative z-10"
                    on:click={(e) => closeTab(tab.id, e)}
                    title="탭 닫기"
                  >
                    <X size="14" class="text-emerald-200 hover:text-white pointer-events-none" />
                  </button>
                {/if}
              </div>
            </div>

            <!-- 활성 탭 인디케이터 -->
            {#if activeTabId === tab.id}
              <div class="absolute bottom-0 left-0 right-0 h-0.5 bg-gradient-to-r from-emerald-200 via-white to-emerald-200 shadow-lg"></div>
            {/if}

            <!-- 호버 효과 -->
            <div class="absolute inset-0 bg-gradient-to-t from-emerald-400/0 to-emerald-400/10 opacity-0 group-hover:opacity-100 transition-opacity duration-200 rounded-lg pointer-events-none"></div>
          </button>
        {/each}
      </div>
    </div>

    <!-- 상태 표시줄 -->
    <div class="px-6 py-2 bg-emerald-500/10 border-t border-emerald-500/20">
      <div class="flex justify-between items-center text-sm">
        <div class="flex items-center space-x-6 text-emerald-200/90">
          <div class="flex items-center space-x-2">
            <div class="w-2 h-2 bg-emerald-300 rounded-full animate-pulse shadow-lg shadow-emerald-300/50"></div>
            <span class="font-medium">온라인</span>
          </div>
          
          <div class="hidden sm:flex items-center space-x-4">
            <span>활성 주문: <span class="font-bold text-emerald-100">{totalOrders}건</span></span>
            <span class="text-emerald-300">•</span>
            <span>오늘 매출: <span class="font-bold text-emerald-100">₩{todaySales.toLocaleString()}</span></span>
          </div>
        </div>
        
        <div class="flex items-center space-x-2 text-emerald-200/80">
          <span class="text-xs">마지막 업데이트:</span>
          <span class="text-xs font-mono">{currentTime.toLocaleTimeString('ko-KR')}</span>
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
    position: relative;
    color: rgb(220 252 231);
    transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
    border-radius: 0.5rem;
    background: rgba(34, 197, 94, 0.1);
    border: 1px solid rgba(34, 197, 94, 0.2);
    backdrop-filter: blur(4px);
  }

  .pos-tab:hover {
    color: white;
    background: rgba(34, 197, 94, 0.25);
    border-color: rgba(34, 197, 94, 0.4);
    transform: translateY(-1px);
    box-shadow: 0 4px 12px rgba(34, 197, 94, 0.3);
  }

  .pos-tab.active {
    color: white;
    background: rgba(34, 197, 94, 0.4);
    border-color: rgba(134, 239, 172, 0.6);
    box-shadow: 
      0 4px 12px rgba(34, 197, 94, 0.4),
      inset 0 1px 0 rgba(255, 255, 255, 0.2);
  }

  .pos-tab.active::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: linear-gradient(135deg, rgba(255, 255, 255, 0.1) 0%, transparent 50%);
    border-radius: inherit;
    pointer-events: none;
  }

  .pos-action-btn {
    display: flex;
    align-items: center;
    space-x: 0.5rem;
    padding: 0.5rem 0.75rem;
    font-size: 0.875rem;
    font-weight: 500;
    color: rgb(220 252 231);
    background: rgba(34, 197, 94, 0.2);
    border: 1px solid rgba(34, 197, 94, 0.3);
    border-radius: 0.5rem;
    transition: all 0.2s ease-in-out;
    backdrop-filter: blur(4px);
  }

  .pos-action-btn:hover {
    color: white;
    background: rgba(34, 197, 94, 0.3);
    border-color: rgba(134, 239, 172, 0.5);
    transform: translateY(-1px);
    box-shadow: 0 4px 8px rgba(34, 197, 94, 0.2);
  }
</style>
