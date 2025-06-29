<script>
  import { onMount } from 'svelte';
  import { authStore } from '$lib/stores/auth.js';
  import { tabStore } from '$lib/stores/tabs.js';
  import { toastStore } from '$lib/stores/toast.js';
  import { userApi } from '$lib/api/admin.js';
  import { Plus, Search, Filter, Edit, Trash2, Shield, Unlock, UserCheck, AlertTriangle } from 'lucide-svelte';
  import CreateUserModal from '$lib/components/SuperAdmin/CreateUserModal.svelte';
  import EditUserModal from '$lib/components/SuperAdmin/EditUserModal.svelte';

  let users = [];
  let loading = true;
  let searchTerm = '';
  let filterStatus = 'all';
  let filterRole = 'all';
  let showCreateModal = false;
  let showEditModal = false;
  let selectedUser = null;
  let currentPage = 0;
  let pageSize = 20;
  let totalCount = 0;

  // 초기화 완료 플래그
  let initialized = false;

  // 인증 상태 구독
  let authToken = '';
  authStore.subscribe(state => {
    authToken = state.token || '';
  });

  // 탭 활성화
  onMount(() => {
    tabStore.setActiveTab('ADMIN_USERS');
    loadUsers().then(() => {
      initialized = true;
    });
  });

  async function loadUsers() {
    if (!authToken) {
      console.warn('인증 토큰이 없습니다.');
      return;
    }

    try {
      loading = true;
      
      const params = {
        page: currentPage,
        size: pageSize
      };

      if (searchTerm.trim()) {
        params.search = searchTerm.trim();
      }

      if (filterStatus !== 'all') {
        params.status = filterStatus;
      }

      if (filterRole !== 'all') {
        params.role = filterRole;
      }

      console.log('🔍 사용자 목록 조회 중...', params);
      console.log('🔐 사용중인 토큰:', authToken ? authToken.substring(0, 20) + '...' : 'null');
      
      const response = await userApi.getUsers(params, authToken);
      
      if (response && response.users) {
        users = response.users;
        totalCount = response.totalCount || users.length;
        console.log('✅ 사용자 목록 로드 완료:', users.length, '개');
      } else {
        console.warn('⚠️ 응답에 users 필드가 없습니다:', response);
        users = [];
        totalCount = 0;
      }
    } catch (error) {
      console.error('❌ 사용자 목록 로드 실패:', error);
      console.error('❌ 에러 상세:', {
        message: error.message,
        stack: error.stack,
        name: error.name
      });
      toastStore.error('사용자 목록을 불러오는데 실패했습니다: ' + error.message);
      users = [];
      totalCount = 0;
    } finally {
      loading = false;
    }
  }

  // 검색 및 필터 변경 핸들러
  let searchTimeout;
  
  function handleSearchChange() {
    if (initialized && !loading) {
      clearTimeout(searchTimeout);
      searchTimeout = setTimeout(() => {
        currentPage = 0;
        loadUsers();
      }, 300); // 300ms 디바운스
    }
  }
  
  function handleFilterChange() {
    if (initialized && !loading) {
      currentPage = 0;
      loadUsers();
    }
  }

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
    selectedUser = user;
    showEditModal = true;
    console.log('Edit user:', user);
  }

  async function deleteUser(user) {
    if (!confirm(`정말로 "${user.username}" 사용자를 삭제하시겠습니까?`)) {
      return;
    }

    if (!authToken) {
      toastStore.error('인증이 필요합니다.');
      return;
    }

    try {
      console.log('🗑️ 사용자 삭제 중:', user.username);
      await userApi.deleteUser(user.id, authToken);
      
      // 목록에서 제거
      users = users.filter(u => u.id !== user.id);
      totalCount = Math.max(0, totalCount - 1);
      
      toastStore.success('사용자가 삭제되었습니다.');
      console.log('✅ 사용자 삭제 완료');
    } catch (error) {
      console.error('❌ 사용자 삭제 실패:', error);
      toastStore.error('사용자 삭제에 실패했습니다: ' + error.message);
    }
  }

  async function unlockUser(user) {
    if (!authToken) {
      toastStore.error('인증이 필요합니다.');
      return;
    }

    try {
      console.log('🔓 사용자 잠금 해제 중:', user.username);
      await userApi.unlockUser(user.id, authToken);
      
      // 사용자 목록 다시 로드
      await loadUsers();
      toastStore.success('사용자 잠금이 해제되었습니다.');
      console.log('✅ 사용자 잠금 해제 완료');
    } catch (error) {
      console.error('❌ 사용자 잠금 해제 실패:', error);
      toastStore.error('사용자 잠금 해제에 실패했습니다: ' + error.message);
    }
  }

  async function handleUserCreated(event) {
    const newUser = event.detail;
    
    // 목록 맨 앞에 추가
    users = [newUser, ...users];
    totalCount += 1;
    
    toastStore.success('새 사용자가 생성되었습니다.');
    console.log('✅ 새 사용자 생성 완료:', newUser.username);
  }

  async function handleUserUpdated(event) {
    const updatedUser = event.detail;
    
    // 목록에서 해당 사용자 업데이트
    const userIndex = users.findIndex(u => u.id === updatedUser.id);
    if (userIndex !== -1) {
      users[userIndex] = updatedUser;
      users = [...users];
    }
    
    toastStore.success('사용자가 수정되었습니다.');
    console.log('✅ 사용자 수정 완료:', updatedUser.username);
  }

  function openCreateModal() {
    showCreateModal = true;
  }

  function nextPage() {
    if ((currentPage + 1) * pageSize < totalCount) {
      currentPage += 1;
      loadUsers();
    }
  }

  function prevPage() {
    if (currentPage > 0) {
      currentPage -= 1;
      loadUsers();
    }
  }

  // 통계 계산
  $: totalUsers = users.length;
  $: activeUsers = users.filter(u => u.userStatus === 'ACTIVE').length;
  $: adminUsers = users.filter(u => u.roles?.some(r => r.includes('ADMIN'))).length;
  $: pendingUsers = users.filter(u => u.userStatus === 'PENDING_VERIFICATION').length;
</script>

<svelte:head>
  <title>사용자 관리 - WebPos</title>
</svelte:head>

<div class="h-full flex flex-col space-y-6">
  <!-- 헤더 -->
  <div class="flex items-center justify-between">
    <div>
      <h1 class="text-2xl font-bold text-gray-900">사용자 관리</h1>
      <p class="text-gray-600 mt-1">시스템 사용자를 관리합니다.</p>
    </div>
    <div class="flex items-center space-x-3">
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

  <!-- 통계 카드 -->
  <div class="grid grid-cols-1 md:grid-cols-4 gap-6">
    <div class="card p-6">
      <div class="flex items-center">
        <div class="p-3 rounded-full bg-blue-100">
          <Shield class="h-6 w-6 text-blue-600" />
        </div>
        <div class="ml-4">
          <p class="text-sm font-medium text-gray-600">총 사용자</p>
          <p class="text-2xl font-bold text-gray-900">{totalCount}</p>
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
          <p class="text-2xl font-bold text-gray-900">{activeUsers}</p>
        </div>
      </div>
    </div>

    <div class="card p-6">
      <div class="flex items-center">
        <div class="p-3 rounded-full bg-orange-100">
          <Shield class="h-6 w-6 text-orange-600" />
        </div>
        <div class="ml-4">
          <p class="text-sm font-medium text-gray-600">관리자</p>
          <p class="text-2xl font-bold text-gray-900">{adminUsers}</p>
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
          <p class="text-2xl font-bold text-gray-900">{pendingUsers}</p>
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
          on:input={handleSearchChange}
          class="input pl-10"
        />
      </div>

      <!-- 상태 필터 -->
      <select bind:value={filterStatus} class="input" on:change={handleFilterChange}>
        <option value="all">모든 상태</option>
        <option value="ACTIVE">활성</option>
        <option value="INACTIVE">비활성</option>
        <option value="SUSPENDED">정지</option>
        <option value="PENDING_VERIFICATION">인증 대기</option>
        <option value="LOCKED">잠김</option>
      </select>

      <!-- 역할 필터 -->
      <select bind:value={filterRole} class="input" on:change={handleFilterChange}>
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
  <div class="card overflow-hidden flex-1 flex flex-col min-h-0">
    {#if loading}
      <div class="p-12 text-center">
        <div class="animate-spin rounded-full h-8 w-8 border-b-2 border-primary-600 mx-auto"></div>
        <p class="mt-4 text-gray-600">로딩 중...</p>
      </div>
    {:else if users.length === 0}
      <div class="p-12 text-center">
        <Shield class="mx-auto h-12 w-12 text-gray-400" />
        <p class="mt-4 text-gray-500">조건에 맞는 사용자가 없습니다.</p>
      </div>
    {:else}
      <div class="flex-1 overflow-y-auto">
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
            {#each users as user}
              <tr class="hover:bg-gray-50">
                <td class="px-6 py-4 whitespace-nowrap">
                  <div class="flex items-center">
                    <div class="w-10 h-10 bg-primary-600 rounded-full flex items-center justify-center">
                      <span class="text-white font-medium text-sm">
                        {user.username?.charAt(0)?.toUpperCase() || '?'}
                      </span>
                    </div>
                    <div class="ml-4">
                      <div class="text-sm font-medium text-gray-900">{user.username || 'N/A'}</div>
                      <div class="text-sm text-gray-500">{user.email || 'N/A'}</div>
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
                    {#each (user.roles || []) as role}
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
                  {user.createdAt ? new Date(user.createdAt).toLocaleDateString('ko-KR') : 'N/A'}
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

      <!-- 페이지네이션 -->
      <div class="bg-white px-4 py-3 flex items-center justify-between border-t border-gray-200 sm:px-6">
        <div class="flex-1 flex justify-between sm:hidden">
          <button
            on:click={prevPage}
            disabled={currentPage === 0}
            class="relative inline-flex items-center px-4 py-2 border border-gray-300 text-sm font-medium rounded-md text-gray-700 bg-white hover:bg-gray-50 disabled:opacity-50"
          >
            이전
          </button>
          <button
            on:click={nextPage}
            disabled={(currentPage + 1) * pageSize >= totalCount}
            class="ml-3 relative inline-flex items-center px-4 py-2 border border-gray-300 text-sm font-medium rounded-md text-gray-700 bg-white hover:bg-gray-50 disabled:opacity-50"
          >
            다음
          </button>
        </div>
        <div class="hidden sm:flex-1 sm:flex sm:items-center sm:justify-between">
          <div>
            <p class="text-sm text-gray-700">
              총 <span class="font-medium">{totalCount}</span>개 중 
              <span class="font-medium">{currentPage * pageSize + 1}</span>-<span class="font-medium">{Math.min((currentPage + 1) * pageSize, totalCount)}</span> 표시
            </p>
          </div>
          <div>
            <nav class="relative z-0 inline-flex rounded-md shadow-sm -space-x-px" aria-label="Pagination">
              <button
                on:click={prevPage}
                disabled={currentPage === 0}
                class="relative inline-flex items-center px-2 py-2 rounded-l-md border border-gray-300 bg-white text-sm font-medium text-gray-500 hover:bg-gray-50 disabled:opacity-50"
              >
                이전
              </button>
              <span class="relative inline-flex items-center px-4 py-2 border border-gray-300 bg-white text-sm font-medium text-gray-700">
                {currentPage + 1}
              </span>
              <button
                on:click={nextPage}
                disabled={(currentPage + 1) * pageSize >= totalCount}
                class="relative inline-flex items-center px-2 py-2 rounded-r-md border border-gray-300 bg-white text-sm font-medium text-gray-500 hover:bg-gray-50 disabled:opacity-50"
              >
                다음
              </button>
            </nav>
          </div>
        </div>
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

<!-- 사용자 편집 모달 -->
<EditUserModal
  bind:open={showEditModal}
  bind:user={selectedUser}
  on:user-updated={handleUserUpdated}
  on:close={() => {
    showEditModal = false;
    selectedUser = null;
  }}
/>
