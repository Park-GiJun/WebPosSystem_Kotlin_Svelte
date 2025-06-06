<script>
  import { createEventDispatcher } from 'svelte';
  import { X, Building, Star, Plus, RotateCcw } from 'lucide-svelte';

  export let tabs = []; // { id, title, path, closeable, starred, modified }
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
</script>

{#if tabs.length > 0}
  <div class="bg-white border-b-2 border-blue-200 shadow-sm">
    <div class="flex items-center px-6 py-3">
      <!-- 시스템 식별자 -->
      <div class="flex items-center mr-6 px-4 py-2 bg-blue-50 rounded-lg border border-blue-200">
        <Building size="18" class="text-blue-600 mr-2" />
        <span class="text-sm font-semibold text-blue-700">영업정보시스템</span>
      </div>

      <!-- 탭 목록 -->
      <div class="flex overflow-x-auto scrollbar-hide flex-1">
        {#each tabs as tab}
          <button
            type="button"
            class="business-tab group relative whitespace-nowrap"
            class:active={activeTabId === tab.id}
            on:click={() => switchTab(tab.id)}
          >
            <span class="flex items-center space-x-2">
              {#if tab.starred}
                <button
                  type="button"
                  class="p-0.5 rounded"
                  on:click={(e) => starTab(tab.id, e)}
                >
                  <Star size="12" class="text-yellow-400 fill-current" />
                </button>
              {:else}
                <button
                  type="button"
                  class="p-0.5 rounded opacity-0 group-hover:opacity-100"
                  on:click={(e) => starTab(tab.id, e)}
                >
                  <Star size="12" class="text-gray-400" />
                </button>
              {/if}
              <span class="font-medium flex items-center">
                {tab.title}
                {#if tab.modified}
                  <span class="ml-1 w-2 h-2 bg-orange-400 rounded-full"></span>
                {/if}
              </span>
              {#if tab.closeable}
                <button
                  type="button"
                  class="ml-2 p-1 rounded hover:bg-blue-100 opacity-0 group-hover:opacity-100 transition-opacity duration-200"
                  on:click={(e) => closeTab(tab.id, e)}
                >
                  <X size="12" class="text-gray-500" />
                </button>
              {/if}
            </span>
          </button>
        {/each}
      </div>

      <!-- 추가 기능 버튼 -->
      <div class="flex items-center space-x-1 ml-4">
        <button 
          class="p-2 hover:bg-blue-50 rounded-lg transition-colors duration-200 border border-transparent hover:border-blue-200" 
          title="새 탭 열기"
          on:click={newTab}
        >
          <Plus size="16" class="text-blue-600" />
        </button>
        <button 
          class="p-2 hover:bg-blue-50 rounded-lg transition-colors duration-200 border border-transparent hover:border-blue-200" 
          title="새로고침"
        >
          <RotateCcw size="16" class="text-blue-600" />
        </button>
        <button 
          class="p-2 hover:bg-blue-50 rounded-lg transition-colors duration-200 border border-transparent hover:border-blue-200" 
          title="모든 탭 닫기"
          on:click={closeAllTabs}
        >
          <X size="16" class="text-blue-600" />
        </button>
      </div>
    </div>

    <!-- 탭 하단 드롭쉐도우 -->
    <div class="h-1 bg-gradient-to-b from-blue-100 to-transparent"></div>
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
    @apply px-4 py-3 text-sm text-gray-600 hover:text-blue-700 transition-all duration-200 border-r border-gray-200 relative rounded-t-lg mx-1;
  }

  .business-tab:hover {
    background-color: rgb(239 246 255);
    transform: translateY(-1px);
    box-shadow: 0 2px 4px rgba(59, 130, 246, 0.1);
  }

  .business-tab.active {
    @apply text-blue-700 bg-blue-100 border-blue-200 shadow-sm;
  }

  .business-tab.active::after {
    content: '';
    position: absolute;
    bottom: -1px;
    left: 0;
    right: 0;
    height: 3px;
    background: rgb(59, 130, 246);
    border-radius: 2px 2px 0 0;
  }
</style>
