<script>
  import { onMount } from 'svelte';
  import { toastStore } from '$lib/stores/toast.js';
  import { 
    Users, 
    Building, 
    Monitor, 
    Plus, 
    Search, 
    AlertTriangle,
    Database
  } from 'lucide-svelte';
  
  import CreateUserModal from '$lib/components/SuperAdmin/CreateUserModal.svelte';
  import CreateStoreModal from '$lib/components/SuperAdmin/CreateStoreModal.svelte';
  import CreatePosModal from '$lib/components/SuperAdmin/CreatePosModal.svelte';
  import Custom404 from '$lib/components/Common/Custom404.svelte';
  
  import { userApi, storeApi, posApi } from '$lib/api/superAdmin.js';

  let activeTab = 'users';
  let loading = false;

  // 모달 상태
  let showCreateUserModal = false;
  let showCreateStoreModal = false;
  let showCreatePosModal = false;

  // 데이터 상태
  let users = [];
  let stores = [];
  let posSystems = [];

  // 검색 및 필터
  let userSearch = '';
  let userStatusFilter = 'all';
  let storeSearch = '';
  let storeTypeFilter = 'all';
  let posSearch = '';
  let posStatusFilter = 'all';

  // 에러 상태
  let apiErrors = {
    users: false,
    stores: false,
    pos: false
  };

  const tabs = [
    { id: 'users', label: '사용자 관리', icon: Users },
    { id: 'stores', label: '매장 관리', icon: Building },
    { id: 'pos', label: 'POS 관리', icon: Monitor }
  ];

  onMount(() => {
    loadAllData();
  });

  async function loadAllData() {
    await Promise.all([
      loadUsers(),
      loadStores(),
      loadPosSystems()
    ]);
  }

  async function loadUsers() {
    loading = true;
    try {
      const response = await userApi.getUsers();
      
      if (response.success && response.data) {
        users = response.data.users || response.data;
        apiErrors.users = false;
      } else if (response.error === 'API_NOT_FOUND') {
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
          }
        ];
        apiErrors.users = true;
      } else {
        apiErrors.users = true;
        users = [];
      }
    } catch (error) {
      console.error('사용자 로드 오류:', error);
      apiErrors.users = true;
      users = [];
    } finally {
      loading = false;
    }
  }

  async function loadStores() {
    try {
      const response = await storeApi.getStores();
      
      if (response.success && response.data) {
        stores = response.data.stores || response.data;
        apiErrors.stores = false;
      } else if (response.error === 'API_NOT_FOUND') {
        stores = [
          {
            storeId: 'HQ001001',
            storeName: '강남점',
            storeType: 'CHAIN',
            hqName: '커피왕 본사',
            regionName: '서울특별시',
            ownerName: '김사장',
            phoneNumber: '02-1234-5678',
            storeStatus: 'ACTIVE',
            posCount: 2,
            employeeCount: 5,
            createdAt: new Date().toISOString()
          }
        ];
        apiErrors.stores = true;
      } else {
        apiErrors.stores = true;
        stores = [];
      }
    } catch (error) {
      console.error('매장 로드 오류:', error);
      apiErrors.stores = true;
      stores = [];
    }
  }

  async function loadPosSystems() {
    try {
      const response = await posApi.getPosSystems();
      
      if (response.success && response.data) {
        posSystems = response.data.posSystems || response.data;
        apiErrors.pos = false;
      } else if (response.error === 'API_NOT_FOUND') {
        posSystems = [
          {
            posId: 'HQ001001P01',
            storeId: 'HQ001001',
            storeName: '강남점',
            posNumber: 1,
            posName: '메인 POS',
            posType: 'MAIN',
            posStatus: 'ACTIVE',
            ipAddress: '192.168.1.101',
            createdAt: new Date().toISOString()
          }
        ];
        apiErrors.pos = true;
      } else {
        apiErrors.pos = true;
        posSystems = [];
      }
    } catch (error) {
      console.error('POS 로드 오류:', error);
      apiErrors.pos = true;
      posSystems = [];
    }
  }

  // 필터링된 데이터
  $: filteredUsers = users.filter(user => {
    const matchesSearch = user.username.toLowerCase().includes(userSearch.toLowerCase()) ||
                         user.email.toLowerCase().includes(userSearch.toLowerCase());
    const matchesStatus = userStatusFilter === 'all' || user.userStatus === userStatusFilter;
    return matchesSearch && matchesStatus;
  });

  $: filteredStores = stores.filter(store => {
    const matchesSearch = store.storeName.toLowerCase().includes(storeSearch.toLowerCase()) ||
                         store.storeId.toLowerCase().includes(storeSearch.toLowerCase());
    const matchesType = storeTypeFilter === 'all' || store.storeType === storeTypeFilter;
    return matchesSearch && matchesType;
  });

  $: filteredPosSystems = posSystems.filter(pos => {
    const matchesSearch = pos.posName?.toLowerCase().includes(posSearch.toLowerCase()) ||
                         pos.storeName.toLowerCase().includes(posSearch.toLowerCase()) ||
                         pos.posId.toLowerCase().includes(posSearch.toLowerCase());
    const matchesStatus = posStatusFilter === 'all' || pos.posStatus === posStatusFilter;
    return matchesSearch && matchesStatus;
  });

  // 이벤트 핸들러
  function handleUserCreated(event) {
    users = [event.detail, ...users];
  }

  function handleStoreCreated(event) {
    stores = [event.detail, ...stores];
  }

  function handlePosCreated(event) {
    posSystems = [event.detail, ...posSystems];
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
      'LOCKED': 'bg-orange-100 text-orange-800',
      'MAINTENANCE': 'bg-yellow-100 text-yellow-800'
    };
    return colors[status] || 'bg-gray-100 text-gray-800';
  }

  function formatDate(dateString) {
    if (!dateString) return '없음';
    return new Date(dateString).toLocaleDateString('ko-KR');
  }
</script>

<svelte:head>
  <title>슈퍼 어드민 - WebPos</title>
</svelte:head>

<div class="space-y-6">
  <!-- 헤더 -->
  <div class="flex items-center justify-between">
    <div>
      <h1 class="text-3xl font-bold text-gray-900">슈퍼 어드민</h1>
      <p class="text-gray-600 mt-1">시스템의 모든 리소스를 관리합니다.</p>
    </div>
    <div class="flex items-center space-x-2">
      <div class="flex items-center px-3 py-1 bg-blue-100 text-blue-800 rounded-full text-sm">
        <Database class="w-4 h-4 mr-1" />
        {apiErrors.users || apiErrors.stores || apiErrors.pos ? '데모 모드' : 'API 연결됨'}
      </div>
    </div>
  </div>

  <!-- 통계 대시보드 -->
  <div class="grid grid-cols-1 md:grid-cols-3 gap-6">
    <div class="card p-6">
      <div class="flex items-center">
        <div class="p-3 rounded-full bg-blue-100">
          <Users class="h-6 w-6 text-blue-600" />
        </div>
        <div class="ml-4">
          <p class="text-sm font-medium text-gray-600">총 사용자</p>
          <p class="text-2xl font-bold text-gray-900">{users.length}</p>
        </div>
      </div>
      {#if apiErrors.users}
        <div class="mt-2 flex items-center text-xs text-orange-600">
          <AlertTriangle class="w-3 h-3 mr-1" />
          데모 데이터
        </div>
      {/if}
    </div>

    <div class="card p-6">
      <div class="flex items-center">
        <div class="p-3 rounded-full bg-green-100">
          <Building class="h-6 w-6 text-green-600" />
        </div>
        <div class="ml-4">
          <p class="text-sm font-medium text-gray-600">총 매장</p>
          <p class="text-2xl font-bold text-gray-900">{stores.length}</p>
        </div>
      </div>
      {#if apiErrors.stores}
        <div class="mt-2 flex items-center text-xs text-orange-600">
          <AlertTriangle class="w-3 h-3 mr-1" />
          데모 데이터
        </div>
      {/if}
    </div>

    <div class="card p-6">
      <div class="flex items-center">
        <div class="p-3 rounded-full bg-purple-100">
          <Monitor class="h-6 w-6 text-purple-600" />
        </div>
        <div class="ml-4">
          <p class="text-sm font-medium text-gray-600">총 POS</p>
          <p class="text-2xl font-bold text-gray-900">{posSystems.length}</p>
        </div>
      </div>
      {#if apiErrors.pos}
        <div class="mt-2 flex items-center text-xs text-orange-600">
          <AlertTriangle class="w-3 h-3 mr-1" />
          데모 데이터
        </div>
      {/if}
    </div>
  </div>

  <!-- 탭 네비게이션과 컨텐츠를 하나의 카드에 통합 -->
  <div class="bg-white rounded-lg shadow">
    <!-- 탭 헤더 -->
    <div class="border-b border-gray-200">
      <nav class="-mb-px flex space-x-8 px-6" aria-label="Tabs">
        {#each tabs as tab}
          <button
            class="py-4 px-1 border-b-2 font-medium text-sm whitespace-nowrap {
              activeTab === tab.id
                ? 'border-primary-500 text-primary-600'
                : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300'
            }"
            on:click={() => activeTab = tab.id}
          >
            <svelte:component this={tab.icon} class="w-5 h-5 mr-2 inline" />
            {tab.label}
          </button>
        {/each}
      </nav>
    </div>

    <!-- 탭 컨텐츠 -->
    <div class="p-6">
      <!-- 사용자 관리 탭 -->
      {#if activeTab === 'users'}
        <div class="space-y-6">
          <div class="flex items-center justify-between">
            <h2 class="text-lg font-medium text-gray-900">사용자 관리</h2>
            <button
              type="button"
              class="btn btn-primary"
              on:click={() => showCreateUserModal = true}
            >
              <Plus class="w-4 h-4 mr-2" />
              사용자 추가
            </button>
          </div>

          {#if apiErrors.users}
            <Custom404
              title="사용자 API를 찾을 수 없습니다"
              message="사용자 관리 API가 구현되지 않았습니다. 현재 데모 데이터로 동작하고 있습니다."
              showHomeButton={false}
              onRetry={loadUsers}
            />
          {/if}

          <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div class="relative">
              <Search class="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400 w-4 h-4" />
              <input
                type="text"
                placeholder="사용자명 또는 이메일 검색..."
                bind:value={userSearch}
                class="input pl-10"
              />
            </div>
            <select bind:value={userStatusFilter} class="input">
              <option value="all">모든 상태</option>
              <option value="ACTIVE">활성</option>
              <option value="INACTIVE">비활성</option>
              <option value="SUSPENDED">정지</option>
              <option value="PENDING_VERIFICATION">인증 대기</option>
              <option value="LOCKED">잠김</option>
            </select>
          </div>

          <div class="overflow-x-auto">
            <table class="min-w-full divide-y divide-gray-200">
              <thead class="bg-gray-50">
                <tr>
                  <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">사용자</th>
                  <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">역할</th>
                  <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">상태</th>
                  <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">생성일</th>
                </tr>
              </thead>
              <tbody class="bg-white divide-y divide-gray-200">
                {#each filteredUsers as user}
                  <tr class="hover:bg-gray-50">
                    <td class="px-6 py-4 whitespace-nowrap">
                      <div class="flex items-center">
                        <div class="w-8 h-8 bg-primary-600 rounded-full flex items-center justify-center">
                          <span class="text-white font-medium text-sm">
                            {user.username.charAt(0).toUpperCase()}
                          </span>
                        </div>
                        <div class="ml-3">
                          <div class="text-sm font-medium text-gray-900">{user.username}</div>
                          <div class="text-sm text-gray-500">{user.email}</div>
                        </div>
                      </div>
                    </td>
                    <td class="px-6 py-4 whitespace-nowrap">
                      <div class="flex flex-wrap gap-1">
                        {#each user.roles as role}
                          <span class="inline-flex items-center px-2 py-0.5 rounded text-xs font-medium {getRoleColor(role)}">
                            {role}
                          </span>
                        {/each}
                      </div>
                    </td>
                    <td class="px-6 py-4 whitespace-nowrap">
                      <span class="inline-flex items-center px-2 py-0.5 rounded text-xs font-medium {getStatusColor(user.userStatus)}">
                        {user.userStatus}
                      </span>
                    </td>
                    <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                      {formatDate(user.createdAt)}
                    </td>
                  </tr>
                {/each}
              </tbody>
            </table>
          </div>
        </div>

      <!-- 매장 관리 탭 -->
      {:else if activeTab === 'stores'}
        <div class="space-y-6">
          <div class="flex items-center justify-between">
            <h2 class="text-lg font-medium text-gray-900">매장 관리</h2>
            <button
              type="button"
              class="btn btn-primary"
              on:click={() => showCreateStoreModal = true}
            >
              <Plus class="w-4 h-4 mr-2" />
              매장 추가
            </button>
          </div>

          {#if apiErrors.stores}
            <Custom404
              title="매장 API를 찾을 수 없습니다"
              message="매장 관리 API가 구현되지 않았습니다. 현재 데모 데이터로 동작하고 있습니다."
              showHomeButton={false}
              onRetry={loadStores}
            />
          {/if}

          <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div class="relative">
              <Search class="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400 w-4 h-4" />
              <input
                type="text"
                placeholder="매장명 또는 매장ID 검색..."
                bind:value={storeSearch}
                class="input pl-10"
              />
            </div>
            <select bind:value={storeTypeFilter} class="input">
              <option value="all">모든 유형</option>
              <option value="CHAIN">체인매장</option>
              <option value="INDIVIDUAL">개인매장</option>
            </select>
          </div>

          <div class="overflow-x-auto">
            <table class="min-w-full divide-y divide-gray-200">
              <thead class="bg-gray-50">
                <tr>
                  <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">매장</th>
                  <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">유형</th>
                  <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">사업자</th>
                  <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">POS 수</th>
                  <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">상태</th>
                </tr>
              </thead>
              <tbody class="bg-white divide-y divide-gray-200">
                {#each filteredStores as store}
                  <tr class="hover:bg-gray-50">
                    <td class="px-6 py-4 whitespace-nowrap">
                      <div>
                        <div class="text-sm font-medium text-gray-900">{store.storeName}</div>
                        <div class="text-sm text-gray-500">{store.storeId}</div>
                        {#if store.hqName}
                          <div class="text-xs text-blue-600">{store.hqName}</div>
                        {/if}
                      </div>
                    </td>
                    <td class="px-6 py-4 whitespace-nowrap">
                      <span class="inline-flex items-center px-2 py-0.5 rounded text-xs font-medium {
                        store.storeType === 'CHAIN' ? 'bg-blue-100 text-blue-800' : 'bg-green-100 text-green-800'
                      }">
                        {store.storeType === 'CHAIN' ? '체인' : '개인'}
                      </span>
                    </td>
                    <td class="px-6 py-4 whitespace-nowrap">
                      <div class="text-sm text-gray-900">{store.ownerName}</div>
                      {#if store.phoneNumber}
                        <div class="text-sm text-gray-500">{store.phoneNumber}</div>
                      {/if}
                    </td>
                    <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                      {store.posCount || 0}대
                    </td>
                    <td class="px-6 py-4 whitespace-nowrap">
                      <span class="inline-flex items-center px-2 py-0.5 rounded text-xs font-medium {getStatusColor(store.storeStatus)}">
                        {store.storeStatus}
                      </span>
                    </td>
                  </tr>
                {/each}
              </tbody>
            </table>
          </div>
        </div>

      <!-- POS 관리 탭 -->
      {:else if activeTab === 'pos'}
        <div class="space-y-6">
          <div class="flex items-center justify-between">
            <h2 class="text-lg font-medium text-gray-900">POS 관리</h2>
            <button
              type="button"
              class="btn btn-primary"
              on:click={() => showCreatePosModal = true}
            >
              <Plus class="w-4 h-4 mr-2" />
              POS 추가
            </button>
          </div>

          {#if apiErrors.pos}
            <Custom404
              title="POS API를 찾을 수 없습니다"
              message="POS 관리 API가 구현되지 않았습니다. 현재 데모 데이터로 동작하고 있습니다."
              showHomeButton={false}
              onRetry={loadPosSystems}
            />
          {/if}

          <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div class="relative">
              <Search class="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400 w-4 h-4" />
              <input
                type="text"
                placeholder="POS명, 매장명 또는 POS ID 검색..."
                bind:value={posSearch}
                class="input pl-10"
              />
            </div>
            <select bind:value={posStatusFilter} class="input">
              <option value="all">모든 상태</option>
              <option value="ACTIVE">활성</option>
              <option value="INACTIVE">비활성</option>
              <option value="MAINTENANCE">점검중</option>
            </select>
          </div>

          <div class="overflow-x-auto">
            <table class="min-w-full divide-y divide-gray-200">
              <thead class="bg-gray-50">
                <tr>
                  <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">POS</th>
                  <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">매장</th>
                  <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">유형</th>
                  <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">IP 주소</th>
                  <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">상태</th>
                </tr>
              </thead>
              <tbody class="bg-white divide-y divide-gray-200">
                {#each filteredPosSystems as pos}
                  <tr class="hover:bg-gray-50">
                    <td class="px-6 py-4 whitespace-nowrap">
                      <div>
                        <div class="text-sm font-medium text-gray-900">{pos.posName || `POS ${pos.posNumber}`}</div>
                        <div class="text-sm text-gray-500">{pos.posId}</div>
                      </div>
                    </td>
                    <td class="px-6 py-4 whitespace-nowrap">
                      <div class="text-sm text-gray-900">{pos.storeName}</div>
                      <div class="text-sm text-gray-500">{pos.storeId}</div>
                    </td>
                    <td class="px-6 py-4 whitespace-nowrap">
                      <span class="inline-flex items-center px-2 py-0.5 rounded text-xs font-medium {
                        pos.posType === 'MAIN' ? 'bg-blue-100 text-blue-800' : 
                        pos.posType === 'SUB' ? 'bg-green-100 text-green-800' : 
                        'bg-purple-100 text-purple-800'
                      }">
                        {pos.posType}
                      </span>
                    </td>
                    <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                      {pos.ipAddress || '미설정'}
                    </td>
                    <td class="px-6 py-4 whitespace-nowrap">
                      <span class="inline-flex items-center px-2 py-0.5 rounded text-xs font-medium {getStatusColor(pos.posStatus)}">
                        {pos.posStatus}
                      </span>
                    </td>
                  </tr>
                {/each}
              </tbody>
            </table>
          </div>
        </div>
      {/if}
    </div>
  </div>
</div>

<!-- 모달들 -->
<CreateUserModal
  bind:open={showCreateUserModal}
  on:user-created={handleUserCreated}
  on:close={() => showCreateUserModal = false}
/>

<CreateStoreModal
  bind:open={showCreateStoreModal}
  on:store-created={handleStoreCreated}
  on:close={() => showCreateStoreModal = false}
/>

<CreatePosModal
  bind:open={showCreatePosModal}
  on:pos-created={handlePosCreated}
  on:close={() => showCreatePosModal = false}
/>
