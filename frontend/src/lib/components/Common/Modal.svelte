<script>
  import { createEventDispatcher } from 'svelte';
  import { X } from 'lucide-svelte';

  export let open = false;
  export let title = '';
  export let size = 'md'; // sm, md, lg, xl
  export let closeable = true;

  const dispatch = createEventDispatcher();

  const sizeClasses = {
    sm: 'max-w-sm sm:max-w-md',
    md: 'max-w-md sm:max-w-lg',
    lg: 'max-w-lg sm:max-w-2xl',
    xl: 'max-w-full sm:max-w-4xl mx-2 sm:mx-auto'
  };

  function close() {
    if (closeable) {
      open = false;
      dispatch('close');
    }
  }

  function handleKeydown(event) {
    if (event.key === 'Escape' && closeable) {
      close();
    }
  }

  function handleBackdropClick(event) {
    if (event.target === event.currentTarget && closeable) {
      close();
    }
  }
</script>

<svelte:window on:keydown={handleKeydown} />

{#if open}
  <!-- 백드롭 -->
  <div 
    class="fixed inset-0 bg-gray-600 bg-opacity-50 z-50 flex items-start sm:items-center justify-center p-2 sm:p-4 overflow-y-auto"
    on:click={handleBackdropClick}
  >
    <!-- 모달 컨테이너 -->
    <div class="relative w-full {sizeClasses[size]} my-4 sm:my-0 bg-white rounded-lg shadow-xl flex flex-col max-h-[calc(100vh-2rem)] sm:max-h-[90vh]">
      <!-- 헤더 -->
      {#if title || closeable}
        <div class="flex items-center justify-between p-4 border-b border-gray-200 flex-shrink-0 sticky top-0 bg-white rounded-t-lg z-10">
          {#if title}
            <h3 class="text-lg font-semibold text-gray-900">{title}</h3>
          {:else}
            <div></div>
          {/if}
          
          {#if closeable}
            <button
              type="button"
              class="text-gray-400 hover:text-gray-600 transition-colors duration-200 p-1 rounded-full hover:bg-gray-100"
              on:click={close}
            >
              <X size="24" />
            </button>
          {/if}
        </div>
      {/if}

      <!-- 콘텐츠 -->
      <div class="flex-1 overflow-y-auto p-4 sm:p-6">
        <slot />
      </div>

      <!-- 푸터 -->
      <div class="flex-shrink-0 sticky bottom-0 bg-white rounded-b-lg">
        <slot name="footer" />
      </div>
    </div>
  </div>
{/if}
