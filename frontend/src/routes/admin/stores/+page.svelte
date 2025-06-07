<script>
  import { onMount } from 'svelte';
  import { authStore } from '$lib/stores/auth.js';
  import { tabStore } from '$lib/stores/tabs.js';
  import { toastStore } from '$lib/stores/toast.js';
  import { Store, Building2, MapPin, Phone, Calendar, Users, Edit, Trash2, BarChart } from 'lucide-svelte';

  let stores = [];
  let loading = true;
  let filterText = '';
  let selectedStatus = 'ALL';
  let selectedType = 'ALL';

  const statusOptions = [
    { value: 'ALL', label: '전체' },
    { value: 'ACTIVE', label: '운영중' },
    { value: 'INACTIVE', label: '운영중지' },
    { value: 'CLOSED', label: '폐점' }
  ];

  const typeOptions = [
    { value: 'ALL', label: '전체' },
    { value: 'FRANCHISE', label: '가맹점' },
    { value: 'DIRECT', label: '직영점' },
    { value: 'INDIVIDUAL', label: '개인매장' }
  ];

  onMount(() => {
    tabStore.setActiveTab('ADMIN_STORES');
    loadAllStores();
  });

  async function loadAllStores() {
    loading = true;
    try {
      const response = await fetch('/api/v1/admin/stores', {
        headers: {
          'Authorization': `Bearer ${$authStore.token}`
        }
      });
      
      if (response.ok) {
        const data = await response.json();
        stores = data.stores || [];
      } else {
        throw new Error('매장 목록을 불러올 수 없습니다.');
      }
    } catch (error) {
      console.error('Failed to load stores:', error);
      toastStore.error(error.message);
    } finally {
      loading = false;
    }
  }

  // 필터링된 매장 목록
  $: filteredStores = stores.filter(store => {
    const matchesText = !filterText || 
                       store.storeName?.toLowerCase().includes(filterText.toLowerCase()) ||
                       store.storeNumber?.toLowerCase().includes(filterText.toLowerCase()) ||
                       store.ownerName?.toLowerCase().includes(filterText.toLowerCase());
    
    const matchesStatus = selectedStatus === 'ALL' || store.storeStatus === selectedStatus;
    const matchesType = selectedType === 'ALL' || store.operationType === selectedType;
    
    return matchesText && matchesStatus && matchesType;
  });

  function getOperationTypeColor(type) {
    const colors = {
      'DIRECT': 'bg-blue-100 text-blue-800',
      'FRANCHISE': 'bg-green-100 text-green-800',
      'INDIVIDUAL': 'bg-purple-100 text-purple-800'
    };
    return colors[type] || 'bg-gray-100 text-gray-800';
  }

  function getStatusColor(status) {
    const colors = {
      'ACTIVE': 'bg-green-100 text-green-800',
      'INACTIVE': 'bg-gray-100 text-gray-800',
      'CLOSED': 'bg-red-100 text-red-800'
    };
    return colors[status] || 'bg-gray-100 text-gray-800';
  }

  function getOperationTypeLabel(type) {
    const labels = {
      'DIRECT': '직영점',
      'FRANCHISE': '가맹점',
      'INDIVIDUAL': '개인매장'
    };
    return labels[type] || type;
  }

  function getStatusLabel(status) {
    const labels = {
      'ACTIVE': '운영중',
      'INACTIVE': '운영중지',
      'CLOSED': '폐점'
    };
    return labels[status] || status;
  }

  function editStore(store) {
    console.log('Edit store:', store);
    // TODO: 편집 모달 구현
  }

  function deleteStore(store) {
    console.log('Delete store:', store);
    // TODO: 삭제 확인 모달 구현
  }

  function viewStoreDetails(store) {
    console.log('View store details:', store);
    // TODO: 상세 정보 모달 구현
  }
</script>

<svelte:head>
  <title>매장 관리 - 슈퍼어드민</title>
</svelte:head>

<div class="space-y-6">
  <!-- 헤더 -->
  <div class="flex items-center justify-between">
    <div>
      <h1 class="text-2xl font-bold text-gray-900">전체 매장 관리</h1>
      <p class="text-gray-600 mt-1">시스템 내 모든 매장을 조회하고 관리합니다.</p>
    </div>
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
          <p class="text-sm font-medium text-gray-600">가맹점</p>
          <p class="text-2xl font-bold text-gray-900">
            {stores.filter(s => s.operationType === 'FRANCHISE').length}
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
          <p class="text-sm font-medium text-gray-600">직영점</p>
          <p class="text-2xl font-bold text-gray-900">
            {stores.filter(s => s.operationType === 'DIRECT').length}
          </p>
        </div>
      </div>
    </div>

    <div class="card p-6">
      <div class="flex items-center">
        <div class="p-3 rounded-full bg-yellow-100">
          <BarChart class="h-6 w-6 text-yellow-600" />
        </div>
        <div class="ml-4">
          <p class="text-sm font-medium text-gray-600">개인매장</p>
          <p class="text-2xl font-bold text-gray-900">
            {stores.filter(s => s.operationType === 'INDIVIDUAL').length}
          </p>
        </div>
      </div>
    </div>
  </div>

  <!-- 필터 및 검색 -->
  <div class="card p-6">
    <div class="grid grid-cols-1 md:grid-cols-4 gap-4">
      <div>
        <label for="filterText" class="block text-sm font-medium text-gray-700 mb-1">검색</label>
        <input
          id="filterText"
          type="text"
          bind:value={filterText}
          class="input"
          placeholder="매장명, 매장번호, 점주명..."
        />
      </div>
      <div>
        <label for="statusFilter" class="block text-sm font-medium text-gray-700 mb-1">운영 상태</label>
        <select
          id="statusFilter"
          bind:value={selectedStatus}
          class="input"
        >
          {#each statusOptions as option}
            <option value={option.value}>{option.label}</option>
          {/each}
        </select>
      </div>
      <div>
        <label for="typeFilter" class="block text-sm font-medium text-gray-700 mb-1">매장 유형</label>
        <select
          id="typeFilter"
          bind:value={selectedType}
          class="input"
        >
          {#each typeOptions as option}
            <option value={option.value}>{option.label}</option>
          {/each}
        </select>
      </div>
      <div class="flex items-end">
        <button
          type="button"
          class="btn btn-secondary w-full"
          on:click={() => {
            filterText = '';
            selectedStatus = 'ALL';
            selectedType = 'ALL';
          }}
        >
          필터 초기화
        </button>
      </div>
    </div>
  </div>

  <!-- 매장 목록 -->
  <div class="card overflow-hidden">
    <div class="px-6 py-4 border-b border-gray-200">
      <h3 class="text-lg font-medium text-gray-900">
        매장 목록 ({filteredStores.length}개)
      </h3>
    </div>

    {#if loading}
      <div class="p-12 text-center">
        <div class="animate-spin rounded-full h-8 w-8 border-b-2 border-primary-600 mx-auto"></div>
        <p class="mt-4 text-gray-600">로딩 중...</p>
      </div>
    {:else if filteredStores.length === 0}
      <div class="p-12 text-center">
        <Store class="mx-auto h-12 w-12 text-gray-400" />
        <p class="mt-4 text-gray-500">
          {stores.length === 0 ? '등록된 매장이 없습니다.' : '조건에 맞는 매장이 없습니다.'}
        </p>
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
                유형/상태
              </th>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                점주/관리자
              </th>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                연락처
              </th>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                개점일
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
                    <div class="flex-shrink-0 h-10 w-10">
                      <div class="h-10 w-10 bg-primary-600 rounded-lg flex items-center justify-center">
                        <Store class="h-5 w-5 text-white" />
                      </div>
                    </div>
                    <div class="ml-4">
                      <div class="text-sm font-medium text-gray-900">{store.storeName}</div>
                      <div class="text-sm text-gray-500">{store.storeNumber}</div>
                    </div>
                  </div>
                </td>
                <td class="px-6 py-4 whitespace-nowrap">
                  <div class="flex flex-col space-y-1">
                    <span class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium {getOperationTypeColor(store.operationType)}">
                      {getOperationTypeLabel(store.operationType)}
                    </span>
                    <span class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium {getStatusColor(store.storeStatus)}">
                      {getStatusLabel(store.storeStatus)}
                    </span>
                  </div>
                </td>
                <td class="px-6 py-4 whitespace-nowrap">
                  <div class="text-sm text-gray-900">{store.ownerName}</div>
                  <div class="text-sm text-gray-500">
                    {#if store.managerUser}
                      매니저: {store.managerUser.username}
                    {/if}
                  </div>
                </td>
                <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                  {store.phoneNumber || '-'}
                </td>
                <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                  {store.openingDate ? new Date(store.openingDate).toLocaleDateString('ko-KR') : '-'}
                </td>
                <td class="px-6 py-4 whitespace-nowrap text-right text-sm font-medium">
                  <div class="flex justify-end space-x-2">
                    <button
                      type="button"
                      class="text-blue-600 hover:text-blue-900"
                      on:click={() => viewStoreDetails(store)}
                      title="상세보기"
                    >
                      <BarChart size="16" />
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
