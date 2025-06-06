<script>
  import { onMount } from 'svelte';
  import { authStore } from '$lib/stores/auth.js';
  import { tabStore } from '$lib/stores/tabs.js';
  import { toastStore } from '$lib/stores/toast.js';
  import { Plus, Search, Filter, Edit, Trash2, Building, MapPin, AlertTriangle } from 'lucide-svelte';
  import CreateStoreModal from '$lib/components/SuperAdmin/CreateStoreModal.svelte';
  import Custom404 from '$lib/components/Common/Custom404.svelte';
  import { storeApi } from '$lib/api/superAdmin.js';

  let stores = [];
  let loading = true;
  let searchTerm = '';
  let filterStatus = 'all';
  let filterType = 'all';
  let showCreateModal = false;
  let apiError = false;

  // 탭 활성화
  onMount(() => {
    tabStore.setActiveTab('ADMIN_STORES');
    loadStores();
  });

  async function loadStores() {
    try {
      const response = await storeApi.getStores();
      
      if (response.success && response.data) {
        stores = response.data.stores || response.data;
        apiError = false;
      } else if (response.error === 'API_NOT_FOUND') {
        // 더미 데이터
        stores = [
          {
            storeId: 'HQ001001',
            storeName: '강남점',
            storeType: 'CHAIN',
            operationType: 'DIRECT',
            hqId: 'HQHQ1',
            hqName: '커피왕 본사',
            regionCode: '001',
            regionName: '서울특별시',
            storeNumber: '001',
            businessLicense: '123-45-67890',
            ownerName: '김사장',
            phoneNumber: '02-1234-5678',
            address: '서울특별시 강남구 테헤란로 123',
            postalCode: '06142',
            openingDate: new Date(Date.now() - 2 * 365 * 24 * 60 * 60 * 1000).toISOString().split('T')[0],
            storeStatus: 'ACTIVE',
            managerUsername: 'manager1',
            posCount: 3,
            employeeCount: 5,
            isActive: true,
            createdAt: new Date(Date.now() - 2 * 365 * 24 * 60 * 60 * 1000).toISOString(),
            updatedAt: new Date().toISOString()
          },
          {
            storeId: 'IN002001',
            storeName: '개인카페 A',
            storeType: 'INDIVIDUAL',
            operationType: null,
            hqId: null,
            hqName: null,
            regionCode: '002',
            regionName: '부산광역시',
            storeNumber: '001',
            businessLicense: '234-56-78901',
            ownerName: '이사장',
            phoneNumber: '051-2345-6789',
            address: '부산광역시 해운대구 해운대로 456',
            postalCode: '48094',
            openingDate: new Date(Date.now() - 6 * 30 * 24 * 60 * 60 * 1000).toISOString().split('T')[0],
            storeStatus: 'ACTIVE',
            managerUsername: null,
            posCount: 1,
            employeeCount: 2,
            isActive: true,
            createdAt: new Date(Date.now() - 6 * 30 * 24 * 60 * 60 * 1000).toISOString(),
            updatedAt: new Date().toISOString()
          }
        ];
        apiError = true;
      } else {
        apiError = true;
        stores = [];
        toastStore.error(response.message || '매장 목록을 불러오는데 실패했습니다.');
      }
    } catch (error) {
      console.error('Failed to load stores:', error);
      apiError = true;
      stores = [];
      toastStore.error('매장 목록을 불러오는데 실패했습니다.');
    } finally {
      loading = false;
    }
  }

  // 필터링된 매장 목록
  $: filteredStores = stores.filter(store => {
    const matchesSearch = store.storeName.toLowerCase().includes(searchTerm.toLowerCase()) ||
                         store.storeId.toLowerCase().includes(searchTerm.toLowerCase()) ||
                         store.ownerName.toLowerCase().includes(searchTerm.toLowerCase());
    const matchesStatus = filterStatus === 'all' || store.storeStatus === filterStatus;
    const matchesType = filterType === 'all' || store.storeType === filterType;
    
    return matchesSearch && matchesStatus && matchesType;
  });

  function getStatusColor(status) {
    const colors = {
      'ACTIVE': 'bg-green-100 text-green-800',
      'INACTIVE': 'bg-gray-100 text-gray-800',
      'SUSPENDED': 'bg-red-100 text-red-800',
      'MAINTENANCE': 'bg-yellow-100 text-yellow-800'
    };
    return colors[status] || 'bg-gray-100 text-gray-800';
  }

  function getStatusText(status) {
    const texts = {
      'ACTIVE': '활성',
      'INACTIVE': '비활성',
      'SUSPENDED': '정지',
      'MAINTENANCE': '점검중'
    };
    return texts[status] || status;
  }

  function getTypeText(type) {
    const texts = {
      'CHAIN': '체인매장',
      'INDIVIDUAL': '개인매장'
    };
    return texts[type] || type;
  }

  async function editStore(store) {
    // TODO: 매장 편집 모달 구현
    console.log('Edit store:', store);
    toastStore.info('매장 편집 기능은 준비 중입니다.');
  }

  async function deleteStore(store) {
    if (!confirm(`정말로 "${store.storeName}" 매장을 삭제하시겠습니까?`)) {
      return;
    }

    try {
      const response = await storeApi.deleteStore(store.storeId);

      if (response.success) {
        stores = stores.filter(s => s.storeId !== store.storeId);
        toastStore.success('매장이 삭제되었습니다.');
      } else if (response.error === 'API_NOT_FOUND') {
        // API가 없어도 로컬에서 삭제
        stores = stores.filter(s => s.storeId !== store.storeId);
        toastStore.success('매장이 삭제되었습니다. (데모 모드)');
      } else {
        toastStore.error(response.message || '매장 삭제에 실패했습니다.');
      }
    } catch (error) {
      console.error('Delete store error:', error);
      toastStore.error('매장 삭제에 실패했습니다.');
    }
  }

  function handleStoreCreated(event) {
    const newStore = event.detail;
    stores = [newStore, ...stores];
    toastStore.success('새 매장이 생성되었습니다.');
  }

  function openCreateModal() {
    showCreateModal = true;
  }

  function formatDate(dateString) {
    if (!dateString) return '없음';
    return new Date(dateString).toLocaleDateString('ko-KR');
  }
</script>

<svelte:head>
  <title>매장 관리 - WebPos</title>
</svelte:head>

<div class="space-y-6">
  <!-- 헤더 -->
  <div class="flex items-center justify-between">
    <div>
      <h1 class="text-2xl font-bold text-gray-900">매장 관리</h1>
      <p class="text-gray-600 mt-1">전체 매장을 관리합니다.</p>
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
        매장 추가
      </button>
    </div>
  </div>

  {#if apiError}
    <Custom404
      title="매장 API를 찾을 수 없습니다"
      message="매장 관리 API가 구현되지 않았습니다. 현재 데모 데이터로 동작하고 있습니다."
      showHomeButton={false}
      onRetry={loadStores}
    />
  {/if}

  <!-- 통계 카드 -->
  <div class="grid grid-cols-1 md:grid-cols-4 gap-6">
    <div class="card p-6">
      <div class="flex items-center">
        <div class="p-3 rounded-full bg-blue-100">
          <Building class="h-6 w-6 text-blue-600" />
        </div>
        <div class="ml-4">
          <p class="text-sm font-medium text-gray-600">총 매장</p>
          <p class="text-2xl font-bold text-gray-900">{stores.length}</p>
        </div>
      </div>
    </div>

    <div class="card p-6">
      <div class="flex items-center">
        <div class="p-3 rounded-full bg-green-100">
          <Building class="h-6 w-6 text-green-600" />
        </div>
        <div class="ml-4">
          <p class="text-sm font-medium text-gray-600">활성 매장</p>
          <p class="text-2xl font-bold text-gray-900">
            {stores.filter(s => s.storeStatus === 'ACTIVE').length}
          </p>
        </div>
      </div>
    </div>

    <div class="card p-6">
      <div class="flex items-center">
        <div class="p-3 rounded-full bg-purple-100">
          <Building class="h-6 w-6 text-purple-600" />
        </div>
        <div class="ml-4">
          <p class="text-sm font-medium text-gray-600">체인매장</p>
          <p class="text-2xl font-bold text-gray-900">
            {stores.filter(s => s.storeType === 'CHAIN').length}
          </p>
        </div>
      </div>
    </div>

    <div class="card p-6">
      <div class="flex items-center">
        <div class="p-3 rounded-full bg-orange-100">
          <MapPin class="h-6 w-6 text-orange-600" />
        </div>
        <div class="ml-4">
          <p class="text-sm font-medium text-gray-600">개인매장</p>
          <p class="text-2xl font-bold text-gray-900">
            {stores.filter(s => s.storeType === 'INDIVIDUAL').length}
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
          placeholder="매장명, 매장ID 또는 사업자명 검색..."
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
        <option value="MAINTENANCE">점검중</option>
      </select>

      <!-- 유형 필터 -->
      <select bind:value={filterType} class="input">
        <option value="all">모든 유형</option>
        <option value="CHAIN">체인매장</option>
        <option value="INDIVIDUAL">개인매장</option>
      </select>
    </div>
  </div>

  <!-- 매장 목록 -->
  <div class="card overflow-hidden">
    {#if loading}
      <div class="p-12 text-center">
        <div class="animate-spin rounded-full h-8 w-8 border-b-2 border-primary-600 mx-auto"></div>
        <p class="mt-4 text-gray-600">로딩 중...</p>
      </div>
    {:else if filteredStores.length === 0}
      <div class="p-12 text-center">
        <Building class="mx-auto h-12 w-12 text-gray-400" />
        <p class="mt-4 text-gray-500">조건에 맞는 매장이 없습니다.</p>
      </div>
    {:else}
      <div class="overflow-x-auto">
        <table class="min-w-full divide-y divide-gray-200">
          <thead class="bg-gray-50">
            <tr>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                매장 정보
              </th>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                유형
              </th>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                사업자 정보
              </th>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                POS 수
              </th>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                상태
              </th>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                개업일
              </th>
              <th class="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase tracking-wider">
                작업
              </th>
            </tr>
          </thead>
          <tbody class="bg-white divide-y divide-gray-200">
            {#each filteredStores as store}
              <tr class="hover:bg-gray-50">
                <td class="px-6 py-4 whitespace-nowrap">
                  <div class="flex items-center">
                    <div class="w-10 h-10 bg-primary-600 rounded-full flex items-center justify-center">
                      <Building class="h-5 w-5 text-white" />
                    </div>
                    <div class="ml-4">
                      <div class="text-sm font-medium text-gray-900">{store.storeName}</div>
                      <div class="text-sm text-gray-500">{store.storeId}</div>
                      {#if store.hqName}
                        <div class="text-xs text-blue-600">{store.hqName}</div>
                      {/if}
                    </div>
                  </div>
                </td>
                <td class="px-6 py-4 whitespace-nowrap">
                  <span class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium {
                    store.storeType === 'CHAIN' ? 'bg-blue-100 text-blue-800' : 'bg-green-100 text-green-800'
                  }">
                    {getTypeText(store.storeType)}
                  </span>
                  {#if store.operationType}
                    <div class="text-xs text-gray-500 mt-1">{store.operationType}</div>
                  {/if}
                </td>
                <td class="px-6 py-4 whitespace-nowrap">
                  <div class="text-sm text-gray-900">{store.ownerName}</div>
                  {#if store.phoneNumber}
                    <div class="text-sm text-gray-500">{store.phoneNumber}</div>
                  {/if}
                  {#if store.businessLicense}
                    <div class="text-xs text-gray-500">{store.businessLicense}</div>
                  {/if}
                </td>
                <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                  {store.posCount || 0}대
                </td>
                <td class="px-6 py-4 whitespace-nowrap">
                  <span class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium {getStatusColor(store.storeStatus)}">
                    {getStatusText(store.storeStatus)}
                  </span>
                </td>
                <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                  {formatDate(store.openingDate)}
                </td>
                <td class="px-6 py-4 whitespace-nowrap text-right text-sm font-medium">
                  <div class="flex justify-end space-x-2">
                    <button
                      type="button"
                      class="text-indigo-600 hover:text-indigo-900"
                      on:click={() => editStore(store)}
                      title="편집"
                    >
                      <Edit size="16" />
                    </button>
                    <button
                      type="button"
                      class="text-red-600 hover:text-red-900"
                      on:click={() => deleteStore(store)}
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

<!-- 매장 생성 모달 -->
<CreateStoreModal
  bind:open={showCreateModal}
  on:store-created={handleStoreCreated}
  on:close={() => showCreateModal = false}
/>
