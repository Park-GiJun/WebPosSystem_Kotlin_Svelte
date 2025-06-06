<script>
  import { authStore } from '$lib/stores/auth.js';
  import { goto } from '$app/navigation';
  import { createEventDispatcher } from 'svelte';

  const dispatch = createEventDispatcher();

  $: user = $authStore.user;

  let showUserMenu = false;

  function toggleUserMenu() {
    showUserMenu = !showUserMenu;
  }

  function handleLogout() {
    authStore.logout();
    goto('/login');
  }

  function handleProfile() {
    showUserMenu = false;
    goto('/profile');
  }

  function handleSettings() {
    showUserMenu = false;
    goto('/settings');
  }

  // 외부 클릭 감지
  function handleClickOutside(event) {
    if (!event.target.closest('.user-menu-container')) {
      showUserMenu = false;
    }
  }
</script>

<svelte:window on:click={handleClickOutside} />

<header class="bg-white shadow-sm border-b border-gray-200">
  <div class="flex items-center justify-between h-16 px-6">
    <!-- 좌측: 페이지 제목 영역 -->
    <div class="flex items-center">
      <h1 class="text-lg font-semibold text-gray-900">
        <slot name="title">대시보드</slot>
      </h1>
    </div>

    <!-- 우측: 사용자 메뉴 영역 -->
    <div class="flex items-center space-x-4">
      <!-- 알림 버튼 -->
      <button
        type="button"
        class="p-2 text-gray-400 hover:text-gray-600 rounded-full hover:bg-gray-100 transition-colors duration-200"
        title="알림"
      >
        🔔
      </button>

      <!-- 사용자 메뉴 -->
      <div class="relative user-menu-container">
        <button
          type="button"
          class="flex items-center space-x-3 p-2 rounded-lg hover:bg-gray-100 transition-colors duration-200"
          on:click={toggleUserMenu}
        >
          <div class="w-8 h-8 bg-primary-600 rounded-full flex items-center justify-center">
            <span class="text-white text-sm">👤</span>
          </div>
          {#if user}
            <div class="text-left">
              <p class="text-sm font-medium text-gray-900">{user.username}</p>
              <p class="text-xs text-gray-500">
                {user.roles?.[0] || 'USER'}
              </p>
            </div>
          {/if}
        </button>

        <!-- 드롭다운 메뉴 -->
        {#if showUserMenu}
          <div class="absolute right-0 mt-2 w-48 bg-white rounded-md shadow-lg ring-1 ring-black ring-opacity-5 z-50">
            <div class="py-1">
              <button
                type="button"
                class="flex items-center w-full px-4 py-2 text-sm text-gray-700 hover:bg-gray-100"
                on:click={handleProfile}
              >
                <span class="mr-3">👤</span>
                프로필
              </button>
              
              <button
                type="button"
                class="flex items-center w-full px-4 py-2 text-sm text-gray-700 hover:bg-gray-100"
                on:click={handleSettings}
              >
                <span class="mr-3">⚙️</span>
                설정
              </button>
              
              <hr class="my-1" />
              
              <button
                type="button"
                class="flex items-center w-full px-4 py-2 text-sm text-red-700 hover:bg-red-50"
                on:click={handleLogout}
              >
                <span class="mr-3">🚪</span>
                로그아웃
              </button>
            </div>
          </div>
        {/if}
      </div>
    </div>
  </div>
</header>
