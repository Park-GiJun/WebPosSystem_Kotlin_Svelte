<script>
  import { onMount } from 'svelte';
  import { authStore } from '$lib/stores/auth.js';
  import { tabStore } from '$lib/stores/tabs.js';
  import { toastStore } from '$lib/stores/toast.js';
  import { Plus, Monitor, Settings, Activity, Edit, Trash2, Power, PowerOff } from 'lucide-svelte';
  import Modal from '$lib/components/Common/Modal.svelte';

  let posDevices = [];
  let loading = true;
  let showCreateModal = false;

  // POS 생성 폼
  let posForm = {
    posName: '',
    storeId: '',
    deviceType: 'TERMINAL',
    serialNumber: '',
    macAddress: '',
    ipAddress: '',
    location: '',
    description: ''
  };

  // 사용 가능한 매장 목록 (사용자 권한에 따라)
  let availableStores = [];

  const deviceTypes = [
    { value: 'TERMINAL', label: 'POS 터미널', description: '일반 POS 단말기' },
    { value: 'TABLET', label: '태블릿 POS', description: '태블릿 형태의 POS' },
    { value: 'MOBILE', label: '모바일 POS', description: '모바일 기기용 POS' },
    { value: 'KIOSK', label: '키오스크', description: '셀프 주문 키오스크' }
  ];

  onMount(() => {
    tabStore.setActiveTab('BUSINESS_POS');
    loadAvailableStores();
    loadPosDevices();
  });

  async function loadAvailableStores() {
    try {
      const user = $authStore.user;
      let endpoint = '';
      
      // 사용자 권한에 따라 다른 엔드포인트 사용
      if (user.roles.includes('SUPER_ADMIN') || user.roles.includes('SYSTEM_ADMIN')) {
        endpoint = '/api/v1/admin/stores'; // 모든 매장
      } else if (user.roles.includes('HQ_MANAGER')) {
        endpoint = '/api/v1/business/headquarters/my-stores'; // 자신의 본사 매장만
      } else {
        endpoint = '/api/v1/business/stores/my-store'; // 자신의 매장만
      }

      const response = await fetch(endpoint, {
        headers: {
          'Authorization': `Bearer ${$authStore.token}`
        }
      });
      
      if (response.ok) {
        const data = await response.json();
        availableStores = data.stores || (data.store ? [data.store] : []);
      }
    } catch (error) {
      console.error('Failed to load stores:', error);
      toastStore.error('매장 목록을 불러오는데 실패했습니다.');
    }
  }

  async function loadPosDevices() {
    try {
      const user = $authStore.user;
      let endpoint = '';
      
      // 사용자 권한에 따라 다른 엔드포인트 사용
      if (user.roles.includes('SUPER_ADMIN') || user.roles.includes('SYSTEM_ADMIN')) {
        endpoint = '/api/v1/admin/pos-devices'; // 모든 POS
      } else if (user.roles.includes('HQ_MANAGER')) {
        endpoint = '/api/v1/business/headquarters/pos-devices'; // 자신의 본사 POS만
      } else {
        endpoint = '/api/v1/business/stores/pos-devices'; // 자신의 매장 POS만
      }

      const response = await fetch(endpoint, {
        headers: {
          'Authorization': `Bearer ${$authStore.token}`
        }
      });
      
      if (response.ok) {
        const data = await response.json();
        posDevices = data.posDevices || [];
      } else {
        throw new Error('POS 목록을 불러올 수 없습니다.');
      }
    } catch (error) {
      console.error('Failed to load POS devices:', error);
      toastStore.error(error.message);
    } finally {
      loading = false;
    }
  }

  async function createPosDevice() {
    try {
      const response = await fetch('/api/v1/business/pos-devices', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${$authStore.token}`
        },
        body: JSON.stringify(posForm)
      });

      if (response.ok) {
        toastStore.success('POS 기기가 성공적으로 생성되었습니다.');
        showCreateModal = false;
        resetForm();
        await loadPosDevices();
      } else {
        const error = await response.json();
        throw new Error(error.message || 'POS 기기 생성에 실패했습니다.');
      }
    } catch (error) {
      console.error('Create POS error:', error);
      toastStore.error(error.message);
    }
  }

  function resetForm() {
    posForm = {
      posName: '',
      storeId: '',
      deviceType: 'TERMINAL',
      serialNumber: '',
      macAddress: '',
      ipAddress: '',
      location: '',
      description: ''
    };
  }

  function getDeviceTypeColor(type) {
    const colors = {
      'TERMINAL': 'bg-blue-100 text-blue-800',
      'TABLET': 'bg-green-100 text-green-800',
      'MOBILE': 'bg-purple-100 text-purple-800',
      'KIOSK': 'bg-orange-100 text-orange-800'
    };
    return colors[type] || 'bg-gray-100 text-gray-800';
  }

  function getStatusColor(status) {
    const colors = {
      'ONLINE': 'bg-green-100 text-green-800',
      'OFFLINE': 'bg-red-100 text-red-800',
      'MAINTENANCE': 'bg-yellow-100 text-yellow-800',
      'INACTIVE': 'bg-gray-100 text-gray-800'
    };
    return colors[status] || 'bg-gray-100 text-gray-800';
  }

  function editPosDevice(device) {
    console.log('Edit POS device:', device);
    // TODO: 편집 모달 구현
  }

  function deletePosDevice(device) {
    console.log('Delete POS device:', device);
    // TODO: 삭제 확인 모달 구현
  }

  function togglePosStatus(device) {
    console.log('Toggle POS status:', device);
    // TODO: 상태 변경 API 호출
  }

  function getStoreName(storeId) {
    const store = availableStores.find(s => s.storeId === storeId || s.id === storeId);
    return store?.storeName || store?.name || '알 수 없음';
  }
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
          <p class="text-2xl font-bold text-gray-900">{posDevices.length}</p>
        </div>
      </div>
    </div>

    <div class="card p-6">
      <div class="flex items-center">
        <div class="p-3 rounded-full bg-green-100">
          <Activity class="h-6 w-6 text-green-600" />
        </div>
        <div class="ml-4">
          <p class="text-sm font-medium text-gray-600">온라인</p>
          <p class="text-2xl font-bold text-gray-900">
            {posDevices.filter(p => p.status === 'ONLINE').length}
          </p>
        </div>
      </div>
    </div>

    <div class="card p-6">
      <div class="flex items-center">
        <div class="p-3 rounded-full bg-red-100">
          <PowerOff class="h-6 w-6 text-red-600" />
        </div>
        <div class="ml-4">
          <p class="text-sm font-medium text-gray-600">오프라인</p>
          <p class="text-2xl font-bold text-gray-900">
            {posDevices.filter(p => p.status === 'OFFLINE').length}
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
            {posDevices.filter(p => p.status === 'MAINTENANCE').length}
          </p>
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
                <h3 class="text-lg font-semibold text-gray-900 mb-1">{device.posName}</h3>
                <p class="text-sm text-gray-600">{getStoreName(device.storeId)}</p>
              </div>
              <div class="flex flex-col items-end space-y-2">
                <span class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium {getStatusColor(device.status)}">
                  {device.status === 'ONLINE' ? '온라인' : 
                   device.status === 'OFFLINE' ? '오프라인' :
                   device.status === 'MAINTENANCE' ? '점검중' : '비활성'}
                </span>
                <span class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium {getDeviceTypeColor(device.deviceType)}">
                  {deviceTypes.find(t => t.value === device.deviceType)?.label}
                </span>
              </div>
            </div>

            <!-- POS 정보 -->
            <div class="space-y-3">
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

              <!-- 위치 -->
              {#if device.location}
                <div class="flex items-center text-sm text-gray-600">
                  <Monitor size="16" class="mr-2" />
                  <span class="font-medium">위치:</span>
                  <span class="ml-1">{device.location}</span>
                </div>
              {/if}

              <!-- 설명 -->
              {#if device.description}
                <div class="text-sm text-gray-600">
                  <p class="line-clamp-2">{device.description}</p>
                </div>
              {/if}
            </div>

            <!-- 마지막 활동 시간 -->
            <div class="mt-4 pt-4 border-t border-gray-200">
              <div class="flex items-center justify-between text-sm">
                <span class="text-gray-500">마지막 활동</span>
                <span class="font-medium text-gray-900">
                  {device.lastActiveAt ? new Date(device.lastActiveAt).toLocaleString('ko-KR') : '없음'}
                </span>
              </div>
            </div>

            <!-- 액션 버튼 -->
            <div class="mt-4 flex justify-end space-x-2">
              <button
                type="button"
                class="text-green-600 hover:text-green-900"
                on:click={() => togglePosStatus(device)}
                title={device.status === 'ONLINE' ? '오프라인으로 전환' : '온라인으로 전환'}
              >
                {#if device.status === 'ONLINE'}
                  <PowerOff size="16" />
                {:else}
                  <Power size="16" />
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
        <label for="posName" class="block text-sm font-medium text-gray-700">POS 이름 *</label>
        <input
          id="posName"
          type="text"
          required
          bind:value={posForm.posName}
          class="mt-1 input"
          placeholder="POS 이름을 입력하세요"
        />
      </div>
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
    </div>

    <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
      <div>
        <label for="deviceType" class="block text-sm font-medium text-gray-700">기기 유형 *</label>
        <select
          id="deviceType"
          required
          bind:value={posForm.deviceType}
          class="mt-1 input"
        >
          {#each deviceTypes as type}
            <option value={type.value}>{type.label}</option>
          {/each}
        </select>
        <p class="mt-1 text-sm text-gray-500">
          {deviceTypes.find(t => t.value === posForm.deviceType)?.description}
        </p>
      </div>
      <div>
        <label for="location" class="block text-sm font-medium text-gray-700">설치 위치</label>
        <input
          id="location"
          type="text"
          bind:value={posForm.location}
          class="mt-1 input"
          placeholder="예: 카운터 1번"
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
          placeholder="시리얼 번호"
        />
      </div>
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

    <div>
      <label for="description" class="block text-sm font-medium text-gray-700">설명</label>
      <textarea
        id="description"
        bind:value={posForm.description}
        class="mt-1 input"
        rows="3"
        placeholder="POS 기기에 대한 추가 설명"
      ></textarea>
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
