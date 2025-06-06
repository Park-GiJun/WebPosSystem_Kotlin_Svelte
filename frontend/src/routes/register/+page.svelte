<script>
  import { goto } from '$app/navigation';
  import { toastStore } from '$lib/stores/toast.js';

  let username = '';
  let email = '';
  let password = '';
  let confirmPassword = '';
  let loading = false;
  let error = '';

  async function handleRegister() {
    if (!username || !email || !password) {
      error = '모든 필드를 입력해주세요.';
      return;
    }

    if (password !== confirmPassword) {
      error = '비밀번호가 일치하지 않습니다.';
      return;
    }

    if (password.length < 6) {
      error = '비밀번호는 최소 6자 이상이어야 합니다.';
      return;
    }

    loading = true;
    error = '';

    try {
      const response = await fetch('/api/v1/auth/register', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          username,
          email,
          password
        }),
      });

      if (response.ok) {
        const data = await response.json();
        toastStore.success('회원가입이 완료되었습니다. 로그인해주세요.');
        goto('/login');
      } else {
        const errorData = await response.json();
        error = errorData.message || '회원가입에 실패했습니다.';
      }
    } catch (err) {
      console.error('Register error:', err);
      error = '서버 오류가 발생했습니다.';
    }

    loading = false;
  }

  function handleKeyPress(event) {
    if (event.key === 'Enter') {
      handleRegister();
    }
  }
</script>

<svelte:head>
  <title>회원가입 - WebPos</title>
</svelte:head>

<div class="min-h-screen flex items-center justify-center bg-gray-50 py-12 px-4 sm:px-6 lg:px-8">
  <div class="max-w-md w-full space-y-8">
    <div>
      <h2 class="mt-6 text-center text-3xl font-extrabold text-gray-900">
        회원가입
      </h2>
      <p class="mt-2 text-center text-sm text-gray-600">
        WebPos 시스템에 가입하세요
      </p>
    </div>
    
    <form class="mt-8 space-y-6" on:submit|preventDefault={handleRegister}>
      <div class="space-y-4">
        <div>
          <label for="username" class="block text-sm font-medium text-gray-700">
            사용자명
          </label>
          <input
            id="username"
            name="username"
            type="text"
            required
            bind:value={username}
            on:keypress={handleKeyPress}
            class="mt-1 input"
            placeholder="사용자명을 입력하세요"
            disabled={loading}
          />
        </div>

        <div>
          <label for="email" class="block text-sm font-medium text-gray-700">
            이메일
          </label>
          <input
            id="email"
            name="email"
            type="email"
            required
            bind:value={email}
            on:keypress={handleKeyPress}
            class="mt-1 input"
            placeholder="이메일을 입력하세요"
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
            placeholder="비밀번호를 입력하세요 (최소 6자)"
            disabled={loading}
          />
        </div>

        <div>
          <label for="confirmPassword" class="block text-sm font-medium text-gray-700">
            비밀번호 확인
          </label>
          <input
            id="confirmPassword"
            name="confirmPassword"
            type="password"
            required
            bind:value={confirmPassword}
            on:keypress={handleKeyPress}
            class="mt-1 input"
            placeholder="비밀번호를 다시 입력하세요"
            disabled={loading}
          />
        </div>
      </div>

      {#if error}
        <div class="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded-lg">
          {error}
        </div>
      {/if}

      <div class="space-y-4">
        <button
          type="submit"
          disabled={loading}
          class="w-full btn btn-primary disabled:opacity-50 disabled:cursor-not-allowed"
        >
          {#if loading}
            <div class="flex items-center justify-center">
              <div class="animate-spin rounded-full h-4 w-4 border-b-2 border-white mr-2"></div>
              회원가입 중...
            </div>
          {:else}
            회원가입
          {/if}
        </button>

        <div class="text-center">
          <a href="/login" class="text-sm text-primary-600 hover:text-primary-500">
            이미 계정이 있으신가요? 로그인
          </a>
        </div>
      </div>
    </form>
  </div>
</div>
