<script>
  import { onMount } from 'svelte';
  import { authStore } from '$lib/stores/auth.js';
  import { tabStore } from '$lib/stores/tabs.js';
  import { toastStore } from '$lib/stores/toast.js';
  import { userApi } from '$lib/api/admin.js';
  import { Plus, Search, Filter, Edit, Trash2, Users, UserCheck, AlertTriangle, ShoppingBag } from 'lucide-svelte';

  let customers = [];
  let loading = true;
  let searchTerm = '';
  let filterStatus = 'all';
  let currentPage = 0;
  let pageSize = 20;
  let totalCount = 0;

  // 초기화 완료 플래그
  let initialized = false;

  // 인증 상태 구독
  let authToken = '';
  authStore.subscribe(state => {
    authToken = state.token || '';
  });

  // 탭 활성화
  onMount(() => {
    tabStore.setActiveTab('ADMIN_USERS_CUSTOMERS');
    loadCustomers().then(() => {
      initialized = true;
    });
  });

  async function loadCustomers() {
    if (!authToken) {
      console.warn('인증 토큰이 없습니다.');
      loading = false;
      toastStore.error('인증이 필요합니다.');
      return;
    }

    try {
      loading = true;
      
      const params = {
        page: currentPage,
        size: pageSize,
        userType: 'CUSTOMER' // 고객 사용자만 조회
      };

      if (searchTerm.trim()) {
        params.search = searchTerm.trim();
      }

      if (filterStatus !== 'all') {
        params.status = filterStatus;
      }

      console.log('🔍 고객 사용자 목록 조회 중...', params);
      
      const response = await userApi.getUsers(params, authToken);
      
      if (response && response.users) {
        // 고객 사용자만 필터링 (백엔드에서 필터링이 안된 경우를 대비)
        customers = response.users.filter(user => 
          user.organizationType === 'CUSTOMER' ||
          user.roles?.some(role => role === 'CUSTOMER') ||
          (!user.roles?.some(role => role.includes('ADMIN')))
        );
        totalCount = response.totalCount || customers.length;
        console.log('✅ 고객 사용자 목록 로드 완료:', customers.length, '개');
      } else {
        console.warn('⚠️ 응답에 users 필드가 없습니다:', response);
        customers = [];
        totalCount = 0;
      }
    } catch (error) {
      console.error('❌ 고객 사용자 목록 로드 실패:', error);
      if (error.message.includes('API 호출실패')) {
        toastStore.error('API 호출실패: 백엔드 서버에 연결할 수 없습니다.');
      } else {
        toastStore.error('고객 목록을 불러오는데 실패했습니다: ' + error.message);
      }
      customers = [];
      totalCount = 0;
    } finally {
      loading = false;
    }
  }

  // 검색 및 필터 변경 핸들러
  let searchTimeout;
  
  function handleSearchChange() {
    if (initialized && !loading) {
      clearTimeout(searchTimeout);
      searchTimeout = setTimeout(() => {
        currentPage = 0;
        loadCustomers();
      }, 300);
    }
  }
  
  function handleFilterChange() {
    if (initialized && !loading) {
      currentPage = 0;
      loadCustomers();
    }
  }

  function getStatusColor(status) {
    const colors = {
      'ACTIVE': 'bg-green-100 text-green-800',
      'INACTIVE': 'bg-gray-100 text-gray-800',
      'SUSPENDED': 'bg-red-100 text-red-800',
      'PENDING_VERIFICATION': 'bg-yellow-100 text-yellow-800',
      'LOCKED': 'bg-orange-100 text-orange-800'
    };
    return colors[status] || 'bg-gray-100 text-gray-800';
  }

  function getStatusText(status) {
    const texts = {
      'ACTIVE': '활성',
      'INACTIVE': '비활성',
      'SUSPENDED': '정지',
      'PENDING_VERIFICATION': '인증대기',
      'LOCKED': '잠김'
    };
    return texts[status] || status;
  }

  async function viewCustomer(customer) {
    console.log('View customer details:', customer);
    toastStore.info('고객 상세 조회 기능은 준비 중입니다.');
  }

  async function suspendCustomer(customer) {
    if (!confirm(`정말로 "${customer.username}" 고객을 정지하시겠습니까?`)) {
      return;
    }

    console.log('Suspend customer:', customer.username);
    toastStore.info('고객 정지 기능은 준비 중입니다.');
  }

  function nextPage() {
    if ((currentPage + 1) * pageSize < totalCount) {
      currentPage += 1;
      loadCustomers();
    }
  }

  function prevPage() {
    if (currentPage > 0) {
      currentPage -= 1;
      loadCustomers();
    }
  }

  // 통계 계산 (고객 사용자만)
  $: totalCustomers = customers.length;
  $: activeCustomers = customers.filter(c => c.userStatus === 'ACTIVE').length;
  $: verifiedCustomers = customers.filter(c => c.isEmailVerified).length;
  $: pendingCustomers = customers.filter(c => c.userStatus === 'PENDING_VERIFICATION').length;
</script>

<svelte:head>
  <title>고객 사용자 관리 - WebPos</title>
</svelte:head>

<div class="h-full flex flex-col space-y-6">
  <!-- 헤더 -->
  <div class="flex items-center justify-between">
    <div>
      <h1 class="text-2xl font-bold text-gray-900">고객 사용자 관리</h1>
      <p class="text-gray-600 mt-1">시스템을 이용하는 고객 사용자를 관리합니다.</p>
    </div>
    <div class="flex items-center space-x-3">
      <span class="text-sm text-gray-500">고객 가입은 별도 프로세스를 통해 처리됩니다</span>
    </div>
  </div>

  <!-- 통계 카드 -->
  <div class="grid grid-cols-1 md:grid-cols-4 gap-6">
    <div class="card p-6">
      <div class="flex items-center">
        <div class="p-3 rounded-full bg-blue-100">
          <Users class="h-6 w-6 text-blue-600" />
        </div>
        <div class="ml-4">
          <p class="text-sm font-medium text-gray-600">총 고객수</p>
          <p class="text-2xl font-bold text-gray-900">{totalCount}</p>
        </div>
      </div>
    </div>

    <div class="card p-6">
      <div class="flex items-center">
        <div class="p-3 rounded-full bg-green-100">
          <UserCheck class="h-6 w-6 text-green-600" />
        </div>
        <div class="ml-4">
          <p class="text-sm font-medium text-gray-600">활성 고객</p>
          <p class="text-2xl font-bold text-gray-900">{activeCustomers}</p>
        </div>
      </div>
    </div>

    <div class="card p-6">
      <div class="flex items-center">
        <div class="p-3 rounded-full bg-purple-100">
          <ShoppingBag class="h-6 w-6 text-purple-600" />
        </div>
        <div class="ml-4">
          <p class="text-sm font-medium text-gray-600">인증완료</p>
          <p class="text-2xl font-bold text-gray-900">{verifiedCustomers}</p>
        </div>
      </div>
    </div>

    <div class="card p-6">
      <div class="flex items-center">
        <div class="p-3 rounded-full bg-yellow-100">
          <AlertTriangle class="h-6 w-6 text-yellow-600" />
        </div>
        <div class="ml-4">
          <p class="text-sm font-medium text-gray-600">인증 대기</p>
          <p class="text-2xl font-bold text-gray-900">{pendingCustomers}</p>
        </div>
      </div>
    </div>
  </div>

  <!-- 검색 및 필터 -->
  <div class="card p-6">
    <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
      <!-- 검색 -->
      <div class="relative">
        <Search size="20" class="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400" />
        <input
          type="text"
          placeholder="고객명, 이메일 또는 전화번호 검색..."
          bind:value={searchTerm}
          on:input={handleSearchChange}
          class="input pl-10"
        />
      </div>

      <!-- 상태 필터 -->
      <select bind:value={filterStatus} class="input" on:change={handleFilterChange}>
        <option value="all">모든 상태</option>
        <option value="ACTIVE">활성</option>
        <option value="INACTIVE">비활성</option>
        <option value="SUSPENDED">정지</option>
        <option value="PENDING_VERIFICATION">인증 대기</option>
        <option value="LOCKED">잠김</option>
      </select>
    </div>
  </div>

  <!-- 고객 목록 -->
  <div class="card overflow-hidden flex-1 flex flex-col min-h-0">
    {#if loading}
      <div class="p-12 text-center">
        <div class="animate-spin rounded-full h-8 w-8 border-b-2 border-primary-600 mx-auto"></div>
        <p class="mt-4 text-gray-600">고객 목록을 불러오는 중...</p>
      </div>
    {:else if customers.length === 0}
      <div class="p-12 text-center">
        <Users class="mx-auto h-12 w-12 text-gray-400" />
        <p class="mt-4 text-gray-500">조건에 맞는 고객이 없습니다.</p>
        <p class="text-sm text-gray-400 mt-2">고객: 웹사이트나 앱을 통해 가입한 일반 사용자</p>
      </div>
    {:else}
      <div class="flex-1 overflow-y-auto">
        <table class="min-w-full divide-y divide-gray-200">
          <thead class="bg-gray-50">
            <tr>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                고객정보
              </th>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                연락처
              </th>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                상태
              </th>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                최근 로그인
              </th>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                가입일
              </th>
              <th class="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase tracking-wider">
                작업
              </th>
            </tr>
          </thead>
          <tbody class="bg-white divide-y divide-gray-200">
            {#each customers as customer}
              <tr class="hover:bg-gray-50">
                <td class="px-6 py-4 whitespace-nowrap">
                  <div class="flex items-center">
                    <div class="w-10 h-10 bg-purple-600 rounded-full flex items-center justify-center">
                      <span class="text-white font-medium text-sm">
                        {customer.username?.charAt(0)?.toUpperCase() || 'C'}
                      </span>
                    </div>
                    <div class="ml-4">
                      <div class="text-sm font-medium text-gray-900">{customer.username || 'N/A'}</div>
                      <div class="text-sm text-gray-500">{customer.displayName || customer.fullName || ''}</div>
                      {#if customer.isEmailVerified}
                        <span class="inline-flex items-center px-2 py-0.5 rounded text-xs font-medium bg-green-100 text-green-800 mt-1">
                          인증완료
                        </span>
                      {:else}
                        <span class="inline-flex items-center px-2 py-0.5 rounded text-xs font-medium bg-yellow-100 text-yellow-800 mt-1">
                          인증대기
                        </span>
                      {/if}
                    </div>
                  </div>
                </td>
                <td class="px-6 py-4 whitespace-nowrap">
                  <div class="text-sm text-gray-900">{customer.email || 'N/A'}</div>
                  <div class="text-sm text-gray-500">{customer.phoneNumber || 'N/A'}</div>
                </td>
                <td class="px-6 py-4 whitespace-nowrap">
                  <span class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium {getStatusColor(customer.userStatus)}">
                    {getStatusText(customer.userStatus)}
                  </span>
                  {#if customer.isLocked}
                    <div class="text-xs text-red-600 mt-1">
                      {customer.lockedUntil ? `잠김 (${new Date(customer.lockedUntil).toLocaleString('ko-KR')})` : '잠김'}
                    </div>
                  {/if}
                  {#if customer.failedLoginAttempts > 0}
                    <div class="text-xs text-orange-600 mt-1">
                      로그인 실패: {customer.failedLoginAttempts}회
                    </div>
                  {/if}
                </td>
                <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                  {customer.lastLoginAt ? new Date(customer.lastLoginAt).toLocaleDateString('ko-KR') : '없음'}
                </td>
                <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                  {customer.createdAt ? new Date(customer.createdAt).toLocaleDateString('ko-KR') : 'N/A'}
                </td>
                <td class="px-6 py-4 whitespace-nowrap text-right text-sm font-medium">
                  <div class="flex justify-end space-x-2">
                    <button
                      type="button"
                      class="text-indigo-600 hover:text-indigo-900"
                      on:click={() => viewCustomer(customer)}
                      title="상세보기"
                    >
                      <Search size="16" />
                    </button>
                    {#if customer.userStatus !== 'SUSPENDED'}
                      <button
                        type="button"
                        class="text-red-600 hover:text-red-900"
                        on:click={() => suspendCustomer(customer)}
                        title="정지"
                      >
                        <AlertTriangle size="16" />
                      </button>
                    {/if}
                  </div>
                </td>
              </tr>
            {/each}
          </tbody>
        </table>
      </div>

      <!-- 페이지네이션 -->
      <div class="bg-white px-4 py-3 flex items-center justify-between border-t border-gray-200 sm:px-6">
        <div class="flex-1 flex justify-between sm:hidden">
          <button
            on:click={prevPage}
            disabled={currentPage === 0}
            class="relative inline-flex items-center px-4 py-2 border border-gray-300 text-sm font-medium rounded-md text-gray-700 bg-white hover:bg-gray-50 disabled:opacity-50"
          >
            이전
          </button>
          <button
            on:click={nextPage}
            disabled={(currentPage + 1) * pageSize >= totalCount}
            class="ml-3 relative inline-flex items-center px-4 py-2 border border-gray-300 text-sm font-medium rounded-md text-gray-700 bg-white hover:bg-gray-50 disabled:opacity-50"
          >
            다음
          </button>
        </div>
        <div class="hidden sm:flex-1 sm:flex sm:items-center sm:justify-between">
          <div>
            <p class="text-sm text-gray-700">
              총 <span class="font-medium">{totalCount}</span>개 중 
              <span class="font-medium">{currentPage * pageSize + 1}</span>-<span class="font-medium">{Math.min((currentPage + 1) * pageSize, totalCount)}</span> 표시
            </p>
          </div>
          <div>
            <nav class="relative z-0 inline-flex rounded-md shadow-sm -space-x-px" aria-label="Pagination">
              <button
                on:click={prevPage}
                disabled={currentPage === 0}
                class="relative inline-flex items-center px-2 py-2 rounded-l-md border border-gray-300 bg-white text-sm font-medium text-gray-500 hover:bg-gray-50 disabled:opacity-50"
              >
                이전
              </button>
              <span class="relative inline-flex items-center px-4 py-2 border border-gray-300 bg-white text-sm font-medium text-gray-700">
                {currentPage + 1}
              </span>
              <button
                on:click={nextPage}
                disabled={(currentPage + 1) * pageSize >= totalCount}
                class="relative inline-flex items-center px-2 py-2 rounded-r-md border border-gray-300 bg-white text-sm font-medium text-gray-500 hover:bg-gray-50 disabled:opacity-50"
              >
                다음
              </button>
            </nav>
          </div>
        </div>
      </div>
    {/if}
  </div>
</div>
