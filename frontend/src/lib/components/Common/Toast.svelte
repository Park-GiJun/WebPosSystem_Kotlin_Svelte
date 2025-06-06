<script>
  import { createEventDispatcher, onMount } from 'svelte';
  import { CheckCircle, AlertCircle, XCircle, Info, X } from 'lucide-svelte';

  export let type = 'info'; // success, error, warning, info
  export let message = '';
  export let duration = 5000; // 자동 사라지는 시간 (ms)
  export let closeable = true;
  export let show = true;

  const dispatch = createEventDispatcher();

  const typeConfig = {
    success: {
      icon: CheckCircle,
      bgClass: 'bg-green-50 border-green-200',
      iconClass: 'text-green-400',
      textClass: 'text-green-800'
    },
    error: {
      icon: XCircle,
      bgClass: 'bg-red-50 border-red-200',
      iconClass: 'text-red-400',
      textClass: 'text-red-800'
    },
    warning: {
      icon: AlertCircle,
      bgClass: 'bg-yellow-50 border-yellow-200',
      iconClass: 'text-yellow-400',
      textClass: 'text-yellow-800'
    },
    info: {
      icon: Info,
      bgClass: 'bg-blue-50 border-blue-200',
      iconClass: 'text-blue-400',
      textClass: 'text-blue-800'
    }
  };

  $: config = typeConfig[type];

  function close() {
    show = false;
    dispatch('close');
  }

  onMount(() => {
    if (duration > 0) {
      const timer = setTimeout(() => {
        close();
      }, duration);

      return () => clearTimeout(timer);
    }
  });
</script>

{#if show}
  <div class="fixed top-4 right-4 z-50 max-w-sm w-full">
    <div class="rounded-lg border p-4 shadow-lg {config.bgClass}">
      <div class="flex">
        <div class="flex-shrink-0">
          <svelte:component this={config.icon} class="h-5 w-5 {config.iconClass}" />
        </div>
        <div class="ml-3 flex-1">
          <p class="text-sm font-medium {config.textClass}">
            {message}
          </p>
        </div>
        {#if closeable}
          <div class="ml-4 flex-shrink-0">
            <button
              type="button"
              class="inline-flex rounded-md {config.textClass} hover:opacity-75 focus:outline-none focus:ring-2 focus:ring-offset-2"
              on:click={close}
            >
              <X class="h-4 w-4" />
            </button>
          </div>
        {/if}
      </div>
    </div>
  </div>
{/if}
