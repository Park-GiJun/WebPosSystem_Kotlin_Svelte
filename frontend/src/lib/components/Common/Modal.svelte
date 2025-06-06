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
    class="fixed inset-0 bg-gray-600 bg-opacity-50 overflow-y-auto h-full w-full z-50"
    on:click={handleBackdropClick}
  >
    <!-- 모달 컨테이너 -->
    <div class="relative top-20 mx-auto p-5 border w-11/12 {sizeClasses[size]} shadow-lg rounded-md bg-white">
      <!-- 헤더 -->
      {#if title || closeable}
        <div class="flex items-center justify-between p-4 border-b">
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
      <div class="p-4">
        <slot />
      </div>

      <!-- 푸터 -->
      <slot name="footer" />
    </div>
  </div>
{/if}
