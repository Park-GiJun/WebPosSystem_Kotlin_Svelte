<script>
  import { onMount } from 'svelte';
  import { authStore } from '$lib/stores/auth.js';
  import { tabStore } from '$lib/stores/tabs.js';
  import { toastStore } from '$lib/stores/toast.js';
  import { Plus, Search, Monitor, Wifi, Calendar, Settings, Edit, Eye, Trash2, Wrench } from 'lucide-svelte';
  import CreatePosModal from '$lib/components/Business/CreatePosModal.svelte';

  let posSystems = [];
  let loading = true;
  let searchTerm = '';
  let filterStatus = 'all';
  let filterType = 'all';
  let filterStore = 'all';
  let showCreateModal = false;
  let stores = [];

  // 탭 활성화
  onMount(() => {
    tabStore.setActiveTab('BUSINESS_POS');
    loadPosSystems();
    loadStores();
  });

  async function loadPosSystems() {
    loading = true;
    try {
      const response = await fetch('/api/v1/business/pos', {
        headers: {
          'Authorization': `Bearer ${$authStore.token}`
        }
      });
      
      if (response.ok) {
        const data = await response.json();
        posSystems = data.posSystems || [];
      }
    } catch (error) {
      console.error('Failed to load POS systems:', error);
      toastStore.error('POS 시스템 목록을 불러오는데 실패했습니다.');
    } finally {
      loading = false;
    }
  }

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
    }
  }

  // 필터링된 POS 목록
  $: filteredPosSystems = posSystems.filter(pos => {
    const matchesSearch = pos.posName?.toLowerCase().includes(searchTerm.toLowerCase()) ||
                         pos.storeName.toLowerCase().includes(searchTerm.toLowerCase()) ||
                         pos.posId.toLowerCase().includes(searchTerm.toLowerCase()) ||
                         pos.serialNumber?.toLowerCase().includes(searchTerm.toLowerCase());
    const matchesStatus = filterStatus === 'all' || pos.posStatus === filterStatus;
    const matchesType = filterType === 'all' || pos.posType === filterType;
    const matchesStore = filterStore === 'all' || pos.storeId === filterStore;
    
    return matchesSearch && matchesStatus && matchesType && matchesStore;
  });

  function getStatusColor(status) {
    const colors = {
      'ACTIVE': 'bg-green-100 text-green-800',
      'INACTIVE': 'bg-gray-100 text-gray-800',
      'MAINTENANCE': 'bg-yellow-100 text-yellow-800'
    };
    return colors[status] || 'bg-gray-100 text-gray-800';
  }

  function getStatusText(status) {
    const texts = {
      'ACTIVE': '정상',
      'INACTIVE': '비활성',
      'MAINTENANCE': '점검중'
    };
    return texts[status] || status;
  }

  function getTypeColor(type) {
    const colors = {
      'MAIN': 'bg-blue-100 text-blue-800',
      'SUB': 'bg-purple-100 text-purple-800',
      'MOBILE': 'bg-green-100 text-green-800'
    };
    return colors[type] || 'bg-gray-100 text-gray-800';
  }

  function getTypeText(type) {
    const texts = {
      'MAIN': '메인 POS',
      'SUB': '서브 POS',
      'MOBILE': '모바일 POS'
    };
    return texts[type] || type;
  }

  function getDaysSinceMaintenance(lastMaintenanceDate) {
    const today = new Date();
    const maintenance = new Date(lastMaintenanceDate);
    const diffTime = Math.abs(today - maintenance);
    const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));
    return diffDays;
  }

  async function viewPosSystem(pos) {
    console.log('View POS system:', pos);
    toastStore.info('POS 상세 보기 기능은 준비 중입니다.');
  }

  async function editPosSystem(pos) {
    console.log('Edit POS system:', pos);
    toastStore.info('POS 편집 기능은 준비 중입니다.');
  }

  async function deletePosSystem(pos) {
    if (!confirm(`정말로 "${pos.posName || pos.posId}" POS를 삭제하시겠습니까?`)) {
      return;
    }

    try {
      const response = await fetch(`/api/v1/business/pos/${pos.posId}`, {
        method: 'DELETE',
        headers: {
          'Authorization': `Bearer ${$authStore.token}`
        }
      });

      if (response.ok) {
        posSystems = posSystems.filter(p => p.posId !== pos.posId);
        toastStore.success('POS 시스템이 삭제되었습니다.');
      } else {
        throw new Error('삭제 실패');
      }
    } catch (error) {
      console.error('Delete POS error:', error);
      toastStore.error('POS 시스템 삭제에 실패했습니다.');
    }
  }

  async function performMaintenance(pos) {
    try {
      const response = await fetch(`/api/v1/business/pos/${pos.posId}/maintenance`, {
        method: 'POST',
        headers: {
          'Authorization': `Bearer ${$authStore.token}`
        }
      });

      if (response.ok) {
        await loadPosSystems();
        toastStore.success('POS 점검 상태로 변경되었습니다.');
      } else {
        throw new Error('점검 시작 실패');
      }
    } catch (error) {
      console.error('Maintenance error:', error);
      toastStore.error('POS 점검 시작에 실패했습니다.');
    }
  }

  async function completeMaintenance(pos) {
    try {
      const response = await fetch(`/api/v1/business/pos/${pos.posId}/complete-maintenance`, {
        method: 'POST',
        headers: {
          'Authorization': `Bearer ${$authStore.token}`
        }
      });

      if (response.ok) {
        await loadPosSystems();
        toastStore.success('POS 점검이 완료되었습니다.');
      } else {
        throw new Error('점검 완료 실패');
      }
    } catch (error) {
      console.error('Complete maintenance error:', error);
      toastStore.error('POS 점검 완료에 실패했습니다.');
    }
  }

  function handlePosCreated(event) {
    const newPos = event.detail;
    posSystems = [newPos, ...posSystems];
    toastStore.success('새 POS 시스템이 생성되었습니다.');
  }

  function openCreateModal() {
    showCreateModal = true;
  }
</script>

<svelte:head>
  <title>POS 관리 - WebPos</title>
</svelte:head>

<div class="space-y-6">
  <!-- 헤더 -->
  <div class="flex items-center justify-between">
    <div>
      <h1 class="text-2xl font-bold text-gray-900">POS 시스템 관리</h1>
      <p class="text-gray-600 mt-1">매장의 POS 시스템을 관리합니다.</p>
    </div>
    <button 
      type="button" 
      class="btn btn-primary"
      on:click={openCreateModal}
    >
      <Plus size="16" class="mr-2" />
      POS 추가
    </button>
  </div>

  <!-- 통계 카드 -->
  <div class="grid grid-cols-1 md:grid-cols-4 gap-6">
    <div class="card p-6">
      <div class="flex items-center">
        <div class="p-3 rounded-full bg-blue-100">
          <Monitor class="h-6 w-6 text-blue-600" />
        </div>
        <div class="ml-4">
          <p class="text-sm font-medium text-gray-600">총 POS 수</p>
          <p class="text-2xl font-bold text-gray-900">{posSystems.length}</p>
        </div>
      </div>
    </div>

    <div class="card p-6">
      <div class="flex items-center">
        <div class="p-3 rounded-full bg-green-100">
          <Wifi class="h-6 w-6 text-green-600" />
        </div>
        <div class="ml-4">
          <p class="text-sm font-medium text-gray-600">정상 작동</p>
          <p class="text-2xl font-bold text-gray-900">
            {posSystems.filter(p => p.posStatus === 'ACTIVE').length}
          </p>
        </div>
      </div>
    </div>

    <div class="card p-6">
      <div class="flex items-center">
        <div class="p-3 rounded-full bg-yellow-100">
          <Wrench class="h-6 w-6 text-yellow-600" />
        </div>
        <div class="ml-4">
          <p class="text-sm font-medium text-gray-600">점검중</p>
          <p class="text-2xl font-bold text-gray-900">
            {posSystems.filter(p => p.posStatus === 'MAINTENANCE').length}
          </p>
        </div>
      </div>
    </div>

    <div class="card p-6">
      <div class="flex items-center">
        <div class="p-3 rounded-full bg-red-100">
          <Settings class="h-6 w-6 text-red-600" />
        </div>
        <div class="ml-4">
          <p class="text-sm font-medium text-gray-600">비활성</p>
          <p class="text-2xl font-bold text-gray-900">
            {posSystems.filter(p => p.posStatus === 'INACTIVE').length}
          </p>
        </div>
      </div>
    </div>
  </div>

  <!-- 검색 및 필터 -->
  <div class="card p-6">
    <div class="grid grid-cols-1 md:grid-cols-4 gap-4">
      <!-- 검색 -->
      <div class="relative">
        <Search size="20" class="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400" />
        <input
          type="text"
          placeholder="POS명, 매장명, 시리얼 검색..."
          bind:value={searchTerm}
          class="input pl-10"
        />
      </div>

      <!-- 매장 필터 -->
      <select bind:value={filterStore} class="input">
        <option value="all">모든 매장</option>
        {#each stores as store}
          <option value={store.storeId}>{store.storeName}</option>
        {/each}
      </select>

      <!-- 상태 필터 -->
      <select bind:value={filterStatus} class="input">
        <option value="all">모든 상태</option>
        <option value="ACTIVE">정상</option>
        <option value="MAINTENANCE">점검중</option>
        <option value="INACTIVE">비활성</option>
      </select>

      <!-- 타입 필터 -->
      <select bind:value={filterType} class="input">
        <option value="all">모든 타입</option>
        <option value="MAIN">메인 POS</option>
        <option value="SUB">서브 POS</option>
        <option value="MOBILE">모바일 POS</option>
      </select>
    </div>
  </div>

  <!-- POS 목록 -->
  <div class="card overflow-hidden">
    {#if loading}
      <div class="p-12 text-center">
        <div class="animate-spin rounded-full h-8 w-8 border-b-2 border-primary-600 mx-auto"></div>
        <p class="mt-4 text-gray-600">로딩 중...</p>
      </div>
    {:else if filteredPosSystems.length === 0}
      <div class="p-12 text-center">
        <Monitor class="mx-auto h-12 w-12 text-gray-400" />
        <p class="mt-4 text-gray-500">조건에 맞는 POS 시스템이 없습니다.</p>
      </div>
    {:else}
      <div class="overflow-x-auto">
        <table class="min-w-full divide-y divide-gray-200">
          <thead class="bg-gray-50">
            <tr>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                POS 정보
              </th>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                매장 정보
              </th>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                상태/타입
              </th>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                네트워크 정보
              </th>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                점검 정보
              </th>
              <th class="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase tracking-wider">
                작업
              </th>
            </tr>
          </thead>
          <tbody class="bg-white divide-y divide-gray-200">
            {#each filteredPosSystems as pos}
              <tr class="hover:bg-gray-50">
                <td class="px-6 py-4 whitespace-nowrap">
                  <div class="flex items-center">
                    <div class="w-10 h-10 bg-gray-600 rounded-lg flex items-center justify-center">
                      <Monitor size="20" class="text-white" />
                    </div>
                    <div class="ml-4">
                      <div class="text-sm font-medium text-gray-900">{pos.posName || `POS ${pos.posNumber}`}</div>
                      <div class="text-sm text-gray-500">{pos.posId}</div>
                      {#if pos.serialNumber}
                        <div class="text-xs text-gray-400">S/N: {pos.serialNumber}</div>
                      {/if}
                    </div>
                  </div>
                </td>
                <td class="px-6 py-4 whitespace-nowrap">
                  <div>
                    <div class="text-sm font-medium text-gray-900">{pos.storeName}</div>
                    <div class="text-sm text-gray-500">POS #{pos.posNumber}</div>
                  </div>
                </td>
                <td class="px-6 py-4 whitespace-nowrap">
                  <div class="space-y-1">
                    <span class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium {getStatusColor(pos.posStatus)}">
                      {getStatusText(pos.posStatus)}
                    </span>
                    <div>
                      <span class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium {getTypeColor(pos.posType)}">
                        {getTypeText(pos.posType)}
                      </span>
                    </div>
                  </div>
                </td>
                <td class="px-6 py-4">
                  <div class="space-y-1">
                    {#if pos.ipAddress}
                      <div class="text-sm text-gray-900 flex items-center">
                        <Wifi size="12" class="mr-1" />
                        {pos.ipAddress}
                      </div>
                    {/if}
                    {#if pos.macAddress}
                      <div class="text-xs text-gray-500">MAC: {pos.macAddress}</div>
                    {/if}
                  </div>
                </td>
                <td class="px-6 py-4">
                  <div class="space-y-1">
                    <div class="text-sm text-gray-900 flex items-center">
                      <Calendar size="12" class="mr-1" />
                      {getDaysSinceMaintenance(pos.lastMaintenanceDate)}일 전
                    </div>
                    <div class="text-xs text-gray-500">
                      {new Date(pos.lastMaintenanceDate).toLocaleDateString('ko-KR')}
                    </div>
                    {#if getDaysSinceMaintenance(pos.lastMaintenanceDate) > 90}
                      <div class="text-xs text-red-600">점검 필요</div>
                    {/if}
                  </div>
                </td>
                <td class="px-6 py-4 whitespace-nowrap text-right text-sm font-medium">
                  <div class="flex justify-end space-x-2">
                    {#if pos.posStatus === 'MAINTENANCE'}
                      <button
                        type="button"
                        class="text-green-600 hover:text-green-900"
                        on:click={() => completeMaintenance(pos)}
                        title="점검 완료"
                      >
                        <Wrench size="16" />
                      </button>
                    {:else if pos.posStatus === 'ACTIVE'}
                      <button
                        type="button"
                        class="text-yellow-600 hover:text-yellow-900"
                        on:click={() => performMaintenance(pos)}
                        title="점검 시작"
                      >
                        <Settings size="16" />
                      </button>
                    {/if}
                    <button
                      type="button"
                      class="text-blue-600 hover:text-blue-900"
                      on:click={() => viewPosSystem(pos)}
                      title="상세 보기"
                    >
                      <Eye size="16" />
                    </button>
                    <button
                      type="button"
                      class="text-indigo-600 hover:text-indigo-900"
                      on:click={() => editPosSystem(pos)}
                      title="편집"
                    >
                      <Edit size="16" />
                    </button>
                    <button
                      type="button"
                      class="text-red-600 hover:text-red-900"
                      on:click={() => deletePosSystem(pos)}
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

<!-- POS 생성 모달 -->
<CreatePosModal
  bind:open={showCreateModal}
  {stores}
  on:pos-created={handlePosCreated}
  on:close={() => showCreateModal = false}
/>
