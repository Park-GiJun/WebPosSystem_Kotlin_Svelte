<script>
  export let size = 'md'; // sm, md, lg
  export let color = 'primary'; // primary, white, gray
  export let text = '';

  const sizeClasses = {
    sm: 'h-4 w-4',
    md: 'h-8 w-8',
    lg: 'h-12 w-12'
  };

  const colorClasses = {
    primary: 'border-primary-600',
    white: 'border-white',
    gray: 'border-gray-600'
  };
</script>

<div class="flex flex-col items-center justify-center">
  <div 
    class="animate-spin rounded-full border-b-2 {sizeClasses[size]} {colorClasses[color]}"
  ></div>
  {#if text}
    <p class="mt-2 text-sm text-gray-600">{text}</p>
  {/if}
</div>
