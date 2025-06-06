<script>
  import { createEventDispatcher } from 'svelte';
  import { X, Shield, Lock, Plus, XCircle, Star } from 'lucide-svelte';

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

  function getPriorityIcon(priority) {
    switch(priority) {
      case 'HIGH': return { emoji: '🔴', class: 'text-red-500 animate-pulse' };
      case 'MEDIUM': return { emoji: '🟡', class: 'text-yellow-500' };
      case 'LOW': return { emoji: '🟢', class: 'text-green-500' };
      default: return { emoji: '', class: '' };
    }
  }
</script>

{#if tabs.length > 0}
  <div class="bg-gradient-to-r from-red-600 via-red-650 to-red-700 border-b border-red-800/50 shadow-lg backdrop-blur-sm">
    <!-- 시스템 헤더 -->
    <div class="px-6 py-2 border-b border-red-500/20">
      <div class="flex items-center justify-between">
        <div class="flex items-center space-x-3">
          <div class="flex items-center space-x-2 px-3 py-1.5 bg-red-500/20 rounded-full border border-red-400/30 backdrop-blur-sm">
            <Shield size="16" class="text-red-200" />
            <span class="text-sm font-bold text-red-100 uppercase tracking-wider">관리자 시스템</span>
          </div>
          <div class="hidden sm:flex items-center space-x-1 text-xs text-red-200/80">
            <span>활성 탭:</span>
            <span class="font-semibold text-red-100">{tabs.length}</span>
          </div>
        </div>
        
        <!-- 탭 액션 버튼들 -->
        <div class="flex items-center space-x-2">
          <button 
            class="flex items-center space-x-2 px-3 py-1.5 text-xs font-medium text-red-200 hover:text-red-100 bg-red-500/20 hover:bg-red-500/30 rounded-lg border border-red-400/30 hover:border-red-300/50 transition-all duration-200 backdrop-blur-sm"
            title="새 탭 열기"
          >
            <Plus size="14" />
            <span class="hidden sm:inline">새 탭</span>
          </button>
          
          <button 
            class="flex items-center space-x-2 px-3 py-1.5 text-xs font-medium text-red-200 hover:text-red-100 bg-red-500/20 hover:bg-red-500/30 rounded-lg border border-red-400/30 hover:border-red-300/50 transition-all duration-200 backdrop-blur-sm"
            title="모든 탭 닫기"
            on:click={() => dispatch('close-all-tabs')}
          >
            <XCircle size="14" />
            <span class="hidden sm:inline">모두 닫기</span>
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
            class="admin-tab group relative whitespace-nowrap flex-shrink-0"
            class:active={activeTabId === tab.id}
            on:click={() => switchTab(tab.id)}
          >
            <div class="flex items-center space-x-2 px-4 py-2.5">
              <!-- 우선순위 표시 -->
              {#if tab.priority}
                {@const priority = getPriorityIcon(tab.priority)}
                <span class="text-xs {priority.class}" title="{tab.priority} 우선순위">
                  {priority.emoji}
                </span>
              {/if}
              
              <!-- 탭 제목 -->
              <span class="font-medium text-sm" title="ID: {tab.id}">
                {tab.title || 'Untitled'}
              </span>
              
              <!-- 보안 아이콘 -->
              {#if tab.secure}
                <Lock size="12" class="text-red-300/80" />
              {/if}

              <!-- 즐겨찾기 -->
              {#if tab.starred}
                <Star size="12" class="text-yellow-300 fill-current" />
              {/if}
              
              <!-- 닫기 버튼 -->
              {#if tab.closeable}
                <button
                  type="button"
                  class="ml-1 p-1 rounded-md hover:bg-red-400/40 opacity-0 group-hover:opacity-100 transition-all duration-200 hover:scale-110"
                  on:click={(e) => closeTab(tab.id, e)}
                  title="탭 닫기"
                >
                  <X size="12" class="text-red-200 hover:text-white" />
                </button>
              {/if}
            </div>

            <!-- 활성 탭 인디케이터 -->
            {#if activeTabId === tab.id}
              <div class="absolute bottom-0 left-0 right-0 h-0.5 bg-gradient-to-r from-red-200 via-white to-red-200 shadow-lg"></div>
            {/if}

            <!-- 호버 효과 -->
            <div class="absolute inset-0 bg-gradient-to-t from-red-400/0 to-red-400/10 opacity-0 group-hover:opacity-100 transition-opacity duration-200 rounded-lg"></div>
          </button>
        {/each}
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

  .admin-tab {
    position: relative;
    color: rgb(254 202 202);
    transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
    border-radius: 0.5rem;
    background: rgba(239, 68, 68, 0.1);
    border: 1px solid rgba(239, 68, 68, 0.2);
    backdrop-filter: blur(4px);
  }

  .admin-tab:hover {
    color: white;
    background: rgba(239, 68, 68, 0.25);
    border-color: rgba(239, 68, 68, 0.4);
    transform: translateY(-1px);
    box-shadow: 0 4px 12px rgba(239, 68, 68, 0.3);
  }

  .admin-tab.active {
    color: white;
    background: rgba(239, 68, 68, 0.4);
    border-color: rgba(252, 165, 165, 0.6);
    box-shadow: 
      0 4px 12px rgba(239, 68, 68, 0.4),
      inset 0 1px 0 rgba(255, 255, 255, 0.2);
  }

  .admin-tab.active::before {
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
</style>
