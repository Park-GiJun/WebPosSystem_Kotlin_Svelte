<script>
  import { authStore } from '$lib/stores/auth.js';
  import { goto } from '$app/navigation';
  import { onMount } from 'svelte';
  import { Shield, Building, Monitor } from 'lucide-svelte';
  import LoadingSpinner from '$lib/components/Common/LoadingSpinner.svelte';

  $: authState = $authStore;
  $: user = authState.user;
  $: menus = authState.menus || [];
  $: isLoading = authState.isAuthenticated && menus.length === 0; // 로그인은 되었지만 메뉴 로딩 중

  // 사용자 권한에 따른 시스템 접근 권한 확인
  $: availableSystems = getAvailableSystems(user?.roles || [], menus);

  onMount(() => {
    // 인증 상태 확인
    if (!authState.isAuthenticated) {
      goto('/login');
    }
  });

  function getAvailableSystems(roles, menus) {
    const systems = [];

    // 메뉴 데이터를 기반으로 접근 가능한 시스템들 확인
    const hasAdminMenus = menus.some(menu => menu.menuCode && menu.menuCode.startsWith('ADMIN'));
    const hasBusinessMenus = menus.some(menu => menu.menuCode && menu.menuCode.startsWith('BUSINESS'));
    const hasPosMenus = menus.some(menu => menu.menuCode && menu.menuCode.startsWith('POS'));

    console.log('🔍 시스템별 메뉴 접근 권한:', { hasAdminMenus, hasBusinessMenus, hasPosMenus });

    // 관리자 시스템
    if (hasAdminMenus && (roles.includes('SUPER_ADMIN') || roles.includes('SYSTEM_ADMIN'))) {
      systems.push({
        id: 'admin',
        title: '관리자',
        description: '사용자, 권한, 시스템 관리',
        icon: Shield,
        color: 'bg-red-100 text-red-600',
        hoverColor: 'hover:bg-red-50',
        borderColor: 'border-red-200',
        path: '/admin/users'
      });
    }

    // 영업정보시스템 - 사용자 권한에 따라 다른 기본 경로
    if (hasBusinessMenus) {
      if (roles.includes('SUPER_ADMIN') || roles.includes('SYSTEM_ADMIN')) {
        // 관리자도 영업정보시스템으로 갈 수 있음 - 조직 관리는 admin 시스템임
        systems.push({
          id: 'business',
          title: '영업정보시스템',
          description: '본사, 매장, 매출 관리',
          icon: Building,
          color: 'bg-blue-100 text-blue-600',
          hoverColor: 'hover:bg-blue-50',
          borderColor: 'border-blue-200',
          path: '/business/stores'
        });
      } else if (roles.includes('HQ_MANAGER')) {
        // 본사 관리자는 가맹점 관리로
        systems.push({
          id: 'business',
          title: '영업정보시스템',
          description: '가맹점, POS, 매출 관리',
          icon: Building,
          color: 'bg-blue-100 text-blue-600',
          hoverColor: 'hover:bg-blue-50',
          borderColor: 'border-blue-200',
          path: '/business/headquarters/stores'
        });
      } else if (roles.includes('STORE_MANAGER')) {
        // 매장 관리자는 POS 관리로
        systems.push({
          id: 'business',
          title: '영업정보시스템',
          description: 'POS, 직원, 매출 관리',
          icon: Building,
          color: 'bg-blue-100 text-blue-600',
          hoverColor: 'hover:bg-blue-50',
          borderColor: 'border-blue-200',
          path: '/business/pos'
        });
      }
    }

    // POS 시스템
    if (hasPosMenus && (roles.includes('SUPER_ADMIN') || roles.includes('SYSTEM_ADMIN') || 
        roles.includes('STORE_MANAGER') || roles.includes('USER'))) {
      systems.push({
        id: 'pos',
        title: 'POS 시스템',
        description: '판매, 상품, 고객 관리',
        icon: Monitor,
        color: 'bg-green-100 text-green-600',
        hoverColor: 'hover:bg-green-50',
        borderColor: 'border-green-200',
        path: '/pos/sales'
      });
    }

    console.log('✅ 접근 가능한 시스템들:', systems.map(s => s.id));
    return systems;
  }

  function selectSystem(system) {
    console.log('🎯 시스템 선택:', system.id, '→', system.path);
    try {
      goto(system.path);
    } catch (error) {
      console.error('Navigation error:', error);
      // 라우팅 에러 발생 시 대체 경로로 이동
      if (system.id === 'pos') {
        goto('/pos');
      } else if (system.id === 'admin') {
        goto('/admin');
      } else if (system.id === 'business') {
        goto('/business');
      }
    }
  }

  function logout() {
    authStore.logout();
    goto('/login');
  }
</script>

<svelte:head>
  <title>시스템 선택 - WebPos</title>
</svelte:head>

<div class="min-h-screen bg-gray-50 flex flex-col justify-center py-8 sm:py-12 px-4 sm:px-6 lg:px-8">
  <!-- 헤더 -->
  <div class="sm:mx-auto sm:w-full sm:max-w-md">
    <h1 class="text-center text-2xl sm:text-3xl font-extrabold text-gray-900 mb-2">
      WebPos 시스템
    </h1>
    <p class="text-center text-sm text-gray-600">
      사용할 시스템을 선택해주세요
    </p>
  </div>

  <!-- 사용자 정보 -->
  {#if user}
    <div class="mt-6 sm:mt-8 sm:mx-auto sm:w-full sm:max-w-md">
      <div class="bg-white py-4 px-4 sm:px-6 shadow rounded-lg border border-gray-200">
        <div class="flex items-center">
          <div class="w-8 h-8 sm:w-10 sm:h-10 bg-primary-600 rounded-full flex items-center justify-center">
            <span class="text-white font-medium text-xs sm:text-sm">
              {user.username.charAt(0).toUpperCase()}
            </span>
          </div>
          <div class="ml-3">
            <p class="text-sm font-medium text-gray-900">{user.username}</p>
            <p class="text-xs text-gray-500">{user.email}</p>
          </div>
        </div>
        <div class="mt-3 flex flex-wrap gap-1">
          {#each user.roles || [] as role}
            <span class="inline-flex items-center px-2 py-1 rounded-full text-xs font-medium bg-primary-100 text-primary-800">
              {role}
            </span>
          {/each}
        </div>
      </div>
    </div>
  {/if}

  <!-- 시스템 선택 -->
  <div class="mt-6 sm:mt-8 sm:mx-auto sm:w-full sm:max-w-4xl">
    {#if isLoading}
      <div class="bg-white py-8 px-4 sm:px-6 shadow rounded-lg border border-gray-200 text-center">
        <LoadingSpinner size="lg" />
        <p class="mt-4 text-gray-500">메뉴 정보를 로딩 중입니다...</p>
      </div>
    {:else if availableSystems.length === 0}
      <div class="bg-white py-8 px-4 sm:px-6 shadow rounded-lg border border-gray-200 text-center">
        <p class="text-gray-500">접근 가능한 시스템이 없습니다.</p>
        <button
          type="button"
          class="mt-4 btn btn-secondary"
          on:click={logout}
        >
          로그아웃
        </button>
      </div>
    {:else}
      <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4 sm:gap-6">
        {#each availableSystems as system}
          <button
            type="button"
            class="bg-white p-4 sm:p-6 rounded-lg shadow border-2 {system.borderColor} {system.hoverColor} transition-all duration-200 hover:shadow-md text-left group touch-manipulation"
            on:click={() => selectSystem(system)}
          >
            <div class="flex items-center mb-3 sm:mb-4">
              <div class="p-2 sm:p-3 rounded-full {system.color}">
                <svelte:component this={system.icon} class="h-5 w-5 sm:h-6 sm:w-6" />
              </div>
            </div>
            
            <h3 class="text-base sm:text-lg font-semibold text-gray-900 mb-2 group-hover:text-gray-700">
              {system.title}
            </h3>
            
            <p class="text-xs sm:text-sm text-gray-600 group-hover:text-gray-500 mb-3 sm:mb-0">
              {system.description}
            </p>
            
            <div class="mt-3 sm:mt-4 text-right">
              <span class="text-xs sm:text-sm font-medium text-primary-600 group-hover:text-primary-700">
                접속하기 →
              </span>
            </div>
          </button>
        {/each}
      </div>
      
      <!-- 로그아웃 버튼 -->
      <div class="mt-6 sm:mt-8 text-center">
        <button
          type="button"
          class="text-sm text-gray-500 hover:text-gray-700 py-2 px-4 rounded-lg hover:bg-gray-100 transition-colors touch-manipulation"
          on:click={logout}
        >
          로그아웃
        </button>
      </div>
    {/if}
  </div>
</div>
