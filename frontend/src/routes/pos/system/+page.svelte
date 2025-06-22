<script>
  import { onMount } from 'svelte';
  import { authStore } from '$lib/stores/auth.js';
  import { tabStore } from '$lib/stores/tabs.js';
  import { toastStore } from '$lib/stores/toast.js';
  import { Plus, Monitor, Wifi, WifiOff, Settings, Edit, Trash2, Activity } from 'lucide-svelte';
  import Modal from '$lib/components/Common/Modal.svelte';

  let posSystems = [];
  let loading = true;
  let showCreateModal = false;

  // POS 생성 폼
  let posForm = {
    posNumber: 1,
    posName: '',
    posType: 'MAIN',
    ipAddress: '',
    macAddress: '',
    serialNumber: ''
  };

  const posTypes = [
    { value: 'MAIN', label: '메인 POS', description: '주 결제 시스템' },
    { value: 'SUB', label: '서브 POS', description: '보조 결제 시스템' },
    { value: 'MOBILE', label: '모바일 POS', description: '모바일 결제 시스템' }
  ];

  onMount(() => {
    tabStore.setActiveTab('POS_SYSTEM');
    loadPosSystems();
  });

  async function loadPosSystems() {
    try {
      // 임시 데이터 - 실제로는 API 호출
      posSystems = [
        {
          posId: 'pos1',
          posNumber: 1,
          posName: '메인 카운터 POS',
          posType: 'MAIN',
          ipAddress: '192.168.1.100',
          macAddress: 'AA:BB:CC:DD:EE:FF',
          serialNumber: 'POS001',
          posStatus: 'ACTIVE',
          createdAt: new Date().toISOString()
        },
        {
          posId: 'pos2',
          posNumber: 2,
          posName: '서브 POS',
          posType: 'SUB',
          ipAddress: '192.168.1.101',
          macAddress: 'BB:CC:DD:EE:FF:AA',
          serialNumber: 'POS002',
          posStatus: 'MAINTENANCE',
          createdAt: new Date().toISOString()
        }
      ];
    } catch (error) {
      console.error('Failed to load POS systems:', error);
      toastStore.error('POS 시스템 목록을 불러오는데 실패했습니다.');
    } finally {
      loading = false;
    }
  }

  async function createPosSystem() {
    try {
      // 임시로 클라이언트에서 처리
      const newPos = {
        posId: `pos${Date.now()}`,
        ...posForm,
        posStatus: 'ACTIVE',
        createdAt: new Date().toISOString()
      };
      
      posSystems = [...posSystems, newPos];
      toastStore.success('POS 시스템이 성공적으로 추가되었습니다.');
      showCreateModal = false;
      resetForm();
    } catch (error) {
      console.error('Create POS system error:', error);
      toastStore.error('POS 시스템 추가에 실패했습니다.');
    }
  }

  function resetForm() {
    posForm = {
      posNumber: getNextPosNumber(),
      posName: '',
      posType: 'MAIN',
      ipAddress: '',
      macAddress: '',
      serialNumber: ''
    };
  }

  function getNextPosNumber() {
    if (posSystems.length === 0) return 1;
    return Math.max(...posSystems.map(pos => pos.posNumber)) + 1;
  }

  function getPosTypeColor(type) {
    const colors = {
      'MAIN': 'bg-blue-100 text-blue-800',
      'SUB': 'bg-green-100 text-green-800',
      'MOBILE': 'bg-purple-100 text-purple-800'
    };
    return colors[type] || 'bg-gray-100 text-gray-800';
  }

  function getStatusColor(status) {
    const colors = {
      'ACTIVE': 'bg-green-100 text-green-800',
      'INACTIVE': 'bg-gray-100 text-gray-800',
      'MAINTENANCE': 'bg-yellow-100 text-yellow-800'
    };
    return colors[status] || 'bg-gray-100 text-gray-800';
  }

  function editPosSystem(pos) {
    console.log('Edit POS system:', pos);
  }

  function deletePosSystem(pos) {
    console.log('Delete POS system:', pos);
  }

  function togglePosStatus(pos) {
    const newStatus = pos.posStatus === 'ACTIVE' ? 'INACTIVE' : 'ACTIVE';
    posSystems = posSystems.map(p => 
      p.posId === pos.posId ? { ...p, posStatus: newStatus } : p
    );
    toastStore.success(`POS가 ${newStatus === 'ACTIVE' ? '활성화' : '비활성화'}되었습니다.`);
  }

  function performMaintenance(pos) {
    const newStatus = pos.posStatus === 'MAINTENANCE' ? 'ACTIVE' : 'MAINTENANCE';
    posSystems = posSystems.map(p => 
      p.posId === pos.posId ? { ...p, posStatus: newStatus } : p
    );
    toastStore.success(`POS가 ${newStatus === 'MAINTENANCE' ? '점검 모드로' : '정상 운영으로'} 변경되었습니다.`);
  }

  // 폼 검증
  $: isFormValid = posForm.posNumber > 0 && posForm.posType;
  
  // 모달이 열릴 때 POS 번호 자동 설정
  $: if (showCreateModal) {
    posForm.posNumber = getNextPosNumber();
  }
</script>

<svelte:head>
  <title>POS 시스템 관리 - WebPos</title>
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
          <p class="text-sm font-medium text-gray-600">총 POS</p>
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
          <p class="text-sm font-medium text-gray-600">활성 POS</p>
          <p class="text-2xl font-bold text-gray-900">
            {posSystems.filter(pos => pos.posStatus === 'ACTIVE').length}
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
            {posSystems.filter(pos => pos.posStatus === 'MAINTENANCE').length}
          </p>
        </div>
      </div>
    </div>

    <div class="card p-6">
      <div class="flex items-center">
        <div class="p-3 rounded-full bg-red-100">
          <WifiOff class="h-6 w-6 text-red-600" />
        </div>
        <div class="ml-4">
          <p class="text-sm font-medium text-gray-600">비활성</p>
          <p class="text-2xl font-bold text-gray-900">
            {posSystems.filter(pos => pos.posStatus === 'INACTIVE').length}
          </p>
        </div>
      </div>
    </div>
  </div>

  <!-- POS 시스템 목록 -->
  <div class="card overflow-hidden">
    <div class="px-6 py-4 border-b border-gray-200">
      <h3 class="text-lg font-medium text-gray-900">POS 시스템 목록</h3>
    </div>

    {#if loading}
      <div class="p-12 text-center">
        <div class="animate-spin rounded-full h-8 w-8 border-b-2 border-primary-600 mx-auto"></div>
        <p class="mt-4 text-gray-600">로딩 중...</p>
      </div>
    {:else if posSystems.length === 0}
      <div class="p-12 text-center">
        <Monitor class="mx-auto h-12 w-12 text-gray-400" />
        <p class="mt-4 text-gray-500">등록된 POS 시스템이 없습니다.</p>
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
        {#each posSystems as pos}
          <div class="card p-6 hover:shadow-md transition-shadow">
            <!-- POS 헤더 -->
            <div class="flex items-start justify-between mb-4">
              <div class="flex-1">
                <h3 class="text-lg font-semibold text-gray-900 mb-1">
                  {pos.posName || `POS ${pos.posNumber}`}
                </h3>
                <p class="text-sm text-gray-600">POS #{pos.posNumber}</p>
              </div>
              <div class="flex flex-col items-end space-y-2">
                <span class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium {getStatusColor(pos.posStatus)}">
                  {pos.posStatus === 'ACTIVE' ? '활성' : 
                   pos.posStatus === 'INACTIVE' ? '비활성' : '점검중'}
                </span>
                <span class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium {getPosTypeColor(pos.posType)}">
                  {posTypes.find(t => t.value === pos.posType)?.label}
                </span>
              </div>
            </div>

            <!-- POS 정보 -->
            <div class="space-y-3">
              <!-- IP 주소 -->
              {#if pos.ipAddress}
                <div class="flex items-center text-sm text-gray-600">
                  <Wifi size="16" class="mr-2" />
                  <span>IP: {pos.ipAddress}</span>
                </div>
              {/if}

              <!-- MAC 주소 -->
              {#if pos.macAddress}
                <div class="flex items-center text-sm text-gray-600">
                  <Monitor size="16" class="mr-2" />
                  <span>MAC: {pos.macAddress}</span>
                </div>
              {/if}

              <!-- 시리얼 번호 -->
              {#if pos.serialNumber}
                <div class="flex items-center text-sm text-gray-600">
                  <Settings size="16" class="mr-2" />
                  <span>S/N: {pos.serialNumber}</span>
                </div>
              {/if}

              <!-- 설치일 -->
              <div class="flex items-center text-sm text-gray-600">
                <Activity size="16" class="mr-2" />
                <span>설치: {new Date(pos.createdAt).toLocaleDateString('ko-KR')}</span>
              </div>
            </div>

            <!-- 액션 버튼 -->
            <div class="mt-4 flex justify-end space-x-2">
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
                class="text-yellow-600 hover:text-yellow-900"
                on:click={() => performMaintenance(pos)}
                title={pos.posStatus === 'MAINTENANCE' ? '점검 완료' : '점검 시작'}
              >
                <Settings size="16" />
              </button>
              <button
                type="button"
                class="text-green-600 hover:text-green-900"
                on:click={() => togglePosStatus(pos)}
                title={pos.posStatus === 'ACTIVE' ? '비활성화' : '활성화'}
              >
                {#if pos.posStatus === 'ACTIVE'}
                  <WifiOff size="16" />
                {:else}
                  <Wifi size="16" />
                {/if}
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
          </div>
        {/each}
      </div>
    {/if}
  </div>
</div>

<!-- POS 시스템 추가 모달 -->
<Modal bind:open={showCreateModal} title="POS 시스템 추가" size="md">
  <form on:submit|preventDefault={createPosSystem} class="space-y-6">
    <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
      <div>
        <label for="posNumber" class="block text-sm font-medium text-gray-700">POS 번호 *</label>
        <input
          id="posNumber"
          type="number"
          min="1"
          required
          bind:value={posForm.posNumber}
          class="mt-1 input"
          placeholder="1"
        />
      </div>
      <div>
        <label for="posType" class="block text-sm font-medium text-gray-700">POS 타입 *</label>
        <select
          id="posType"
          required
          bind:value={posForm.posType}
          class="mt-1 input"
        >
          {#each posTypes as type}
            <option value={type.value}>{type.label}</option>
          {/each}
        </select>
      </div>
    </div>

    <div>
      <label for="posName" class="block text-sm font-medium text-gray-700">POS 이름</label>
      <input
        id="posName"
        type="text"
        bind:value={posForm.posName}
        class="mt-1 input"
        placeholder="예: 카운터 POS"
      />
      <p class="mt-1 text-sm text-gray-500">비워두면 'POS {posForm.posNumber}'로 자동 설정됩니다.</p>
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
          placeholder="AA:BB:CC:DD:EE:FF"
        />
      </div>
    </div>

    <div>
      <label for="serialNumber" class="block text-sm font-medium text-gray-700">시리얼 번호</label>
      <input
        id="serialNumber"
        type="text"
        bind:value={posForm.serialNumber}
        class="mt-1 input"
        placeholder="장비 시리얼 번호"
      />
    </div>

    <div class="bg-blue-50 border border-blue-200 rounded-lg p-4">
      <h4 class="text-sm font-medium text-blue-900 mb-2">
        {posTypes.find(t => t.value === posForm.posType)?.label}
      </h4>
      <p class="text-sm text-blue-700">
        {posTypes.find(t => t.value === posForm.posType)?.description}
      </p>
    </div>

    <div class="flex justify-end space-x-3 pt-4">
      <button
        type="button"
        class="btn btn-secondary"
        on:click={() => showCreateModal = false}
      >
        취소
      </button>
      <button 
        type="submit" 
        class="btn btn-primary"
        disabled={!isFormValid}
      >
        POS 추가
      </button>
    </div>
  </form>
</Modal>
