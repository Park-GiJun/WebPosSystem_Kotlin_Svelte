<script>
  import { onMount } from 'svelte';
  import { authStore } from '$lib/stores/auth.js';
  import { tabStore } from '$lib/stores/tabs.js';
  import { toastStore } from '$lib/stores/toast.js';
  import { organizationApi } from '$lib/api/admin.js';
  import { Plus, Building2, Edit, Trash2 } from 'lucide-svelte';
  import Modal from '$lib/components/Common/Modal.svelte';

  let loading = false;
  let headquarters = [];
  let showCreateModal = false;

  // 인증 상태 구독
  let authToken = '';
  authStore.subscribe(state => {
    authToken = state.token || '';
  });

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

  onMount(() => {
    tabStore.setActiveTab('ADMIN_ORG_HQ');
    loadHeadquarters();
  });

  async function loadHeadquarters() {
    if (!authToken) {
      console.warn('인증 토큰이 없습니다.');
      return;
    }

    try {
      loading = true;
      console.log('🏢 본사 목록 조회 중...');
      
      const response = await organizationApi.getOrganizations({}, authToken);
      
      if (response) {
        headquarters = response.headquarters.map(hq => ({
          ...hq,
          type: 'HEADQUARTERS'
        }));
        console.log('✅ 본사 목록 로드 완료:', headquarters.length, '개');
      } else {
        console.warn('⚠️ 응답이 비어있습니다:', response);
        headquarters = [];
      }
    } catch (error) {
      console.error('❌ 본사 목록 로드 실패:', error);
      toastStore.error('본사 목록을 불러오는데 실패했습니다: ' + error.message);
      headquarters = [];
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
      
      // 백엔드 API에 맞는 요청 구조로 변경
      const request = {
        name: hqForm.name,
        businessNumber: hqForm.businessLicense, // businessLicense -> businessNumber 변경
        address: hqForm.address,
        phoneNumber: hqForm.contactPhone,
        email: hqForm.adminEmail,
        adminUsername: hqForm.adminUsername
      };
      
      console.log('📤 요청 데이터:', request);
      
      const response = await fetch('/api/v1/admin/organizations/headquarters', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${authToken}`
        },
        body: JSON.stringify(request)
      });
      
      console.log('📥 응답 상태:', response.status);
      
      if (response.ok) {
        const newHq = await response.json();
        console.log('✅ 생성된 본사:', newHq);
        
        headquarters = [...headquarters, {
          ...newHq,
          type: 'HEADQUARTERS'
        }];
        toastStore.success('체인본부가 성공적으로 생성되었습니다.');
        showCreateModal = false;
        resetForm();
        console.log('✅ 체인본부 생성 완료');
      } else {
        const errorText = await response.text();
        console.error('❌ 응답 오류:', errorText);
        let errorMessage = '체인본부 생성에 실패했습니다.';
        
        try {
          const errorData = JSON.parse(errorText);
          errorMessage = errorData.message || errorMessage;
        } catch (parseError) {
          errorMessage = errorText || errorMessage;
        }
        
        throw new Error(errorMessage);
      }
    } catch (error) {
      console.error('❌ 체인본부 생성 실패:', error);
      toastStore.error('체인본부 생성에 실패했습니다: ' + error.message);
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
      
      headquarters = headquarters.filter(o => o.id !== org.id);
      toastStore.success('조직이 삭제되었습니다.');
      console.log('✅ 조직 삭제 완료');
    } catch (error) {
      console.error('❌ 조직 삭제 실패:', error);
      toastStore.error('조직 삭제에 실패했습니다: ' + error.message);
    }
  }

  function resetForm() {
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
</script>

<svelte:head>
  <title>본사 관리 - WebPos</title>
</svelte:head>

<div class="p-6">
  <!-- 헤더 -->
  <div class="flex items-center justify-between mb-6">
    <div>
      <h1 class="text-2xl font-bold text-gray-900">본사 관리</h1>
      <p class="text-gray-600 mt-1">체인본부를 생성하고 관리합니다.</p>
    </div>
    <button 
      type="button" 
      class="bg-blue-600 hover:bg-blue-700 text-white px-4 py-2 rounded-lg flex items-center"
      on:click={() => showCreateModal = true}
    >
      <Plus size="16" class="mr-2" />
      체인본부 생성
    </button>
  </div>

  {#if loading}
    <div class="text-center py-12">
      <div class="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600 mx-auto"></div>
      <p class="mt-4 text-gray-600">로딩 중...</p>
    </div>
  {:else if headquarters.length === 0}
    <div class="text-center py-12">
      <Building2 class="mx-auto h-12 w-12 text-gray-400" />
      <p class="mt-4 text-gray-500">등록된 체인본부가 없습니다.</p>
      <button 
        type="button" 
        class="mt-4 bg-blue-600 hover:bg-blue-700 text-white px-4 py-2 rounded-lg flex items-center mx-auto"
        on:click={() => showCreateModal = true}
      >
        <Plus size="16" class="mr-2" />
        첫 번째 체인본부 생성
      </button>
    </div>
  {:else}
    <div class="grid grid-cols-1 lg:grid-cols-2 xl:grid-cols-3 gap-6">
      {#each headquarters as hq}
        <div class="bg-white shadow rounded-lg p-6 hover:shadow-md transition-shadow">
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

<!-- 체인본부 생성 모달 -->
<Modal bind:open={showCreateModal} title="체인본부 생성" size="lg">
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
            class="mt-1 w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500"
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
            class="mt-1 w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500"
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
            class="mt-1 w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500"
            placeholder="000-00-00000"
          />
        </div>
        <div>
          <label for="ceoName" class="block text-sm font-medium text-gray-700">대표자명</label>
          <input
            id="ceoName"
            type="text"
            bind:value={hqForm.ceoName}
            class="mt-1 w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500"
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
          class="mt-1 w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500"
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
            class="mt-1 w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500"
            placeholder="02-000-0000"
          />
        </div>
        <div>
          <label for="website" class="block text-sm font-medium text-gray-700">웹사이트</label>
          <input
            id="website"
            type="url"
            bind:value={hqForm.website}
            class="mt-1 w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500"
            placeholder="https://www.example.com"
          />
        </div>
      </div>

      <div>
        <label for="description" class="block text-sm font-medium text-gray-700">설명</label>
        <textarea
          id="description"
          bind:value={hqForm.description}
          class="mt-1 w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500"
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
            class="mt-1 w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500"
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
            class="mt-1 w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500"
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
          class="mt-1 w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500"
          placeholder="비밀번호"
        />
      </div>
    </form>
  </div>
  
  <div slot="footer" class="px-4 py-3 bg-gray-50 border-t border-gray-200">
    <div class="flex justify-end space-x-3">
      <button
        type="button"
        class="bg-gray-300 hover:bg-gray-400 text-gray-700 px-4 py-2 rounded-lg"
        on:click={() => showCreateModal = false}
      >
        취소
      </button>
      <button 
        type="submit" 
        class="bg-blue-600 hover:bg-blue-700 text-white px-4 py-2 rounded-lg"
        form="hq-form"
      >
        체인본부 생성
      </button>
    </div>
  </div>
</Modal>
