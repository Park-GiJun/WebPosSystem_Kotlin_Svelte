<script>
  import { onMount } from 'svelte';
  import { authStore } from '$lib/stores/auth.js';
  import { tabStore } from '$lib/stores/tabs.js';
  import { Plus, Search, MapPin, Phone, Calendar, Building2, Store, Users } from 'lucide-svelte';

  let stores = [];
  let loading = true;
  let searchTerm = '';
  let filterStatus = 'all';
  let filterType = 'all';

  // 탭 활성화
  onMount(() => {
    tabStore.setActiveTab('BUSINESS_STORES');
    loadStores();
  });

  async function loadStores() {
    try {
      const response = await fetch('/api/v1/business/stores', {
        headers: {
          'Authorization': `Bearer ${$authStore.token}`
        }
      });
      
      if (response.ok) {
        const data = await response.json();
        stores = data.stores || [];
      }
    } catch (error) {
      console.error('Failed to load stores:', error);
    } finally {
      loading = false;
    }
  }

  // 필터링된 매장 목록
  $: filteredStores = stores.filter(store => {
    const matchesSearch = store.storeName.toLowerCase().includes(searchTerm.toLowerCase()) ||
                         store.ownerName.toLowerCase().includes(searchTerm.toLowerCase());
    const matchesStatus = filterStatus === 'all' || store.storeStatus === filterStatus;
    const matchesType = filterType === 'all' || store.storeType === filterType;
    
    return matchesSearch && matchesStatus && matchesType;
  });

  function getStatusColor(status) {
    const colors = {
      'ACTIVE': 'bg-green-100 text-green-800',
      'INACTIVE': 'bg-gray-100 text-gray-800',
      'CLOSED': 'bg-red-100 text-red-800'
    };
    return colors[status] || 'bg-gray-100 text-gray-800';
  }

  function getTypeColor(type) {
    const colors = {
      'CHAIN': 'bg-blue-100 text-blue-800',
      'INDIVIDUAL': 'bg-purple-100 text-purple-800'
    };
    return colors[type] || 'bg-gray-100 text-gray-800';
  }

  function viewStore(store) {
    console.log('View store:', store);
  }

  function createStore() {
    console.log('Create store');
  }
</script>

<svelte:head>
  <title>매장 관리 - WebPos</title>
</svelte:head>

<div slot="title">매장 관리</div>

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
      on:click={createStore}
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
          placeholder="매장명 또는 점주명 검색..."
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
  <div class="grid grid-cols-1 lg:grid-cols-2 xl:grid-cols-3 gap-6">
    {#if loading}
      {#each Array(6) as _}
        <div class="card p-6 animate-pulse">
          <div class="h-4 bg-gray-300 rounded w-3/4 mb-4"></div>
          <div class="h-4 bg-gray-300 rounded w-1/2 mb-2"></div>
          <div class="h-4 bg-gray-300 rounded w-2/3"></div>
        </div>
      {/each}
    {:else if filteredStores.length === 0}
      <div class="col-span-full">
        <div class="card p-12 text-center">
          <Store class="mx-auto h-12 w-12 text-gray-400" />
          <p class="mt-4 text-gray-500">매장이 없습니다.</p>
        </div>
      </div>
    {:else}
      {#each filteredStores as store}
        <div class="card p-6 hover:shadow-md transition-shadow cursor-pointer" on:click={() => viewStore(store)}>
          <!-- 매장 헤더 -->
          <div class="flex items-start justify-between mb-4">
            <div class="flex-1">
              <h3 class="text-lg font-semibold text-gray-900 mb-1">{store.storeName}</h3>
              <p class="text-sm text-gray-600">{store.storeNumber}</p>
            </div>
            <div class="flex flex-col items-end space-y-2">
              <span class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium {getStatusColor(store.storeStatus)}">
                {store.storeStatus}
              </span>
              <span class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium {getTypeColor(store.storeType)}">
                {store.storeType}
              </span>
            </div>
          </div>

          <!-- 매장 정보 -->
          <div class="space-y-3">
            <!-- 점주 정보 -->
            <div class="flex items-center text-sm text-gray-600">
              <Users size="16" class="mr-2" />
              <span>{store.ownerName}</span>
            </div>

            <!-- 전화번호 -->
            {#if store.phoneNumber}
              <div class="flex items-center text-sm text-gray-600">
                <Phone size="16" class="mr-2" />
                <span>{store.phoneNumber}</span>
              </div>
            {/if}

            <!-- 주소 -->
            {#if store.address}
              <div class="flex items-start text-sm text-gray-600">
                <MapPin size="16" class="mr-2 mt-0.5 flex-shrink-0" />
                <span class="line-clamp-2">{store.address}</span>
              </div>
            {/if}

            <!-- 개점일 -->
            <div class="flex items-center text-sm text-gray-600">
              <Calendar size="16" class="mr-2" />
              <span>{new Date(store.openingDate).toLocaleDateString('ko-KR')}</span>
            </div>

            <!-- 본사 정보 (체인점인 경우) -->
            {#if store.storeType === 'CHAIN' && store.hqName}
              <div class="flex items-center text-sm text-gray-600">
                <Building2 size="16" class="mr-2" />
                <span>{store.hqName}</span>
              </div>
            {/if}
          </div>

          <!-- 매장 통계 -->
          <div class="mt-4 pt-4 border-t border-gray-200">
            <div class="grid grid-cols-2 gap-4 text-center">
              <div>
                <p class="text-xs text-gray-500">POS 수</p>
                <p class="text-lg font-semibold text-gray-900">{store.posCount || 0}</p>
              </div>
              <div>
                <p class="text-xs text-gray-500">직원 수</p>
                <p class="text-lg font-semibold text-gray-900">{store.employeeCount || 0}</p>
              </div>
            </div>
          </div>
        </div>
      {/each}
    {/if}
  </div>
</div>
