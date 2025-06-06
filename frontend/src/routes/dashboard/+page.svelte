<script>
  import { authStore } from '$lib/stores/auth.js';
  import { onMount } from 'svelte';
  import { BarChart3, Users, Store, DollarSign } from 'lucide-svelte';

  $: user = $authStore.user;

  let stats = {
    totalStores: 0,
    totalUsers: 0,
    totalSales: 0,
    activePos: 0
  };

  onMount(async () => {
    // 대시보드 통계 데이터 로드
    await loadDashboardStats();
  });

  async function loadDashboardStats() {
    try {
      const response = await fetch('/api/v1/dashboard/stats', {
        headers: {
          'Authorization': `Bearer ${$authStore.token}`
        }
      });
      
      if (response.ok) {
        const data = await response.json();
        stats = data;
      }
    } catch (error) {
      console.error('Failed to load dashboard stats:', error);
    }
  }
</script>

<svelte:head>
  <title>대시보드 - WebPos</title>
</svelte:head>

<div class="space-y-6">
  <!-- 환영 메시지 -->
  <div class="card p-6">
    <h1 class="text-2xl font-bold text-gray-900 mb-2">
      안녕하세요, {user?.username || '사용자'}님!
    </h1>
    <p class="text-gray-600">
      WebPos 시스템에 오신 것을 환영합니다. 오늘도 좋은 하루 되세요.
    </p>
  </div>

  <!-- 통계 카드 -->
  <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
    <!-- 총 매장 수 -->
    <div class="card p-6">
      <div class="flex items-center">
        <div class="p-3 rounded-full bg-blue-100">
          <Store class="h-6 w-6 text-blue-600" />
        </div>
        <div class="ml-4">
          <p class="text-sm font-medium text-gray-600">총 매장 수</p>
          <p class="text-2xl font-bold text-gray-900">{stats.totalStores}</p>
        </div>
      </div>
    </div>

    <!-- 총 사용자 수 -->
    <div class="card p-6">
      <div class="flex items-center">
        <div class="p-3 rounded-full bg-green-100">
          <Users class="h-6 w-6 text-green-600" />
        </div>
        <div class="ml-4">
          <p class="text-sm font-medium text-gray-600">총 사용자 수</p>
          <p class="text-2xl font-bold text-gray-900">{stats.totalUsers}</p>
        </div>
      </div>
    </div>

    <!-- 오늘 매출 -->
    <div class="card p-6">
      <div class="flex items-center">
        <div class="p-3 rounded-full bg-yellow-100">
          <DollarSign class="h-6 w-6 text-yellow-600" />
        </div>
        <div class="ml-4">
          <p class="text-sm font-medium text-gray-600">오늘 매출</p>
          <p class="text-2xl font-bold text-gray-900">
            {stats.totalSales.toLocaleString()}원
          </p>
        </div>
      </div>
    </div>

    <!-- 활성 POS -->
    <div class="card p-6">
      <div class="flex items-center">
        <div class="p-3 rounded-full bg-purple-100">
          <BarChart3 class="h-6 w-6 text-purple-600" />
        </div>
        <div class="ml-4">
          <p class="text-sm font-medium text-gray-600">활성 POS</p>
          <p class="text-2xl font-bold text-gray-900">{stats.activePos}</p>
        </div>
      </div>
    </div>
  </div>

  <!-- 최근 활동 -->
  <div class="grid grid-cols-1 lg:grid-cols-2 gap-6">
    <!-- 최근 로그인 -->
    <div class="card p-6">
      <h3 class="text-lg font-medium text-gray-900 mb-4">최근 로그인</h3>
      <div class="space-y-3">
        <div class="flex items-center justify-between py-2 border-b border-gray-100 last:border-b-0">
          <div>
            <p class="text-sm font-medium text-gray-900">홍길동</p>
            <p class="text-xs text-gray-500">매장관리자</p>
          </div>
          <p class="text-xs text-gray-500">2시간 전</p>
        </div>
        <div class="flex items-center justify-between py-2 border-b border-gray-100 last:border-b-0">
          <div>
            <p class="text-sm font-medium text-gray-900">김영희</p>
            <p class="text-xs text-gray-500">본사관리자</p>
          </div>
          <p class="text-xs text-gray-500">3시간 전</p>
        </div>
        <div class="flex items-center justify-between py-2 border-b border-gray-100 last:border-b-0">
          <div>
            <p class="text-sm font-medium text-gray-900">이철수</p>
            <p class="text-xs text-gray-500">직원</p>
          </div>
          <p class="text-xs text-gray-500">4시간 전</p>
        </div>
      </div>
    </div>

    <!-- 시스템 알림 -->
    <div class="card p-6">
      <h3 class="text-lg font-medium text-gray-900 mb-4">시스템 알림</h3>
      <div class="space-y-3">
        <div class="p-3 bg-yellow-50 border border-yellow-200 rounded-lg">
          <p class="text-sm font-medium text-yellow-800">시스템 점검 예정</p>
          <p class="text-xs text-yellow-600 mt-1">
            2024년 12월 15일 오전 2:00 ~ 4:00 시스템 점검이 예정되어 있습니다.
          </p>
        </div>
        <div class="p-3 bg-blue-50 border border-blue-200 rounded-lg">
          <p class="text-sm font-medium text-blue-800">새 기능 업데이트</p>
          <p class="text-xs text-blue-600 mt-1">
            재고 관리 기능이 추가되었습니다. 메뉴에서 확인해보세요.
          </p>
        </div>
        <div class="p-3 bg-green-50 border border-green-200 rounded-lg">
          <p class="text-sm font-medium text-green-800">백업 완료</p>
          <p class="text-xs text-green-600 mt-1">
            일일 데이터 백업이 성공적으로 완료되었습니다.
          </p>
        </div>
      </div>
    </div>
  </div>
</div>
