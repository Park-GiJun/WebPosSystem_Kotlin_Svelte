<script>
  import { onMount } from 'svelte';
  import { authStore } from '$lib/stores/auth.js';
  import { tabStore } from '$lib/stores/tabs.js';
  import { toastStore } from '$lib/stores/toast.js';
  import { organizationApi } from '$lib/api/admin.js';
  import { Plus, Building2, Store, Users, Settings, Edit, Trash2 } from 'lucide-svelte';
  import Modal from '$lib/components/Common/Modal.svelte';

  let activeTab = 'headquarters';
  let loading = false;
  let organizations = [];
  let showCreateHqModal = false;
  let showCreateStoreModal = false;

  // 인증 상태 구독
  let authToken = '';
  authStore.subscribe(state => {
    authToken = state.token || '';
  });

  const tabs = [
    { id: 'headquarters', label: '체인본부', icon: Building2 },
    { id: 'individual-stores', label: '개인매장', icon: Store }
  ];

  // 본사 생성 폼
  let hqForm = {
    type: 'HEADQUARTERS',
    name: '',
    code: '',
    businessLicense: '',
    ceoName: '',
    address: '',
    contactPhone: '',
    website: '',
    description: '',
    adminUsername: '',
    adminEmail: '',
    adminPassword: ''
  };

  // 개인매장 생성 폼
  let storeForm = {
    type: 'STORE',
    name: '',
    regionCode: '001',
    storeNumber: '',
    businessLicense: '',
    ownerName: '',
    phoneNumber: '',
    address: '',
    postalCode: '',
    openingDate: new Date().toISOString().split('T')[0],
    description: '',
    adminUsername: '',
    adminEmail: '',
    adminPassword: ''
  };

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
    tabStore.setActiveTab('ADMIN_ORGANIZATIONS');
    loadOrganizations();
  });

  async function loadOrganizations() {
    if (!authToken) {
      console.warn('인증 토큰이 없습니다.');
      return;
    }

    try {
      loading = true;
      console.log('🏢 조직 목록 조회 중...');
      
      const response = await organizationApi.getOrganizations({}, authToken);
      
      if (response && response.organizations) {
        organizations = response.organizations;
        console.log('✅ 조직 목록 로드 완료:', organizations.length, '개');
      } else {
        console.warn('⚠️ 응답에 organizations 필드가 없습니다:', response);
        organizations = [];
      }
    } catch (error) {
      console.error('❌ 조직 목록 로드 실패:', error);
      toastStore.error('조직 목록을 불러오는데 실패했습니다: ' + error.message);
      organizations = [];
    } finally {
      loading = false;
    }
  }

  async function createHeadquarters() {
    if (!authToken) {
      toastStore.error('인증이 필요합니다.');
      return;
    }

    try {
      console.log('🏢 체인본부 생성 중:', hqForm.name);
      
      const response = await organizationApi.createOrganization(hqForm, authToken);
      
      if (response) {
        organizations = [...organizations, response];
        toastStore.success('체인본부가 성공적으로 생성되었습니다.');
        showCreateHqModal = false;
        resetHqForm();
        console.log('✅ 체인본부 생성 완료');
      }
    } catch (error) {
      console.error('❌ 체인본부 생성 실패:', error);
      toastStore.error('체인본부 생성에 실패했습니다: ' + error.message);
    }
  }

  async function createIndividualStore() {
    if (!authToken) {
      toastStore.error('인증이 필요합니다.');
      return;
    }

    try {
      console.log('🏪 개인매장 생성 중:', storeForm.name);
      
      const response = await organizationApi.createOrganization(storeForm, authToken);
      
      if (response) {
        organizations = [...organizations, response];
        toastStore.success('개인매장이 성공적으로 생성되었습니다.');
        showCreateStoreModal = false;
        resetStoreForm();
        console.log('✅ 개인매장 생성 완료');
      }
    } catch (error) {
      console.error('❌ 개인매장 생성 실패:', error);
      toastStore.error('개인매장 생성에 실패했습니다: ' + error.message);
    }
  }

  async function editOrganization(org) {
    console.log('Edit organization:', org);
    toastStore.info('조직 편집 기능은 준비 중입니다.');
  }

  async function deleteOrganization(org) {
    if (!confirm(`정말로 "${org.name}" 조직을 삭제하시겠습니까?`)) {
      return;
    }

    if (!authToken) {
      toastStore.error('인증이 필요합니다.');
      return;
    }

    try {
      console.log('🗑️ 조직 삭제 중:', org.name);
      
      await organizationApi.deleteOrganization(org.id, authToken);
      
      organizations = organizations.filter(o => o.id !== org.id);
      toastStore.success('조직이 삭제되었습니다.');
      console.log('✅ 조직 삭제 완료');
    } catch (error) {
      console.error('❌ 조직 삭제 실패:', error);
      toastStore.error('조직 삭제에 실패했습니다: ' + error.message);
    }
  }

  function resetHqForm() {
    hqForm = {
      type: 'HEADQUARTERS',
      name: '',
      code: '',
      businessLicense: '',
      ceoName: '',
      address: '',
      contactPhone: '',
      website: '',
      description: '',
      adminUsername: '',
      adminEmail: '',
      adminPassword: ''
    };
  }

  function resetStoreForm() {
    storeForm = {
      type: 'STORE',
      name: '',
      regionCode: '001',
      storeNumber: '',
      businessLicense: '',
      ownerName: '',
      phoneNumber: '',
      address: '',
      postalCode: '',
      openingDate: new Date().toISOString().split('T')[0],
      description: '',
      adminUsername: '',
      adminEmail: '',
      adminPassword: ''
    };
  }

  // 필터링된 조직들
  $: headquarters = organizations.filter(org => org.type === 'HEADQUARTERS');
  $: individualStores = organizations.filter(org => org.type === 'STORE' && !org.parentId);
</script>

<svelte:head>
  <title>조직 관리 - WebPos</title>
</svelte:head>

<div class="h-full flex flex-col space-y-6">
  <!-- 헤더 -->
  <div class="flex items-center justify-between">
    <div>
      <h1 class="text-2xl font-bold text-gray-900">조직 관리</h1>
      <p class="text-gray-600 mt-1">체인본부와 개인매장을 생성하고 관리합니다.</p>
      <div class="mt-2 p-3 bg-blue-50 border border-blue-200 rounded-lg">
        <p class="text-sm text-blue-800">
          <strong>업무 흐름:</strong> 
          1) 여기서 본사 생성 → 2) 본사에서 가맹점 생성 → 3) 가맹점에서 POS 생성
        </p>
      </div>
    </div>
    <div class="flex space-x-3">
      <button 
        type="button" 
        class="btn btn-primary"
        on:click={() => showCreateHqModal = true}
      >
        <Building2 size="16" class="mr-2" />
        체인본부 생성
      </button>
      <button 
        type="button" 
        class="btn btn-secondary"
        on:click={() => showCreateStoreModal = true}
      >
        <Store size="16" class="mr-2" />
        개인매장 생성
      </button>
    </div>
  </div>

  <!-- 통계 카드 -->
  <div class="grid grid-cols-1 md:grid-cols-3 gap-6">
    <div class="card p-6">
      <div class="flex items-center">
        <div class="p-3 rounded-full bg-blue-100">
          <Building2 class="h-6 w-6 text-blue-600" />
        </div>
        <div class="ml-4">
          <p class="text-sm font-medium text-gray-600">체인본부</p>
          <p class="text-2xl font-bold text-gray-900">{headquarters.length}</p>
        </div>
      </div>
    </div>

    <div class="card p-6">
      <div class="flex items-center">
        <div class="p-3 rounded-full bg-green-100">
          <Store class="h-6 w-6 text-green-600" />
        </div>
        <div class="ml-4">
          <p class="text-sm font-medium text-gray-600">개인매장</p>
          <p class="text-2xl font-bold text-gray-900">{individualStores.length}</p>
        </div>
      </div>
    </div>

    <div class="card p-6">
      <div class="flex items-center">
        <div class="p-3 rounded-full bg-purple-100">
          <Users class="h-6 w-6 text-purple-600" />
        </div>
        <div class="ml-4">
          <p class="text-sm font-medium text-gray-600">총 조직</p>
          <p class="text-2xl font-bold text-gray-900">{organizations.length}</p>
        </div>
      </div>
    </div>
  </div>

  <!-- 탭 네비게이션 -->
  <div class="card flex-1 flex flex-col min-h-0">
    <div class="border-b border-gray-200 flex-shrink-0">
      <nav class="-mb-px flex">
        {#each tabs as tab}
          <button
            type="button"
            class="tab flex items-center"
            class:active={activeTab === tab.id}
            on:click={() => activeTab = tab.id}
          >
            <svelte:component this={tab.icon} size="16" class="mr-2" />
            {tab.label}
          </button>
        {/each}
      </nav>
    </div>

    <div class="flex-1 overflow-y-auto">
      <div class="p-6">
      {#if loading}
        <div class="text-center py-12">
          <div class="animate-spin rounded-full h-8 w-8 border-b-2 border-primary-600 mx-auto"></div>
          <p class="mt-4 text-gray-600">로딩 중...</p>
        </div>
      {:else if activeTab === 'headquarters'}
        <!-- 체인본부 탭 -->
        <div class="h-full flex flex-col space-y-6">
          {#if headquarters.length === 0}
            <div class="text-center py-12">
              <Building2 class="mx-auto h-12 w-12 text-gray-400" />
              <p class="mt-4 text-gray-500">등록된 체인본부가 없습니다.</p>
              <button 
                type="button" 
                class="mt-4 btn btn-primary"
                on:click={() => showCreateHqModal = true}
              >
                <Plus size="16" class="mr-2" />
                첫 번째 체인본부 생성
              </button>
            </div>
          {:else}
            <div class="grid grid-cols-1 lg:grid-cols-2 xl:grid-cols-3 gap-6">
              {#each headquarters as hq}
                <div class="card p-6 hover:shadow-md transition-shadow">
                  <div class="flex items-start justify-between mb-4">
                    <div class="flex-1">
                      <h3 class="text-lg font-semibold text-gray-900">{hq.name}</h3>
                      <p class="text-sm text-gray-600">{hq.code || 'N/A'}</p>
                    </div>
                    <div class="flex space-x-2">
                      <button
                        type="button"
                        class="text-indigo-600 hover:text-indigo-900"
                        on:click={() => editOrganization(hq)}
                        title="편집"
                      >
                        <Edit size="16" />
                      </button>
                      <button
                        type="button"
                        class="text-red-600 hover:text-red-900"
                        on:click={() => deleteOrganization(hq)}
                        title="삭제"
                      >
                        <Trash2 size="16" />
                      </button>
                    </div>
                  </div>

                  <div class="space-y-2 text-sm text-gray-600">
                    {#if hq.businessLicense}
                      <p><span class="font-medium">사업자번호:</span> {hq.businessLicense}</p>
                    {/if}
                    {#if hq.contactPhone}
                      <p><span class="font-medium">연락처:</span> {hq.contactPhone}</p>
                    {/if}
                    {#if hq.address}
                      <p><span class="font-medium">주소:</span> {hq.address}</p>
                    {/if}
                    {#if hq.description}
                      <p class="text-gray-500 line-clamp-2">{hq.description}</p>
                    {/if}
                  </div>

                  <div class="mt-4 pt-4 border-t border-gray-200">
                    <div class="flex items-center justify-between text-sm">
                      <span class="text-gray-500">생성일</span>
                      <span class="font-medium text-gray-900">
                        {hq.createdAt ? new Date(hq.createdAt).toLocaleDateString('ko-KR') : 'N/A'}
                      </span>
                    </div>
                  </div>
                </div>
              {/each}
            </div>
          {/if}
        </div>

      {:else if activeTab === 'individual-stores'}
        <!-- 개인매장 탭 -->
        <div class="h-full flex flex-col space-y-6">
          {#if individualStores.length === 0}
            <div class="text-center py-12">
              <Store class="mx-auto h-12 w-12 text-gray-400" />
              <p class="mt-4 text-gray-500">등록된 개인매장이 없습니다.</p>
              <button 
                type="button" 
                class="mt-4 btn btn-primary"
                on:click={() => showCreateStoreModal = true}
              >
                <Plus size="16" class="mr-2" />
                첫 번째 개인매장 생성
              </button>
            </div>
          {:else}
            <div class="grid grid-cols-1 lg:grid-cols-2 xl:grid-cols-3 gap-6">
              {#each individualStores as store}
                <div class="card p-6 hover:shadow-md transition-shadow">
                  <div class="flex items-start justify-between mb-4">
                    <div class="flex-1">
                      <h3 class="text-lg font-semibold text-gray-900">{store.name}</h3>
                      <p class="text-sm text-gray-600">{store.storeNumber || 'N/A'}</p>
                    </div>
                    <div class="flex space-x-2">
                      <button
                        type="button"
                        class="text-indigo-600 hover:text-indigo-900"
                        on:click={() => editOrganization(store)}
                        title="편집"
                      >
                        <Edit size="16" />
                      </button>
                      <button
                        type="button"
                        class="text-red-600 hover:text-red-900"
                        on:click={() => deleteOrganization(store)}
                        title="삭제"
                      >
                        <Trash2 size="16" />
                      </button>
                    </div>
                  </div>

                  <div class="space-y-2 text-sm text-gray-600">
                    {#if store.ownerName}
                      <p><span class="font-medium">점주:</span> {store.ownerName}</p>
                    {/if}
                    {#if store.businessLicense}
                      <p><span class="font-medium">사업자번호:</span> {store.businessLicense}</p>
                    {/if}
                    {#if store.phoneNumber}
                      <p><span class="font-medium">연락처:</span> {store.phoneNumber}</p>
                    {/if}
                    {#if store.address}
                      <p><span class="font-medium">주소:</span> {store.address}</p>
                    {/if}
                    {#if store.regionCode}
                      <p><span class="font-medium">지역:</span> {regions.find(r => r.code === store.regionCode)?.name || store.regionCode}</p>
                    {/if}
                  </div>

                  <div class="mt-4 pt-4 border-t border-gray-200">
                    <div class="flex items-center justify-between text-sm">
                      <span class="text-gray-500">생성일</span>
                      <span class="font-medium text-gray-900">
                        {store.createdAt ? new Date(store.createdAt).toLocaleDateString('ko-KR') : 'N/A'}
                      </span>
                    </div>
                  </div>
                </div>
              {/each}
            </div>
          {/if}
        </div>
      {/if}
      </div>
    </div>
  </div>
</div>

<!-- 체인본부 생성 모달 -->
<Modal bind:open={showCreateHqModal} title="체인본부 생성" size="lg">
  <div class="max-h-[60vh] overflow-y-auto">
    <form id="hq-form" on:submit|preventDefault={createHeadquarters} class="space-y-6">
      <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
        <div>
          <label for="hqCode" class="block text-sm font-medium text-gray-700">본사 코드 *</label>
          <input
            id="hqCode"
            type="text"
            required
            bind:value={hqForm.code}
            class="mt-1 input"
            placeholder="예: ABC"
          />
        </div>
        <div>
          <label for="hqName" class="block text-sm font-medium text-gray-700">본사명 *</label>
          <input
            id="hqName"
            type="text"
            required
            bind:value={hqForm.name}
            class="mt-1 input"
            placeholder="본사명을 입력하세요"
          />
        </div>
      </div>

      <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
        <div>
          <label for="businessLicense" class="block text-sm font-medium text-gray-700">사업자등록번호</label>
          <input
            id="businessLicense"
            type="text"
            bind:value={hqForm.businessLicense}
            class="mt-1 input"
            placeholder="000-00-00000"
          />
        </div>
        <div>
          <label for="ceoName" class="block text-sm font-medium text-gray-700">대표자명</label>
          <input
            id="ceoName"
            type="text"
            bind:value={hqForm.ceoName}
            class="mt-1 input"
            placeholder="대표자명을 입력하세요"
          />
        </div>
      </div>

      <div>
        <label for="address" class="block text-sm font-medium text-gray-700">주소</label>
        <input
          id="address"
          type="text"
          bind:value={hqForm.address}
          class="mt-1 input"
          placeholder="본사 주소를 입력하세요"
        />
      </div>

      <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
        <div>
          <label for="contactPhone" class="block text-sm font-medium text-gray-700">연락처</label>
          <input
            id="contactPhone"
            type="tel"
            bind:value={hqForm.contactPhone}
            class="mt-1 input"
            placeholder="02-000-0000"
          />
        </div>
        <div>
          <label for="website" class="block text-sm font-medium text-gray-700">웹사이트</label>
          <input
            id="website"
            type="url"
            bind:value={hqForm.website}
            class="mt-1 input"
            placeholder="https://www.example.com"
          />
        </div>
      </div>

      <div>
        <label for="description" class="block text-sm font-medium text-gray-700">설명</label>
        <textarea
          id="description"
          bind:value={hqForm.description}
          class="mt-1 input"
          rows="3"
          placeholder="본사에 대한 설명"
        ></textarea>
      </div>

      <hr class="border-gray-200" />

      <h4 class="text-lg font-medium text-gray-900">관리자 계정 정보</h4>

      <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
        <div>
          <label for="adminUsername" class="block text-sm font-medium text-gray-700">관리자 아이디 *</label>
          <input
            id="adminUsername"
            type="text"
            required
            bind:value={hqForm.adminUsername}
            class="mt-1 input"
            placeholder="관리자 아이디"
          />
        </div>
        <div>
          <label for="adminEmail" class="block text-sm font-medium text-gray-700">관리자 이메일 *</label>
          <input
            id="adminEmail"
            type="email"
            required
            bind:value={hqForm.adminEmail}
            class="mt-1 input"
            placeholder="admin@example.com"
          />
        </div>
      </div>

      <div>
        <label for="adminPassword" class="block text-sm font-medium text-gray-700">관리자 비밀번호 *</label>
        <input
          id="adminPassword"
          type="password"
          required
          bind:value={hqForm.adminPassword}
          class="mt-1 input"
          placeholder="비밀번호"
        />
      </div>
    </form>
  </div>
  
  <div slot="footer" class="px-4 py-3 bg-gray-50 border-t border-gray-200">
    <div class="flex justify-end space-x-3">
      <button
        type="button"
        class="btn btn-secondary"
        on:click={() => showCreateHqModal = false}
      >
        취소
      </button>
      <button 
        type="submit" 
        class="btn btn-primary"
        form="hq-form"
      >
        체인본부 생성
      </button>
    </div>
  </div>
</Modal>

<!-- 개인매장 생성 모달 -->
<Modal bind:open={showCreateStoreModal} title="개인매장 생성" size="lg">
  <div class="max-h-[60vh] overflow-y-auto">
    <form id="store-form" on:submit|preventDefault={createIndividualStore} class="space-y-6">
      <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
        <div>
          <label for="storeName" class="block text-sm font-medium text-gray-700">매장명 *</label>
          <input
            id="storeName"
            type="text"
            required
            bind:value={storeForm.name}
            class="mt-1 input"
            placeholder="매장명을 입력하세요"
          />
        </div>
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
          <label for="storeBusinessLicense" class="block text-sm font-medium text-gray-700">사업자등록번호</label>
          <input
            id="storeBusinessLicense"
            type="text"
            bind:value={storeForm.businessLicense}
            class="mt-1 input"
            placeholder="000-00-00000"
          />
        </div>
        <div>
          <label for="storePhoneNumber" class="block text-sm font-medium text-gray-700">연락처</label>
          <input
            id="storePhoneNumber"
            type="tel"
            bind:value={storeForm.phoneNumber}
            class="mt-1 input"
            placeholder="02-000-0000"
          />
        </div>
      </div>

      <div class="grid grid-cols-1 md:grid-cols-4 gap-4">
        <div class="md:col-span-3">
          <label for="storeAddress" class="block text-sm font-medium text-gray-700">주소</label>
          <input
            id="storeAddress"
            type="text"
            bind:value={storeForm.address}
            class="mt-1 input"
            placeholder="매장 주소를 입력하세요"
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
        <label for="storeDescription" class="block text-sm font-medium text-gray-700">설명</label>
        <textarea
          id="storeDescription"
          bind:value={storeForm.description}
          class="mt-1 input"
          rows="3"
          placeholder="매장에 대한 설명"
        ></textarea>
      </div>

      <hr class="border-gray-200" />

      <h4 class="text-lg font-medium text-gray-900">관리자 계정 정보</h4>

      <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
        <div>
          <label for="storeAdminUsername" class="block text-sm font-medium text-gray-700">관리자 아이디 *</label>
          <input
            id="storeAdminUsername"
            type="text"
            required
            bind:value={storeForm.adminUsername}
            class="mt-1 input"
            placeholder="관리자 아이디"
          />
        </div>
        <div>
          <label for="storeAdminEmail" class="block text-sm font-medium text-gray-700">관리자 이메일 *</label>
          <input
            id="storeAdminEmail"
            type="email"
            required
            bind:value={storeForm.adminEmail}
            class="mt-1 input"
            placeholder="admin@example.com"
          />
        </div>
      </div>

      <div>
        <label for="storeAdminPassword" class="block text-sm font-medium text-gray-700">관리자 비밀번호 *</label>
        <input
          id="storeAdminPassword"
          type="password"
          required
          bind:value={storeForm.adminPassword}
          class="mt-1 input"
          placeholder="비밀번호"
        />
      </div>
    </form>
  </div>
  
  <div slot="footer" class="px-4 py-3 bg-gray-50 border-t border-gray-200">
    <div class="flex justify-end space-x-3">
      <button
        type="button"
        class="btn btn-secondary"
        on:click={() => showCreateStoreModal = false}
      >
        취소
      </button>
      <button 
        type="submit" 
        class="btn btn-primary"
        form="store-form"
      >
        개인매장 생성
      </button>
    </div>
  </div>
</Modal>
