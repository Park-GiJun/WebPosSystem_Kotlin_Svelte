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
    openingDate: new Date().toISOString().split('T')[0],
    // 매장 관리자 계정 정보
    managerUsername: '',
    managerEmail: '',
    managerPassword: ''
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
      openingDate: new Date().toISOString().split('T')[0],
      // 매장 관리자 계정 정보 초기화
      managerUsername: '',
      managerEmail: '',
      managerPassword: ''
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
        const errorData = await response.json();
        
        // 구체적인 오류 메시지 처리
        if (errorData.error === 'DUPLICATE_STORE_ID') {
          toastStore.error(`매장 ID가 중복됩니다: ${errorData.storeId}. 다른 매장 번호를 사용해주세요.`);
          errors.storeNumber = '이미 사용 중인 매장 번호입니다.';
        } else if (errorData.error === 'DUPLICATE_USERNAME') {
          toastStore.error(`사용자명이 중복됩니다: ${errorData.username}`);
          errors.managerUsername = '이미 사용 중인 사용자명입니다.';
        } else if (errorData.error === 'DUPLICATE_EMAIL') {
          toastStore.error(`이메일이 중복됩니다: ${errorData.email}`);
          errors.managerEmail = '이미 사용 중인 이메일입니다.';
        } else {
          toastStore.error(errorData.message || '매장 생성에 실패했습니다.');
        }
        return;
      }

      const newStore = await response.json();
      
      toastStore.success(`매장이 성공적으로 생성되었습니다. (관리자: ${newStore.managerUsername})`);
      dispatch('store-created', newStore);
      
      resetForm();
      open = false;
    } catch (error) {
      console.error('Create store error:', error);
      toastStore.error('매장 생성 중 네트워크 오류가 발생했습니다.');
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

    <!-- 매장 관리자 계정 정보 -->
    <div class="space-y-4">
      <div class="flex items-center justify-between">
        <h3 class="text-lg font-medium text-gray-900">매장 관리자 계정</h3>
        <span class="text-sm text-gray-500">선택사항 (비워두면 자동 생성됩니다)</span>
      </div>
      
      <div class="p-4 bg-yellow-50 border border-yellow-200 rounded-lg">
        <div class="flex">
          <div class="flex-shrink-0">
            <svg class="h-5 w-5 text-yellow-400" fill="currentColor" viewBox="0 0 20 20">
              <path fill-rule="evenodd" d="M8.257 3.099c.765-1.36 2.722-1.36 3.486 0l5.58 9.92c.75 1.334-.213 2.98-1.742 2.98H4.42c-1.53 0-2.493-1.646-1.743-2.98l5.58-9.92zM11 13a1 1 0 11-2 0 1 1 0 012 0zm-1-8a1 1 0 00-1 1v3a1 1 0 002 0V6a1 1 0 00-1-1z" clip-rule="evenodd"></path>
            </svg>
          </div>
          <div class="ml-3">
            <h4 class="text-sm font-medium text-yellow-800">자동 생성 안내</h4>
            <div class="mt-1 text-sm text-yellow-700">
              <p>계정 정보를 입력하지 않으면 다음과 같이 자동 생성됩니다:</p>
              <ul class="mt-1 list-disc list-inside space-y-1">
                <li>사용자명: {formData.storeType?.toLowerCase() || 'store'}_{formData.storeNumber || '000'}_manager</li>
                <li>이메일: [사용자명]@{formData.storeName?.replace(/\s+/g, '').toLowerCase() || 'store'}.com</li>
                <li>비밀번호: temp123! (최초 로그인 후 변경 필요)</li>
              </ul>
            </div>
          </div>
        </div>
      </div>

      <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
        <!-- 관리자 사용자명 -->
        <div>
          <label for="managerUsername" class="block text-sm font-medium text-gray-700 mb-1">
            관리자 사용자명
          </label>
          <input
            id="managerUsername"
            type="text"
            bind:value={formData.managerUsername}
            class="input"
            placeholder="예: store_001_manager"
            disabled={loading}
          />
          <p class="mt-1 text-sm text-gray-500">영문, 숫자, 언더스코어(_)만 사용 가능</p>
        </div>

        <!-- 관리자 이메일 -->
        <div>
          <label for="managerEmail" class="block text-sm font-medium text-gray-700 mb-1">
            관리자 이메일
          </label>
          <input
            id="managerEmail"
            type="email"
            bind:value={formData.managerEmail}
            class="input"
            placeholder="manager@store.com"
            disabled={loading}
          />
        </div>
      </div>

      <!-- 관리자 비밀번호 -->
      <div>
        <label for="managerPassword" class="block text-sm font-medium text-gray-700 mb-1">
          관리자 초기 비밀번호
        </label>
        <input
          id="managerPassword"
          type="password"
          bind:value={formData.managerPassword}
          class="input"
          placeholder="초기 비밀번호 (8자 이상)"
          disabled={loading}
        />
        <p class="mt-1 text-sm text-gray-500">비워두면 'temp123!'으로 자동 설정됩니다</p>
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
