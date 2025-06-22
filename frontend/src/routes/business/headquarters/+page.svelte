<script>
  import { onMount } from 'svelte';
  import { authStore } from '$lib/stores/auth.js';
  import { tabStore } from '$lib/stores/tabs.js';
  import { toastStore } from '$lib/stores/toast.js';
  import { Building, Plus, Search, Edit, Trash2, Eye, Users, MapPin, Phone, Mail } from 'lucide-svelte';

  let headquarters = [];
  let loading = false;
  let searchTerm = '';

  // 탭 활성화
  onMount(async () => {
    tabStore.setActiveTab('BUSINESS_HQ');
    await loadHeadquarters();
  });

  // 본사 데이터 로드 (임시 데이터)
  async function loadHeadquarters() {
    try {
      loading = true;
      
      // 임시 더미 데이터
      headquarters = [
        {
          id: 'HQ001',
          name: 'ABC 유통 본사',
          businessNumber: '123-45-67890',
          ceoName: '김대표',
          address: '서울시 강남구 테헤란로 123',
          phone: '02-1234-5678',
          email: 'ceo@abc.com',
          storeCount: 15,
          employeeCount: 48,
          isActive: true,
          registrationDate: '2020-01-15'
        },
        {
          id: 'HQ002',
          name: 'XYZ 체인 본사',
          businessNumber: '987-65-43210',
          ceoName: '이사장',
          address: '부산시 해운대구 센텀로 456',
          phone: '051-987-6543',
          email: 'ceo@xyz.com',
          storeCount: 8,
          employeeCount: 24,
          isActive: true,
          registrationDate: '2021-03-22'
        }
      ];
      
    } catch (error) {
      console.error('본사 데이터 로드 실패:', error);
      toastStore.add('본사 데이터를 불러오는데 실패했습니다.', 'error');
      headquarters = [];
    } finally {
      loading = false;
    }
  }

  // 필터링된 본사 목록
  $: filteredHeadquarters = headquarters.filter(hq => 
    hq.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
    hq.businessNumber.includes(searchTerm) ||
    hq.ceoName.toLowerCase().includes(searchTerm.toLowerCase())
  );

  // 통계 정보
  $: stats = {
    total: headquarters.length,
    active: headquarters.filter(hq => hq.isActive).length,
    totalStores: headquarters.reduce((sum, hq) => sum + hq.storeCount, 0),
    totalEmployees: headquarters.reduce((sum, hq) => sum + hq.employeeCount, 0)
  };

  function handleCreate() {
    toastStore.add('본사 등록 기능은 준비 중입니다.', 'info');
  }

  function handleEdit(hq) {
    toastStore.add(`${hq.name} 수정 기능은 준비 중입니다.`, 'info');
  }

  function handleView(hq) {
    toastStore.add(`${hq.name} 상세보기 기능은 준비 중입니다.`, 'info');
  }

  function handleDelete(hq) {
    if (confirm(`정말로 "${hq.name}"을(를) 삭제하시겠습니까?`)) {
      toastStore.add(`${hq.name} 삭제 기능은 준비 중입니다.`, 'info');
    }
  }
</script>

<svelte:head>
  <title>본사 관리 - WebPos</title>
</svelte:head>

<div class="space-y-6">
  <!-- 헤더 -->
  <div class="flex justify-between items-center">
    <div>
      <h1 class="text-2xl font-bold text-gray-900 flex items-center">
        <Building class="mr-3" size="28" />
        본사 관리
      </h1>
      <p class="text-gray-600 mt-1">체인 본사 정보를 관리합니다</p>
    </div>
    <button
      type="button"
      class="btn btn-primary"
      on:click={handleCreate}
    >
      <Plus size="20" class="mr-2" />
      본사 등록
    </button>
  </div>

  <!-- 통계 카드 -->
  <div class="grid grid-cols-1 md:grid-cols-4 gap-4">
    <div class="card p-4">
      <div class="flex items-center">
        <div class="p-2 bg-blue-100 rounded-lg">
          <Building class="h-6 w-6 text-blue-600" />
        </div>
        <div class="ml-3">
          <p class="text-sm font-medium text-gray-500">전체 본사</p>
          <p class="text-2xl font-bold text-gray-900">{stats.total}</p>
        </div>
      </div>
    </div>

    <div class="card p-4">
      <div class="flex items-center">
        <div class="p-2 bg-green-100 rounded-lg">
          <Building class="h-6 w-6 text-green-600" />
        </div>
        <div class="ml-3">
          <p class="text-sm font-medium text-gray-500">활성 본사</p>
          <p class="text-2xl font-bold text-gray-900">{stats.active}</p>
        </div>
      </div>
    </div>

    <div class="card p-4">
      <div class="flex items-center">
        <div class="p-2 bg-purple-100 rounded-lg">
          <MapPin class="h-6 w-6 text-purple-600" />
        </div>
        <div class="ml-3">
          <p class="text-sm font-medium text-gray-500">총 매장수</p>
          <p class="text-2xl font-bold text-gray-900">{stats.totalStores}</p>
        </div>
      </div>
    </div>

    <div class="card p-4">
      <div class="flex items-center">
        <div class="p-2 bg-orange-100 rounded-lg">
          <Users class="h-6 w-6 text-orange-600" />
        </div>
        <div class="ml-3">
          <p class="text-sm font-medium text-gray-500">총 직원수</p>
          <p class="text-2xl font-bold text-gray-900">{stats.totalEmployees}</p>
        </div>
      </div>
    </div>
  </div>

  <!-- 검색 필터 -->
  <div class="card p-6">
    <div class="relative max-w-md">
      <Search size="20" class="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400" />
      <input
        type="text"
        placeholder="본사명, 사업자번호, 대표자명 검색..."
        bind:value={searchTerm}
        class="input pl-10"
      />
    </div>
  </div>

  <!-- 본사 목록 -->
  <div class="card">
    {#if loading}
      <div class="p-12 text-center">
        <div class="animate-spin rounded-full h-12 w-12 border-b-2 border-primary-600 mx-auto mb-4"></div>
        <p class="text-gray-500">본사 정보를 불러오는 중...</p>
      </div>
    {:else if filteredHeadquarters.length === 0}
      <div class="p-12 text-center">
        <Building class="mx-auto h-12 w-12 text-gray-400 mb-4" />
        <h3 class="text-lg font-medium text-gray-900 mb-2">본사가 없습니다</h3>
        <p class="text-gray-500 mb-4">새로운 본사를 등록해보세요.</p>
        <button
          type="button"
          class="btn btn-primary"
          on:click={handleCreate}
        >
          <Plus size="20" class="mr-2" />
          본사 등록
        </button>
      </div>
    {:else}
      <div class="overflow-x-auto">
        <table class="min-w-full divide-y divide-gray-200">
          <thead class="bg-gray-50">
            <tr>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">본사 정보</th>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">대표자</th>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">연락처</th>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">매장/직원</th>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">상태</th>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">작업</th>
            </tr>
          </thead>
          <tbody class="bg-white divide-y divide-gray-200">
            {#each filteredHeadquarters as hq}
              <tr class="hover:bg-gray-50">
                <td class="px-6 py-4 whitespace-nowrap">
                  <div class="flex items-center">
                    <div class="h-10 w-10 bg-blue-100 rounded-lg flex items-center justify-center mr-3">
                      <Building class="h-5 w-5 text-blue-600" />
                    </div>
                    <div>
                      <div class="text-sm font-medium text-gray-900">{hq.name}</div>
                      <div class="text-sm text-gray-500">사업자: {hq.businessNumber}</div>
                    </div>
                  </div>
                </td>
                <td class="px-6 py-4 whitespace-nowrap">
                  <div class="text-sm text-gray-900">{hq.ceoName}</div>
                  <div class="text-sm text-gray-500">등록일: {hq.registrationDate}</div>
                </td>
                <td class="px-6 py-4 whitespace-nowrap">
                  <div class="flex items-center text-sm text-gray-900 mb-1">
                    <Phone size="14" class="mr-1" />
                    {hq.phone}
                  </div>
                  <div class="flex items-center text-sm text-gray-500">
                    <Mail size="14" class="mr-1" />
                    {hq.email}
                  </div>
                </td>
                <td class="px-6 py-4 whitespace-nowrap">
                  <div class="text-sm text-gray-900">매장 {hq.storeCount}개</div>
                  <div class="text-sm text-gray-500">직원 {hq.employeeCount}명</div>
                </td>
                <td class="px-6 py-4 whitespace-nowrap">
                  <span class="inline-flex px-2 py-1 text-xs font-semibold rounded-full {hq.isActive ? 'bg-green-100 text-green-800' : 'bg-red-100 text-red-800'}">
                    {hq.isActive ? '활성' : '비활성'}
                  </span>
                </td>
                <td class="px-6 py-4 whitespace-nowrap text-sm font-medium">
                  <div class="flex space-x-2">
                    <button
                      type="button"
                      class="text-indigo-600 hover:text-indigo-900"
                      on:click={() => handleView(hq)}
                      title="상세보기"
                    >
                      <Eye size="16" />
                    </button>
                    <button
                      type="button"
                      class="text-yellow-600 hover:text-yellow-900"
                      on:click={() => handleEdit(hq)}
                      title="수정"
                    >
                      <Edit size="16" />
                    </button>
                    <button
                      type="button"
                      class="text-red-600 hover:text-red-900"
                      on:click={() => handleDelete(hq)}
                      title="삭제"
                    >
                      <Trash2 size="16" />
                    </button>
                  </div>
                </td>
              </tr>
            {/each}
          </tbody>
        </table>
      </div>
    {/if}
  </div>
</div>
