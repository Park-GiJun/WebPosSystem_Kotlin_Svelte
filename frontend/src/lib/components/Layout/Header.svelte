<script>
  import { authStore } from '$lib/stores/auth.js';
  import { goto } from '$app/navigation';
  import { createEventDispatcher } from 'svelte';
  import { User, Bell, Settings, LogOut, Shield, ShoppingCart, BarChart, Menu } from 'lucide-svelte';

  export let title = "WebPos 시스템";
  export let subtitle = "";
  export let systemType = "default"; // admin, pos, business, default
  export let showNotificationsMenu = true;

  const dispatch = createEventDispatcher();

  $: user = $authStore.user;

  let showUserMenu = false;
  let showNotifications = false;

  const systemConfig = {
    admin: {
      gradient: 'from-red-600 via-red-700 to-red-800',
      icon: Shield,
      iconColor: 'text-red-200',
      glowColor: 'shadow-red-500/20',
      accent: 'border-red-400/30'
    },
    pos: {
      gradient: 'from-emerald-600 via-emerald-700 to-emerald-800', 
      icon: ShoppingCart,
      iconColor: 'text-emerald-200',
      glowColor: 'shadow-emerald-500/20',
      accent: 'border-emerald-400/30'
    },
    business: {
      gradient: 'from-blue-600 via-blue-700 to-blue-800',
      icon: BarChart, 
      iconColor: 'text-blue-200',
      glowColor: 'shadow-blue-500/20',
      accent: 'border-blue-400/30'
    },
    default: {
      gradient: 'from-slate-600 via-slate-700 to-slate-800',
      icon: Shield,
      iconColor: 'text-slate-200', 
      glowColor: 'shadow-slate-500/20',
      accent: 'border-slate-400/30'
    }
  };

  $: config = systemConfig[systemType] || systemConfig.default;
  $: IconComponent = config.icon;

  function toggleUserMenu() {
    showUserMenu = !showUserMenu;
    showNotifications = false;
  }

  function toggleNotifications() {
    showNotifications = !showNotifications;
    showUserMenu = false;
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

  function handleClickOutside(event) {
    if (!event.target.closest('.dropdown-container')) {
      showUserMenu = false;
      showNotifications = false;
    }
  }

  function getRoleDisplayName(role) {
    const roleNames = {
      'SUPER_ADMIN': '슈퍼관리자',
      'SYSTEM_ADMIN': '시스템관리자', 
      'HQ_MANAGER': '본사관리자',
      'STORE_MANAGER': '매장관리자',
      'USER': '사용자'
    };
    return roleNames[role] || role;
  }
</script>

<svelte:window on:click={handleClickOutside} />

<header class="relative bg-gradient-to-r {config.gradient} shadow-2xl {config.glowColor} backdrop-blur-sm fixed w-full top-0 z-50">
  <!-- Background Pattern -->
  <div class="absolute inset-0 bg-gradient-to-r from-black/10 via-transparent to-black/10"></div>
  <div class="absolute inset-0 opacity-30" style="background-image: url('data:image/svg+xml,%3Csvg width=&quot;60&quot; height=&quot;60&quot; viewBox=&quot;0 0 60 60&quot; xmlns=&quot;http://www.w3.org/2000/svg&quot;%3E%3Cg fill=&quot;none&quot; fill-rule=&quot;evenodd&quot;%3E%3Cg fill=&quot;%23ffffff&quot; fill-opacity=&quot;0.03&quot;%3E%3Ccircle cx=&quot;30&quot; cy=&quot;30&quot; r=&quot;2&quot;/%3E%3C/g%3E%3C/g%3E%3C/svg%3E')"></div>
  
  <div class="relative flex items-center justify-between h-16 px-6">
    <!-- 좌측: 시스템 정보 -->
    <div class="flex items-center space-x-4">
      <div class="flex items-center space-x-3">
        <!-- 시스템 아이콘 -->
        <div class="p-2 bg-white/10 rounded-xl backdrop-blur-sm border border-white/20 shadow-lg">
          <svelte:component this={IconComponent} size="20" class="{config.iconColor}" />
        </div>
        
        <!-- 제목 -->
        <div class="flex flex-col">
          <h1 class="text-xl font-bold text-white leading-tight tracking-tight">
            {title}
          </h1>
          {#if subtitle}
            <p class="text-sm text-white/80 font-medium">
              {subtitle}
            </p>
          {/if}
        </div>
      </div>
    </div>

    <!-- 우측: 액션 버튼들 -->
    <div class="flex items-center space-x-3">
      <!-- 알림 버튼 -->
      {#if showNotificationsMenu}
        <div class="relative dropdown-container">
          <button
            type="button"
            class="relative p-3 text-white/80 hover:text-white rounded-xl hover:bg-white/10 transition-all duration-200 backdrop-blur-sm border border-white/10 hover:border-white/20 group"
            on:click={toggleNotifications}
            title="알림"
          >
            <Bell size="18" class="transition-transform group-hover:scale-110" />
            <!-- 알림 배지 -->
            <span class="absolute -top-1 -right-1 w-3 h-3 bg-yellow-400 rounded-full border-2 border-white animate-pulse"></span>
          </button>

          <!-- 알림 드롭다운 -->
          {#if showNotifications}
            <div class="absolute right-0 mt-3 w-80 bg-white rounded-2xl shadow-2xl ring-1 ring-black/5 z-50 backdrop-blur-xl border border-gray-100">
              <div class="p-4 border-b border-gray-100">
                <h3 class="text-lg font-semibold text-gray-900">알림</h3>
              </div>
              <div class="max-h-96 overflow-y-auto">
                <div class="p-4 space-y-3">
                  <div class="flex items-start space-x-3 p-3 rounded-xl hover:bg-gray-50 transition-colors">
                    <div class="w-2 h-2 bg-blue-500 rounded-full mt-2 flex-shrink-0"></div>
                    <div class="flex-1 min-w-0">
                      <p class="text-sm font-medium text-gray-900">새로운 주문이 접수되었습니다</p>
                      <p class="text-xs text-gray-500 mt-1">5분 전</p>
                    </div>
                  </div>
                  <div class="flex items-start space-x-3 p-3 rounded-xl hover:bg-gray-50 transition-colors">
                    <div class="w-2 h-2 bg-green-500 rounded-full mt-2 flex-shrink-0"></div>
                    <div class="flex-1 min-w-0">
                      <p class="text-sm font-medium text-gray-900">시스템 업데이트가 완료되었습니다</p>
                      <p class="text-xs text-gray-500 mt-1">1시간 전</p>
                    </div>
                  </div>
                </div>
              </div>
              <div class="p-3 border-t border-gray-100">
                <button class="w-full text-center text-sm text-blue-600 hover:text-blue-800 font-medium py-2 hover:bg-blue-50 rounded-lg transition-colors">
                  모든 알림 보기
                </button>
              </div>
            </div>
          {/if}
        </div>
      {/if}

      <!-- 사용자 메뉴 -->
      <div class="relative dropdown-container">
        <button
          type="button"
          class="flex items-center space-x-3 p-2 pr-4 rounded-xl hover:bg-white/10 transition-all duration-200 backdrop-blur-sm border border-white/10 hover:border-white/20 group"
          on:click={toggleUserMenu}
        >
          <!-- 사용자 아바타 -->
          <div class="relative">
            <div class="w-9 h-9 bg-gradient-to-br from-white/20 to-white/10 rounded-xl flex items-center justify-center border border-white/20 group-hover:border-white/30 transition-all">
              <User size="18" class="text-white" />
            </div>
            <!-- 온라인 상태 표시 -->
            <div class="absolute -bottom-0.5 -right-0.5 w-3 h-3 bg-green-400 rounded-full border-2 border-white"></div>
          </div>
          
          {#if user}
            <div class="text-left hidden sm:block">
              <p class="text-sm font-semibold text-white leading-tight">{user.username}</p>
              <p class="text-xs text-white/70 font-medium">
                {getRoleDisplayName(user.roles?.[0] || 'USER')}
              </p>
            </div>
          {/if}

          <!-- 드롭다운 화살표 -->
          <div class="text-white/60 group-hover:text-white/80 transition-colors">
            <svg class="w-4 h-4 transform group-hover:scale-110 transition-transform" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 9l-7 7-7-7"></path>
            </svg>
          </div>
        </button>

        <!-- 사용자 드롭다운 메뉴 -->
        {#if showUserMenu}
          <div class="absolute right-0 mt-3 w-64 bg-white rounded-2xl shadow-2xl ring-1 ring-black/5 z-50 backdrop-blur-xl border border-gray-100">
            <!-- 사용자 정보 헤더 -->
            {#if user}
              <div class="p-4 border-b border-gray-100">
                <div class="flex items-center space-x-3">
                  <div class="w-12 h-12 bg-gradient-to-br {config.gradient} rounded-xl flex items-center justify-center">
                    <User size="20" class="text-white" />
                  </div>
                  <div class="flex-1 min-w-0">
                    <p class="text-sm font-semibold text-gray-900 truncate">{user.username}</p>
                    <p class="text-xs text-gray-500">{getRoleDisplayName(user.roles?.[0] || 'USER')}</p>
                    <p class="text-xs text-gray-400">{user.email || 'example@company.com'}</p>
                  </div>
                </div>
              </div>
            {/if}

            <!-- 메뉴 항목들 -->
            <div class="py-2">
              <button
                type="button"
                class="flex items-center w-full px-4 py-3 text-sm text-gray-700 hover:bg-gray-50 transition-colors group"
                on:click={handleProfile}
              >
                <User size="16" class="mr-3 text-gray-400 group-hover:text-gray-600" />
                <span class="font-medium">내 프로필</span>
              </button>
              
              <button
                type="button"
                class="flex items-center w-full px-4 py-3 text-sm text-gray-700 hover:bg-gray-50 transition-colors group"
                on:click={handleSettings}
              >
                <Settings size="16" class="mr-3 text-gray-400 group-hover:text-gray-600" />
                <span class="font-medium">설정</span>
              </button>
              
              <hr class="my-2 border-gray-100" />
              
              <button
                type="button"
                class="flex items-center w-full px-4 py-3 text-sm text-red-600 hover:bg-red-50 transition-colors group"
                on:click={handleLogout}
              >
                <LogOut size="16" class="mr-3 text-red-400 group-hover:text-red-600" />
                <span class="font-medium">로그아웃</span>
              </button>
            </div>
          </div>
        {/if}
      </div>
    </div>
  </div>

  <!-- 하단 그라디언트 테두리 -->
  <div class="absolute bottom-0 left-0 right-0 h-px bg-gradient-to-r from-transparent via-white/30 to-transparent"></div>
</header>

<style>
  @keyframes shimmer {
    0% { transform: translateX(-100%); }
    100% { transform: translateX(100%); }
  }
  
  .shimmer::before {
    content: '';
    position: absolute;
    top: 0;
    left: -100%;
    width: 100%;
    height: 100%;
    background: linear-gradient(90deg, transparent, rgba(255,255,255,0.2), transparent);
    animation: shimmer 2s infinite;
  }
</style>
