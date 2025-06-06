<script>
  import { onMount } from 'svelte';
  import { authStore } from '$lib/stores/auth.js';
  import { tabStore } from '$lib/stores/tabs.js';
  import { toastStore } from '$lib/stores/toast.js';
  import { Key, Users, Menu, Settings, Plus, Edit, Trash2, Shield } from 'lucide-svelte';

  let activeTab = 'menus';
  let loading = false;
  let menus = [];
  let rolePermissions = [];
  let selectedMenu = null;
  let selectedRole = null;

  const tabs = [
    { id: 'menus', label: '메뉴 권한', icon: Menu },
    { id: 'roles', label: '역할별 권한', icon: Users },
    { id: 'users', label: '사용자별 권한', icon: Shield }
  ];

  const permissionTypes = [
    { value: 'READ', label: '읽기', color: 'bg-blue-100 text-blue-800' },
    { value: 'WRITE', label: '쓰기', color: 'bg-green-100 text-green-800' },
    { value: 'DELETE', label: '삭제', color: 'bg-orange-100 text-orange-800' },
    { value: 'ADMIN', label: '관리자', color: 'bg-red-100 text-red-800' }
  ];

  const roles = [
    { value: 'SUPER_ADMIN', label: '최고 관리자', description: '모든 시스템 관리' },
    { value: 'SYSTEM_ADMIN', label: '시스템 관리자', description: '시스템 전반 관리' },
    { value: 'HQ_MANAGER', label: '본사 관리자', description: '영업정보시스템 관리' },
    { value: 'STORE_MANAGER', label: '매장 관리자', description: 'POS 시스템 관리' },
    { value: 'AREA_MANAGER', label: '지역 관리자', description: '담당 지역 매장 관리' },
    { value: 'USER', label: '일반 사용자', description: 'POS 판매 기능만' }
  ];

  onMount(() => {
    tabStore.setActiveTab('ADMIN_PERMISSIONS');
    loadMenus();
    loadRolePermissions();
  });

  async function loadMenus() {
    loading = true;
    try {
      const response = await fetch('/api/v1/admin/permissions/menus', {
        headers: {
          'Authorization': `Bearer ${$authStore.token}`
        }
      });
      
      if (response.ok) {
        const data = await response.json();
        menus = data.menus || [];
      }
    } catch (error) {
      console.error('Failed to load menus:', error);
      toastStore.error('메뉴 목록을 불러오는데 실패했습니다.');
    } finally {
      loading = false;
    }
  }

  async function loadRolePermissions() {
    try {
      const response = await fetch('/api/v1/admin/permissions/roles', {
        headers: {
          'Authorization': `Bearer ${$authStore.token}`
        }
      });
      
      if (response.ok) {
        const data = await response.json();
        rolePermissions = data || [];
      }
    } catch (error) {
      console.error('Failed to load role permissions:', error);
    }
  }

  function organizeMenusHierarchy(menus) {
    const menuMap = new Map();
    const rootMenus = [];

    // 먼저 모든 메뉴를 맵에 저장
    menus.forEach(menu => {
      menuMap.set(menu.menuId, { ...menu, children: [] });
    });

    // 계층 구조 구성
    menus.forEach(menu => {
      const menuItem = menuMap.get(menu.menuId);
      if (menu.parentMenuId) {
        const parent = menuMap.get(menu.parentMenuId);
        if (parent) {
          parent.children.push(menuItem);
        }
      } else {
        rootMenus.push(menuItem);
      }
    });

    // 표시 순서대로 정렬
    const sortMenus = (menus) => {
      return menus.sort((a, b) => a.displayOrder - b.displayOrder)
        .map(menu => ({
          ...menu,
          children: sortMenus(menu.children)
        }));
    };

    return sortMenus(rootMenus);
  }

  $: organizedMenus = organizeMenusHierarchy(menus);

  function getMenuTypeColor(type) {
    const colors = {
      'CATEGORY': 'bg-purple-100 text-purple-800',
      'MENU': 'bg-blue-100 text-blue-800',
      'FUNCTION': 'bg-green-100 text-green-800'
    };
    return colors[type] || 'bg-gray-100 text-gray-800';
  }

  function getPermissionColor(permissionType) {
    const type = permissionTypes.find(p => p.value === permissionType);
    return type?.color || 'bg-gray-100 text-gray-800';
  }

  async function grantPermission(menuCode, targetType, targetId, permissionType) {
    try {
      const response = await fetch('/api/v1/admin/permissions/grant', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${$authStore.token}`
        },
        body: JSON.stringify({
          menuCode,
          targetType,
          targetId,
          permissionType
        })
      });

      if (response.ok) {
        toastStore.success('권한이 부여되었습니다.');
        await loadRolePermissions();
      } else {
        throw new Error('권한 부여 실패');
      }
    } catch (error) {
      console.error('Grant permission error:', error);
      toastStore.error('권한 부여에 실패했습니다.');
    }
  }

  async function revokePermission(menuCode, targetType, targetId) {
    try {
      const response = await fetch('/api/v1/admin/permissions/revoke', {
        method: 'DELETE',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${$authStore.token}`
        },
        body: JSON.stringify({
          menuCode,
          targetType,
          targetId
        })
      });

      if (response.ok) {
        toastStore.success('권한이 회수되었습니다.');
        await loadRolePermissions();
      } else {
        throw new Error('권한 회수 실패');
      }
    } catch (error) {
      console.error('Revoke permission error:', error);
      toastStore.error('권한 회수에 실패했습니다.');
    }
  }

  function renderMenuTree(menus, level = 0) {
    return menus.map(menu => ({
      ...menu,
      level,
      children: renderMenuTree(menu.children, level + 1)
    }));
  }

  $: flatMenus = renderMenuTree(organizedMenus).flat(Infinity);
</script>

<svelte:head>
  <title>권한 관리 - WebPos</title>
</svelte:head>

<div class="space-y-6">
  <!-- 헤더 -->
  <div class="flex items-center justify-between">
    <div>
      <h1 class="text-2xl font-bold text-gray-900">권한 관리</h1>
      <p class="text-gray-600 mt-1">시스템 메뉴 및 사용자 권한을 관리합니다.</p>
    </div>
  </div>

  <!-- 탭 네비게이션 -->
  <div class="card">
    <div class="border-b border-gray-200">
      <nav class="-mb-px flex">
        {#each tabs as tab}
          <button
            type="button"
            class="tab flex items-center"
            class:active={activeTab === tab.id}
            on:click={() => activeTab = tab.id}
          >
            <svelte:component this={tab.icon} size="16" class="mr-2" />
            {tab.label}
          </button>
        {/each}
      </nav>
    </div>

    <div class="p-6">
      {#if activeTab === 'menus'}
        <!-- 메뉴 권한 탭 -->
        <div class="space-y-6">
          <div class="flex items-center justify-between">
            <h3 class="text-lg font-medium text-gray-900">메뉴 구조 및 권한</h3>
            <button type="button" class="btn btn-primary">
              <Plus size="16" class="mr-2" />
              메뉴 추가
            </button>
          </div>

          {#if loading}
            <div class="text-center py-12">
              <div class="animate-spin rounded-full h-8 w-8 border-b-2 border-primary-600 mx-auto"></div>
              <p class="mt-4 text-gray-600">로딩 중...</p>
            </div>
          {:else if flatMenus.length === 0}
            <div class="text-center py-12">
              <Menu class="mx-auto h-12 w-12 text-gray-400" />
              <p class="mt-4 text-gray-500">메뉴가 없습니다.</p>
            </div>
          {:else}
            <div class="overflow-x-auto">
              <table class="min-w-full divide-y divide-gray-200">
                <thead class="bg-gray-50">
                  <tr>
                    <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                      메뉴
                    </th>
                    <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                      경로
                    </th>
                    <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                      타입
                    </th>
                    <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                      상태
                    </th>
                    <th class="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase tracking-wider">
                      작업
                    </th>
                  </tr>
                </thead>
                <tbody class="bg-white divide-y divide-gray-200">
                  {#each flatMenus as menu}
                    <tr class="hover:bg-gray-50">
                      <td class="px-6 py-4 whitespace-nowrap">
                        <div class="flex items-center" style="margin-left: {menu.level * 20}px">
                          <div class="flex-shrink-0">
                            {#if menu.iconName}
                              <div class="w-8 h-8 bg-gray-100 rounded flex items-center justify-center">
                                <span class="text-xs">{menu.iconName}</span>
                              </div>
                            {/if}
                          </div>
                          <div class="ml-4">
                            <div class="text-sm font-medium text-gray-900">{menu.menuName}</div>
                            <div class="text-sm text-gray-500">{menu.menuCode}</div>
                          </div>
                        </div>
                      </td>
                      <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                        {menu.menuPath}
                      </td>
                      <td class="px-6 py-4 whitespace-nowrap">
                        <span class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium {getMenuTypeColor(menu.menuType)}">
                          {menu.menuType}
                        </span>
                      </td>
                      <td class="px-6 py-4 whitespace-nowrap">
                        <span class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium {menu.isActive ? 'bg-green-100 text-green-800' : 'bg-red-100 text-red-800'}">
                          {menu.isActive ? '활성' : '비활성'}
                        </span>
                      </td>
                      <td class="px-6 py-4 whitespace-nowrap text-right text-sm font-medium">
                        <div class="flex justify-end space-x-2">
                          <button
                            type="button"
                            class="text-indigo-600 hover:text-indigo-900"
                            title="편집"
                          >
                            <Edit size="16" />
                          </button>
                          <button
                            type="button"
                            class="text-red-600 hover:text-red-900"
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

      {:else if activeTab === 'roles'}
        <!-- 역할별 권한 탭 -->
        <div class="space-y-6">
          <h3 class="text-lg font-medium text-gray-900">역할별 권한 설정</h3>

          <div class="grid grid-cols-1 lg:grid-cols-2 xl:grid-cols-3 gap-6">
            {#each roles as role}
              {@const rolePermission = rolePermissions.find(rp => rp.roleName === role.value)}
              <div class="card p-6">
                <div class="flex items-center justify-between mb-4">
                  <div>
                    <h4 class="text-lg font-medium text-gray-900">{role.label}</h4>
                    <p class="text-sm text-gray-500">{role.description}</p>
                  </div>
                  <button
                    type="button"
                    class="text-indigo-600 hover:text-indigo-900"
                    title="권한 편집"
                  >
                    <Settings size="16" />
                  </button>
                </div>

                <div class="space-y-3">
                  {#if rolePermission && rolePermission.permissions.length > 0}
                    {#each rolePermission.permissions as permission}
                      <div class="flex items-center justify-between p-2 bg-gray-50 rounded">
                        <div>
                          <span class="text-sm font-medium text-gray-900">{permission.menuName}</span>
                        </div>
                        <span class="inline-flex items-center px-2 py-1 rounded-full text-xs font-medium {getPermissionColor(permission.permissionType)}">
                          {permissionTypes.find(p => p.value === permission.permissionType)?.label || permission.permissionType}
                        </span>
                      </div>
                    {/each}
                  {:else}
                    <p class="text-sm text-gray-500 text-center py-4">설정된 권한이 없습니다.</p>
                  {/if}
                </div>
              </div>
            {/each}
          </div>
        </div>

      {:else if activeTab === 'users'}
        <!-- 사용자별 권한 탭 -->
        <div class="space-y-6">
          <h3 class="text-lg font-medium text-gray-900">사용자별 개별 권한</h3>
          
          <div class="text-center py-12">
            <Users class="mx-auto h-12 w-12 text-gray-400" />
            <p class="mt-4 text-gray-500">사용자별 권한 관리 기능은 준비 중입니다.</p>
          </div>
        </div>
      {/if}
    </div>
  </div>
</div>
