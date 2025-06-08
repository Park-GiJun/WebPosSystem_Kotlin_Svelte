<script>
  import { onMount } from 'svelte';
  import { authStore } from '$lib/stores/auth.js';
  import { dashboardApi } from '$lib/api/business.js';
  import { toastStore } from '$lib/stores/toast.js';
  import LoadingSpinner from '$lib/components/Common/LoadingSpinner.svelte';
  import { 
    Users, 
    Store, 
    Monitor, 
    DollarSign, 
    TrendingUp, 
    Activity,
    Clock,
    AlertCircle,
    CheckCircle
  } from 'lucide-svelte';

  let authState = {};
  let dashboardStats = null;
  let loading = true;
  let error = null;

  // 인증 상태 구독
  authStore.subscribe(state => {
    authState = state;
  });

  onMount(async () => {
    await loadDashboardData();
  });

  async function loadDashboardData() {
    try {
      loading = true;
      error = null;

      if (!authState.token) {
        throw new Error('인증 토큰이 없습니다.');
      }

      console.log('📊 대시보드 데이터 로드 중...');
      const stats = await dashboardApi.getStats(authState.token);
      
      dashboardStats = stats;
      console.log('✅ 대시보드 데이터 로드 완료:', stats);
      
    } catch (err) {
      console.error('❌ 대시보드 데이터 로드 실패:', err);
      error = err.message;
      toastStore.error('대시보드 데이터를 불러오는데 실패했습니다.');
      
      // 에러 발생 시 더미 데이터로 대체 (개발용)
      dashboardStats = {
        totalStores: 15,
        totalUsers: 48,
        activePosDevices: 32,
        totalSales: 2840000,
        todaySales: 150000,
        recentActivities: [
          {
            id: 1,
            type: 'store_created',
            message: '새 매장 "청담점"이 등록되었습니다.',
            timestamp: new Date().toISOString(),
            status: 'success'
          },
          {
            id: 2,
            type: 'pos_maintenance',
            message: 'POS-001 정기 점검이 완료되었습니다.',
            timestamp: new Date(Date.now() - 3600000).toISOString(),
            status: 'info'
          },
          {
            id: 3,
            type: 'user_login',
            message: '관리자 "admin"이 로그인했습니다.',
            timestamp: new Date(Date.now() - 7200000).toISOString(),
            status: 'success'
          }
        ]
      };
    } finally {
      loading = false;
    }
  }

  function formatCurrency(amount) {
    return new Intl.NumberFormat('ko-KR', {
      style: 'currency',
      currency: 'KRW'
    }).format(amount);
  }

  function formatTime(timestamp) {
    return new Intl.DateTimeFormat('ko-KR', {
      hour: '2-digit',
      minute: '2-digit',
      second: '2-digit'
    }).format(new Date(timestamp));
  }

  function getActivityIcon(type) {
    switch (type) {
      case 'store_created':
        return Store;
      case 'pos_maintenance':
        return Monitor;
      case 'user_login':
        return Users;
      default:
        return Activity;
    }
  }

  function getStatusColor(status) {
    switch (status) {
      case 'success':
        return 'text-green-600';
      case 'warning':
        return 'text-yellow-600';
      case 'error':
        return 'text-red-600';
      default:
        return 'text-blue-600';
    }
  }

  async function refreshData() {
    await loadDashboardData();
    toastStore.success('데이터가 새로고침되었습니다.');
  }
</script>

<svelte:head>
  <title>대시보드 - WebPos</title>
</svelte:head>

<div class="max-w-7xl mx-auto p-6">
  <!-- 헤더 -->
  <div class="mb-8">
    <div class="flex items-center justify-between">
      <div>
        <h1 class="text-3xl font-bold text-gray-900">대시보드</h1>
        <p class="text-gray-600 mt-2">
          안녕하세요, <span class="font-semibold">{authState.user?.username || 'Guest'}</span>님! 
          오늘도 좋은 하루 되세요.
        </p>
      </div>
      
      <div class="flex items-center space-x-4">
        <button
          on:click={refreshData}
          disabled={loading}
          class="flex items-center px-4 py-2 bg-indigo-600 text-white rounded-lg hover:bg-indigo-700 transition-colors disabled:opacity-50"
        >
          <Activity class="w-4 h-4 mr-2" />
          새로고침
        </button>
        
        <div class="text-sm text-gray-500">
          마지막 업데이트: {new Date().toLocaleTimeString('ko-KR')}
        </div>
      </div>
    </div>
  </div>

  {#if loading}
    <div class="flex justify-center items-center h-64">
      <LoadingSpinner size="lg" />
    </div>
  {:else if error}
    <div class="bg-red-50 border border-red-200 rounded-lg p-4">
      <div class="flex items-center">
        <AlertCircle class="w-5 h-5 text-red-600 mr-2" />
        <p class="text-red-800 font-medium">데이터 로드 오류</p>
      </div>
      <p class="text-red-700 mt-2">{error}</p>
      <button
        on:click={refreshData}
        class="mt-3 px-4 py-2 bg-red-600 text-white rounded-lg hover:bg-red-700 transition-colors"
      >
        다시 시도
      </button>
    </div>
  {:else if dashboardStats}
    <!-- 통계 카드 -->
    <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">
      <!-- 총 매장 수 -->
      <div class="bg-white rounded-lg shadow p-6">
        <div class="flex items-center">
          <div class="p-3 rounded-full bg-blue-100">
            <Store class="w-6 h-6 text-blue-600" />
          </div>
          <div class="ml-4">
            <p class="text-sm font-medium text-gray-600">총 매장 수</p>
            <p class="text-2xl font-bold text-gray-900">{dashboardStats.totalStores}</p>
          </div>
        </div>
      </div>

      <!-- 총 사용자 수 -->
      <div class="bg-white rounded-lg shadow p-6">
        <div class="flex items-center">
          <div class="p-3 rounded-full bg-green-100">
            <Users class="w-6 h-6 text-green-600" />
          </div>
          <div class="ml-4">
            <p class="text-sm font-medium text-gray-600">총 사용자 수</p>
            <p class="text-2xl font-bold text-gray-900">{dashboardStats.totalUsers}</p>
          </div>
        </div>
      </div>

      <!-- 활성 POS -->
      <div class="bg-white rounded-lg shadow p-6">
        <div class="flex items-center">
          <div class="p-3 rounded-full bg-purple-100">
            <Monitor class="w-6 h-6 text-purple-600" />
          </div>
          <div class="ml-4">
            <p class="text-sm font-medium text-gray-600">활성 POS</p>
            <p class="text-2xl font-bold text-gray-900">{dashboardStats.activePosDevices}</p>
          </div>
        </div>
      </div>

      <!-- 오늘 매출 -->
      <div class="bg-white rounded-lg shadow p-6">
        <div class="flex items-center">
          <div class="p-3 rounded-full bg-yellow-100">
            <DollarSign class="w-6 h-6 text-yellow-600" />
          </div>
          <div class="ml-4">
            <p class="text-sm font-medium text-gray-600">오늘 매출</p>
            <p class="text-2xl font-bold text-gray-900">{formatCurrency(dashboardStats.todaySales)}</p>
          </div>
        </div>
      </div>
    </div>

    <!-- 최근 활동 -->
    <div class="grid grid-cols-1 lg:grid-cols-2 gap-6">
      <!-- 최근 활동 목록 -->
      <div class="bg-white rounded-lg shadow">
        <div class="px-6 py-4 border-b border-gray-200">
          <h3 class="text-lg font-medium text-gray-900 flex items-center">
            <Activity class="w-5 h-5 mr-2" />
            최근 활동
          </h3>
        </div>
        <div class="p-6">
          {#if dashboardStats.recentActivities && dashboardStats.recentActivities.length > 0}
            <div class="space-y-4">
              {#each dashboardStats.recentActivities as activity}
                <div class="flex items-start space-x-3">
                  <div class="flex-shrink-0">
                    <svelte:component 
                      this={getActivityIcon(activity.type)} 
                      class="w-5 h-5 {getStatusColor(activity.status)}" 
                    />
                  </div>
                  <div class="flex-1 min-w-0">
                    <p class="text-sm text-gray-900">{activity.message}</p>
                    <p class="text-xs text-gray-500 mt-1">
                      {formatTime(activity.timestamp)}
                    </p>
                  </div>
                </div>
              {/each}
            </div>
          {:else}
            <p class="text-gray-500 text-center py-8">최근 활동이 없습니다.</p>
          {/if}
        </div>
      </div>

      <!-- 시스템 상태 -->
      <div class="bg-white rounded-lg shadow">
        <div class="px-6 py-4 border-b border-gray-200">
          <h3 class="text-lg font-medium text-gray-900 flex items-center">
            <CheckCircle class="w-5 h-5 mr-2" />
            시스템 상태
          </h3>
        </div>
        <div class="p-6">
          <div class="space-y-4">
            <div class="flex items-center justify-between">
              <span class="text-sm text-gray-600">데이터베이스</span>
              <span class="flex items-center text-sm text-green-600">
                <CheckCircle class="w-4 h-4 mr-1" />
                정상
              </span>
            </div>
            <div class="flex items-center justify-between">
              <span class="text-sm text-gray-600">POS 연결</span>
              <span class="flex items-center text-sm text-green-600">
                <CheckCircle class="w-4 h-4 mr-1" />
                정상
              </span>
            </div>
            <div class="flex items-center justify-between">
              <span class="text-sm text-gray-600">캐시 서버</span>
              <span class="flex items-center text-sm text-green-600">
                <CheckCircle class="w-4 h-4 mr-1" />
                정상
              </span>
            </div>
            <div class="flex items-center justify-between">
              <span class="text-sm text-gray-600">결제 시스템</span>
              <span class="flex items-center text-sm text-green-600">
                <CheckCircle class="w-4 h-4 mr-1" />
                정상
              </span>
            </div>
          </div>
        </div>
      </div>
    </div>
  {/if}
</div>
