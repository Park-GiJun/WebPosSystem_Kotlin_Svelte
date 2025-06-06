<script>
  import { createEventDispatcher } from 'svelte';
  import { X, BarChart3, Star, Plus, RotateCcw, BookOpen, TrendingUp, Users, FileText } from 'lucide-svelte';

  export let tabs = [];
  export let activeTabId = null;

  const dispatch = createEventDispatcher();

  function switchTab(tabId) {
    dispatch('tab-switch', { tabId });
  }

  function closeTab(tabId, event) {
    event.stopPropagation();
    dispatch('tab-close', { tabId });
  }

  function starTab(tabId, event) {
    event.stopPropagation();
    dispatch('tab-star', { tabId });
  }

  function closeAllTabs() {
    dispatch('close-all-tabs');
  }

  function newTab() {
    dispatch('new-tab');
  }

  // 통계 데이터 (임시)
  $: starredTabs = tabs.filter(tab => tab.starred).length;
  $: modifiedTabs = tabs.filter(tab => tab.modified).length;
</script>

{#if tabs.length > 0}
  <div class="bg-gradient-to-r from-blue-600 via-blue-650 to-blue-700 border-b border-blue-800/50 shadow-lg backdrop-blur-sm">
    <!-- 시스템 헤더 -->
    <div class="px-6 py-2 border-b border-blue-500/20">
      <div class="flex items-center justify-between">
        <div class="flex items-center space-x-4">
          <!-- 시스템 식별자 -->
          <div class="flex items-center space-x-2 px-3 py-1.5 bg-blue-500/20 rounded-full border border-blue-400/30 backdrop-blur-sm">
            <BarChart3 size="16" class="text-blue-200" />
            <span class="text-sm font-bold text-blue-100 uppercase tracking-wider">영업정보시스템</span>
          </div>

          <!-- 워크스페이스 정보 -->
          <div class="hidden md:flex items-center space-x-4 text-blue-200/90">
            <div class="flex items-center space-x-1.5">
              <BookOpen size="14" />
              <span class="text-sm">워크스페이스: <span class="font-semibold text-blue-100">본사 통합관리</span></span>
            </div>
            
            <div class="flex items-center space-x-1.5">
              <Star size="14" class="text-yellow-300" />
              <span class="text-sm">즐겨찾기 {starredTabs}개</span>
            </div>

            {#if modifiedTabs > 0}
              <div class="flex items-center space-x-1.5">
                <div class="w-2 h-2 bg-orange-400 rounded-full animate-pulse"></div>
                <span class="text-sm">수정됨 {modifiedTabs}개</span>
              </div>
            {/if}
          </div>
        </div>

        <!-- 액션 버튼들 -->
        <div class="flex items-center space-x-2">
          <button 
            class="business-action-btn group" 
            title="새 문서"
            on:click={newTab}
          >
            <Plus size="14" class="group-hover:scale-110 transition-transform" />
            <span class="hidden sm:inline text-sm font-medium">새 문서</span>
          </button>
          
          <button 
            class="business-action-btn group" 
            title="새로고침"
          >
            <RotateCcw size="14" class="group-hover:rotate-180 transition-transform duration-300" />
            <span class="hidden sm:inline text-sm font-medium">새로고침</span>
          </button>

          <button 
            class="business-action-btn group" 
            title="모든 탭 닫기"
            on:click={closeAllTabs}
          >
            <X size="14" class="group-hover:scale-110 transition-transform" />
            <span class="hidden sm:inline text-sm font-medium">모두 닫기</span>
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
            class="business-tab group relative whitespace-nowrap flex-shrink-0"
            class:active={activeTabId === tab.id}
            on:click={() => switchTab(tab.id)}
          >
            <div class="flex items-center space-x-2 px-4 py-2.5">
              <!-- 즐겨찾기 버튼 -->
              <button
                type="button"
                class="p-0.5 rounded-md transition-all duration-200 hover:scale-110"
                class:opacity-100={tab.starred}
                class:opacity-0={!tab.starred}
                class:group-hover:opacity-100={!tab.starred}
                on:click={(e) => starTab(tab.id, e)}
                title={tab.starred ? '즐겨찾기 해제' : '즐겨찾기 추가'}
              >
                <Star size="12" class={tab.starred ? 'text-yellow-300 fill-current' : 'text-blue-300'} />
              </button>
              
              <!-- 탭 제목 -->
              <div class="flex items-center space-x-1.5">
                <span class="font-medium text-sm" title="ID: {tab.id}">
                  {tab.title || 'Untitled'}
                </span>
                
                <!-- 수정됨 표시 -->
                {#if tab.modified}
                  <div class="w-2 h-2 bg-orange-400 rounded-full animate-pulse shadow-lg shadow-orange-400/50" title="수정된 문서"></div>
                {/if}
              </div>
              
              <!-- 닫기 버튼 -->
              {#if tab.closeable}
                <button
                  type="button"
                  class="p-1 rounded-md hover:bg-blue-400/40 opacity-0 group-hover:opacity-100 transition-all duration-200 hover:scale-110"
                  on:click={(e) => closeTab(tab.id, e)}
                  title="탭 닫기"
                >
                  <X size="12" class="text-blue-200 hover:text-white" />
                </button>
              {/if}
            </div>

            <!-- 활성 탭 인디케이터 -->
            {#if activeTabId === tab.id}
              <div class="absolute bottom-0 left-0 right-0 h-0.5 bg-gradient-to-r from-blue-200 via-white to-blue-200 shadow-lg"></div>
            {/if}

            <!-- 호버 효과 -->
            <div class="absolute inset-0 bg-gradient-to-t from-blue-400/0 to-blue-400/10 opacity-0 group-hover:opacity-100 transition-opacity duration-200 rounded-lg"></div>
          </button>
        {/each}
      </div>
    </div>

    <!-- 상태 표시줄 -->
    <div class="px-6 py-2 bg-blue-500/10 border-t border-blue-500/20">
      <div class="flex justify-between items-center text-sm">
        <div class="flex items-center space-x-6 text-blue-200/90">
          <div class="flex items-center space-x-2">
            <TrendingUp size="14" class="text-blue-300" />
            <span class="font-medium">실시간 데이터 동기화</span>
          </div>
          
          <div class="hidden sm:flex items-center space-x-4">
            <div class="flex items-center space-x-1.5">
              <Users size="14" />
              <span>활성 사용자: <span class="font-bold text-blue-100">12명</span></span>
            </div>
            
            <span class="text-blue-300">•</span>
            
            <div class="flex items-center space-x-1.5">
              <FileText size="14" />
              <span>열린 문서: <span class="font-bold text-blue-100">{tabs.length}개</span></span>
            </div>
          </div>
        </div>
        
        <div class="flex items-center space-x-2 text-blue-200/80">
          <div class="w-2 h-2 bg-blue-300 rounded-full animate-pulse shadow-lg shadow-blue-300/50"></div>
          <span class="text-xs font-medium">온라인</span>
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

  .business-tab {
    position: relative;
    color: rgb(219 234 254);
    transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
    border-radius: 0.5rem;
    background: rgba(59, 130, 246, 0.1);
    border: 1px solid rgba(59, 130, 246, 0.2);
    backdrop-filter: blur(4px);
  }

  .business-tab:hover {
    color: white;
    background: rgba(59, 130, 246, 0.25);
    border-color: rgba(59, 130, 246, 0.4);
    transform: translateY(-1px);
    box-shadow: 0 4px 12px rgba(59, 130, 246, 0.3);
  }

  .business-tab.active {
    color: white;
    background: rgba(59, 130, 246, 0.4);
    border-color: rgba(147, 197, 253, 0.6);
    box-shadow: 
      0 4px 12px rgba(59, 130, 246, 0.4),
      inset 0 1px 0 rgba(255, 255, 255, 0.2);
  }

  .business-tab.active::before {
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

  .business-action-btn {
    display: flex;
    align-items: center;
    gap: 0.5rem;
    padding: 0.5rem 0.75rem;
    font-size: 0.875rem;
    font-weight: 500;
    color: rgb(219 234 254);
    background: rgba(59, 130, 246, 0.2);
    border: 1px solid rgba(59, 130, 246, 0.3);
    border-radius: 0.5rem;
    transition: all 0.2s ease-in-out;
    backdrop-filter: blur(4px);
  }

  .business-action-btn:hover {
    color: white;
    background: rgba(59, 130, 246, 0.3);
    border-color: rgba(147, 197, 253, 0.5);
    transform: translateY(-1px);
    box-shadow: 0 4px 8px rgba(59, 130, 246, 0.2);
  }
</style>
