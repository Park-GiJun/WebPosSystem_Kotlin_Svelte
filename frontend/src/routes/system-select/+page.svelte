<script>
  import { authStore } from '$lib/stores/auth.js';
  import { goto } from '$app/navigation';
  import { Shield, Building, Monitor } from 'lucide-svelte';

  $: user = $authStore.user;

  // 사용자 권한에 따른 시스템 접근 권한 확인
  $: availableSystems = getAvailableSystems(user?.roles || []);

  function getAvailableSystems(roles) {
    const systems = [];

    // 슈퍼어드민 시스템
    if (roles.includes('SUPER_ADMIN') || roles.includes('SYSTEM_ADMIN')) {
      systems.push({
        id: 'admin',
        title: '슈퍼어드민',
        description: '사용자, 권한, 시스템 관리',
        icon: Shield,
        color: 'bg-red-100 text-red-600',
        hoverColor: 'hover:bg-red-50',
        borderColor: 'border-red-200',
        path: '/admin/users'
      });
    }

    // 영업정보시스템
    if (roles.includes('SUPER_ADMIN') || roles.includes('SYSTEM_ADMIN') || 
        roles.includes('HQ_MANAGER') || roles.includes('AREA_MANAGER')) {
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
    }

    // POS 시스템
    if (roles.includes('SUPER_ADMIN') || roles.includes('SYSTEM_ADMIN') || 
        roles.includes('STORE_MANAGER') || roles.includes('USER')) {
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

    return systems;
  }

  function selectSystem(system) {
    try {
      goto(system.path);
    } catch (error) {
      console.error('Navigation error:', error);
      // 라우팅 에러 발생 시 대체 경로로 이동
      if (system.id === 'pos') {
        goto('/pos/sales');
      } else {
        goto(system.path);
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

<div class="min-h-screen bg-gray-50 flex flex-col justify-center py-12 sm:px-6 lg:px-8">
  <!-- 헤더 -->
  <div class="sm:mx-auto sm:w-full sm:max-w-md">
    <h1 class="text-center text-3xl font-extrabold text-gray-900 mb-2">
      WebPos 시스템
    </h1>
    <p class="text-center text-sm text-gray-600">
      사용할 시스템을 선택해주세요
    </p>
  </div>

  <!-- 사용자 정보 -->
  {#if user}
    <div class="mt-8 sm:mx-auto sm:w-full sm:max-w-md">
      <div class="bg-white py-4 px-6 shadow rounded-lg border border-gray-200">
        <div class="flex items-center">
          <div class="w-10 h-10 bg-primary-600 rounded-full flex items-center justify-center">
            <span class="text-white font-medium text-sm">
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
  <div class="mt-8 sm:mx-auto sm:w-full sm:max-w-2xl">
    {#if availableSystems.length === 0}
      <div class="bg-white py-8 px-6 shadow rounded-lg border border-gray-200 text-center">
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
      <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
        {#each availableSystems as system}
          <button
            type="button"
            class="bg-white p-6 rounded-lg shadow border-2 {system.borderColor} {system.hoverColor} transition-all duration-200 hover:shadow-md text-left group"
            on:click={() => selectSystem(system)}
          >
            <div class="flex items-center mb-4">
              <div class="p-3 rounded-full {system.color}">
                <svelte:component this={system.icon} class="h-6 w-6" />
              </div>
            </div>
            
            <h3 class="text-lg font-semibold text-gray-900 mb-2 group-hover:text-gray-700">
              {system.title}
            </h3>
            
            <p class="text-sm text-gray-600 group-hover:text-gray-500">
              {system.description}
            </p>
            
            <div class="mt-4 text-right">
              <span class="text-sm font-medium text-primary-600 group-hover:text-primary-700">
                접속하기 →
              </span>
            </div>
          </button>
        {/each}
      </div>
      
      <!-- 로그아웃 버튼 -->
      <div class="mt-8 text-center">
        <button
          type="button"
          class="text-sm text-gray-500 hover:text-gray-700"
          on:click={logout}
        >
          로그아웃
        </button>
      </div>
    {/if}
  </div>
</div>
