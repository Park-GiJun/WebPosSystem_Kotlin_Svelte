<script>
  import { authStore } from '$lib/stores/auth.js';
  import { goto } from '$app/navigation';
  import { onMount } from 'svelte';

  let username = '';
  let password = '';
  let loading = false;
  let error = '';

  onMount(() => {
    // 이미 로그인된 경우 대시보드로 리다이렉트
    const unsubscribe = authStore.subscribe(auth => {
      if (auth.isAuthenticated) {
        goto('/dashboard');
      }
    });

    return unsubscribe;
  });

  async function handleLogin() {
    if (!username || !password) {
      error = '아이디와 비밀번호를 입력해주세요.';
      return;
    }

    loading = true;
    error = '';

    const result = await authStore.login({ username, password });

    if (result.success) {
      goto('/dashboard');
    } else {
      error = result.error;
    }

    loading = false;
  }

  function handleKeyPress(event) {
    if (event.key === 'Enter') {
      handleLogin();
    }
  }
</script>

<svelte:head>
  <title>로그인 - WebPos</title>
</svelte:head>

<div class="min-h-screen flex items-center justify-center bg-gray-50 py-12 px-4 sm:px-6 lg:px-8">
  <div class="max-w-md w-full space-y-8">
    <div>
      <h2 class="mt-6 text-center text-3xl font-extrabold text-gray-900">
        WebPos 시스템
      </h2>
      <p class="mt-2 text-center text-sm text-gray-600">
        계정으로 로그인하세요
      </p>
    </div>
    
    <form class="mt-8 space-y-6" on:submit|preventDefault={handleLogin}>
      <div class="space-y-4">
        <div>
          <label for="username" class="block text-sm font-medium text-gray-700">
            아이디
          </label>
          <input
            id="username"
            name="username"
            type="text"
            required
            bind:value={username}
            on:keypress={handleKeyPress}
            class="mt-1 input"
            placeholder="아이디를 입력하세요"
            disabled={loading}
          />
        </div>
        
        <div>
          <label for="password" class="block text-sm font-medium text-gray-700">
            비밀번호
          </label>
          <input
            id="password"
            name="password"
            type="password"
            required
            bind:value={password}
            on:keypress={handleKeyPress}
            class="mt-1 input"
            placeholder="비밀번호를 입력하세요"
            disabled={loading}
          />
        </div>
      </div>

      {#if error}
        <div class="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded-lg">
          {error}
        </div>
      {/if}

      <div>
        <button
          type="submit"
          disabled={loading}
          class="w-full btn btn-primary disabled:opacity-50 disabled:cursor-not-allowed"
        >
          {#if loading}
            <div class="flex items-center justify-center">
              <div class="animate-spin rounded-full h-4 w-4 border-b-2 border-white mr-2"></div>
              로그인 중...
            </div>
          {:else}
            로그인
          {/if}
        </button>
      </div>
    </form>
  </div>
</div>
