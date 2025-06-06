<script>
  import { onMount } from 'svelte';
  import { tabStore } from '$lib/stores/tabs.js';
  import { toastStore } from '$lib/stores/toast.js';
  import { Plus, Search, Edit, Trash2, Monitor, Settings, Wifi, AlertTriangle } from 'lucide-svelte';
  import CreatePosModal from '$lib/components/SuperAdmin/CreatePosModal.svelte';
  import Custom404 from '$lib/components/Common/Custom404.svelte';
  import { posApi } from '$lib/api/superAdmin.js';

  let posSystems = [];
  let loading = true;
  let searchTerm = '';
  let filterStatus = 'all';
  let filterType = 'all';
  let filterStore = 'all';
  let showCreateModal = false;
  let apiError = false;

  onMount(() => {
    tabStore.setActiveTab('ADMIN_POS');
    loadPosSystems();
  });

  async function loadPosSystems() {
    try {
      const response = await posApi.getPosSystems();
      
      if (response.success && response.data) {
        posSystems = response.data.posSystems || response.data;
        apiError = false;
      } else if (response.error === 'API_NOT_FOUND') {
        posSystems = [
          {
            posId: 'HQ001001P01',
            storeId: 'HQ001001',
            storeName: '강남점',
            posNumber: 1,
            posName: '메인 POS',
            posType: 'MAIN',
            ipAddress: '192.168.1.101',
            macAddress: '00:1B:44:11:3A:B7',
            serialNumber: 'POS001234567',
            installedDate: new Date(Date.now() - 365 * 24 * 60 * 60 * 1000).toISOString().split('T')[0],
            lastMaintenanceDate: new Date(Date.now() - 90 * 24 * 60 * 60 * 1000).toISOString().split('T')[0],
            posStatus: 'ACTIVE',
            isActive: true,
            createdAt: new Date(Date.now() - 365 * 24 * 60 * 60 * 1000).toISOString(),
            updatedAt: new Date().toISOString()
          },
          {
            posId: 'HQ001001P02',
            storeId: 'HQ001001',
            storeName: '강남점',
            posNumber: 2,
            posName: '서브 POS',
            posType: 'SUB',
            ipAddress: '192.168.1.102',
            macAddress: '00:1B:44:11:3A:B8',
            serialNumber: 'POS001234568',
            installedDate: new Date(Date.now() - 240 * 24 * 60 * 60 * 1000).toISOString().split('T')[0],
            lastMaintenanceDate: new Date(Date.now() - 30 * 24 * 60 * 60 * 1000).toISOString().split('T')[0],
            posStatus: 'ACTIVE',
            isActive: true,
            createdAt: new Date(Date.now() - 240 * 24 * 60 * 60 * 1000).toISOString(),
            updatedAt: new Date().toISOString()
          },
          {
            posId: 'IN002001P01',
            storeId: 'IN002001',
            storeName: '개인카페 A',
            posNumber: 1,
            posName: '메인 POS',
            posType: 'MAIN',
            ipAddress: '192.168.1.201',
            macAddress: '00:1B:44:11:3A:C1',
            serialNumber: 'POS001234569',
            installedDate: new Date(Date.now() - 180 * 24 * 60 * 60 * 1000).toISOString().split('T')[0],
            lastMaintenanceDate: new Date(Date.now() - 14 * 24 * 60 * 60 * 1000).toISOString().split('T')[0],
            posStatus: 'MAINTENANCE',
            isActive: true,
            createdAt: new Date(Date.now() - 180 * 24 * 60 * 60 * 1000).toISOString(),
            updatedAt: new Date().toISOString()
          }
        ];
        apiError = true;
      } else {
        apiError = true;
        posSystems = [];
        toastStore.error(response.message || 'POS 목록을 불러오는데 실패했습니다.');
      }
    } catch (error) {
      console.error('Failed to load POS systems:', error);
      apiError = true;
      posSystems = [];
      toastStore.error('POS 목록을 불러오는데 실패했습니다.');
    } finally {
      loading = false;
    }
  }

  $: uniqueStores = [...new Set(posSystems.map(pos => pos.storeName))];

  $: filteredPosSystems = posSystems.filter(pos => {
    const matchesSearch = pos.posName?.toLowerCase().includes(searchTerm.toLowerCase()) ||
                         pos.posId.toLowerCase().includes(searchTerm.toLowerCase()) ||
                         pos.storeName.toLowerCase().includes(searchTerm.toLowerCase()) ||
                         pos.storeId.toLowerCase().includes(searchTerm.toLowerCase());
    const matchesStatus = filterStatus === 'all' || pos.posStatus === filterStatus;
    const matchesType = filterType === 'all' || pos.posType === filterType;
    const matchesStore = filterStore === 'all' || pos.storeName === filterStore;
    
    return matchesSearch && matchesStatus && matchesType && matchesStore;
  });

  function getStatusColor(status) {
    const colors = {
      'ACTIVE': 'bg-green-100 text-green-800',
      'INACTIVE': 'bg-gray-100 text-gray-800',
      'MAINTENANCE': 'bg-yellow-100 text-yellow-800',
      'ERROR': 'bg-red-100 text-red-800'
    };
    return colors[status] || 'bg-gray-100 text-gray-800';
  }

  function getStatusText(status) {
    const texts = {
      'ACTIVE': '활성',
      'INACTIVE': '비활성',
      'MAINTENANCE': '점검중',
      'ERROR': '오류'
    };
    return texts[status] || status;
  }

  function getTypeColor(type) {
    const colors = {
      'MAIN': 'bg-blue-100 text-blue-800',
      'SUB': 'bg-green-100 text-green-800',
      'MOBILE': 'bg-purple-100 text-purple-800'
    };
    return colors[type] || 'bg-gray-100 text-gray-800';
  }

  function getTypeText(type) {
    const texts = {
      'MAIN': '메인',
      'SUB': '서브',
      'MOBILE': '모바일'
    };
    return texts[type] || type;
  }

  async function editPos(pos) {
    console.log('Edit POS:', pos);
    toastStore.info('POS 편집 기능은 준비 중입니다.');
  }

  async function deletePos(pos) {
    if (!confirm(`정말로 "${pos.posName || pos.posId}" POS를 삭제하시겠습니까?`)) {
      return;
    }

    try {
      const response = await posApi.deletePosSystem(pos.posId);

      if (response.success) {
        posSystems = posSystems.filter(p => p.posId !== pos.posId);
        toastStore.success('POS가 삭제되었습니다.');
      } else if (response.error === 'API_NOT_FOUND') {
        posSystems = posSystems.filter(p => p.posId !== pos.posId);
        toastStore.success('POS가 삭제되었습니다. (데모 모드)');
      } else {
        toastStore.error(response.message || 'POS 삭제에 실패했습니다.');
      }
    } catch (error) {
      console.error('Delete POS error:', error);
      toastStore.error('POS 삭제에 실패했습니다.');
    }
  }

  async function performMaintenance(pos) {
    try {
      const response = await posApi.startMaintenance(pos.posId);

      if (response.success) {
        await loadPosSystems();
        toastStore.success('POS 점검이 시작되었습니다.');
      } else if (response.error === 'API_NOT_FOUND') {
        const posIndex = posSystems.findIndex(p => p.posId === pos.posId);
        if (posIndex !== -1) {
          posSystems[posIndex] = { ...posSystems[posIndex], posStatus: 'MAINTENANCE' };
          posSystems = [...posSystems];
        }
        toastStore.success('POS 점검이 시작되었습니다. (데모 모드)');
      } else {
        toastStore.error(response.message || 'POS 점검 시작에 실패했습니다.');
      }
    } catch (error) {
      console.error('POS maintenance error:', error);
      toastStore.error('POS 점검 시작에 실패했습니다.');
    }
  }

  async function completeMaintenance(pos) {
    try {
      const response = await posApi.completeMaintenance(pos.posId);

      if (response.success) {
        await loadPosSystems();
        toastStore.success('POS 점검이 완료되었습니다.');
      } else if (response.error === 'API_NOT_FOUND') {
        const posIndex = posSystems.findIndex(p => p.posId === pos.posId);
        if (posIndex !== -1) {
          posSystems[posIndex] = { 
            ...posSystems[posIndex], 
            posStatus: 'ACTIVE',
            lastMaintenanceDate: new Date().toISOString().split('T')[0]
          };
          posSystems = [...posSystems];
        }
        toastStore.success('POS 점검이 완료되었습니다. (데모 모드)');
      } else {
        toastStore.error(response.message || 'POS 점검 완료에 실패했습니다.');
      }
    } catch (error) {
      console.error('POS maintenance complete error:', error);
      toastStore.error('POS 점검 완료에 실패했습니다.');
    }
  }

  function handlePosCreated(event) {
    const newPos = event.detail;
    posSystems = [newPos, ...posSystems];
    toastStore.success('새 POS가 생성되었습니다.');
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
  <title>POS 관리 - WebPos</title>
</svelte:head>

<div class="space-y-6">
  <div class="flex items-center justify-between">
    <div>
      <h1 class="text-2xl font-bold text-gray-900">POS 관리</h1>
      <p class="text-gray-600 mt-1">전체 POS 시스템을 관리합니다.</p>
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
        POS 추가
      </button>
    </div>
  </div>

  {#if apiError}
    <Custom404
      title="POS API를 찾을 수 없습니다"
      message="POS 관리 API가 구현되지 않았습니다. 현재 데모 데이터로 동작하고 있습니다."
      showHomeButton={false}
      onRetry={loadPosSystems}
    />
  {/if}

  <div class="grid grid-cols-1 md:grid-cols-4 gap-6">
    <div class="card p-6">
      <div class="flex items-center">
        <div class="p-3 rounded-full bg-blue-100">
          <Monitor class="h-6 w-6 text-blue-600" />
        </div>
        <div class="ml-4">
          <p class="text-sm font-medium text-gray-600">총 POS</p>
          <p class="text-2xl font-bold text-gray-900">{posSystems.length}</p>
        </div>
      </div>
    </div>

    <div class="card p-6">
      <div class="flex items-center">
        <div class="p-3 rounded-full bg-green-100">
          <Monitor class="h-6 w-6 text-green-600" />
        </div>
        <div class="ml-4">
          <p class="text-sm font-medium text-gray-600">활성 POS</p>
          <p class="text-2xl font-bold text-gray-900">
            {posSystems.filter(p => p.posStatus === 'ACTIVE').length}
          </p>
        </div>
      </div>
    </div>

    <div class="card p-6">
      <div class="flex items-center">
        <div class="p-3 rounded-full bg-yellow-100">
          <Settings class="h-6 w-6 text-yellow-600" />
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
        <div class="p-3 rounded-full bg-purple-100">
          <Wifi class="h-6 w-6 text-purple-600" />
        </div>
        <div class="ml-4">
          <p class="text-sm font-medium text-gray-600">온라인</p>
          <p class="text-2xl font-bold text-gray-900">
            {posSystems.filter(p => p.ipAddress).length}
          </p>
        </div>
      </div>
    </div>
  </div>

  <div class="card p-6">
    <div class="grid grid-cols-1 md:grid-cols-4 gap-4">
      <div class="relative">
        <Search size="20" class="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400" />
        <input
          type="text"
          placeholder="POS명, POS ID 또는 매장명 검색..."
          bind:value={searchTerm}
          class="input pl-10"
        />
      </div>

      <select bind:value={filterStatus} class="input">
        <option value="all">모든 상태</option>
        <option value="ACTIVE">활성</option>
        <option value="INACTIVE">비활성</option>
        <option value="MAINTENANCE">점검중</option>
        <option value="ERROR">오류</option>
      </select>

      <select bind:value={filterType} class="input">
        <option value="all">모든 유형</option>
        <option value="MAIN">메인 POS</option>
        <option value="SUB">서브 POS</option>
        <option value="MOBILE">모바일 POS</option>
      </select>

      <select bind:value={filterStore} class="input">
        <option value="all">모든 매장</option>
        {#each uniqueStores as storeName}
          <option value={storeName}>{storeName}</option>
        {/each}
      </select>
    </div>
  </div>

  <div class="card overflow-hidden">
    {#if loading}
      <div class="p-12 text-center">
        <div class="animate-spin rounded-full h-8 w-8 border-b-2 border-primary-600 mx-auto"></div>
        <p class="mt-4 text-gray-600">로딩 중...</p>
      </div>
    {:else if filteredPosSystems.length === 0}
      <div class="p-12 text-center">
        <Monitor class="mx-auto h-12 w-12 text-gray-400" />
        <p class="mt-4 text-gray-500">조건에 맞는 POS가 없습니다.</p>
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
                매장
              </th>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                유형
              </th>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                네트워크
              </th>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                상태
              </th>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                최근 점검
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
                    <div class="w-10 h-10 bg-primary-600 rounded-full flex items-center justify-center">
                      <Monitor class="h-5 w-5 text-white" />
                    </div>
                    <div class="ml-4">
                      <div class="text-sm font-medium text-gray-900">{pos.posName || `POS ${pos.posNumber}`}</div>
                      <div class="text-sm text-gray-500">{pos.posId}</div>
                      {#if pos.serialNumber}
                        <div class="text-xs text-gray-500">S/N: {pos.serialNumber}</div>
                      {/if}
                    </div>
                  </div>
                </td>
                <td class="px-6 py-4 whitespace-nowrap">
                  <div class="text-sm text-gray-900">{pos.storeName}</div>
                  <div class="text-sm text-gray-500">{pos.storeId}</div>
                </td>
                <td class="px-6 py-4 whitespace-nowrap">
                  <span class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium {getTypeColor(pos.posType)}">
                    {getTypeText(pos.posType)}
                  </span>
                  <div class="text-xs text-gray-500 mt-1">#{pos.posNumber}</div>
                </td>
                <td class="px-6 py-4 whitespace-nowrap">
                  <div class="text-sm text-gray-900">{pos.ipAddress || '미설정'}</div>
                  {#if pos.macAddress}
                    <div class="text-xs text-gray-500">{pos.macAddress}</div>
                  {/if}
                </td>
                <td class="px-6 py-4 whitespace-nowrap">
                  <span class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium {getStatusColor(pos.posStatus)}">
                    {getStatusText(pos.posStatus)}
                  </span>
                </td>
                <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                  {formatDate(pos.lastMaintenanceDate)}
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
                        <Settings size="16" />
                      </button>
                    {:else}
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
                      class="text-indigo-600 hover:text-indigo-900"
                      on:click={() => editPos(pos)}
                      title="편집"
                    >
                      <Edit size="16" />
                    </button>
                    <button
                      type="button"
                      class="text-red-600 hover:text-red-900"
                      on:click={() => deletePos(pos)}
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

<CreatePosModal
  bind:open={showCreateModal}
  on:pos-created={handlePosCreated}
  on:close={() => showCreateModal = false}
/>
