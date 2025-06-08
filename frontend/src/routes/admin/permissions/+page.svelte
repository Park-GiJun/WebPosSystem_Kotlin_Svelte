<script>
  import { onMount } from 'svelte';
  import { authStore } from '$lib/stores/auth.js';
  import { tabStore } from '$lib/stores/tabs.js';
  import { toastStore } from '$lib/stores/toast.js';
  import { permissionApi } from '$lib/api/admin.js';
  import { Key, Search, Plus, Shield, Users, Building, Store as StoreIcon, Edit, Trash2, RefreshCw } from 'lucide-svelte';

  let permissions = [];
  let loading = true;
  let searchTerm = '';
  let filterTargetType = 'all';
  let filterPermissionType = 'all';
  let showCreateModal = false;
  let cacheInvalidating = false;

  // 인증 상태 구독
  let authToken = '';
  authStore.subscribe(state => {
    authToken = state.token || '';
  });

  // 탭 활성화
  onMount(() => {
    tabStore.setActiveTab('ADMIN_PERMISSIONS');
    loadPermissions();
  });

  async function loadPermissions() {
    if (!authToken) {
      console.warn('인증 토큰이 없습니다.');
      return;
    }

    try {
      loading = true;
      console.log('🔑 권한 목록 조회 중...');
      
      const response = await permissionApi.getPermissions(authToken);
      
      if (response && response.permissions) {
        permissions = response.permissions;
        console.log('✅ 권한 목록 로드 완료:', permissions.length, '개');
      } else {
        console.warn('⚠️ 응답에 permissions 필드가 없습니다:', response);
        permissions = [];
      }
    } catch (error) {
      console.error('❌ 권한 목록 로드 실패:', error);
      toastStore.error('권한 목록을 불러오는데 실패했습니다: ' + error.message);
      permissions = [];
    } finally {
      loading = false;
    }
  }

  // 필터링된 권한 목록
  $: filteredPermissions = permissions.filter(permission => {
    const matchesSearch = (permission.menuName || '').toLowerCase().includes(searchTerm.toLowerCase()) ||
                         (permission.targetName || '').toLowerCase().includes(searchTerm.toLowerCase()) ||
                         (permission.menuCode || '').toLowerCase().includes(searchTerm.toLowerCase());
    const matchesTargetType = filterTargetType === 'all' || permission.targetType === filterTargetType;
    const matchesPermissionType = filterPermissionType === 'all' || permission.permissionType === filterPermissionType;
    
    return matchesSearch && matchesTargetType && matchesPermissionType;
  });

  function getTargetTypeColor(targetType) {
    const colors = {
      'USER': 'bg-blue-100 text-blue-800',
      'ROLE': 'bg-purple-100 text-purple-800',
      'STORE': 'bg-green-100 text-green-800',
      'HEADQUARTERS': 'bg-orange-100 text-orange-800'
    };
    return colors[targetType] || 'bg-gray-100 text-gray-800';
  }

  function getTargetTypeText(targetType) {
    const texts = {
      'USER': '사용자',
      'ROLE': '역할',
      'STORE': '매장',
      'HEADQUARTERS': '본사'
    };
    return texts[targetType] || targetType;
  }

  function getPermissionTypeColor(permissionType) {
    const colors = {
      'READ': 'bg-gray-100 text-gray-800',
      'WRITE': 'bg-blue-100 text-blue-800',
      'DELETE': 'bg-red-100 text-red-800',
      'ADMIN': 'bg-purple-100 text-purple-800'
    };
    return colors[permissionType] || 'bg-gray-100 text-gray-800';
  }

  function getPermissionTypeText(permissionType) {
    const texts = {
      'READ': '읽기',
      'WRITE': '쓰기',
      'DELETE': '삭제',
      'ADMIN': '관리'
    };
    return texts[permissionType] || permissionType;
  }

  function getTargetTypeIcon(targetType) {
    const icons = {
      'USER': Users,
      'ROLE': Shield,
      'STORE': StoreIcon,
      'HEADQUARTERS': Building
    };
    return icons[targetType] || Key;
  }

  async function invalidateCache() {
    if (!authToken) {
      toastStore.error('인증이 필요합니다.');
      return;
    }

    try {
      cacheInvalidating = true;
      console.log('🔄 권한 캐시 무효화 중...');
      
      await permissionApi.invalidateCache(authToken);
      
      toastStore.success('권한 캐시가 무효화되었습니다.');
      console.log('✅ 권한 캐시 무효화 완료');
      
      // 권한 목록 다시 로드
      await loadPermissions();
    } catch (error) {
      console.error('❌ 권한 캐시 무효화 실패:', error);
      toastStore.error('권한 캐시 무효화에 실패했습니다: ' + error.message);
    } finally {
      cacheInvalidating = false;
    }
  }

  async function editPermission(permission) {
    // TODO: 권한 편집 모달 구현
    console.log('Edit permission:', permission);
    toastStore.info('권한 편집 기능은 준비 중입니다.');
  }

  async function deletePermission(permission) {
    if (!confirm(`정말로 이 권한을 삭제하시겠습니까?`)) {
      return;
    }

    if (!authToken) {
      toastStore.error('인증이 필요합니다.');
      return;
    }

    try {
      console.log('🗑️ 권한 삭제 중:', permission);
      
      // TODO: 권한 삭제 API 호출
      // await permissionApi.deletePermission(permission.id, authToken);
      
      // 임시로 로컬에서 제거
      permissions = permissions.filter(p => p.id !== permission.id);
      toastStore.success('권한이 삭제되었습니다.');
      console.log('✅ 권한 삭제 완료');
    } catch (error) {
      console.error('❌ 권한 삭제 실패:', error);
      toastStore.error('권한 삭제에 실패했습니다: ' + error.message);
    }
  }

  function openCreateModal() {
    showCreateModal = true;
  }

  // 통계 계산
  $: totalPermissions = permissions.length;
  $: roleBasedPermissions = permissions.filter(p => p.targetType === 'ROLE').length;
  $: userDirectPermissions = permissions.filter(p => p.targetType === 'USER').length;
  $: storeBasedPermissions = permissions.filter(p => p.targetType === 'STORE').length;
</script>

<svelte:head>
  <title>권한 관리 - WebPos</title>
</svelte:head>

<div class="h-full flex flex-col space-y-6">
  <!-- 헤더 -->
  <div class="flex items-center justify-between">
    <div>
      <h1 class="text-2xl font-bold text-gray-900">권한 관리</h1>
      <p class="text-gray-600 mt-1">시스템 메뉴별 접근 권한을 관리합니다.</p>
    </div>
    <div class="flex items-center space-x-3">
      <button 
        type="button" 
        class="btn btn-secondary"
        on:click={invalidateCache}
        disabled={cacheInvalidating}
      >
        {#if cacheInvalidating}
          <RefreshCw size="16" class="mr-2 animate-spin" />
          무효화 중...
        {:else}
          <RefreshCw size="16" class="mr-2" />
          캐시 무효화
        {/if}
      </button>
      <button 
        type="button" 
        class="btn btn-primary"
        on:click={openCreateModal}
      >
        <Plus size="16" class="mr-2" />
        권한 추가
      </button>
    </div>
  </div>

  <!-- 통계 카드 -->
  <div class="grid grid-cols-1 md:grid-cols-4 gap-6">
    <div class="card p-6">
      <div class="flex items-center">
        <div class="p-3 rounded-full bg-blue-100">
          <Key class="h-6 w-6 text-blue-600" />
        </div>
        <div class="ml-4">
          <p class="text-sm font-medium text-gray-600">총 권한</p>
          <p class="text-2xl font-bold text-gray-900">{totalPermissions}</p>
        </div>
      </div>
    </div>

    <div class="card p-6">
      <div class="flex items-center">
        <div class="p-3 rounded-full bg-purple-100">
          <Shield class="h-6 w-6 text-purple-600" />
        </div>
        <div class="ml-4">
          <p class="text-sm font-medium text-gray-600">역할 기반</p>
          <p class="text-2xl font-bold text-gray-900">{roleBasedPermissions}</p>
        </div>
      </div>
    </div>

    <div class="card p-6">
      <div class="flex items-center">
        <div class="p-3 rounded-full bg-green-100">
          <Users class="h-6 w-6 text-green-600" />
        </div>
        <div class="ml-4">
          <p class="text-sm font-medium text-gray-600">사용자 직접</p>
          <p class="text-2xl font-bold text-gray-900">{userDirectPermissions}</p>
        </div>
      </div>
    </div>

    <div class="card p-6">
      <div class="flex items-center">
        <div class="p-3 rounded-full bg-orange-100">
          <StoreIcon class="h-6 w-6 text-orange-600" />
        </div>
        <div class="ml-4">
          <p class="text-sm font-medium text-gray-600">매장 기반</p>
          <p class="text-2xl font-bold text-gray-900">{storeBasedPermissions}</p>
        </div>
      </div>
    </div>
  </div>

  <!-- 검색 및 필터 -->
  <div class="card p-6">
    <div class="grid grid-cols-1 md:grid-cols-3 gap-4">
      <!-- 검색 -->
      <div class="relative">
        <Search size="20" class="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400" />
        <input
          type="text"
          placeholder="메뉴명 또는 대상명 검색..."
          bind:value={searchTerm}
          class="input pl-10"
        />
      </div>

      <!-- 대상 타입 필터 -->
      <select bind:value={filterTargetType} class="input">
        <option value="all">모든 대상 타입</option>
        <option value="USER">사용자</option>
        <option value="ROLE">역할</option>
        <option value="STORE">매장</option>
        <option value="HEADQUARTERS">본사</option>
      </select>

      <!-- 권한 타입 필터 -->
      <select bind:value={filterPermissionType} class="input">
        <option value="all">모든 권한 타입</option>
        <option value="READ">읽기</option>
        <option value="WRITE">쓰기</option>
        <option value="DELETE">삭제</option>
        <option value="ADMIN">관리</option>
      </select>
    </div>
  </div>

  <!-- 권한 목록 -->
  <div class="card overflow-hidden flex-1 flex flex-col min-h-0">
    {#if loading}
      <div class="p-12 text-center">
        <div class="animate-spin rounded-full h-8 w-8 border-b-2 border-primary-600 mx-auto"></div>
        <p class="mt-4 text-gray-600">로딩 중...</p>
      </div>
    {:else if filteredPermissions.length === 0}
      <div class="p-12 text-center">
        <Key class="mx-auto h-12 w-12 text-gray-400" />
        <p class="mt-4 text-gray-500">조건에 맞는 권한이 없습니다.</p>
        {#if totalPermissions === 0}
          <p class="text-sm text-gray-400 mt-2">권한 데이터를 불러오지 못했거나 권한이 설정되지 않았습니다.</p>
        {/if}
      </div>
    {:else}
      <div class="flex-1 overflow-y-auto">
        <table class="min-w-full divide-y divide-gray-200">
          <thead class="bg-gray-50">
            <tr>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                메뉴
              </th>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                대상
              </th>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                권한 타입
              </th>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                부여자
              </th>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                부여일
              </th>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                만료일
              </th>
              <th class="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase tracking-wider">
                작업
              </th>
            </tr>
          </thead>
          <tbody class="bg-white divide-y divide-gray-200">
            {#each filteredPermissions as permission}
              <tr class="hover:bg-gray-50">
                <td class="px-6 py-4 whitespace-nowrap">
                  <div>
                    <div class="text-sm font-medium text-gray-900">{permission.menuName || 'N/A'}</div>
                    <div class="text-sm text-gray-500">{permission.menuCode || 'N/A'}</div>
                  </div>
                </td>
                <td class="px-6 py-4 whitespace-nowrap">
                  <div class="flex items-center">
                    <div class="flex-shrink-0 mr-3">
                      <svelte:component 
                        this={getTargetTypeIcon(permission.targetType)} 
                        class="h-5 w-5 text-gray-400" 
                      />
                    </div>
                    <div>
                      <div class="text-sm font-medium text-gray-900">{permission.targetName || permission.targetId || 'N/A'}</div>
                      <span class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium {getTargetTypeColor(permission.targetType)}">
                        {getTargetTypeText(permission.targetType)}
                      </span>
                    </div>
                  </div>
                </td>
                <td class="px-6 py-4 whitespace-nowrap">
                  <span class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium {getPermissionTypeColor(permission.permissionType)}">
                    {getPermissionTypeText(permission.permissionType)}
                  </span>
                </td>
                <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                  {permission.grantedBy || 'N/A'}
                </td>
                <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                  {permission.grantedAt ? new Date(permission.grantedAt).toLocaleDateString('ko-KR') : 'N/A'}
                </td>
                <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                  {permission.expiresAt ? new Date(permission.expiresAt).toLocaleDateString('ko-KR') : '무제한'}
                </td>
                <td class="px-6 py-4 whitespace-nowrap text-right text-sm font-medium">
                  <div class="flex justify-end space-x-2">
                    <button
                      type="button"
                      class="text-indigo-600 hover:text-indigo-900"
                      on:click={() => editPermission(permission)}
                      title="편집"
                    >
                      <Edit size="16" />
                    </button>
                    <button
                      type="button"
                      class="text-red-600 hover:text-red-900"
                      on:click={() => deletePermission(permission)}
                      title="삭제"
                    >
                      <Trash2 size="16" />
                    </button>
                  </div>
                </td>
              </tr>
            {/each}
          </tbody>
        </table>
      </div>
    {/if}
  </div>
</div>

<!-- TODO: 권한 생성/편집 모달 구현 -->
{#if showCreateModal}
  <div class="fixed inset-0 bg-gray-600 bg-opacity-50 overflow-y-auto h-full w-full z-50">
    <div class="relative top-20 mx-auto p-5 border w-96 shadow-lg rounded-md bg-white">
      <div class="mt-3 text-center">
        <h3 class="text-lg font-medium text-gray-900">권한 추가</h3>
        <p class="text-sm text-gray-500 mt-2">권한 추가 기능은 준비 중입니다.</p>
        <div class="items-center px-4 py-3">
          <button
            type="button"
            class="btn btn-secondary"
            on:click={() => showCreateModal = false}
          >
            닫기
          </button>
        </div>
      </div>
    </div>
  </div>
{/if}
