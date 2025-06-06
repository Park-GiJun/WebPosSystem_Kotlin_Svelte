<script>
  import { toastStore } from '$lib/stores/toast.js';
  import Toast from './Toast.svelte';

  $: toasts = $toastStore;
</script>

<!-- 토스트 컨테이너 -->
<div class="fixed top-4 right-4 z-50 space-y-2">
  {#each toasts as toast (toast.id)}
    <Toast
      type={toast.type}
      message={toast.message}
      duration={0}
      closeable={toast.closeable}
      on:close={() => toastStore.remove(toast.id)}
    />
  {/each}
</div>
