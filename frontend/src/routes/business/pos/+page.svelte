<script>
  import { onMount } from 'svelte';
  import { authStore } from '$lib/stores/auth.js';
  import { tabStore } from '$lib/stores/tabs.js';
  import { toastStore } from '$lib/stores/toast.js';
  import { posApi, storeApi } from '$lib/api/business.js';
  import { Plus, Monitor, Settings, Activity, Edit, Trash2, Power, PowerOff } from 'lucide-svelte';
  import Modal from '$lib/components/Common/Modal.svelte';

  let posDevices = [];
  let loading = true;
  let showCreateModal = false;
  let availableStores = [];

  // 인증 상태 구독
  let authToken = '';
  authStore.subscribe(state => {
    authToken = state.token || '';
  });

  // POS 생성 폼
  let posForm = {
    storeId: '',
    posNumber: 1,
    posName: '',
    posType: 'MAIN',
    ipAddress: '',
    macAddress: '',
    serialNumber: '',
    installedDate: new Date().toISOString().split('T')[0]
  };

  const deviceTypes = [
    { value: 'MAIN', label: '메인 POS', description: '주요 계산대 POS' },
    { value: 'SUB', label: '서브 POS', description: '보조 계산대 POS' },
    { value: 'MOBILE', label: '모바일 POS', description: '이동식 POS' },
    { value: 'KIOSK', label: '키오스크', description: '셀프 주문 키오스크' }
  ];

  onMount(() => {
    tabStore.setActiveTab('BUSINESS_POS');
    loadAvailableStores();
    loadPosDevices();
  });

  async function loadAvailableStores() {
    if (!authToken) {
      console.warn('인증 토큰이 없습니다.');
      return;
    }

    try {
      console.log('🏪 사용 가능한 매장 목록 조회 중...');
      const response = await storeApi.getStores({}, authToken);
      
      if (response && response.stores) {
        availableStores = response.stores;
        console.log('✅ 매장 목록 로드 완료:', availableStores.length, '개');
      } else {
        console.warn('⚠️ 응답에 stores 필드가 없습니다:', response);
        availableStores = [];
      }
    } catch (error) {
      console.error('❌ 매장 목록 로드 실패:', error);
      toastStore.error('매장 목록을 불러오는데 실패했습니다: ' + error.message);
      availableStores = [];
    }
  }

  async function loadPosDevices() {
    if (!authToken) {
      console.warn('인증 토큰이 없습니다.');
      return;
    }

    try {
      loading = true;
      console.log('🖥️ POS 기기 목록 조회 중...');
      
      const response = await posApi.getPosSystems({}, authToken);
      
      if (response && response.posSystems) {
        posDevices = response.posSystems;
        console.log('✅ POS 목록 로드 완료:', posDevices.length, '개');
      } else {
        console.warn('⚠️ 응답에 posSystems 필드가 없습니다:', response);
        posDevices = [];
      }
    } catch (error) {
      console.error('❌ POS 목록 로드 실패:', error);
      toastStore.error('POS 목록을 불러오는데 실패했습니다: ' + error.message);
      posDevices = [];
    } finally {
      loading = false;
    }
  }

  async function createPosDevice() {
    if (!authToken) {
      toastStore.error('인증이 필요합니다.');
      return;
    }

    try {
      console.log('🖥️ POS 기기 생성 중:', posForm.posName);
      
      const response = await posApi.createPosSystem(posForm, authToken);
      
      if (response) {
        // 목록에 추가
        posDevices = [...posDevices, response];
        
        toastStore.success('POS 기기가 성공적으로 생성되었습니다.');
        showCreateModal = false;
        resetForm();
        console.log('✅ POS 기기 생성 완료');
      }
    } catch (error) {
      console.error('❌ POS 기기 생성 실패:', error);
      toastStore.error('POS 기기 생성에 실패했습니다: ' + error.message);
    }
  }

  async function startMaintenance(device) {
    if (!authToken) {
      toastStore.error('인증이 필요합니다.');
      return;
    }

    try {
      console.log('🔧 POS 점검 시작:', device.posName);
      
      await posApi.startMaintenance(device.posId, authToken);
      
      // 로컬 상태 업데이트
      const deviceIndex = posDevices.findIndex(p => p.posId === device.posId);
      if (deviceIndex !== -1) {
        posDevices[deviceIndex] = { ...posDevices[deviceIndex], posStatus: 'MAINTENANCE' };
        posDevices = [...posDevices];
      }
      
      toastStore.success('POS 점검이 시작되었습니다.');
      console.log('✅ POS 점검 시작 완료');
    } catch (error) {
      console.error('❌ POS 점검 시작 실패:', error);
      toastStore.error('POS 점검 시작에 실패했습니다: ' + error.message);
    }
  }

  async function completeMaintenance(device) {
    if (!authToken) {
      toastStore.error('인증이 필요합니다.');
      return;
    }

    try {
      console.log('✅ POS 점검 완료:', device.posName);
      
      await posApi.completeMaintenance(device.posId, authToken);
      
      // 로컬 상태 업데이트
      const deviceIndex = posDevices.findIndex(p => p.posId === device.posId);
      if (deviceIndex !== -1) {
        posDevices[deviceIndex] = { ...posDevices[deviceIndex], posStatus: 'ACTIVE' };
        posDevices = [...posDevices];
      }
      
      toastStore.success('POS 점검이 완료되었습니다.');
      console.log('✅ POS 점검 완료');
    } catch (error) {
      console.error('❌ POS 점검 완료 실패:', error);
      toastStore.error('POS 점검 완료에 실패했습니다: ' + error.message);
    }
  }

  function resetForm() {
    posForm = {
      storeId: '',
      posNumber: 1,
      posName: '',
      posType: 'MAIN',
      ipAddress: '',
      macAddress: '',
      serialNumber: '',
      installedDate: new Date().toISOString().split('T')[0]
    };
  }

  function getDeviceTypeColor(type) {
    const colors = {
      'MAIN': 'bg-blue-100 text-blue-800',
      'SUB': 'bg-green-100 text-green-800',
      'MOBILE': 'bg-purple-100 text-purple-800',
      'KIOSK': 'bg-orange-100 text-orange-800'
    };
    return colors[type] || 'bg-gray-100 text-gray-800';
  }

  function getStatusColor(status) {
    const colors = {
      'ACTIVE': 'bg-green-100 text-green-800',
      'INACTIVE': 'bg-red-100 text-red-800',
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

  function editPosDevice(device) {
    console.log('Edit POS device:', device);
    toastStore.info('POS 편집 기능은 준비 중입니다.');
  }

  function deletePosDevice(device) {
    if (!confirm(`정말로 "${device.posName}" POS를 삭제하시겠습니까?`)) {
      return;
    }
    
    console.log('Delete POS device:', device);
    toastStore.info('POS 삭제 기능은 준비 중입니다.');
  }

  function togglePosStatus(device) {
    if (device.posStatus === 'MAINTENANCE') {
      completeMaintenance(device);
    } else {
      startMaintenance(device);
    }
  }

  function getStoreName(storeId) {
    const store = availableStores.find(s => s.storeId === storeId || s.id === storeId);
    return store?.storeName || store?.name || '알 수 없음';
  }

  // 통계 계산
  $: totalPos = posDevices.length;
  $: activePos = posDevices.filter(p => p.posStatus === 'ACTIVE').length;
  $: inactivePos = posDevices.filter(p => p.posStatus === 'INACTIVE').length;
  $: maintenancePos = posDevices.filter(p => p.posStatus === 'MAINTENANCE').length;
</script>

<svelte:head>
  <title>POS 관리 - WebPos</title>
</svelte:head>

<div class="space-y-6">
  <!-- 헤더 -->
  <div class="flex items-center justify-between">
    <div>
      <h1 class="text-2xl font-bold text-gray-900">POS 관리</h1>
      <p class="text-gray-600 mt-1">매장의 POS 기기를 관리합니다.</p>
      <div class="mt-2 p-3 bg-purple-50 border border-purple-200 rounded-lg">
        <p class="text-sm text-purple-800">
          <strong>매장 관리자 업무:</strong> 
          본사에서 매장이 생성된 후, 여기서 POS 기기를 추가하고 관리할 수 있습니다. 각 POS는 실제 판매 업무에 사용됩니다.
        </p>
      </div>
    </div>
    <button 
      type="button" 
      class="btn btn-primary"
      on:click={() => showCreateModal = true}
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
          <p class="text-2xl font-bold text-gray-900">{totalPos}</p>
        </div>
      </div>
    </div>

    <div class="card p-6">
      <div class="flex items-center">
        <div class="p-3 rounded-full bg-green-100">
          <Activity class="h-6 w-6 text-green-600" />
        </div>
        <div class="ml-4">
          <p class="text-sm font-medium text-gray-600">활성</p>
          <p class="text-2xl font-bold text-gray-900">{activePos}</p>
        </div>
      </div>
    </div>

    <div class="card p-6">
      <div class="flex items-center">
        <div class="p-3 rounded-full bg-red-100">
          <PowerOff class="h-6 w-6 text-red-600" />
        </div>
        <div class="ml-4">
          <p class="text-sm font-medium text-gray-600">비활성</p>
          <p class="text-2xl font-bold text-gray-900">{inactivePos}</p>
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
          <p class="text-2xl font-bold text-gray-900">{maintenancePos}</p>
        </div>
      </div>
    </div>
  </div>

  <!-- POS 목록 -->
  <div class="card overflow-hidden">
    <div class="px-6 py-4 border-b border-gray-200">
      <h3 class="text-lg font-medium text-gray-900">POS 기기 목록</h3>
    </div>

    {#if loading}
      <div class="p-12 text-center">
        <div class="animate-spin rounded-full h-8 w-8 border-b-2 border-primary-600 mx-auto"></div>
        <p class="mt-4 text-gray-600">로딩 중...</p>
      </div>
    {:else if posDevices.length === 0}
      <div class="p-12 text-center">
        <Monitor class="mx-auto h-12 w-12 text-gray-400" />
        <p class="mt-4 text-gray-500">등록된 POS 기기가 없습니다.</p>
        <button 
          type="button" 
          class="mt-4 btn btn-primary"
          on:click={() => showCreateModal = true}
        >
          <Plus size="16" class="mr-2" />
          첫 번째 POS 추가
        </button>
      </div>
    {:else}
      <div class="grid grid-cols-1 lg:grid-cols-2 xl:grid-cols-3 gap-6 p-6">
        {#each posDevices as device}
          <div class="card p-6 hover:shadow-md transition-shadow">
            <!-- POS 헤더 -->
            <div class="flex items-start justify-between mb-4">
              <div class="flex-1">
                <h3 class="text-lg font-semibold text-gray-900 mb-1">{device.posName || `POS ${device.posNumber}`}</h3>
                <p class="text-sm text-gray-600">{getStoreName(device.storeId)}</p>
              </div>
              <div class="flex flex-col items-end space-y-2">
                <span class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium {getStatusColor(device.posStatus)}">
                  {getStatusText(device.posStatus)}
                </span>
                <span class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium {getDeviceTypeColor(device.posType)}">
                  {deviceTypes.find(t => t.value === device.posType)?.label || device.posType}
                </span>
              </div>
            </div>

            <!-- POS 정보 -->
            <div class="space-y-3">
              <!-- POS ID -->
              <div class="flex items-center text-sm text-gray-600">
                <Monitor size="16" class="mr-2" />
                <span class="font-medium">ID:</span>
                <span class="ml-1 font-mono">{device.posId}</span>
              </div>

              <!-- 시리얼 번호 -->
              {#if device.serialNumber}
                <div class="flex items-center text-sm text-gray-600">
                  <Settings size="16" class="mr-2" />
                  <span class="font-medium">S/N:</span>
                  <span class="ml-1 font-mono">{device.serialNumber}</span>
                </div>
              {/if}

              <!-- IP 주소 -->
              {#if device.ipAddress}
                <div class="flex items-center text-sm text-gray-600">
                  <Activity size="16" class="mr-2" />
                  <span class="font-medium">IP:</span>
                  <span class="ml-1 font-mono">{device.ipAddress}</span>
                </div>
              {/if}

              <!-- MAC 주소 -->
              {#if device.macAddress}
                <div class="flex items-center text-sm text-gray-600">
                  <Settings size="16" class="mr-2" />
                  <span class="font-medium">MAC:</span>
                  <span class="ml-1 font-mono">{device.macAddress}</span>
                </div>
              {/if}
            </div>

            <!-- 설치일 -->
            <div class="mt-4 pt-4 border-t border-gray-200">
              <div class="flex items-center justify-between text-sm">
                <span class="text-gray-500">설치일</span>
                <span class="font-medium text-gray-900">
                  {device.installedDate ? new Date(device.installedDate).toLocaleDateString('ko-KR') : 'N/A'}
                </span>
              </div>
              {#if device.lastMaintenanceDate}
                <div class="flex items-center justify-between text-sm mt-1">
                  <span class="text-gray-500">최근 점검</span>
                  <span class="font-medium text-gray-900">
                    {new Date(device.lastMaintenanceDate).toLocaleDateString('ko-KR')}
                  </span>
                </div>
              {/if}
            </div>

            <!-- 액션 버튼 -->
            <div class="mt-4 flex justify-end space-x-2">
              <button
                type="button"
                class="text-yellow-600 hover:text-yellow-900"
                on:click={() => togglePosStatus(device)}
                title={device.posStatus === 'MAINTENANCE' ? '점검 완료' : '점검 시작'}
              >
                {#if device.posStatus === 'MAINTENANCE'}
                  <Power size="16" />
                {:else}
                  <Settings size="16" />
                {/if}
              </button>
              <button
                type="button"
                class="text-indigo-600 hover:text-indigo-900"
                on:click={() => editPosDevice(device)}
                title="편집"
              >
                <Edit size="16" />
              </button>
              <button
                type="button"
                class="text-red-600 hover:text-red-900"
                on:click={() => deletePosDevice(device)}
                title="삭제"
              >
                <Trash2 size="16" />
              </button>
            </div>
          </div>
        {/each}
      </div>
    {/if}
  </div>
</div>

<!-- POS 생성 모달 -->
<Modal bind:open={showCreateModal} title="POS 추가" size="lg">
  <form on:submit|preventDefault={createPosDevice} class="space-y-6">
    <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
      <div>
        <label for="storeId" class="block text-sm font-medium text-gray-700">매장 *</label>
        <select
          id="storeId"
          required
          bind:value={posForm.storeId}
          class="mt-1 input"
        >
          <option value="">매장을 선택하세요</option>
          {#each availableStores as store}
            <option value={store.storeId || store.id}>{store.storeName || store.name}</option>
          {/each}
        </select>
      </div>
      <div>
        <label for="posNumber" class="block text-sm font-medium text-gray-700">POS 번호 *</label>
        <input
          id="posNumber"
          type="number"
          required
          min="1"
          bind:value={posForm.posNumber}
          class="mt-1 input"
          placeholder="1"
        />
      </div>
    </div>

    <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
      <div>
        <label for="posName" class="block text-sm font-medium text-gray-700">POS 이름</label>
        <input
          id="posName"
          type="text"
          bind:value={posForm.posName}
          class="mt-1 input"
          placeholder="메인 POS"
        />
      </div>
      <div>
        <label for="posType" class="block text-sm font-medium text-gray-700">POS 유형 *</label>
        <select
          id="posType"
          required
          bind:value={posForm.posType}
          class="mt-1 input"
        >
          {#each deviceTypes as type}
            <option value={type.value}>{type.label}</option>
          {/each}
        </select>
        <p class="mt-1 text-sm text-gray-500">
          {deviceTypes.find(t => t.value === posForm.posType)?.description}
        </p>
      </div>
    </div>

    <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
      <div>
        <label for="ipAddress" class="block text-sm font-medium text-gray-700">IP 주소</label>
        <input
          id="ipAddress"
          type="text"
          bind:value={posForm.ipAddress}
          class="mt-1 input"
          placeholder="192.168.1.100"
        />
      </div>
      <div>
        <label for="macAddress" class="block text-sm font-medium text-gray-700">MAC 주소</label>
        <input
          id="macAddress"
          type="text"
          bind:value={posForm.macAddress}
          class="mt-1 input"
          placeholder="00:11:22:33:44:55"
        />
      </div>
    </div>

    <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
      <div>
        <label for="serialNumber" class="block text-sm font-medium text-gray-700">시리얼 번호</label>
        <input
          id="serialNumber"
          type="text"
          bind:value={posForm.serialNumber}
          class="mt-1 input"
          placeholder="POS001234567"
        />
      </div>
      <div>
        <label for="installedDate" class="block text-sm font-medium text-gray-700">설치일</label>
        <input
          id="installedDate"
          type="date"
          bind:value={posForm.installedDate}
          class="mt-1 input"
        />
      </div>
    </div>

    <div class="flex justify-end space-x-3 pt-4">
      <button
        type="button"
        class="btn btn-secondary"
        on:click={() => showCreateModal = false}
      >
        취소
      </button>
      <button type="submit" class="btn btn-primary">
        POS 추가
      </button>
    </div>
  </form>
</Modal>
