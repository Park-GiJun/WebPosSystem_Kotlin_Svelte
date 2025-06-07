<script>
  import { onMount } from 'svelte';
  import { authStore } from '$lib/stores/auth.js';
  import { tabStore } from '$lib/stores/tabs.js';
  import { toastStore } from '$lib/stores/toast.js';
  import { Plus, Store, Building2, MapPin, Phone, Calendar, Users, Edit, Trash2 } from 'lucide-svelte';
  import Modal from '$lib/components/Common/Modal.svelte';

  let stores = [];
  let loading = true;
  let showCreateModal = false;

  // 가맹점 생성 폼
  let storeForm = {
    storeName: '',
    operationType: 'FRANCHISE',
    regionCode: '001',
    storeNumber: '',
    businessLicense: '',
    ownerName: '',
    phoneNumber: '',
    address: '',
    postalCode: '',
    openingDate: new Date().toISOString().split('T')[0],
    managerUsername: '',
    managerEmail: '',
    managerPassword: ''
  };

  const operationTypes = [
    { value: 'DIRECT', label: '직영점', color: 'bg-blue-100 text-blue-800' },
    { value: 'FRANCHISE', label: '가맹점', color: 'bg-green-100 text-green-800' }
  ];

  const regions = [
    { code: '001', name: '서울특별시' },
    { code: '002', name: '부산광역시' },
    { code: '003', name: '대구광역시' },
    { code: '004', name: '인천광역시' },
    { code: '005', name: '광주광역시' },
    { code: '006', name: '대전광역시' },
    { code: '007', name: '울산광역시' },
    { code: '008', name: '세종특별자치시' },
    { code: '009', name: '경기도' },
    { code: '010', name: '강원도' },
    { code: '011', name: '충청북도' },
    { code: '012', name: '충청남도' },
    { code: '013', name: '전라북도' },
    { code: '014', name: '전라남도' },
    { code: '015', name: '경상북도' },
    { code: '016', name: '경상남도' },
    { code: '017', name: '제주특별자치도' }
  ];

  onMount(() => {
    tabStore.setActiveTab('BUSINESS_HQ_STORES');
    loadStores();
  });

  async function loadStores() {
    try {
      const response = await fetch('/api/v1/business/headquarters/my-stores', {
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

  async function createStore() {
    try {
      const response = await fetch('/api/v1/business/headquarters/stores', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${$authStore.token}`
        },
        body: JSON.stringify(storeForm)
      });

      if (response.ok) {
        toastStore.success('매장이 성공적으로 생성되었습니다.');
        showCreateModal = false;
        resetForm();
        await loadStores();
      } else {
        const error = await response.json();
        throw new Error(error.message || '매장 생성에 실패했습니다.');
      }
    } catch (error) {
      console.error('Create store error:', error);
      toastStore.error(error.message);
    }
  }

  function resetForm() {
    storeForm = {
      storeName: '',
      operationType: 'FRANCHISE',
      regionCode: '001',
      storeNumber: '',
      businessLicense: '',
      ownerName: '',
      phoneNumber: '',
      address: '',
      postalCode: '',
      openingDate: new Date().toISOString().split('T')[0],
      managerUsername: '',
      managerEmail: '',
      managerPassword: ''
    };
  }

  function getOperationTypeColor(type) {
    const config = operationTypes.find(t => t.value === type);
    return config?.color || 'bg-gray-100 text-gray-800';
  }

  function getStatusColor(status) {
    const colors = {
      'ACTIVE': 'bg-green-100 text-green-800',
      'INACTIVE': 'bg-gray-100 text-gray-800',
      'CLOSED': 'bg-red-100 text-red-800'
    };
    return colors[status] || 'bg-gray-100 text-gray-800';
  }

  function editStore(store) {
    console.log('Edit store:', store);
  }

  function deleteStore(store) {
    console.log('Delete store:', store);
  }
</script>

<svelte:head>
  <title>가맹점 관리 - WebPos</title>
</svelte:head>



<div class="space-y-6">
  <!-- 헤더 -->
  <div class="flex items-center justify-between">
    <div>
      <h1 class="text-2xl font-bold text-gray-900">가맹점 관리</h1>
      <p class="text-gray-600 mt-1">우리 본사의 가맹점과 직영점을 관리합니다.</p>
      <div class="mt-2 p-3 bg-green-50 border border-green-200 rounded-lg">
        <p class="text-sm text-green-800">
          <strong>본사 관리자 업무:</strong> 
          여기서 매장을 생성하면 매장 관리자 계정이 자동으로 생성됩니다. 매장 관리자는 POS 시스템에서 기기를 추가할 수 있습니다.
        </p>
      </div>
    </div>
    <button 
      type="button" 
      class="btn btn-primary"
      on:click={() => showCreateModal = true}
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
          <Store class="h-6 w-6 text-yellow-600" />
        </div>
        <div class="ml-4">
          <p class="text-sm font-medium text-gray-600">운영중</p>
          <p class="text-2xl font-bold text-gray-900">
            {stores.filter(s => s.storeStatus === 'ACTIVE').length}
          </p>
        </div>
      </div>
    </div>
  </div>

  <!-- 매장 목록 -->
  <div class="card overflow-hidden">
    <div class="px-6 py-4 border-b border-gray-200">
      <h3 class="text-lg font-medium text-gray-900">매장 목록</h3>
    </div>

    {#if loading}
      <div class="p-12 text-center">
        <div class="animate-spin rounded-full h-8 w-8 border-b-2 border-primary-600 mx-auto"></div>
        <p class="mt-4 text-gray-600">로딩 중...</p>
      </div>
    {:else if stores.length === 0}
      <div class="p-12 text-center">
        <Store class="mx-auto h-12 w-12 text-gray-400" />
        <p class="mt-4 text-gray-500">등록된 매장이 없습니다.</p>
        <button 
          type="button" 
          class="mt-4 btn btn-primary"
          on:click={() => showCreateModal = true}
        >
          <Plus size="16" class="mr-2" />
          첫 번째 매장 추가
        </button>
      </div>
    {:else}
      <div class="grid grid-cols-1 lg:grid-cols-2 xl:grid-cols-3 gap-6 p-6">
        {#each stores as store}
          <div class="card p-6 hover:shadow-md transition-shadow">
            <!-- 매장 헤더 -->
            <div class="flex items-start justify-between mb-4">
              <div class="flex-1">
                <h3 class="text-lg font-semibold text-gray-900 mb-1">{store.storeName}</h3>
                <p class="text-sm text-gray-600">{store.storeNumber}</p>
              </div>
              <div class="flex flex-col items-end space-y-2">
                <span class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium {getStatusColor(store.storeStatus)}">
                  {store.storeStatus === 'ACTIVE' ? '운영중' : store.storeStatus === 'INACTIVE' ? '운영중지' : '폐점'}
                </span>
                <span class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium {getOperationTypeColor(store.operationType)}">
                  {operationTypes.find(t => t.value === store.operationType)?.label}
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

              <!-- 지역 -->
              <div class="flex items-center text-sm text-gray-600">
                <Building2 size="16" class="mr-2" />
                <span>{regions.find(r => r.code === store.regionCode)?.name}</span>
              </div>
            </div>

            <!-- 매장 관리자 정보 -->
            <div class="mt-4 pt-4 border-t border-gray-200">
              <div class="flex items-center justify-between text-sm">
                <span class="text-gray-500">매장 관리자</span>
                <span class="font-medium text-gray-900">{store.managerUser.username}</span>
              </div>
            </div>

            <!-- 액션 버튼 -->
            <div class="mt-4 flex justify-end space-x-2">
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
          </div>
        {/each}
      </div>
    {/if}
  </div>
</div>

<!-- 매장 생성 모달 -->
<Modal bind:open={showCreateModal} title="매장 추가" size="lg">
  <form on:submit|preventDefault={createStore} class="space-y-6">
    <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
      <div>
        <label for="storeName" class="block text-sm font-medium text-gray-700">매장명 *</label>
        <input
          id="storeName"
          type="text"
          required
          bind:value={storeForm.storeName}
          class="mt-1 input"
          placeholder="매장명을 입력하세요"
        />
      </div>
      <div>
        <label for="operationType" class="block text-sm font-medium text-gray-700">운영형태 *</label>
        <select
          id="operationType"
          required
          bind:value={storeForm.operationType}
          class="mt-1 input"
        >
          {#each operationTypes as type}
            <option value={type.value}>{type.label}</option>
          {/each}
        </select>
      </div>
    </div>

    <div class="grid grid-cols-1 md:grid-cols-3 gap-4">
      <div>
        <label for="regionCode" class="block text-sm font-medium text-gray-700">지역 *</label>
        <select
          id="regionCode"
          required
          bind:value={storeForm.regionCode}
          class="mt-1 input"
        >
          {#each regions as region}
            <option value={region.code}>{region.name}</option>
          {/each}
        </select>
      </div>
      <div>
        <label for="storeNumber" class="block text-sm font-medium text-gray-700">매장번호 *</label>
        <input
          id="storeNumber"
          type="text"
          required
          bind:value={storeForm.storeNumber}
          class="mt-1 input"
          placeholder="001"
        />
      </div>
      <div>
        <label for="openingDate" class="block text-sm font-medium text-gray-700">개점일</label>
        <input
          id="openingDate"
          type="date"
          bind:value={storeForm.openingDate}
          class="mt-1 input"
        />
      </div>
    </div>

    <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
      <div>
        <label for="ownerName" class="block text-sm font-medium text-gray-700">점주명 *</label>
        <input
          id="ownerName"
          type="text"
          required
          bind:value={storeForm.ownerName}
          class="mt-1 input"
          placeholder="점주명을 입력하세요"
        />
      </div>
      <div>
        <label for="businessLicense" class="block text-sm font-medium text-gray-700">사업자등록번호</label>
        <input
          id="businessLicense"
          type="text"
          bind:value={storeForm.businessLicense}
          class="mt-1 input"
          placeholder="000-00-00000"
        />
      </div>
    </div>

    <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
      <div>
        <label for="phoneNumber" class="block text-sm font-medium text-gray-700">연락처</label>
        <input
          id="phoneNumber"
          type="tel"
          bind:value={storeForm.phoneNumber}
          class="mt-1 input"
          placeholder="02-000-0000"
        />
      </div>
      <div>
        <label for="postalCode" class="block text-sm font-medium text-gray-700">우편번호</label>
        <input
          id="postalCode"
          type="text"
          bind:value={storeForm.postalCode}
          class="mt-1 input"
          placeholder="12345"
        />
      </div>
    </div>

    <div>
      <label for="address" class="block text-sm font-medium text-gray-700">주소</label>
      <input
        id="address"
        type="text"
        bind:value={storeForm.address}
        class="mt-1 input"
        placeholder="매장 주소를 입력하세요"
      />
    </div>

    <hr class="border-gray-200" />

    <h4 class="text-lg font-medium text-gray-900">매장 관리자 계정</h4>

    <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
      <div>
        <label for="managerUsername" class="block text-sm font-medium text-gray-700">관리자 아이디 *</label>
        <input
          id="managerUsername"
          type="text"
          required
          bind:value={storeForm.managerUsername}
          class="mt-1 input"
          placeholder="관리자 아이디"
        />
      </div>
      <div>
        <label for="managerEmail" class="block text-sm font-medium text-gray-700">관리자 이메일 *</label>
        <input
          id="managerEmail"
          type="email"
          required
          bind:value={storeForm.managerEmail}
          class="mt-1 input"
          placeholder="manager@example.com"
        />
      </div>
    </div>

    <div>
      <label for="managerPassword" class="block text-sm font-medium text-gray-700">관리자 비밀번호 *</label>
      <input
        id="managerPassword"
        type="password"
        required
        bind:value={storeForm.managerPassword}
        class="mt-1 input"
        placeholder="비밀번호"
      />
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
        매장 추가
      </button>
    </div>
  </form>
</Modal>
