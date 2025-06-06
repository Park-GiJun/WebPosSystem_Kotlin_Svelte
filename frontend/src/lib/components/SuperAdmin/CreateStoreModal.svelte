<script>
  import { createEventDispatcher, onMount } from 'svelte';
  import { X, Building, MapPin, User, Phone, FileText } from 'lucide-svelte';
  import Modal from '$lib/components/Common/Modal.svelte';
  import { storeApi } from '$lib/api/superAdmin.js';
  import { toastStore } from '$lib/stores/toast.js';

  export let open = false;

  const dispatch = createEventDispatcher();

  let form = {
    storeName: '',
    storeType: 'INDIVIDUAL',
    operationType: '',
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
    openingDate: ''
  };

  let loading = false;
  let loadingData = false;
  let errors = {};
  let regions = [];
  let headquarters = [];

  const storeTypes = [
    { value: 'INDIVIDUAL', label: '개인매장' },
    { value: 'CHAIN', label: '체인매장' }
  ];

  const operationTypes = [
    { value: 'DIRECT', label: '직영' },
    { value: 'FRANCHISE', label: '가맹' }
  ];

  onMount(async () => {
    if (open) {
      await loadInitialData();
    }
  });

  $: if (open) {
    loadInitialData();
  }

  $: {
    // 스토어 타입이 변경되면 운영타입 초기화
    if (form.storeType === 'INDIVIDUAL') {
      form.operationType = '';
      form.hqId = '';
      form.hqCode = '';
      form.hqName = '';
    }
  }

  $: {
    // 본사가 선택되면 본사 코드 자동 설정
    if (form.hqId) {
      const selectedHq = headquarters.find(hq => hq.hqId === form.hqId);
      if (selectedHq) {
        form.hqCode = selectedHq.hqCode;
        form.hqName = selectedHq.hqName;
      }
    }
  }

  $: {
    // 지역이 선택되면 지역명 자동 설정
    if (form.regionCode) {
      const selectedRegion = regions.find(region => region.regionCode === form.regionCode);
      if (selectedRegion) {
        form.regionName = selectedRegion.regionName;
      }
    }
  }

  async function loadInitialData() {
    loadingData = true;
    
    try {
      // 지역 정보 로드
      const regionsResponse = await storeApi.getRegions();
      if (regionsResponse.success && regionsResponse.data) {
        regions = regionsResponse.data;
      } else if (regionsResponse.error === 'API_NOT_FOUND') {
        regions = [
          { regionCode: '001', regionName: '서울특별시' },
          { regionCode: '002', regionName: '부산광역시' },
          { regionCode: '003', regionName: '대구광역시' },
          { regionCode: '004', regionName: '인천광역시' },
          { regionCode: '009', regionName: '경기도' }
        ];
      } else {
        console.warn('지역 정보 로드 실패:', regionsResponse.message);
        regions = [];
      }

      // 본사 정보 로드
      const hqResponse = await storeApi.getHeadquarters();
      if (hqResponse.success && hqResponse.data) {
        headquarters = hqResponse.data;
      } else if (hqResponse.error === 'API_NOT_FOUND') {
        headquarters = [
          { hqId: 'HQHQ1', hqCode: 'HQ1', hqName: '커피왕 본사' },
          { hqId: 'HQHQ2', hqCode: 'HQ2', hqName: '베이커리킹 본사' },
          { hqId: 'HQHQ3', hqCode: 'HQ3', hqName: '프랜차이즈마스터 본사' }
        ];
      } else {
        console.warn('본사 정보 로드 실패:', hqResponse.message);
        headquarters = [];
      }
    } catch (error) {
      console.error('초기 데이터 로드 오류:', error);
      toastStore.error('데이터 로드 중 오류가 발생했습니다.');
    } finally {
      loadingData = false;
    }
  }

  function validateForm() {
    errors = {};

    if (!form.storeName.trim()) {
      errors.storeName = '매장명은 필수입니다.';
    }

    if (!form.storeType) {
      errors.storeType = '매장 유형을 선택해주세요.';
    }

    if (form.storeType === 'CHAIN' && !form.operationType) {
      errors.operationType = '운영 방식을 선택해주세요.';
    }

    if (form.storeType === 'CHAIN' && !form.hqId) {
      errors.hqId = '본사를 선택해주세요.';
    }

    if (!form.regionCode) {
      errors.regionCode = '지역을 선택해주세요.';
    }

    if (!form.storeNumber.trim()) {
      errors.storeNumber = '매장 번호는 필수입니다.';
    } else if (!/^\d{1,3}$/.test(form.storeNumber)) {
      errors.storeNumber = '매장 번호는 1-3자리 숫자여야 합니다.';
    }

    if (!form.ownerName.trim()) {
      errors.ownerName = '사업자명은 필수입니다.';
    }

    if (form.phoneNumber && !/^[\d-\s()]+$/.test(form.phoneNumber)) {
      errors.phoneNumber = '올바른 전화번호 형식이 아닙니다.';
    }

    if (form.businessLicense && !/^\d{3}-\d{2}-\d{5}$/.test(form.businessLicense)) {
      errors.businessLicense = '사업자등록번호는 000-00-00000 형식이어야 합니다.';
    }

    return Object.keys(errors).length === 0;
  }

  async function handleSubmit() {
    if (!validateForm()) {
      return;
    }

    loading = true;

    try {
      const storeData = {
        ...form,
        openingDate: form.openingDate || null
      };

      const response = await storeApi.createStore(storeData);

      if (response.success) {
        dispatch('store-created', response.data);
        handleClose();
        toastStore.success('매장이 성공적으로 생성되었습니다.');
      } else if (response.error === 'API_NOT_FOUND') {
        // API가 없는 경우 더미 데이터로 응답
        const dummyStore = {
          storeId: generateStoreId(form.storeType, form.hqCode, form.regionCode, form.storeNumber),
          ...storeData,
          hqName: form.hqName,
          regionName: form.regionName,
          storeStatus: 'ACTIVE',
          managerUsername: null,
          posCount: 0,
          employeeCount: 0,
          isActive: true,
          createdAt: new Date().toISOString(),
          updatedAt: new Date().toISOString()
        };
        
        dispatch('store-created', dummyStore);
        handleClose();
        toastStore.success('매장이 성공적으로 생성되었습니다. (데모 모드)');
      } else {
        errors.submit = response.message || '매장 생성에 실패했습니다.';
      }
    } catch (error) {
      console.error('매장 생성 오류:', error);
      errors.submit = '매장 생성 중 오류가 발생했습니다.';
    } finally {
      loading = false;
    }
  }

  function generateStoreId(storeType, hqCode, regionCode, storeNumber) {
    if (storeType === 'CHAIN' && hqCode) {
      return `${hqCode}${regionCode}${storeNumber.padStart(3, '0')}`;
    } else {
      return `IN${regionCode}${storeNumber.padStart(3, '0')}`;
    }
  }

  function formatBusinessLicense(value) {
    // 숫자만 추출
    const numbers = value.replace(/\D/g, '');
    
    // 000-00-00000 형식으로 포맷
    if (numbers.length <= 3) {
      return numbers;
    } else if (numbers.length <= 5) {
      return `${numbers.slice(0, 3)}-${numbers.slice(3)}`;
    } else {
      return `${numbers.slice(0, 3)}-${numbers.slice(3, 5)}-${numbers.slice(5, 10)}`;
    }
  }

  function handleBusinessLicenseInput(event) {
    const formatted = formatBusinessLicense(event.target.value);
    form.businessLicense = formatted;
  }

  function handleClose() {
    if (!loading) {
      open = false;
      form = {
        storeName: '',
        storeType: 'INDIVIDUAL',
        operationType: '',
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
        openingDate: ''
      };
      errors = {};
      dispatch('close');
    }
  }
</script>

<Modal bind:open title="매장 생성" size="lg" on:close={handleClose}>
  {#if loadingData}
    <div class="p-8 text-center">
      <div class="animate-spin rounded-full h-8 w-8 border-b-2 border-primary-600 mx-auto"></div>
      <p class="mt-4 text-gray-600">데이터를 불러오는 중...</p>
    </div>
  {:else}
    <form on:submit|preventDefault={handleSubmit} class="space-y-6">
      <!-- 기본 정보 섹션 -->
      <div class="bg-gray-50 p-4 rounded-lg">
        <h3 class="text-lg font-medium text-gray-900 mb-4 flex items-center">
          <Building class="mr-2 h-5 w-5" />
          기본 정보
        </h3>
        
        <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
          <!-- 매장명 -->
          <div>
            <label for="storeName" class="block text-sm font-medium text-gray-700 mb-1">
              매장명 <span class="text-red-500">*</span>
            </label>
            <input
              id="storeName"
              type="text"
              bind:value={form.storeName}
              class="input"
              class:border-red-300={errors.storeName}
              placeholder="매장명을 입력하세요"
              disabled={loading}
              required
            />
            {#if errors.storeName}
              <p class="text-sm text-red-600 mt-1">{errors.storeName}</p>
            {/if}
          </div>

          <!-- 매장 유형 -->
          <div>
            <label for="storeType" class="block text-sm font-medium text-gray-700 mb-1">
              매장 유형 <span class="text-red-500">*</span>
            </label>
            <select
              id="storeType"
              bind:value={form.storeType}
              class="input"
              class:border-red-300={errors.storeType}
              disabled={loading}
              required
            >
              {#each storeTypes as type}
                <option value={type.value}>{type.label}</option>
              {/each}
            </select>
            {#if errors.storeType}
              <p class="text-sm text-red-600 mt-1">{errors.storeType}</p>
            {/if}
          </div>

          <!-- 운영 방식 (체인매장만) -->
          {#if form.storeType === 'CHAIN'}
            <div>
              <label for="operationType" class="block text-sm font-medium text-gray-700 mb-1">
                운영 방식 <span class="text-red-500">*</span>
              </label>
              <select
                id="operationType"
                bind:value={form.operationType}
                class="input"
                class:border-red-300={errors.operationType}
                disabled={loading}
                required
              >
                <option value="">선택하세요</option>
                {#each operationTypes as type}
                  <option value={type.value}>{type.label}</option>
                {/each}
              </select>
              {#if errors.operationType}
                <p class="text-sm text-red-600 mt-1">{errors.operationType}</p>
              {/if}
            </div>

            <!-- 본사 선택 -->
            <div>
              <label for="hqId" class="block text-sm font-medium text-gray-700 mb-1">
                본사 <span class="text-red-500">*</span>
              </label>
              <select
                id="hqId"
                bind:value={form.hqId}
                class="input"
                class:border-red-300={errors.hqId}
                disabled={loading}
                required
              >
                <option value="">본사를 선택하세요</option>
                {#each headquarters as hq}
                  <option value={hq.hqId}>{hq.hqName} ({hq.hqCode})</option>
                {/each}
              </select>
              {#if errors.hqId}
                <p class="text-sm text-red-600 mt-1">{errors.hqId}</p>
              {/if}
            </div>
          {/if}
        </div>
      </div>

      <!-- 위치 정보 섹션 -->
      <div class="bg-gray-50 p-4 rounded-lg">
        <h3 class="text-lg font-medium text-gray-900 mb-4 flex items-center">
          <MapPin class="mr-2 h-5 w-5" />
          위치 정보
        </h3>
        
        <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
          <!-- 지역 -->
          <div>
            <label for="regionCode" class="block text-sm font-medium text-gray-700 mb-1">
              지역 <span class="text-red-500">*</span>
            </label>
            <select
              id="regionCode"
              bind:value={form.regionCode}
              class="input"
              class:border-red-300={errors.regionCode}
              disabled={loading}
              required
            >
              <option value="">지역을 선택하세요</option>
              {#each regions as region}
                <option value={region.regionCode}>{region.regionName} ({region.regionCode})</option>
              {/each}
            </select>
            {#if errors.regionCode}
              <p class="text-sm text-red-600 mt-1">{errors.regionCode}</p>
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
              bind:value={form.storeNumber}
              class="input"
              class:border-red-300={errors.storeNumber}
              placeholder="001"
              disabled={loading}
              maxlength="3"
              pattern="[0-9]{1,3}"
              required
            />
            {#if errors.storeNumber}
              <p class="text-sm text-red-600 mt-1">{errors.storeNumber}</p>
            {/if}
            <p class="text-xs text-gray-500 mt-1">1-3자리 숫자를 입력하세요</p>
          </div>

          <!-- 주소 -->
          <div class="md:col-span-2">
            <label for="address" class="block text-sm font-medium text-gray-700 mb-1">
              주소
            </label>
            <input
              id="address"
              type="text"
              bind:value={form.address}
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
              bind:value={form.postalCode}
              class="input"
              placeholder="12345"
              disabled={loading}
              maxlength="5"
              pattern="[0-9]{5}"
            />
          </div>

          <!-- 개업일 -->
          <div>
            <label for="openingDate" class="block text-sm font-medium text-gray-700 mb-1">
              개업일
            </label>
            <input
              id="openingDate"
              type="date"
              bind:value={form.openingDate}
              class="input"
              disabled={loading}
            />
          </div>
        </div>
      </div>

      <!-- 사업자 정보 섹션 -->
      <div class="bg-gray-50 p-4 rounded-lg">
        <h3 class="text-lg font-medium text-gray-900 mb-4 flex items-center">
          <User class="mr-2 h-5 w-5" />
          사업자 정보
        </h3>
        
        <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
          <!-- 사업자명 -->
          <div>
            <label for="ownerName" class="block text-sm font-medium text-gray-700 mb-1">
              사업자명 <span class="text-red-500">*</span>
            </label>
            <input
              id="ownerName"
              type="text"
              bind:value={form.ownerName}
              class="input"
              class:border-red-300={errors.ownerName}
              placeholder="사업자명을 입력하세요"
              disabled={loading}
              required
            />
            {#if errors.ownerName}
              <p class="text-sm text-red-600 mt-1">{errors.ownerName}</p>
            {/if}
          </div>

          <!-- 전화번호 -->
          <div>
            <label for="phoneNumber" class="block text-sm font-medium text-gray-700 mb-1">
              전화번호
            </label>
            <input
              id="phoneNumber"
              type="tel"
              bind:value={form.phoneNumber}
              class="input"
              class:border-red-300={errors.phoneNumber}
              placeholder="02-1234-5678"
              disabled={loading}
            />
            {#if errors.phoneNumber}
              <p class="text-sm text-red-600 mt-1">{errors.phoneNumber}</p>
            {/if}
          </div>

          <!-- 사업자등록번호 -->
          <div class="md:col-span-2">
            <label for="businessLicense" class="block text-sm font-medium text-gray-700 mb-1">
              사업자등록번호
            </label>
            <input
              id="businessLicense"
              type="text"
              value={form.businessLicense}
              on:input={handleBusinessLicenseInput}
              class="input"
              class:border-red-300={errors.businessLicense}
              placeholder="123-45-67890"
              disabled={loading}
              maxlength="12"
            />
            {#if errors.businessLicense}
              <p class="text-sm text-red-600 mt-1">{errors.businessLicense}</p>
            {/if}
            <p class="text-xs text-gray-500 mt-1">000-00-00000 형식으로 입력하세요</p>
          </div>
        </div>
      </div>

      <!-- 매장 ID 미리보기 -->
      {#if form.regionCode && form.storeNumber}
        <div class="bg-blue-50 border border-blue-200 rounded-lg p-4">
          <h4 class="text-sm font-medium text-blue-900 mb-2">생성될 매장 ID</h4>
          <p class="text-lg font-mono text-blue-700">
            {generateStoreId(form.storeType, form.hqCode, form.regionCode, form.storeNumber)}
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
        매장 생성
      {/if}
    </button>
  </div>
</Modal>
