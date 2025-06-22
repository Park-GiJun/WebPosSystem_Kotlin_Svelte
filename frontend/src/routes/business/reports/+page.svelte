<script>
  import { onMount } from 'svelte';
  import { authStore } from '$lib/stores/auth.js';
  import { tabStore } from '$lib/stores/tabs.js';
  import { BarChart3, TrendingUp, TrendingDown, DollarSign, Package, Users, Calendar, Download, RefreshCw } from 'lucide-svelte';

  let loading = false;
  let selectedPeriod = 'month';
  let selectedStore = 'all';

  // 임시 통계 데이터
  let dashboardData = {
    totalSales: 15750000,
    todaySales: 2450000,
    totalOrders: 1234,
    avgOrderValue: 12765,
    topProducts: [
      { name: '아메리카노', sales: 3250000, quantity: 2150 },
      { name: '카페라떼', sales: 2890000, quantity: 1820 },
      { name: '크로아상', sales: 1560000, quantity: 890 },
      { name: '샌드위치', sales: 1234000, quantity: 567 },
      { name: '케이크', sales: 980000, quantity: 234 }
    ],
    salesTrend: [
      { period: '1주전', sales: 1890000 },
      { period: '6일전', sales: 2100000 },
      { period: '5일전', sales: 1950000 },
      { period: '4일전', sales: 2300000 },
      { period: '3일전', sales: 2150000 },
      { period: '2일전', sales: 2450000 },
      { period: '어제', sales: 2680000 },
      { period: '오늘', sales: 1890000 }
    ],
    stores: [
      { id: 'all', name: '전체 매장' },
      { id: 'ST001', name: '강남점' },
      { id: 'ST002', name: '홍대점' },
      { id: 'ST003', name: '역삼점' }
    ]
  };

  // 탭 활성화
  onMount(async () => {
    tabStore.setActiveTab('BUSINESS_REPORTS');
    await loadReports();
  });

  async function loadReports() {
    loading = true;
    // 실제로는 API 호출
    setTimeout(() => {
      loading = false;
    }, 1000);
  }

  function handleRefresh() {
    loadReports();
  }

  function handleExport() {
    // 임시 알림
    alert('보고서 내보내기 기능은 준비 중입니다.');
  }

  function formatCurrency(amount) {
    return new Intl.NumberFormat('ko-KR', {
      style: 'currency',
      currency: 'KRW'
    }).format(amount);
  }

  function calculatePercentChange(current, previous) {
    if (previous === 0) return 0;
    return ((current - previous) / previous * 100).toFixed(1);
  }
</script>

<svelte:head>
  <title>통계/보고서 - WebPos</title>
</svelte:head>

<div class="space-y-6">
  <!-- 헤더 -->
  <div class="flex justify-between items-center">
    <div>
      <h1 class="text-2xl font-bold text-gray-900 flex items-center">
        <BarChart3 class="mr-3" size="28" />
        통계/보고서
      </h1>
      <p class="text-gray-600 mt-1">매출 및 운영 통계를 확인합니다</p>
    </div>
    <div class="flex space-x-3">
      <button
        type="button"
        class="btn btn-secondary"
        on:click={handleRefresh}
        disabled={loading}
      >
        <RefreshCw size="20" class="mr-2" class:animate-spin={loading} />
        새로고침
      </button>
      <button
        type="button"
        class="btn btn-primary"
        on:click={handleExport}
      >
        <Download size="20" class="mr-2" />
        내보내기
      </button>
    </div>
  </div>

  <!-- 필터 -->
  <div class="card p-6">
    <div class="grid grid-cols-1 md:grid-cols-3 gap-4">
      <div>
        <label for="period" class="block text-sm font-medium text-gray-700 mb-1">기간</label>
        <select id="period" bind:value={selectedPeriod} class="input">
          <option value="day">일별</option>
          <option value="week">주별</option>
          <option value="month">월별</option>
          <option value="year">연별</option>
        </select>
      </div>
      
      <div>
        <label for="store" class="block text-sm font-medium text-gray-700 mb-1">매장</label>
        <select id="store" bind:value={selectedStore} class="input">
          {#each dashboardData.stores as store}
            <option value={store.id}>{store.name}</option>
          {/each}
        </select>
      </div>
      
      <div>
        <label for="dateRange" class="block text-sm font-medium text-gray-700 mb-1">날짜 범위</label>
        <input type="date" id="dateRange" class="input" />
      </div>
    </div>
  </div>

  {#if loading}
    <div class="card p-12 text-center">
      <div class="animate-spin rounded-full h-12 w-12 border-b-2 border-primary-600 mx-auto mb-4"></div>
      <p class="text-gray-500">통계 데이터를 불러오는 중...</p>
    </div>
  {:else}
    <!-- 주요 지표 카드 -->
    <div class="grid grid-cols-1 md:grid-cols-4 gap-4">
      <div class="card p-6">
        <div class="flex items-center">
          <div class="p-3 bg-blue-100 rounded-full">
            <DollarSign class="h-8 w-8 text-blue-600" />
          </div>
          <div class="ml-4">
            <p class="text-sm font-medium text-gray-500">총 매출</p>
            <p class="text-2xl font-bold text-gray-900">{formatCurrency(dashboardData.totalSales)}</p>
            <div class="flex items-center mt-1">
              <TrendingUp class="h-4 w-4 text-green-500 mr-1" />
              <span class="text-sm text-green-600">+12.5%</span>
            </div>
          </div>
        </div>
      </div>

      <div class="card p-6">
        <div class="flex items-center">
          <div class="p-3 bg-green-100 rounded-full">
            <Calendar class="h-8 w-8 text-green-600" />
          </div>
          <div class="ml-4">
            <p class="text-sm font-medium text-gray-500">오늘 매출</p>
            <p class="text-2xl font-bold text-gray-900">{formatCurrency(dashboardData.todaySales)}</p>
            <div class="flex items-center mt-1">
              <TrendingUp class="h-4 w-4 text-green-500 mr-1" />
              <span class="text-sm text-green-600">+8.3%</span>
            </div>
          </div>
        </div>
      </div>

      <div class="card p-6">
        <div class="flex items-center">
          <div class="p-3 bg-purple-100 rounded-full">
            <Package class="h-8 w-8 text-purple-600" />
          </div>
          <div class="ml-4">
            <p class="text-sm font-medium text-gray-500">총 주문</p>
            <p class="text-2xl font-bold text-gray-900">{dashboardData.totalOrders.toLocaleString()}</p>
            <div class="flex items-center mt-1">
              <TrendingDown class="h-4 w-4 text-red-500 mr-1" />
              <span class="text-sm text-red-600">-2.1%</span>
            </div>
          </div>
        </div>
      </div>

      <div class="card p-6">
        <div class="flex items-center">
          <div class="p-3 bg-orange-100 rounded-full">
            <Users class="h-8 w-8 text-orange-600" />
          </div>
          <div class="ml-4">
            <p class="text-sm font-medium text-gray-500">평균 주문액</p>
            <p class="text-2xl font-bold text-gray-900">{formatCurrency(dashboardData.avgOrderValue)}</p>
            <div class="flex items-center mt-1">
              <TrendingUp class="h-4 w-4 text-green-500 mr-1" />
              <span class="text-sm text-green-600">+15.2%</span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 차트 영역 -->
    <div class="grid grid-cols-1 lg:grid-cols-2 gap-6">
      <!-- 매출 추이 -->
      <div class="card p-6">
        <h3 class="text-lg font-semibold text-gray-900 mb-4 flex items-center">
          <TrendingUp class="mr-2" size="20" />
          매출 추이
        </h3>
        <div class="space-y-3">
          {#each dashboardData.salesTrend as item, index}
            <div class="flex items-center justify-between">
              <span class="text-sm text-gray-600">{item.period}</span>
              <div class="flex items-center space-x-2">
                <div class="w-32 bg-gray-200 rounded-full h-2">
                  <div 
                    class="bg-blue-600 h-2 rounded-full transition-all duration-300"
                    style="width: {(item.sales / Math.max(...dashboardData.salesTrend.map(s => s.sales))) * 100}%"
                  ></div>
                </div>
                <span class="text-sm font-medium text-gray-900 w-20 text-right">
                  {formatCurrency(item.sales)}
                </span>
              </div>
            </div>
          {/each}
        </div>
      </div>

      <!-- 인기 상품 -->
      <div class="card p-6">
        <h3 class="text-lg font-semibold text-gray-900 mb-4 flex items-center">
          <Package class="mr-2" size="20" />
          인기 상품 TOP 5
        </h3>
        <div class="space-y-4">
          {#each dashboardData.topProducts as product, index}
            <div class="flex items-center justify-between">
              <div class="flex items-center space-x-3">
                <div class="w-8 h-8 bg-blue-100 rounded-full flex items-center justify-center">
                  <span class="text-sm font-bold text-blue-600">{index + 1}</span>
                </div>
                <div>
                  <p class="text-sm font-medium text-gray-900">{product.name}</p>
                  <p class="text-xs text-gray-500">{product.quantity}개 판매</p>
                </div>
              </div>
              <div class="text-right">
                <p class="text-sm font-medium text-gray-900">{formatCurrency(product.sales)}</p>
                <div class="w-16 bg-gray-200 rounded-full h-1 mt-1">
                  <div 
                    class="bg-green-500 h-1 rounded-full"
                    style="width: {(product.sales / dashboardData.topProducts[0].sales) * 100}%"
                  ></div>
                </div>
              </div>
            </div>
          {/each}
        </div>
      </div>
    </div>

    <!-- 상세 테이블 -->
    <div class="card">
      <div class="px-6 py-4 border-b border-gray-200">
        <h3 class="text-lg font-semibold text-gray-900">상세 매출 내역</h3>
      </div>
      <div class="overflow-x-auto">
        <table class="min-w-full divide-y divide-gray-200">
          <thead class="bg-gray-50">
            <tr>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">날짜</th>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">매장</th>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">매출액</th>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">주문수</th>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">평균 주문액</th>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">증감률</th>
            </tr>
          </thead>
          <tbody class="bg-white divide-y divide-gray-200">
            <tr>
              <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900">2025-06-22</td>
              <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900">강남점</td>
              <td class="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-900">₩2,450,000</td>
              <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900">187</td>
              <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900">₩13,102</td>
              <td class="px-6 py-4 whitespace-nowrap">
                <span class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-green-100 text-green-800">
                  <TrendingUp class="mr-1" size="12" />
                  +8.3%
                </span>
              </td>
            </tr>
            <!-- 더 많은 데이터 행들... -->
          </tbody>
        </table>
      </div>
    </div>
  {/if}
</div>
