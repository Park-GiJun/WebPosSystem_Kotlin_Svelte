<script>
  import { createEventDispatcher } from 'svelte';
  import { authStore } from '$lib/stores/auth.js';
  import { toastStore } from '$lib/stores/toast.js';
  import Modal from '$lib/components/Common/Modal.svelte';

  export let open = false;
  export let stores = [];

  const dispatch = createEventDispatcher();

  let formData = {
    storeId: '',
    storeName: '',
    posNumber: 1,
    posName: '',
    posType: 'MAIN',
    ipAddress: '',
    macAddress: '',
    serialNumber: '',
    installedDate: new Date().toISOString().split('T')[0]
  };
  
  let loading = false;
  let errors = {};

  const posTypes = [
    { value: 'MAIN', label: '메인 POS', description: '주요 결제 및 관리용 POS' },
    { value: 'SUB', label: '서브 POS', description: '보조 결제용 POS' },
    { value: 'MOBILE', label: '모바일 POS', description: '이동형 결제용 POS' }
  ];

  function resetForm() {
    formData = {
      storeId: '',
      storeName: '',
      posNumber: 1,
      posName: '',
      posType: 'MAIN',
      ipAddress: '',
      macAddress: '',
      serialNumber: '',
      installedDate: new Date().toISOString().split('T')[0]
    };
    errors = {};
  }

  function validateForm() {
    errors = {};

    if (!formData.storeId) {
      errors.storeId = '매장을 선택해주세요.';
    }

    if (!formData.posNumber || formData.posNumber < 1) {
      errors.posNumber = '올바른 POS 번호를 입력해주세요.';
    }

    if (!formData.posType) {
      errors.posType = 'POS 유형을 선택해주세요.';
    }

    if (formData.ipAddress && !isValidIP(formData.ipAddress)) {
      errors.ipAddress = '올바른 IP 주소를 입력해주세요.';
    }

    if (formData.macAddress && !isValidMac(formData.macAddress)) {
      errors.macAddress = '올바른 MAC 주소를 입력해주세요.';
    }

    return Object.keys(errors).length === 0;
  }

  function isValidIP(ip) {
    const ipRegex = /^(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$/;
    return ipRegex.test(ip);
  }

  function isValidMac(mac) {
    const macRegex = /^([0-9A-F]{2}[:-]){5}([0-9A-F]{2})$/i;
    return macRegex.test(mac);
  }

  async function handleSubmit() {
    if (!validateForm()) return;

    loading = true;

    try {
      const response = await fetch('/api/v1/business/pos', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${$authStore.token}`
        },
        body: JSON.stringify(formData)
      });

      if (!response.ok) {
        const error = await response.json();
        throw new Error(error.message || 'POS 생성에 실패했습니다.');
      }

      const newPos = await response.json();
      
      toastStore.success('POS 시스템이 성공적으로 생성되었습니다.');
      dispatch('pos-created', newPos);
      
      resetForm();
      open = false;
    } catch (error) {
      console.error('Create POS error:', error);
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

  function handleStoreChange() {
    const selectedStore = stores.find(store => store.storeId === formData.storeId);
    if (selectedStore) {
      formData.storeName = selectedStore.storeName;
    }
  }

  function generatePosName() {
    if (formData.posType && formData.posNumber) {
      const typeText = posTypes.find(t => t.value === formData.posType)?.label || formData.posType;
      formData.posName = `${typeText} ${formData.posNumber}`;
    }
  }

  function formatMacAddress(event) {
    let value = event.target.value.replace(/[^0-9A-Fa-f]/g, '');
    if (value.length > 12) value = value.slice(0, 12);
    
    // MAC 주소 형식으로 변환 (XX:XX:XX:XX:XX:XX)
    value = value.replace(/(.{2})(?=.)/g, '$1:');
    formData.macAddress = value.toUpperCase();
  }

  $: if (!open) {
    resetForm();
  }

  $: if (formData.posType || formData.posNumber) {
    generatePosName();
  }
</script>

<Modal bind:open title="새 POS 시스템 추가" size="lg" on:close={handleClose}>
  <form on:submit|preventDefault={handleSubmit} class="space-y-6">
    <!-- 기본 정보 -->
    <div class="space-y-4">
      <h3 class="text-lg font-medium text-gray-900">기본 정보</h3>
      
      <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
        <!-- 매장 선택 -->
        <div>
          <label for="storeId" class="block text-sm font-medium text-gray-700 mb-1">
            매장 <span class="text-red-500">*</span>
          </label>
          <select
            id="storeId"
            bind:value={formData.storeId}
            on:change={handleStoreChange}
            class="input"
            class:border-red-300={errors.storeId}
            disabled={loading}
          >
            <option value="">매장을 선택하세요</option>
            {#each stores as store}
              <option value={store.storeId}>{store.storeName} ({store.storeId})</option>
            {/each}
          </select>
          {#if errors.storeId}
            <p class="mt-1 text-sm text-red-600">{errors.storeId}</p>
          {/if}
        </div>

        <!-- POS 번호 -->
        <div>
          <label for="posNumber" class="block text-sm font-medium text-gray-700 mb-1">
            POS 번호 <span class="text-red-500">*</span>
          </label>
          <input
            id="posNumber"
            type="number"
            min="1"
            max="99"
            bind:value={formData.posNumber}
            class="input"
            class:border-red-300={errors.posNumber}
            placeholder="1"
            disabled={loading}
          />
          {#if errors.posNumber}
            <p class="mt-1 text-sm text-red-600">{errors.posNumber}</p>
          {/if}
          <p class="mt-1 text-sm text-gray-500">매장 내에서 고유한 번호를 입력하세요</p>
        </div>
      </div>

      <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
        <!-- POS명 -->
        <div>
          <label for="posName" class="block text-sm font-medium text-gray-700 mb-1">
            POS명
          </label>
          <input
            id="posName"
            type="text"
            bind:value={formData.posName}
            class="input"
            placeholder="자동 생성됩니다"
            disabled={loading}
          />
          <p class="mt-1 text-sm text-gray-500">비워두면 자동으로 생성됩니다</p>
        </div>

        <!-- POS 유형 -->
        <div>
          <label for="posType" class="block text-sm font-medium text-gray-700 mb-1">
            POS 유형 <span class="text-red-500">*</span>
          </label>
          <select
            id="posType"
            bind:value={formData.posType}
            class="input"
            class:border-red-300={errors.posType}
            disabled={loading}
          >
            <option value="">POS 유형을 선택하세요</option>
            {#each posTypes as type}
              <option value={type.value}>{type.label}</option>
            {/each}
          </select>
          {#if errors.posType}
            <p class="mt-1 text-sm text-red-600">{errors.posType}</p>
          {/if}
        </div>
      </div>

      <!-- POS 유형 설명 -->
      {#if formData.posType}
        {@const selectedType = posTypes.find(t => t.value === formData.posType)}
        {#if selectedType}
          <div class="p-3 bg-blue-50 border border-blue-200 rounded-lg">
            <p class="text-sm text-blue-800">
              <strong>{selectedType.label}:</strong> {selectedType.description}
            </p>
          </div>
        {/if}
      {/if}
    </div>

    <!-- 하드웨어 정보 -->
    <div class="space-y-4">
      <h3 class="text-lg font-medium text-gray-900">하드웨어 정보</h3>
      
      <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
        <!-- 시리얼 번호 -->
        <div>
          <label for="serialNumber" class="block text-sm font-medium text-gray-700 mb-1">
            시리얼 번호
          </label>
          <input
            id="serialNumber"
            type="text"
            bind:value={formData.serialNumber}
            class="input"
            placeholder="POS001234567"
            disabled={loading}
          />
        </div>

        <!-- 설치일 -->
        <div>
          <label for="installedDate" class="block text-sm font-medium text-gray-700 mb-1">
            설치일
          </label>
          <input
            id="installedDate"
            type="date"
            bind:value={formData.installedDate}
            class="input"
            disabled={loading}
          />
        </div>
      </div>
    </div>

    <!-- 네트워크 정보 -->
    <div class="space-y-4">
      <h3 class="text-lg font-medium text-gray-900">네트워크 정보</h3>
      
      <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
        <!-- IP 주소 -->
        <div>
          <label for="ipAddress" class="block text-sm font-medium text-gray-700 mb-1">
            IP 주소
          </label>
          <input
            id="ipAddress"
            type="text"
            bind:value={formData.ipAddress}
            class="input"
            class:border-red-300={errors.ipAddress}
            placeholder="192.168.1.100"
            disabled={loading}
          />
          {#if errors.ipAddress}
            <p class="mt-1 text-sm text-red-600">{errors.ipAddress}</p>
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
            value={formData.macAddress}
            on:input={formatMacAddress}
            class="input"
            class:border-red-300={errors.macAddress}
            placeholder="00:1B:44:11:3A:B7"
            maxlength="17"
            disabled={loading}
          />
          {#if errors.macAddress}
            <p class="mt-1 text-sm text-red-600">{errors.macAddress}</p>
          {/if}
          <p class="mt-1 text-sm text-gray-500">숫자와 영문자만 입력하세요 (자동 포맷됨)</p>
        </div>
      </div>
    </div>

    <!-- 미리보기 -->
    {#if formData.storeId && formData.posNumber}
      <div class="p-4 bg-green-50 border border-green-200 rounded-lg">
        <h4 class="text-sm font-medium text-green-900 mb-2">생성될 POS ID:</h4>
        <p class="text-lg font-mono text-green-800">
          {formData.storeId}P{formData.posNumber.toString().padStart(2, '0')}
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
      disabled={loading || !formData.storeId || !formData.posNumber || !formData.posType}
    >
      {#if loading}
        <div class="flex items-center">
          <div class="animate-spin rounded-full h-4 w-4 border-b-2 border-white mr-2"></div>
          생성 중...
        </div>
      {:else}
        POS 추가
      {/if}
    </button>
  </div>
</Modal>
