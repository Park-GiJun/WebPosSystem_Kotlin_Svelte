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
    event.stopPropagation();
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
              <span>{tab.title}</span>
              {#if tab.closeable}
                <button
                  type="button"
                  class="ml-2 p-1 rounded hover:bg-gray-200 opacity-0 group-hover:opacity-100 transition-opacity duration-200"
                  on:click={(e) => closeTab(tab.id, e)}
                >
                  <X size="14" />
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
