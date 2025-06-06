<script>
  import { createEventDispatcher, onMount } from 'svelte';
  import { authStore } from '$lib/stores/auth.js';
  import { toastStore } from '$lib/stores/toast.js';
  import Modal from '$lib/components/Common/Modal.svelte';

  export let open = false;

  const dispatch = createEventDispatcher();

  let formData = {
    storeName: '',
    storeType: 'CHAIN',
    operationType: 'DIRECT',
    hqId: '',
    hqCode: '',
    hqName: '',
    regionCode: '',
    regionName: '',
    storeNumber: '',
    businessLicense: '',
    ownerName: '',
    phoneNumber: '',
    address: '',
    postalCode: '',
    openingDate: new Date().toISOString().split('T')[0]
  };
  
  let loading = false;
  let errors = {};
  let regions = [];
  let headquarters = [];

  const storeTypes = [
    { value: 'CHAIN', label: '체인점', description: '본사에 소속된 매장' },
    { value: 'INDIVIDUAL', label: '개인매장', description: '독립적으로 운영되는 매장' }
  ];

  const operationTypes = [
    { value: 'DIRECT', label: '직영점', description: '본사에서 직접 운영' },
    { value: 'FRANCHISE', label: '가맹점', description: '가맹주가 운영' }
  ];

  onMount(() => {
    if (open) {
      loadRegions();
      loadHeadquarters();
    }
  });

  $: if (open) {
    loadRegions();
    loadHeadquarters();
  }

  async function loadRegions() {
    try {
      const response = await fetch('/api/v1/business/stores/regions', {
        headers: {
          'Authorization': `Bearer ${$authStore.token}`
        }
      });
      
      if (response.ok) {
        regions = await response.json();
      }
    } catch (error) {
      console.error('Failed to load regions:', error);
    }
  }

  async function loadHeadquarters() {
    try {
      const response = await fetch('/api/v1/business/stores/headquarters', {
        headers: {
          'Authorization': `Bearer ${$authStore.token}`
        }
      });
      
      if (response.ok) {
        headquarters = await response.json();
      }
    } catch (error) {
      console.error('Failed to load headquarters:', error);
    }
  }

  function resetForm() {
    formData = {
      storeName: '',
      storeType: 'CHAIN',
      operationType: 'DIRECT',
      hqId: '',
      hqCode: '',
      hqName: '',
      regionCode: '',
      regionName: '',
      storeNumber: '',
      businessLicense: '',
      ownerName: '',
      phoneNumber: '',
      address: '',
      postalCode: '',
      openingDate: new Date().toISOString().split('T')[0]
    };
    errors = {};
  }

  function validateForm() {
    errors = {};

    if (!formData.storeName.trim()) {
      errors.storeName = '매장명을 입력해주세요.';
    }

    if (!formData.storeType) {
      errors.storeType = '매장 유형을 선택해주세요.';
    }

    if (formData.storeType === 'CHAIN') {
      if (!formData.hqId) {
        errors.hqId = '본사를 선택해주세요.';
      }
      if (!formData.operationType) {
        errors.operationType = '운영 형태를 선택해주세요.';
      }
    }

    if (!formData.regionCode) {
      errors.regionCode = '지역을 선택해주세요.';
    }

    if (!formData.storeNumber.trim()) {
      errors.storeNumber = '매장 번호를 입력해주세요.';
    }

    if (!formData.ownerName.trim()) {
      errors.ownerName = '점주명을 입력해주세요.';
    }

    return Object.keys(errors).length === 0;
  }

  async function handleSubmit() {
    if (!validateForm()) return;

    loading = true;

    try {
      const response = await fetch('/api/v1/business/stores', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${$authStore.token}`
        },
        body: JSON.stringify(formData)
      });

      if (!response.ok) {
        const error = await response.json();
        throw new Error(error.message || '매장 생성에 실패했습니다.');
      }

      const newStore = await response.json();
      
      toastStore.success('매장이 성공적으로 생성되었습니다.');
      dispatch('store-created', newStore);
      
      resetForm();
      open = false;
    } catch (error) {
      console.error('Create store error:', error);
      toastStore.error(error.message);
    } finally {
      loading = false;
    }
  }

  function handleClose() {
    resetForm();
    open = false;
    dispatch('close');
  }

  function handleStoreTypeChange() {
    if (formData.storeType === 'INDIVIDUAL') {
      formData.hqId = '';
      formData.hqCode = '';
      formData.hqName = '';
      formData.operationType = '';
    }
  }

  function handleHeadquartersChange() {
    const selectedHq = headquarters.find(hq => hq.hqId === formData.hqId);
    if (selectedHq) {
      formData.hqCode = selectedHq.hqCode;
      formData.hqName = selectedHq.hqName;
    }
  }

  function handleRegionChange() {
    const selectedRegion = regions.find(region => region.regionCode === formData.regionCode);
    if (selectedRegion) {
      formData.regionName = selectedRegion.regionName;
    }
  }

  $: if (!open) {
    resetForm();
  }
</script>

<Modal bind:open title="새 매장 생성" size="xl" on:close={handleClose}>
  <form on:submit|preventDefault={handleSubmit} class="space-y-6">
    <!-- 기본 정보 -->
    <div class="space-y-4">
      <h3 class="text-lg font-medium text-gray-900">기본 정보</h3>
      
      <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
        <!-- 매장명 -->
        <div>
          <label for="storeName" class="block text-sm font-medium text-gray-700 mb-1">
            매장명 <span class="text-red-500">*</span>
          </label>
          <input
            id="storeName"
            type="text"
            bind:value={formData.storeName}
            class="input"
            class:border-red-300={errors.storeName}
            placeholder="매장명을 입력하세요"
            disabled={loading}
          />
          {#if errors.storeName}
            <p class="mt-1 text-sm text-red-600">{errors.storeName}</p>
          {/if}
        </div>

        <!-- 점주명 -->
        <div>
          <label for="ownerName" class="block text-sm font-medium text-gray-700 mb-1">
            점주명 <span class="text-red-500">*</span>
          </label>
          <input
            id="ownerName"
            type="text"
            bind:value={formData.ownerName}
            class="input"
            class:border-red-300={errors.ownerName}
            placeholder="점주명을 입력하세요"
            disabled={loading}
          />
          {#if errors.ownerName}
            <p class="mt-1 text-sm text-red-600">{errors.ownerName}</p>
          {/if}
        </div>
      </div>

      <!-- 매장 유형 -->
      <div>
        <label class="block text-sm font-medium text-gray-700 mb-3">
          매장 유형 <span class="text-red-500">*</span>
        </label>
        <div class="space-y-3">
          {#each storeTypes as type}
            <label class="flex items-start space-x-3 p-3 border border-gray-200 rounded-lg hover:bg-gray-50 cursor-pointer">
              <input
                type="radio"
                bind:group={formData.storeType}
                value={type.value}
                on:change={handleStoreTypeChange}
                class="mt-1 h-4 w-4 text-primary-600 focus:ring-primary-500 border-gray-300"
                disabled={loading}
              />
              <div class="flex-1 min-w-0">
                <div class="text-sm font-medium text-gray-900">{type.label}</div>
                <div class="text-sm text-gray-500">{type.description}</div>
              </div>
            </label>
          {/each}
        </div>
        {#if errors.storeType}
          <p class="mt-1 text-sm text-red-600">{errors.storeType}</p>
        {/if}
      </div>
    </div>

    <!-- 체인점 정보 (체인점인 경우에만) -->
    {#if formData.storeType === 'CHAIN'}
      <div class="space-y-4">
        <h3 class="text-lg font-medium text-gray-900">체인점 정보</h3>
        
        <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
          <!-- 본사 선택 -->
          <div>
            <label for="hqId" class="block text-sm font-medium text-gray-700 mb-1">
              본사 <span class="text-red-500">*</span>
            </label>
            <select
              id="hqId"
              bind:value={formData.hqId}
              on:change={handleHeadquartersChange}
              class="input"
              class:border-red-300={errors.hqId}
              disabled={loading}
            >
              <option value="">본사를 선택하세요</option>
              {#each headquarters as hq}
                <option value={hq.hqId}>{hq.hqName} ({hq.hqCode})</option>
              {/each}
            </select>
            {#if errors.hqId}
              <p class="mt-1 text-sm text-red-600">{errors.hqId}</p>
            {/if}
          </div>

          <!-- 운영 형태 -->
          <div>
            <label for="operationType" class="block text-sm font-medium text-gray-700 mb-1">
              운영 형태 <span class="text-red-500">*</span>
            </label>
            <select
              id="operationType"
              bind:value={formData.operationType}
              class="input"
              class:border-red-300={errors.operationType}
              disabled={loading}
            >
              <option value="">운영 형태를 선택하세요</option>
              {#each operationTypes as type}
                <option value={type.value}>{type.label} - {type.description}</option>
              {/each}
            </select>
            {#if errors.operationType}
              <p class="mt-1 text-sm text-red-600">{errors.operationType}</p>
            {/if}
          </div>
        </div>
      </div>
    {/if}

    <!-- 위치 정보 -->
    <div class="space-y-4">
      <h3 class="text-lg font-medium text-gray-900">위치 정보</h3>
      
      <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
        <!-- 지역 -->
        <div>
          <label for="regionCode" class="block text-sm font-medium text-gray-700 mb-1">
            지역 <span class="text-red-500">*</span>
          </label>
          <select
            id="regionCode"
            bind:value={formData.regionCode}
            on:change={handleRegionChange}
            class="input"
            class:border-red-300={errors.regionCode}
            disabled={loading}
          >
            <option value="">지역을 선택하세요</option>
            {#each regions as region}
              <option value={region.regionCode}>{region.regionName}</option>
            {/each}
          </select>
          {#if errors.regionCode}
            <p class="mt-1 text-sm text-red-600">{errors.regionCode}</p>
          {/if}
        </div>

        <!-- 매장 번호 -->
        <div>
          <label for="storeNumber" class="block text-sm font-medium text-gray-700 mb-1">
            매장 번호 <span class="text-red-500">*</span>
          </label>
          <input
            id="storeNumber"
            type="text"
            bind:value={formData.storeNumber}
            class="input"
            class:border-red-300={errors.storeNumber}
            placeholder="예: 001"
            maxlength="3"
            disabled={loading}
          />
          {#if errors.storeNumber}
            <p class="mt-1 text-sm text-red-600">{errors.storeNumber}</p>
          {/if}
          <p class="mt-1 text-sm text-gray-500">3자리 숫자로 입력하세요</p>
        </div>
      </div>

      <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
        <!-- 주소 -->
        <div>
          <label for="address" class="block text-sm font-medium text-gray-700 mb-1">
            주소
          </label>
          <input
            id="address"
            type="text"
            bind:value={formData.address}
            class="input"
            placeholder="주소를 입력하세요"
            disabled={loading}
          />
        </div>

        <!-- 우편번호 -->
        <div>
          <label for="postalCode" class="block text-sm font-medium text-gray-700 mb-1">
            우편번호
          </label>
          <input
            id="postalCode"
            type="text"
            bind:value={formData.postalCode}
            class="input"
            placeholder="12345"
            maxlength="5"
            disabled={loading}
          />
        </div>
      </div>
    </div>

    <!-- 추가 정보 -->
    <div class="space-y-4">
      <h3 class="text-lg font-medium text-gray-900">추가 정보</h3>
      
      <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
        <!-- 사업자등록번호 -->
        <div>
          <label for="businessLicense" class="block text-sm font-medium text-gray-700 mb-1">
            사업자등록번호
          </label>
          <input
            id="businessLicense"
            type="text"
            bind:value={formData.businessLicense}
            class="input"
            placeholder="000-00-00000"
            disabled={loading}
          />
        </div>

        <!-- 전화번호 -->
        <div>
          <label for="phoneNumber" class="block text-sm font-medium text-gray-700 mb-1">
            전화번호
          </label>
          <input
            id="phoneNumber"
            type="tel"
            bind:value={formData.phoneNumber}
            class="input"
            placeholder="02-1234-5678"
            disabled={loading}
          />
        </div>
      </div>

      <!-- 개점일 -->
      <div>
        <label for="openingDate" class="block text-sm font-medium text-gray-700 mb-1">
          개점일
        </label>
        <input
          id="openingDate"
          type="date"
          bind:value={formData.openingDate}
          class="input"
          disabled={loading}
        />
      </div>
    </div>

    <!-- 미리보기 -->
    {#if formData.storeName || formData.regionCode || formData.storeNumber}
      <div class="p-4 bg-blue-50 border border-blue-200 rounded-lg">
        <h4 class="text-sm font-medium text-blue-900 mb-2">생성될 매장 ID:</h4>
        <p class="text-lg font-mono text-blue-800">
          {#if formData.storeType === 'CHAIN' && formData.hqCode && formData.regionCode && formData.storeNumber}
            {formData.hqCode}{formData.regionCode}{formData.storeNumber.padStart(3, '0')}
          {:else if formData.storeType === 'INDIVIDUAL' && formData.regionCode && formData.storeNumber}
            IN{formData.regionCode}{formData.storeNumber.padStart(3, '0')}
          {:else}
            매장 정보를 입력하세요
          {/if}
        </p>
      </div>
    {/if}
  </form>

  <!-- 버튼 -->
  <div slot="footer" class="flex justify-end space-x-3 p-4 border-t border-gray-200">
    <button
      type="button"
      class="btn btn-secondary"
      on:click={handleClose}
      disabled={loading}
    >
      취소
    </button>
    <button
      type="button"
      class="btn btn-primary"
      on:click={handleSubmit}
      disabled={loading || !formData.storeName || !formData.ownerName || !formData.regionCode || !formData.storeNumber}
    >
      {#if loading}
        <div class="flex items-center">
          <div class="animate-spin rounded-full h-4 w-4 border-b-2 border-white mr-2"></div>
          생성 중...
        </div>
      {:else}
        매장 생성
      {/if}
    </button>
  </div>
</Modal>
