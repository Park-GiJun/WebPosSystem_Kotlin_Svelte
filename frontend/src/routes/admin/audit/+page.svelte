<script>
  import { onMount } from 'svelte';
  import { authStore } from '$lib/stores/auth.js';
  import { tabStore } from '$lib/stores/tabs.js';
  import { toastStore } from '$lib/stores/toast.js';
  import { auditLogApi } from '$lib/api/admin.js';
  import { 
    Search, Filter, Calendar, Eye, RefreshCw, BarChart3, 
    FileText, User, Database, Clock, MapPin, Monitor,
    ChevronLeft, ChevronRight, Download
  } from 'lucide-svelte';
  import AuditLogDetailModal from '$lib/components/Admin/AuditLogDetailModal.svelte';

  let auditLogs = [];
  let statistics = null;
  let loading = false;
  let totalCount = 0;
  let currentPage = 0;
  let pageSize = 20;
  let totalPages = 0;

  // 필터 상태
  let searchTerm = '';
  let selectedTable = '';
  let selectedAction = '';
  let selectedUser = '';
  let startDate = '';
  let endDate = '';
  let showFilters = false;
  let showStatistics = false;
  let showDetailModal = false;
  let selectedAuditLog = null;

  // 인증 토큰
  let authToken = '';
  authStore.subscribe(state => {
    authToken = state.token || '';
  });

  // 테이블 목록
  const tables = [
    { value: '', label: '모든 테이블' },
    { value: 'users', label: '사용자' },
    { value: 'stores', label: '매장' },
    { value: 'permissions', label: '권한' },
    { value: 'headquarters', label: '본사' },
    { value: 'pos_systems', label: 'POS 시스템' },
    { value: 'menus', label: '메뉴' }
  ];

  // 액션 타입 목록
  const actionTypes = [
    { value: '', label: '모든 액션' },
    { value: 'INSERT', label: '생성' },
    { value: 'UPDATE', label: '수정' },
    { value: 'DELETE', label: '삭제' }
  ];

  onMount(() => {
    tabStore.setActiveTab('ADMIN_AUDIT');
    
    // 기본 날짜 설정 (최근 7일)
    const now = new Date();
    const weekAgo = new Date(now.getTime() - 7 * 24 * 60 * 60 * 1000);
    
    endDate = now.toISOString().slice(0, 16);
    startDate = weekAgo.toISOString().slice(0, 16);
    
    loadAuditLogs();
    loadStatistics();
  });

  async function loadAuditLogs() {
    if (!authToken) {
      console.warn('인증 토큰이 없습니다.');
      return;
    }

    try {
      loading = true;
      console.log('📋 감사 로그 조회 중...');

      const params = {
        page: currentPage,
        size: pageSize
      };

      if (searchTerm) params.searchTerm = searchTerm;
      if (selectedTable) params.tableName = selectedTable;
      if (selectedAction) params.actionType = selectedAction;
      if (selectedUser) params.userName = selectedUser;
      if (startDate) params.startDate = startDate + ':00';
      if (endDate) params.endDate = endDate + ':00';

      const data = await auditLogApi.getAuditLogs(params, authToken);
      
      auditLogs = data.auditLogs || [];
      totalCount = data.totalCount || 0;
      totalPages = data.totalPages || 0;
      console.log('✅ 감사 로그 로드 완료:', auditLogs.length, '개');
    } catch (error) {
      console.error('❌ 감사 로그 로드 실패:', error);
      toastStore.error('감사 로그를 불러오는데 실패했습니다: ' + error.message);
      auditLogs = [];
    } finally {
      loading = false;
    }
  }

  async function loadStatistics() {
    if (!authToken) return;

    try {
      statistics = await auditLogApi.getStatistics(7, authToken);
      console.log('📊 통계 로드 완료:', statistics);
    } catch (error) {
      console.error('❌ 통계 로드 실패:', error);
    }
  }

  async function viewDetail(auditLog) {
    try {
      const detail = await auditLogApi.getAuditLogById(auditLog.id, authToken);
      openDetailModal(detail);
    } catch (error) {
      console.error('❌ 상세 조회 실패:', error);
      toastStore.error('상세 정보 조회에 실패했습니다: ' + error.message);
    }
  }

  function openDetailModal(auditLog) {
    selectedAuditLog = auditLog;
    showDetailModal = true;
  }

  function applyFilters() {
    currentPage = 0;
    loadAuditLogs();
  }

  function resetFilters() {
    searchTerm = '';
    selectedTable = '';
    selectedAction = '';
    selectedUser = '';
    
    const now = new Date();
    const weekAgo = new Date(now.getTime() - 7 * 24 * 60 * 60 * 1000);
    endDate = now.toISOString().slice(0, 16);
    startDate = weekAgo.toISOString().slice(0, 16);
    
    currentPage = 0;
    loadAuditLogs();
  }

  function changePage(newPage) {
    currentPage = newPage;
    loadAuditLogs();
  }

  function getActionColor(actionType) {
    const colors = {
      'INSERT': 'bg-green-100 text-green-800',
      'UPDATE': 'bg-blue-100 text-blue-800',
      'DELETE': 'bg-red-100 text-red-800'
    };
    return colors[actionType] || 'bg-gray-100 text-gray-800';
  }

  function getTableIcon(tableName) {
    const icons = {
      'users': User,
      'stores': Database,
      'permissions': FileText,
      'headquarters': Database,
      'pos_systems': Monitor,
      'menus': FileText
    };
    return icons[tableName] || Database;
  }

  function formatDateTime(dateTime) {
    return new Date(dateTime).toLocaleString('ko-KR', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit',
      second: '2-digit'
    });
  }

  function formatChangedFields(changedFields) {
    if (!changedFields) return '';
    
    try {
      const fields = typeof changedFields === 'string' ? JSON.parse(changedFields) : changedFields;
      return Object.keys(fields).join(', ');
    } catch (e) {
      return '';
    }
  }

  // 검색어 디바운스
  let searchDebounceTimer;
  $: {
    clearTimeout(searchDebounceTimer);
    searchDebounceTimer = setTimeout(() => {
      if (searchTerm !== undefined) {
        applyFilters();
      }
    }, 500);
  }
</script>

<svelte:head>
  <title>감사 로그 - WebPos</title>
</svelte:head>

<div class="h-full flex flex-col space-y-6">
  <!-- 헤더 -->
  <div class="flex items-center justify-between">
    <div>
      <h1 class="text-2xl font-bold text-gray-900">감사 로그</h1>
      <p class="text-gray-600 mt-1">시스템에서 발생한 모든 변경 사항을 추적합니다.</p>
    </div>
    <div class="flex space-x-3">
      <button 
        type="button" 
        class="btn btn-secondary"
        on:click={() => showStatistics = !showStatistics}
      >
        <BarChart3 size="16" class="mr-2" />
        통계 {showStatistics ? '숨기기' : '보기'}
      </button>
      <button 
        type="button" 
        class="btn btn-secondary"
        on:click={() => showFilters = !showFilters}
      >
        <Filter size="16" class="mr-2" />
        필터 {showFilters ? '숨기기' : '보기'}
      </button>
      <button 
        type="button" 
        class="btn btn-primary"
        on:click={loadAuditLogs}
        disabled={loading}
      >
        <RefreshCw size="16" class="mr-2" class:animate-spin={loading} />
        새로고침
      </button>
    </div>
  </div>

  <!-- 통계 카드 -->
  {#if showStatistics && statistics}
    <div class="grid grid-cols-1 md:grid-cols-4 gap-6">
      <div class="card p-6">
        <div class="flex items-center">
          <div class="p-3 rounded-full bg-blue-100">
            <FileText class="h-6 w-6 text-blue-600" />
          </div>
          <div class="ml-4">
            <p class="text-sm font-medium text-gray-600">총 이벤트</p>
            <p class="text-2xl font-bold text-gray-900">{statistics.totalEvents}</p>
          </div>
        </div>
      </div>

      <div class="card p-6">
        <div class="flex items-center">
          <div class="p-3 rounded-full bg-green-100">
            <Database class="h-6 w-6 text-green-600" />
          </div>
          <div class="ml-4">
            <p class="text-sm font-medium text-gray-600">생성</p>
            <p class="text-2xl font-bold text-gray-900">{statistics.actionStatistics?.INSERT || 0}</p>
          </div>
        </div>
      </div>

      <div class="card p-6">
        <div class="flex items-center">
          <div class="p-3 rounded-full bg-yellow-100">
            <FileText class="h-6 w-6 text-yellow-600" />
          </div>
          <div class="ml-4">
            <p class="text-sm font-medium text-gray-600">수정</p>
            <p class="text-2xl font-bold text-gray-900">{statistics.actionStatistics?.UPDATE || 0}</p>
          </div>
        </div>
      </div>

      <div class="card p-6">
        <div class="flex items-center">
          <div class="p-3 rounded-full bg-red-100">
            <FileText class="h-6 w-6 text-red-600" />
          </div>
          <div class="ml-4">
            <p class="text-sm font-medium text-gray-600">삭제</p>
            <p class="text-2xl font-bold text-gray-900">{statistics.actionStatistics?.DELETE || 0}</p>
          </div>
        </div>
      </div>
    </div>
  {/if}

  <!-- 필터 패널 -->
  {#if showFilters}
    <div class="card p-6">
      <div class="grid grid-cols-1 md:grid-cols-3 lg:grid-cols-6 gap-4">
        <!-- 검색어 -->
        <div class="relative">
          <Search size="20" class="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400" />
          <input
            type="text"
            placeholder="검색..."
            bind:value={searchTerm}
            class="input pl-10"
          />
        </div>

        <!-- 테이블 필터 -->
        <select bind:value={selectedTable} class="input">
          {#each tables as table}
            <option value={table.value}>{table.label}</option>
          {/each}
        </select>

        <!-- 액션 필터 -->
        <select bind:value={selectedAction} class="input">
          {#each actionTypes as action}
            <option value={action.value}>{action.label}</option>
          {/each}
        </select>

        <!-- 사용자 필터 -->
        <input
          type="text"
          placeholder="사용자명..."
          bind:value={selectedUser}
          class="input"
        />

        <!-- 시작 날짜 -->
        <input
          type="datetime-local"
          bind:value={startDate}
          class="input"
        />

        <!-- 종료 날짜 -->
        <input
          type="datetime-local"
          bind:value={endDate}
          class="input"
        />
      </div>

      <div class="flex justify-end space-x-3 mt-4">
        <button type="button" class="btn btn-secondary" on:click={resetFilters}>
          초기화
        </button>
        <button type="button" class="btn btn-primary" on:click={applyFilters}>
          적용
        </button>
      </div>
    </div>
  {/if}

  <!-- 감사 로그 목록 -->
  <div class="card flex-1 flex flex-col min-h-0">
    <div class="p-6 border-b border-gray-200 flex-shrink-0">
      <div class="flex items-center justify-between">
        <h2 class="text-lg font-medium text-gray-900">
          감사 로그 목록 
          <span class="text-sm text-gray-500">({totalCount.toLocaleString()}건)</span>
        </h2>
        <div class="flex items-center space-x-2 text-sm text-gray-500">
          <Clock size="16" />
          <span>최근 업데이트: {new Date().toLocaleString('ko-KR')}</span>
        </div>
      </div>
    </div>

    <div class="flex-1 overflow-y-auto">
      {#if loading}
        <div class="p-12 text-center">
          <div class="animate-spin rounded-full h-8 w-8 border-b-2 border-primary-600 mx-auto"></div>
          <p class="mt-4 text-gray-600">로딩 중...</p>
        </div>
      {:else if auditLogs.length === 0}
        <div class="p-12 text-center">
          <FileText class="mx-auto h-12 w-12 text-gray-400" />
          <p class="mt-4 text-gray-500">조건에 맞는 감사 로그가 없습니다.</p>
        </div>
      {:else}
        <div class="overflow-x-auto">
          <table class="min-w-full divide-y divide-gray-200">
            <thead class="bg-gray-50 sticky top-0">
              <tr>
                <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  테이블/레코드
                </th>
                <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  액션
                </th>
                <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  사용자
                </th>
                <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  변경 필드
                </th>
                <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  실행 시간
                </th>
                <th class="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase tracking-wider">
                  작업
                </th>
              </tr>
            </thead>
            <tbody class="bg-white divide-y divide-gray-200">
              {#each auditLogs as log}
                <tr class="hover:bg-gray-50">
                  <td class="px-6 py-4 whitespace-nowrap">
                    <div class="flex items-center">
                      <svelte:component 
                        this={getTableIcon(log.tableName)} 
                        size="16" 
                        class="text-gray-400 mr-3" 
                      />
                      <div>
                        <div class="text-sm font-medium text-gray-900">
                          {log.tableDisplayName}
                        </div>
                        <div class="text-sm text-gray-500">
                          ID: {log.recordId}
                        </div>
                      </div>
                    </div>
                  </td>
                  <td class="px-6 py-4 whitespace-nowrap">
                    <span class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium {getActionColor(log.actionType)}">
                      {log.actionDisplayName}
                    </span>
                  </td>
                  <td class="px-6 py-4 whitespace-nowrap">
                    <div class="flex items-center">
                      <User size="16" class="text-gray-400 mr-2" />
                      <div>
                        <div class="text-sm font-medium text-gray-900">
                          {log.userName || '시스템'}
                        </div>
                        {#if log.ipAddress}
                          <div class="text-xs text-gray-500 flex items-center">
                            <MapPin size="12" class="mr-1" />
                            {log.ipAddress}
                          </div>
                        {/if}
                      </div>
                    </div>
                  </td>
                  <td class="px-6 py-4">
                    <div class="text-sm text-gray-900">
                      {log.description || ''}
                    </div>
                    {#if formatChangedFields(log.changedFields)}
                      <div class="text-xs text-gray-500 mt-1">
                        변경: {formatChangedFields(log.changedFields)}
                      </div>
                    {/if}
                  </td>
                  <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                    {formatDateTime(log.createdAt)}
                  </td>
                  <td class="px-6 py-4 whitespace-nowrap text-right text-sm font-medium">
                    <button
                      type="button"
                      class="text-blue-600 hover:text-blue-900"
                      on:click={() => viewDetail(log)}
                      title="상세 보기"
                    >
                      <Eye size="16" />
                    </button>
                  </td>
                </tr>
              {/each}
            </tbody>
          </table>
        </div>
      {/if}
    </div>

    <!-- 페이지네이션 -->
    {#if totalPages > 1}
      <div class="px-6 py-4 border-t border-gray-200 flex-shrink-0">
        <div class="flex items-center justify-between">
          <div class="text-sm text-gray-700">
            {currentPage * pageSize + 1} - {Math.min((currentPage + 1) * pageSize, totalCount)} / {totalCount} 건
          </div>
          <div class="flex items-center space-x-2">
            <button
              type="button"
              class="btn btn-secondary p-2"
              disabled={currentPage === 0}
              on:click={() => changePage(currentPage - 1)}
            >
              <ChevronLeft size="16" />
            </button>
            
            {#each Array.from({length: Math.min(5, totalPages)}, (_, i) => i + Math.max(0, currentPage - 2)) as page}
              {#if page < totalPages}
                <button
                  type="button"
                  class="btn p-2 {page === currentPage ? 'btn-primary' : 'btn-secondary'}"
                  on:click={() => changePage(page)}
                >
                  {page + 1}
                </button>
              {/if}
            {/each}
            
            <button
              type="button"
              class="btn btn-secondary p-2"
              disabled={currentPage >= totalPages - 1}
              on:click={() => changePage(currentPage + 1)}
            >
              <ChevronRight size="16" />
            </button>
          </div>
        </div>
      </div>
    {/if}
  </div>
</div>

<!-- 감사 로그 상세 모달 -->
<AuditLogDetailModal
  bind:open={showDetailModal}
  auditLog={selectedAuditLog}
  on:close={() => showDetailModal = false}
/>
