<script>
  import { createEventDispatcher } from 'svelte';
  import { X, Shield, Lock } from 'lucide-svelte';

  export let tabs = []; // { id, title, path, closeable, priority }
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
      case 'HIGH': return '🔴';
      case 'MEDIUM': return '🟡';
      case 'LOW': return '🟢';
      default: return '';
    }
  }
</script>

{#if tabs.length > 0}
  <div class="bg-gradient-to-r from-red-600 to-red-700 border-b-2 border-red-800 shadow-md">
    <div class="flex items-center px-4 py-2">
      <!-- 시스템 식별자 -->
      <div class="flex items-center mr-4 px-3 py-1 bg-red-500/30 rounded-full border border-red-400/50">
        <Shield size="16" class="text-red-200 mr-2" />
        <span class="text-xs font-semibold text-red-200 uppercase tracking-wider">Admin</span>
      </div>

      <!-- 탭 목록 -->
      <div class="flex overflow-x-auto scrollbar-hide flex-1">
        {#each tabs as tab}
          <button
            type="button"
            class="admin-tab group relative whitespace-nowrap"
            class:active={activeTabId === tab.id}
            on:click={() => switchTab(tab.id)}
          >
            <span class="flex items-center space-x-2">
              {#if tab.priority}
                <span class="text-xs">{getPriorityIcon(tab.priority)}</span>
              {/if}
              <span class="font-medium">{tab.title}</span>
              {#if tab.secure}
                <Lock size="12" class="text-red-300" />
              {/if}
              {#if tab.closeable}
                <button
                  type="button"
                  class="ml-2 p-1 rounded hover:bg-red-400/50 opacity-0 group-hover:opacity-100 transition-opacity duration-200"
                  on:click={(e) => closeTab(tab.id, e)}
                >
                  <X size="12" class="text-red-200" />
                </button>
              {/if}
            </span>
          </button>
        {/each}
      </div>

      <!-- 추가 기능 버튼 -->
      <div class="flex items-center space-x-2 ml-4">
        <button class="p-2 hover:bg-red-500/30 rounded transition-colors duration-200" title="새 탭 열기">
          <span class="text-red-200">+</span>
        </button>
        <button class="p-2 hover:bg-red-500/30 rounded transition-colors duration-200" title="모든 탭 닫기">
          <span class="text-red-200">×</span>
        </button>
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
    @apply px-4 py-2 text-sm text-red-200 hover:text-white transition-all duration-200 border-r relative;
    border-color: rgba(239, 68, 68, 0.3);
  }

  .admin-tab:hover {
    background-color: rgba(239, 68, 68, 0.3);
    transform: translateY(-1px);
  }

  .admin-tab.active {
    @apply text-white shadow-inner;
    background-color: rgba(239, 68, 68, 0.5);
  }

  .admin-tab.active::after {
    content: '';
    position: absolute;
    bottom: 0;
    left: 0;
    right: 0;
    height: 2px;
    background: rgb(252, 165, 165);
  }
</style>
