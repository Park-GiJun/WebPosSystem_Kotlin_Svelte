<script>
  import { onMount } from 'svelte';
  import { authStore } from '$lib/stores/auth.js';
  import { tabStore } from '$lib/stores/tabs.js';
  import { toastStore } from '$lib/stores/toast.js';
  import { Plus, Search, MapPin, Phone, Calendar, Building2, Store, Users, Edit, Eye, Trash2 } from 'lucide-svelte';
  import { storeApi } from '$lib/api/business.js';
  import CreateStoreModal from '$lib/components/Business/CreateStoreModal.svelte';

  let stores = [];
  let loading = true;
  let searchTerm = '';
  let filterStatus = 'all';
  let filterType = 'all';
  let showCreateModal = false;

  // 탭 활성화
  onMount(() => {
    tabStore.setActiveTab('BUSINESS_STORES');
    loadStores();
  });

  async function loadStores() {
    loading = true;
    try {
      console.log('🏪 매장 목록 조회 중...');
      
      const response = await storeApi.getStores({
        page: 0,
        size: 100, // 모든 매장 조회
        status: filterStatus !== 'all' ? filterStatus : undefined,
        type: filterType !== 'all' ? filterType : undefined,
        search: searchTerm || undefined
      }, $authStore.token);
      
      if (response && response.stores) {
        stores = response.stores;
        console.log('✅ 매장 목록 로드 완료:', stores.length, '개');
      } else {
        console.warn('⚠️ 응답에 stores 필드가 없습니다:', response);
        stores = [];
      }
    } catch (error) {
      console.error('❌ 매장 목록 로드 실패:', error);
      toastStore.error('매장 목록을 불러오는데 실패했습니다: ' + error.message);
      stores = [];
    } finally {
      loading = false;
    }
  }

  // 검색어나 필터가 변경될 때 디바운스 적용하여 매장 목록 다시 로드
  let debounceTimer;
  $: {
    clearTimeout(debounceTimer);
    debounceTimer = setTimeout(() => {
      if (typeof searchTerm !== 'undefined' && typeof filterStatus !== 'undefined' && typeof filterType !== 'undefined') {
        loadStores();
      }
    }, 300); // 300ms 디바운스
  }

  function getStatusColor(status) {
    const colors = {
      'ACTIVE': 'bg-green-100 text-green-800',
      'INACTIVE': 'bg-gray-100 text-gray-800',
      'CLOSED': 'bg-red-100 text-red-800'
    };
    return colors[status] || 'bg-gray-100 text-gray-800';
  }

  function getStatusText(status) {
    const texts = {
      'ACTIVE': '운영중',
      'INACTIVE': '운영중지',
      'CLOSED': '폐점'
    };
    return texts[status] || status;
  }

  function getTypeColor(type) {
    const colors = {
      'CHAIN': 'bg-blue-100 text-blue-800',
      'INDIVIDUAL': 'bg-purple-100 text-purple-800'
    };
    return colors[type] || 'bg-gray-100 text-gray-800';
  }

  function getTypeText(type) {
    const texts = {
      'CHAIN': '체인점',
      'INDIVIDUAL': '개인매장'
    };
    return texts[type] || type;
  }

  function getOperationTypeText(type) {
    const texts = {
      'DIRECT': '직영점',
      'FRANCHISE': '가맹점'
    };
    return texts[type] || type;
  }

  async function viewStore(store) {
    // TODO: 매장 상세 보기 페이지로 이동
    console.log('View store:', store);
    toastStore.info('매장 상세 보기 기능은 준비 중입니다.');
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
      console.log('🗑️ 매장 삭제 중:', store.storeName);
      
      await storeApi.deleteStore(store.storeId, $authStore.token);
      
      stores = stores.filter(s => s.storeId !== store.storeId);
      toastStore.success('매장이 삭제되었습니다.');
      console.log('✅ 매장 삭제 완료');
    } catch (error) {
      console.error('❌ 매장 삭제 실패:', error);
      toastStore.error('매장 삭제에 실패했습니다: ' + error.message);
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
</script>

<svelte:head>
  <title>매장 관리 - WebPos</title>
</svelte:head>

<div class="space-y-6">
  <!-- 헤더 -->
  <div class="flex items-center justify-between">
    <div>
      <h1 class="text-2xl font-bold text-gray-900">매장 관리</h1>
      <p class="text-gray-600 mt-1">매장 정보를 관리합니다.</p>
    </div>
    <button 
      type="button" 
      class="btn btn-primary"
      on:click={openCreateModal}
    >
      <Plus size="16" class="mr-2" />
      매장 추가
    </button>
  </div>

  <!-- 통계 카드 -->
  <div class="grid grid-cols-1 md:grid-cols-4 gap-6">
    <div class="card p-6">
      <div class="flex items-center">
        <div class="p-3 rounded-full bg-blue-100">
          <Store class="h-6 w-6 text-blue-600" />
        </div>
        <div class="ml-4">
          <p class="text-sm font-medium text-gray-600">총 매장 수</p>
          <p class="text-2xl font-bold text-gray-900">{stores.length}</p>
        </div>
      </div>
    </div>

    <div class="card p-6">
      <div class="flex items-center">
        <div class="p-3 rounded-full bg-green-100">
          <Building2 class="h-6 w-6 text-green-600" />
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
          <Users class="h-6 w-6 text-purple-600" />
        </div>
        <div class="ml-4">
          <p class="text-sm font-medium text-gray-600">체인점</p>
          <p class="text-2xl font-bold text-gray-900">
            {stores.filter(s => s.storeType === 'CHAIN').length}
          </p>
        </div>
      </div>
    </div>

    <div class="card p-6">
      <div class="flex items-center">
        <div class="p-3 rounded-full bg-yellow-100">
          <Store class="h-6 w-6 text-yellow-600" />
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
          placeholder="매장명, 점주명, 매장ID 검색..."
          bind:value={searchTerm}
          class="input pl-10"
        />
      </div>

      <!-- 상태 필터 -->
      <select bind:value={filterStatus} class="input">
        <option value="all">모든 상태</option>
        <option value="ACTIVE">운영중</option>
        <option value="INACTIVE">운영중지</option>
        <option value="CLOSED">폐점</option>
      </select>

      <!-- 타입 필터 -->
      <select bind:value={filterType} class="input">
        <option value="all">모든 타입</option>
        <option value="CHAIN">체인점</option>
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
    {:else if stores.length === 0}
      <div class="p-12 text-center">
        <Store class="mx-auto h-12 w-12 text-gray-400" />
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
                타입/운영형태
              </th>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                상태
              </th>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                연락처/주소
              </th>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                POS/직원
              </th>
              <th class="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase tracking-wider">
                작업
              </th>
            </tr>
          </thead>
          <tbody class="bg-white divide-y divide-gray-200">
            {#each stores as store}
              <tr class="hover:bg-gray-50">
                <td class="px-6 py-4 whitespace-nowrap">
                  <div class="flex items-center">
                    <div class="w-10 h-10 bg-primary-600 rounded-lg flex items-center justify-center">
                      <Store size="20" class="text-white" />
                    </div>
                    <div class="ml-4">
                      <div class="text-sm font-medium text-gray-900">{store.storeName}</div>
                      <div class="text-sm text-gray-500">{store.storeId}</div>
                      <div class="text-xs text-gray-400">점주: {store.ownerName}</div>
                      {#if store.hqName}
                        <div class="text-xs text-blue-600">{store.hqName}</div>
                      {/if}
                    </div>
                  </div>
                </td>
                <td class="px-6 py-4 whitespace-nowrap">
                  <div class="space-y-1">
                    <span class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium {getTypeColor(store.storeType)}">
                      {getTypeText(store.storeType)}
                    </span>
                    {#if store.operationType}
                      <div class="text-xs text-gray-500">{getOperationTypeText(store.operationType)}</div>
                    {/if}
                    <div class="text-xs text-gray-400">{store.regionName}</div>
                  </div>
                </td>
                <td class="px-6 py-4 whitespace-nowrap">
                  <span class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium {getStatusColor(store.storeStatus)}">
                    {getStatusText(store.storeStatus)}
                  </span>
                  <div class="text-xs text-gray-500 mt-1 flex items-center">
                    <Calendar size="12" class="mr-1" />
                    개점: {new Date(store.openingDate).toLocaleDateString('ko-KR')}
                  </div>
                </td>
                <td class="px-6 py-4">
                  <div class="space-y-1">
                    {#if store.phoneNumber}
                      <div class="text-sm text-gray-900 flex items-center">
                        <Phone size="12" class="mr-1" />
                        {store.phoneNumber}
                      </div>
                    {/if}
                    {#if store.address}
                      <div class="text-xs text-gray-500 flex items-start">
                        <MapPin size="12" class="mr-1 mt-0.5 flex-shrink-0" />
                        <span class="line-clamp-2">{store.address}</span>
                      </div>
                    {/if}
                    {#if store.businessLicense}
                      <div class="text-xs text-gray-400">사업자: {store.businessLicense}</div>
                    {/if}
                  </div>
                </td>
                <td class="px-6 py-4 whitespace-nowrap">
                  <div class="text-center">
                    <div class="text-sm font-medium text-gray-900">POS: {store.posCount}</div>
                    <div class="text-sm text-gray-500">직원: {store.employeeCount}</div>
                  </div>
                </td>
                <td class="px-6 py-4 whitespace-nowrap text-right text-sm font-medium">
                  <div class="flex justify-end space-x-2">
                    <button
                      type="button"
                      class="text-blue-600 hover:text-blue-900"
                      on:click={() => viewStore(store)}
                      title="상세 보기"
                    >
                      <Eye size="16" />
                    </button>
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
