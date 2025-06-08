<script>
  import { createEventDispatcher } from 'svelte';
  import { X } from 'lucide-svelte';

  export let tabs = []; // { id, title, path, closeable }
  export let activeTabId = null;

  const dispatch = createEventDispatcher();

  function switchTab(tabId) {
    dispatch('tab-switch', { tabId });
  }

  function closeTab(tabId, event) {
    event.preventDefault();
    event.stopPropagation();
    console.log('🗑️ 기본 탭 닫기 클릭:', tabId);
    dispatch('tab-close', { tabId });
  }
</script>

{#if tabs.length > 0}
  <div class="bg-white border-b border-gray-200">
    <div class="flex items-center">
      <!-- 탭 목록 -->
      <div class="flex overflow-x-auto scrollbar-hide">
        {#each tabs as tab}
          <button
            type="button"
            class="tab group relative whitespace-nowrap"
            class:active={activeTabId === tab.id}
            on:click={() => switchTab(tab.id)}
          >
            <span class="flex items-center space-x-2">
              <span title="ID: {tab.id}">{tab.title || 'Untitled'}</span>
              {#if tab.closeable}
                <button
                  type="button"
                  class="ml-2 p-1.5 rounded hover:bg-gray-200 opacity-70 group-hover:opacity-100 transition-opacity duration-150 hover:scale-110 active:scale-95 flex items-center justify-center min-w-[24px] min-h-[24px] relative z-10"
                  on:click={(e) => closeTab(tab.id, e)}
                >
                  <X size="14" class="pointer-events-none" />
                </button>
              {/if}
            </span>
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
</style>
