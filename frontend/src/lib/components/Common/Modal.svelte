<script>
  import { createEventDispatcher } from 'svelte';
  import { X } from 'lucide-svelte';

  export let open = false;
  export let title = '';
  export let size = 'md'; // sm, md, lg, xl
  export let closeable = true;

  const dispatch = createEventDispatcher();

  const sizeClasses = {
    sm: 'max-w-md',
    md: 'max-w-lg',
    lg: 'max-w-2xl',
    xl: 'max-w-4xl'
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
    class="fixed inset-0 bg-gray-600 bg-opacity-50 z-50 flex items-center justify-center p-4"
    on:click={handleBackdropClick}
  >
    <!-- 모달 컨테이너 -->
    <div class="relative w-full {sizeClasses[size]} max-h-[90vh] bg-white rounded-lg shadow-xl flex flex-col">
      <!-- 헤더 -->
      {#if title || closeable}
        <div class="flex items-center justify-between p-4 border-b border-gray-200 flex-shrink-0">
          {#if title}
            <h3 class="text-lg font-semibold text-gray-900">{title}</h3>
          {:else}
            <div></div>
          {/if}
          
          {#if closeable}
            <button
              type="button"
              class="text-gray-400 hover:text-gray-600 transition-colors duration-200"
              on:click={close}
            >
              <X size="24" />
            </button>
          {/if}
        </div>
      {/if}

      <!-- 콘텐츠 -->
      <div class="flex-1 overflow-y-auto p-4">
        <slot />
      </div>

      <!-- 푸터 -->
      <div class="flex-shrink-0">
        <slot name="footer" />
      </div>
    </div>
  </div>
{/if}
