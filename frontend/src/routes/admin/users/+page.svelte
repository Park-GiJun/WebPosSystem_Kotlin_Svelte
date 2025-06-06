<script>
  import { onMount } from 'svelte';
  import { authStore } from '$lib/stores/auth.js';
  import { tabStore } from '$lib/stores/tabs.js';
  import { toastStore } from '$lib/stores/toast.js';
  import { Plus, Search, Filter, Edit, Trash2, Shield, Unlock, UserCheck, AlertTriangle } from 'lucide-svelte';
  import CreateUserModal from '$lib/components/SuperAdmin/CreateUserModal.svelte';
  import Custom404 from '$lib/components/Common/Custom404.svelte';
  import { userApi } from '$lib/api/superAdmin.js';

  let users = [];
  let loading = true;
  let searchTerm = '';
  let filterStatus = 'all';
  let filterRole = 'all';
  let showCreateModal = false;
  let apiError = false;

  // 탭 활성화
  onMount(() => {
    tabStore.setActiveTab('ADMIN_USERS');
    loadUsers();
  });

  async function loadUsers() {
    try {
      const response = await userApi.getUsers();
      
      if (response.success && response.data) {
        users = response.data.users || response.data;
        apiError = false;
      } else if (response.error === 'API_NOT_FOUND') {
        // 더미 데이터
        users = [
          {
            id: '1',
            username: 'admin',
            email: 'admin@webpos.com',
            roles: ['SUPER_ADMIN'],
            userStatus: 'ACTIVE',
            isEmailVerified: true,
            lastLoginAt: new Date().toISOString(),
            failedLoginAttempts: 0,
            isLocked: false,
            lockedUntil: null,
            createdAt: new Date().toISOString(),
            updatedAt: new Date().toISOString()
          },
          {
            id: '2',
            username: 'manager1',
            email: 'manager1@webpos.com',
            roles: ['HQ_MANAGER'],
            userStatus: 'ACTIVE',
            isEmailVerified: true,
            lastLoginAt: new Date(Date.now() - 2 * 60 * 60 * 1000).toISOString(),
            failedLoginAttempts: 0,
            isLocked: false,
            lockedUntil: null,
            createdAt: new Date(Date.now() - 30 * 24 * 60 * 60 * 1000).toISOString(),
            updatedAt: new Date(Date.now() - 2 * 60 * 60 * 1000).toISOString()
          }
        ];
        apiError = true;
      } else {
        apiError = true;
        users = [];
        toastStore.error(response.message || '사용자 목록을 불러오는데 실패했습니다.');
      }
    } catch (error) {
      console.error('Failed to load users:', error);
      apiError = true;
      users = [];
      toastStore.error('사용자 목록을 불러오는데 실패했습니다.');
    } finally {
      loading = false;
    }
  }

  // 필터링된 사용자 목록
  $: filteredUsers = users.filter(user => {
    const matchesSearch = user.username.toLowerCase().includes(searchTerm.toLowerCase()) ||
                         user.email.toLowerCase().includes(searchTerm.toLowerCase());
    const matchesStatus = filterStatus === 'all' || user.userStatus === filterStatus;
    const matchesRole = filterRole === 'all' || user.roles.includes(filterRole);
    
    return matchesSearch && matchesStatus && matchesRole;
  });

  function getRoleColor(role) {
    const colors = {
      'SUPER_ADMIN': 'bg-red-100 text-red-800',
      'SYSTEM_ADMIN': 'bg-orange-100 text-orange-800',
      'HQ_MANAGER': 'bg-blue-100 text-blue-800',
      'STORE_MANAGER': 'bg-green-100 text-green-800',
      'AREA_MANAGER': 'bg-purple-100 text-purple-800',
      'USER': 'bg-gray-100 text-gray-800'
    };
    return colors[role] || 'bg-gray-100 text-gray-800';
  }

  function getStatusColor(status) {
    const colors = {
      'ACTIVE': 'bg-green-100 text-green-800',
      'INACTIVE': 'bg-gray-100 text-gray-800',
      'SUSPENDED': 'bg-red-100 text-red-800',
      'PENDING_VERIFICATION': 'bg-yellow-100 text-yellow-800',
      'LOCKED': 'bg-orange-100 text-orange-800'
    };
    return colors[status] || 'bg-gray-100 text-gray-800';
  }

  function getStatusText(status) {
    const texts = {
      'ACTIVE': '활성',
      'INACTIVE': '비활성',
      'SUSPENDED': '정지',
      'PENDING_VERIFICATION': '인증대기',
      'LOCKED': '잠김'
    };
    return texts[status] || status;
  }

  function getRoleText(role) {
    const texts = {
      'SUPER_ADMIN': '최고관리자',
      'SYSTEM_ADMIN': '시스템관리자',
      'HQ_MANAGER': '본사관리자',
      'STORE_MANAGER': '매장관리자',
      'AREA_MANAGER': '지역관리자',
      'USER': '일반사용자'
    };
    return texts[role] || role;
  }

  async function editUser(user) {
    // TODO: 사용자 편집 모달 구현
    console.log('Edit user:', user);
    toastStore.info('사용자 편집 기능은 준비 중입니다.');
  }

  async function deleteUser(user) {
    if (!confirm(`정말로 "${user.username}" 사용자를 삭제하시겠습니까?`)) {
      return;
    }

    try {
      const response = await userApi.deleteUser(user.id);

      if (response.success) {
        users = users.filter(u => u.id !== user.id);
        toastStore.success('사용자가 삭제되었습니다.');
      } else if (response.error === 'API_NOT_FOUND') {
        // API가 없어도 로컬에서 삭제
        users = users.filter(u => u.id !== user.id);
        toastStore.success('사용자가 삭제되었습니다. (데모 모드)');
      } else {
        toastStore.error(response.message || '사용자 삭제에 실패했습니다.');
      }
    } catch (error) {
      console.error('Delete user error:', error);
      toastStore.error('사용자 삭제에 실패했습니다.');
    }
  }

  async function unlockUser(user) {
    try {
      const response = await userApi.unlockUser(user.id);

      if (response.success) {
        await loadUsers();
        toastStore.success('사용자 잠금이 해제되었습니다.');
      } else if (response.error === 'API_NOT_FOUND') {
        // API가 없어도 로컬에서 처리
        const userIndex = users.findIndex(u => u.id === user.id);
        if (userIndex !== -1) {
          users[userIndex] = { ...users[userIndex], isLocked: false, lockedUntil: null };
          users = [...users];
        }
        toastStore.success('사용자 잠금이 해제되었습니다. (데모 모드)');
      } else {
        toastStore.error(response.message || '사용자 잠금 해제에 실패했습니다.');
      }
    } catch (error) {
      console.error('Unlock user error:', error);
      toastStore.error('사용자 잠금 해제에 실패했습니다.');
    }
  }

  function handleUserCreated(event) {
    const newUser = event.detail;
    users = [newUser, ...users];
    toastStore.success('새 사용자가 생성되었습니다.');
  }

  function openCreateModal() {
    showCreateModal = true;
  }
</script>

<svelte:head>
  <title>사용자 관리 - WebPos</title>
</svelte:head>

<div class="space-y-6">
  <!-- 헤더 -->
  <div class="flex items-center justify-between">
    <div>
      <h1 class="text-2xl font-bold text-gray-900">사용자 관리</h1>
      <p class="text-gray-600 mt-1">시스템 사용자를 관리합니다.</p>
    </div>
    <div class="flex items-center space-x-3">
      {#if apiError}
        <div class="flex items-center px-3 py-1 bg-orange-100 text-orange-800 rounded-full text-sm">
          <AlertTriangle class="w-4 h-4 mr-1" />
          데모 모드
        </div>
      {/if}
      <button 
        type="button" 
        class="btn btn-primary"
        on:click={openCreateModal}
      >
        <Plus size="16" class="mr-2" />
        사용자 추가
      </button>
    </div>
  </div>

  {#if apiError}
    <Custom404
      title="사용자 API를 찾을 수 없습니다"
      message="사용자 관리 API가 구현되지 않았습니다. 현재 데모 데이터로 동작하고 있습니다."
      showHomeButton={false}
      onRetry={loadUsers}
    />
  {/if}

  <!-- 통계 카드 -->
  <div class="grid grid-cols-1 md:grid-cols-4 gap-6">
    <div class="card p-6">
      <div class="flex items-center">
        <div class="p-3 rounded-full bg-blue-100">
          <Shield class="h-6 w-6 text-blue-600" />
        </div>
        <div class="ml-4">
          <p class="text-sm font-medium text-gray-600">총 사용자</p>
          <p class="text-2xl font-bold text-gray-900">{users.length}</p>
        </div>
      </div>
    </div>

    <div class="card p-6">
      <div class="flex items-center">
        <div class="p-3 rounded-full bg-green-100">
          <UserCheck class="h-6 w-6 text-green-600" />
        </div>
        <div class="ml-4">
          <p class="text-sm font-medium text-gray-600">활성 사용자</p>
          <p class="text-2xl font-bold text-gray-900">
            {users.filter(u => u.userStatus === 'ACTIVE').length}
          </p>
        </div>
      </div>
    </div>

    <div class="card p-6">
      <div class="flex items-center">
        <div class="p-3 rounded-full bg-orange-100">
          <Unlock class="h-6 w-6 text-orange-600" />
        </div>
        <div class="ml-4">
          <p class="text-sm font-medium text-gray-600">관리자</p>
          <p class="text-2xl font-bold text-gray-900">
            {users.filter(u => u.roles.some(r => r.includes('ADMIN'))).length}
          </p>
        </div>
      </div>
    </div>

    <div class="card p-6">
      <div class="flex items-center">
        <div class="p-3 rounded-full bg-yellow-100">
          <Filter class="h-6 w-6 text-yellow-600" />
        </div>
        <div class="ml-4">
          <p class="text-sm font-medium text-gray-600">인증 대기</p>
          <p class="text-2xl font-bold text-gray-900">
            {users.filter(u => u.userStatus === 'PENDING_VERIFICATION').length}
          </p>
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
          placeholder="사용자명 또는 이메일 검색..."
          bind:value={searchTerm}
          class="input pl-10"
        />
      </div>

      <!-- 상태 필터 -->
      <select bind:value={filterStatus} class="input">
        <option value="all">모든 상태</option>
        <option value="ACTIVE">활성</option>
        <option value="INACTIVE">비활성</option>
        <option value="SUSPENDED">정지</option>
        <option value="PENDING_VERIFICATION">인증 대기</option>
        <option value="LOCKED">잠김</option>
      </select>

      <!-- 역할 필터 -->
      <select bind:value={filterRole} class="input">
        <option value="all">모든 역할</option>
        <option value="SUPER_ADMIN">최고관리자</option>
        <option value="SYSTEM_ADMIN">시스템관리자</option>
        <option value="HQ_MANAGER">본사관리자</option>
        <option value="STORE_MANAGER">매장관리자</option>
        <option value="AREA_MANAGER">지역관리자</option>
        <option value="USER">일반사용자</option>
      </select>
    </div>
  </div>

  <!-- 사용자 목록 -->
  <div class="card overflow-hidden">
    {#if loading}
      <div class="p-12 text-center">
        <div class="animate-spin rounded-full h-8 w-8 border-b-2 border-primary-600 mx-auto"></div>
        <p class="mt-4 text-gray-600">로딩 중...</p>
      </div>
    {:else if filteredUsers.length === 0}
      <div class="p-12 text-center">
        <Shield class="mx-auto h-12 w-12 text-gray-400" />
        <p class="mt-4 text-gray-500">조건에 맞는 사용자가 없습니다.</p>
      </div>
    {:else}
      <div class="overflow-x-auto">
        <table class="min-w-full divide-y divide-gray-200">
          <thead class="bg-gray-50">
            <tr>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                사용자
              </th>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                역할
              </th>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                상태
              </th>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                최근 로그인
              </th>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                생성일
              </th>
              <th class="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase tracking-wider">
                작업
              </th>
            </tr>
          </thead>
          <tbody class="bg-white divide-y divide-gray-200">
            {#each filteredUsers as user}
              <tr class="hover:bg-gray-50">
                <td class="px-6 py-4 whitespace-nowrap">
                  <div class="flex items-center">
                    <div class="w-10 h-10 bg-primary-600 rounded-full flex items-center justify-center">
                      <span class="text-white font-medium text-sm">
                        {user.username.charAt(0).toUpperCase()}
                      </span>
                    </div>
                    <div class="ml-4">
                      <div class="text-sm font-medium text-gray-900">{user.username}</div>
                      <div class="text-sm text-gray-500">{user.email}</div>
                      {#if user.isEmailVerified}
                        <span class="inline-flex items-center px-2 py-0.5 rounded text-xs font-medium bg-green-100 text-green-800 mt-1">
                          인증완료
                        </span>
                      {:else}
                        <span class="inline-flex items-center px-2 py-0.5 rounded text-xs font-medium bg-yellow-100 text-yellow-800 mt-1">
                          인증대기
                        </span>
                      {/if}
                    </div>
                  </div>
                </td>
                <td class="px-6 py-4 whitespace-nowrap">
                  <div class="flex flex-wrap gap-1">
                    {#each user.roles as role}
                      <span class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium {getRoleColor(role)}">
                        {getRoleText(role)}
                      </span>
                    {/each}
                  </div>
                </td>
                <td class="px-6 py-4 whitespace-nowrap">
                  <span class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium {getStatusColor(user.userStatus)}">
                    {getStatusText(user.userStatus)}
                  </span>
                  {#if user.isLocked}
                    <div class="text-xs text-red-600 mt-1">
                      {user.lockedUntil ? `잠김 (${new Date(user.lockedUntil).toLocaleString('ko-KR')})` : '잠김'}
                    </div>
                  {/if}
                  {#if user.failedLoginAttempts > 0}
                    <div class="text-xs text-orange-600 mt-1">
                      로그인 실패: {user.failedLoginAttempts}회
                    </div>
                  {/if}
                </td>
                <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                  {user.lastLoginAt ? new Date(user.lastLoginAt).toLocaleDateString('ko-KR') : '없음'}
                </td>
                <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                  {new Date(user.createdAt).toLocaleDateString('ko-KR')}
                </td>
                <td class="px-6 py-4 whitespace-nowrap text-right text-sm font-medium">
                  <div class="flex justify-end space-x-2">
                    {#if user.isLocked}
                      <button
                        type="button"
                        class="text-green-600 hover:text-green-900"
                        on:click={() => unlockUser(user)}
                        title="잠금 해제"
                      >
                        <Unlock size="16" />
                      </button>
                    {/if}
                    <button
                      type="button"
                      class="text-indigo-600 hover:text-indigo-900"
                      on:click={() => editUser(user)}
                      title="편집"
                    >
                      <Edit size="16" />
                    </button>
                    <button
                      type="button"
                      class="text-red-600 hover:text-red-900"
                      on:click={() => deleteUser(user)}
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

<!-- 사용자 생성 모달 -->
<CreateUserModal
  bind:open={showCreateModal}
  on:user-created={handleUserCreated}
  on:close={() => showCreateModal = false}
/>
