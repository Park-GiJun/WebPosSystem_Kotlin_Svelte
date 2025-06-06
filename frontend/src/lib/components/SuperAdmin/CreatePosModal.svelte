<script>
  import { createEventDispatcher, onMount } from 'svelte';
  import { X, Monitor, MapPin, Settings, Wifi } from 'lucide-svelte';
  import Modal from '$lib/components/Common/Modal.svelte';
  import { posApi, storeApi } from '$lib/api/superAdmin.js';
  import { toastStore } from '$lib/stores/toast.js';

  export let open = false;

  const dispatch = createEventDispatcher();

  let form = {
    storeId: '',
    storeName: '',
    posNumber: 1,
    posName: '',
    posType: 'MAIN',
    ipAddress: '',
    macAddress: '',
    serialNumber: '',
    installedDate: ''
  };

  let loading = false;
  let loadingData = false;
  let errors = {};
  let stores = [];

  const posTypes = [
    { value: 'MAIN', label: '메인 POS' },
    { value: 'SUB', label: '서브 POS' },
    { value: 'MOBILE', label: '모바일 POS' }
  ];

  onMount(async () => {
    if (open) {
      await loadStores();
    }
  });

  $: if (open) {
    loadStores();
  }

  $: {
    // 매장이 선택되면 매장명 자동 설정
    if (form.storeId) {
      const selectedStore = stores.find(store => store.storeId === form.storeId);
      if (selectedStore) {
        form.storeName = selectedStore.storeName;
      }
    }
  }

  $: {
    // POS 번호나 타입이 변경되면 POS명 자동 생성
    if (form.posNumber && form.posType) {
      const typeLabel = posTypes.find(type => type.value === form.posType)?.label || form.posType;
      form.posName = `${typeLabel} ${form.posNumber}`;
    }
  }

  async function loadStores() {
    loadingData = true;
    
    try {
      const response = await storeApi.getStores();
      
      if (response.success && response.data) {
        stores = response.data.stores || response.data;
      } else if (response.error === 'API_NOT_FOUND') {
        // API가 없는 경우 더미 데이터
        stores = [
          {
            storeId: 'HQ001001',
            storeName: '강남점',
            storeType: 'CHAIN',
            hqName: '커피왕 본사',
            regionName: '서울특별시'
          },
          {
            storeId: 'HQ001002',
            storeName: '홍대점',
            storeType: 'CHAIN',
            hqName: '커피왕 본사',
            regionName: '서울특별시'
          },
          {
            storeId: 'IN002001',
            storeName: '개인카페 A',
            storeType: 'INDIVIDUAL',
            hqName: null,
            regionName: '부산광역시'
          }
        ];
      } else {
        console.warn('매장 정보 로드 실패:', response.message);
        stores = [];
        toastStore.error('매장 정보를 불러올 수 없습니다.');
      }
    } catch (error) {
      console.error('매장 로드 오류:', error);
      toastStore.error('매장 정보 로드 중 오류가 발생했습니다.');
      stores = [];
    } finally {
      loadingData = false;
    }
  }

  function validateForm() {
    errors = {};

    if (!form.storeId) {
      errors.storeId = '매장을 선택해주세요.';
    }

    if (!form.posNumber || form.posNumber < 1 || form.posNumber > 999) {
      errors.posNumber = 'POS 번호는 1-999 사이의 숫자여야 합니다.';
    }

    if (!form.posType) {
      errors.posType = 'POS 유형을 선택해주세요.';
    }

    if (form.ipAddress && !/^(\d{1,3}\.){3}\d{1,3}$/.test(form.ipAddress)) {
      errors.ipAddress = '올바른 IP 주소 형식이 아닙니다.';
    }

    if (form.macAddress && !/^([0-9A-Fa-f]{2}[:-]){5}([0-9A-Fa-f]{2})$/.test(form.macAddress)) {
      errors.macAddress = '올바른 MAC 주소 형식이 아닙니다. (예: 00:1B:44:11:3A:B7)';
    }

    return Object.keys(errors).length === 0;
  }

  async function handleSubmit() {
    if (!validateForm()) {
      return;
    }

    loading = true;

    try {
      const posData = {
        ...form,
        installedDate: form.installedDate || null
      };

      const response = await posApi.createPosSystem(posData);

      if (response.success) {
        dispatch('pos-created', response.data);
        handleClose();
        toastStore.success('POS 시스템이 성공적으로 생성되었습니다.');
      } else if (response.error === 'API_NOT_FOUND') {
        // API가 없는 경우 더미 데이터로 응답
        const dummyPos = {
          posId: generatePosId(form.storeId, form.posNumber),
          ...posData,
          lastMaintenanceDate: new Date().toISOString().split('T')[0],
          posStatus: 'ACTIVE',
          isActive: true,
          createdAt: new Date().toISOString(),
          updatedAt: new Date().toISOString()
        };
        
        dispatch('pos-created', dummyPos);
        handleClose();
        toastStore.success('POS 시스템이 성공적으로 생성되었습니다. (데모 모드)');
      } else {
        errors.submit = response.message || 'POS 시스템 생성에 실패했습니다.';
      }
    } catch (error) {
      console.error('POS 생성 오류:', error);
      errors.submit = 'POS 시스템 생성 중 오류가 발생했습니다.';
    } finally {
      loading = false;
    }
  }

  function generatePosId(storeId, posNumber) {
    const paddedPosNumber = posNumber.toString().padStart(2, '0');
    return `${storeId}P${paddedPosNumber}`;
  }

  function formatMacAddress(value) {
    // 콜론이나 하이픈 제거하고 숫자와 알파벳만 남김
    const clean = value.replace(/[^0-9A-Fa-f]/g, '');
    
    // 2자리씩 끊어서 콜론으로 연결
    const chunks = clean.match(/.{1,2}/g) || [];
    return chunks.slice(0, 6).join(':');
  }

  function handleMacAddressInput(event) {
    const formatted = formatMacAddress(event.target.value);
    form.macAddress = formatted;
  }

  function handleClose() {
    if (!loading) {
      open = false;
      form = {
        storeId: '',
        storeName: '',
        posNumber: 1,
        posName: '',
        posType: 'MAIN',
        ipAddress: '',
        macAddress: '',
        serialNumber: '',
        installedDate: ''
      };
      errors = {};
      dispatch('close');
    }
  }
</script>

<Modal bind:open title="POS 시스템 생성" size="lg" on:close={handleClose}>
  {#if loadingData}
    <div class="p-8 text-center">
      <div class="animate-spin rounded-full h-8 w-8 border-b-2 border-primary-600 mx-auto"></div>
      <p class="mt-4 text-gray-600">매장 정보를 불러오는 중...</p>
    </div>
  {:else}
    <form on:submit|preventDefault={handleSubmit} class="space-y-6">
      <!-- 매장 정보 섹션 -->
      <div class="bg-gray-50 p-4 rounded-lg">
        <h3 class="text-lg font-medium text-gray-900 mb-4 flex items-center">
          <MapPin class="mr-2 h-5 w-5" />
          매장 정보
        </h3>
        
        <div class="grid grid-cols-1 gap-4">
          <!-- 매장 선택 -->
          <div>
            <label for="storeId" class="block text-sm font-medium text-gray-700 mb-1">
              매장 <span class="text-red-500">*</span>
            </label>
            <select
              id="storeId"
              bind:value={form.storeId}
              class="input"
              class:border-red-300={errors.storeId}
              disabled={loading}
              required
            >
              <option value="">매장을 선택하세요</option>
              {#each stores as store}
                <option value={store.storeId}>
                  {store.storeName} ({store.storeId})
                  {#if store.hqName}
                    - {store.hqName}
                  {/if}
                </option>
              {/each}
            </select>
            {#if errors.storeId}
              <p class="text-sm text-red-600 mt-1">{errors.storeId}</p>
            {/if}
          </div>
        </div>
      </div>

      <!-- POS 기본 정보 섹션 -->
      <div class="bg-gray-50 p-4 rounded-lg">
        <h3 class="text-lg font-medium text-gray-900 mb-4 flex items-center">
          <Monitor class="mr-2 h-5 w-5" />
          POS 기본 정보
        </h3>
        
        <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
          <!-- POS 번호 -->
          <div>
            <label for="posNumber" class="block text-sm font-medium text-gray-700 mb-1">
              POS 번호 <span class="text-red-500">*</span>
            </label>
            <input
              id="posNumber"
              type="number"
              bind:value={form.posNumber}
              class="input"
              class:border-red-300={errors.posNumber}
              placeholder="1"
              min="1"
              max="999"
              disabled={loading}
              required
            />
            {#if errors.posNumber}
              <p class="text-sm text-red-600 mt-1">{errors.posNumber}</p>
            {/if}
          </div>

          <!-- POS 유형 -->
          <div>
            <label for="posType" class="block text-sm font-medium text-gray-700 mb-1">
              POS 유형 <span class="text-red-500">*</span>
            </label>
            <select
              id="posType"
              bind:value={form.posType}
              class="input"
              class:border-red-300={errors.posType}
              disabled={loading}
              required
            >
              {#each posTypes as type}
                <option value={type.value}>{type.label}</option>
              {/each}
            </select>
            {#if errors.posType}
              <p class="text-sm text-red-600 mt-1">{errors.posType}</p>
            {/if}
          </div>

          <!-- POS명 -->
          <div class="md:col-span-2">
            <label for="posName" class="block text-sm font-medium text-gray-700 mb-1">
              POS명
            </label>
            <input
              id="posName"
              type="text"
              bind:value={form.posName}
              class="input"
              placeholder="자동 생성됩니다"
              disabled={loading}
              readonly
            />
            <p class="text-xs text-gray-500 mt-1">POS 유형과 번호에 따라 자동 생성됩니다</p>
          </div>

          <!-- 설치일 -->
          <div class="md:col-span-2">
            <label for="installedDate" class="block text-sm font-medium text-gray-700 mb-1">
              설치일
            </label>
            <input
              id="installedDate"
              type="date"
              bind:value={form.installedDate}
              class="input"
              disabled={loading}
            />
          </div>
        </div>
      </div>

      <!-- 네트워크 정보 섹션 -->
      <div class="bg-gray-50 p-4 rounded-lg">
        <h3 class="text-lg font-medium text-gray-900 mb-4 flex items-center">
          <Wifi class="mr-2 h-5 w-5" />
          네트워크 정보
        </h3>
        
        <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
          <!-- IP 주소 -->
          <div>
            <label for="ipAddress" class="block text-sm font-medium text-gray-700 mb-1">
              IP 주소
            </label>
            <input
              id="ipAddress"
              type="text"
              bind:value={form.ipAddress}
              class="input"
              class:border-red-300={errors.ipAddress}
              placeholder="192.168.1.100"
              disabled={loading}
            />
            {#if errors.ipAddress}
              <p class="text-sm text-red-600 mt-1">{errors.ipAddress}</p>
            {/if}
          </div>

          <!-- MAC 주소 -->
          <div>
            <label for="macAddress" class="block text-sm font-medium text-gray-700 mb-1">
              MAC 주소
            </label>
            <input
              id="macAddress"
              type="text"
              value={form.macAddress}
              on:input={handleMacAddressInput}
              class="input"
              class:border-red-300={errors.macAddress}
              placeholder="00:1B:44:11:3A:B7"
              disabled={loading}
              maxlength="17"
            />
            {#if errors.macAddress}
              <p class="text-sm text-red-600 mt-1">{errors.macAddress}</p>
            {/if}
            <p class="text-xs text-gray-500 mt-1">자동으로 형식이 맞춰집니다</p>
          </div>
        </div>
      </div>

      <!-- 하드웨어 정보 섹션 -->
      <div class="bg-gray-50 p-4 rounded-lg">
        <h3 class="text-lg font-medium text-gray-900 mb-4 flex items-center">
          <Settings class="mr-2 h-5 w-5" />
          하드웨어 정보
        </h3>
        
        <div class="grid grid-cols-1 gap-4">
          <!-- 시리얼 번호 -->
          <div>
            <label for="serialNumber" class="block text-sm font-medium text-gray-700 mb-1">
              시리얼 번호
            </label>
            <input
              id="serialNumber"
              type="text"
              bind:value={form.serialNumber}
              class="input"
              placeholder="POS001234567"
              disabled={loading}
            />
          </div>
        </div>
      </div>

      <!-- POS ID 미리보기 -->
      {#if form.storeId && form.posNumber}
        <div class="bg-blue-50 border border-blue-200 rounded-lg p-4">
          <h4 class="text-sm font-medium text-blue-900 mb-2">생성될 POS ID</h4>
          <p class="text-lg font-mono text-blue-700">
            {generatePosId(form.storeId, form.posNumber)}
          </p>
        </div>
      {/if}

      <!-- 전체 폼 에러 -->
      {#if errors.submit}
        <div class="p-3 bg-red-50 border border-red-200 rounded-lg">
          <p class="text-sm text-red-700">{errors.submit}</p>
        </div>
      {/if}
    </form>
  {/if}

  <!-- 푸터 버튼들 -->
  <div slot="footer" class="flex justify-end space-x-3 p-4 border-t border-gray-200">
    <button
      type="button"
      class="btn btn-secondary"
      on:click={handleClose}
      disabled={loading || loadingData}
    >
      취소
    </button>
    <button
      type="submit"
      class="btn btn-primary"
      on:click={handleSubmit}
      disabled={loading || loadingData}
    >
      {#if loading}
        <div class="flex items-center">
          <div class="animate-spin rounded-full h-4 w-4 border-b-2 border-white mr-2"></div>
          생성 중...
        </div>
      {:else}
        POS 생성
      {/if}
    </button>
  </div>
</Modal>
