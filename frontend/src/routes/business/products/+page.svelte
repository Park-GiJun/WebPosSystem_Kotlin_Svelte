<script>
  import { onMount } from 'svelte';
  import { authStore } from '$lib/stores/auth.js';
  import { tabStore } from '$lib/stores/tabs.js';
  import { toastStore } from '$lib/stores/toast.js';
  import { ProductApi } from '$lib/api/product.js';
  import { Package, Plus, Search, Edit, Trash2, Eye, AlertTriangle, TrendingUp, TrendingDown } from 'lucide-svelte';
  import CreateProductModal from '$lib/components/Business/CreateProductModal.svelte';
  import EditProductModal from '$lib/components/Business/EditProductModal.svelte';
  import ProductDetailModal from '$lib/components/Business/ProductDetailModal.svelte';

  let products = [];
  let loading = false;
  let searchTerm = '';
  let selectedCategory = 'all';
  let selectedStatus = 'all';
  let sortBy = 'displayOrder';
  let sortDirection = 'ASC';
  let currentPage = 0;
  let pageSize = 20;
  let totalCount = 0;
  let totalPages = 0;

  // 모달 상태
  let showCreateModal = false;
  let showEditModal = false;
  let showDetailModal = false;
  let selectedProduct = null;

  // 카테고리 및 상태 옵션
  let categories = [
    { id: 'all', name: '전체 카테고리' },
    { id: 'FOOD', name: '음식' },
    { id: 'BEVERAGE', name: '음료' },
    { id: 'DESSERT', name: '디저트' },
    { id: 'MERCHANDISE', name: '상품' },
    { id: 'SERVICE', name: '서비스' },
    { id: 'ETC', name: '기타' }
  ];

  let statusOptions = [
    { id: 'all', name: '전체 상태' },
    { id: 'AVAILABLE', name: '판매중' },
    { id: 'OUT_OF_STOCK', name: '품절' },
    { id: 'DISCONTINUED', name: '단종' },
    { id: 'TEMPORARILY_UNAVAILABLE', name: '일시품절' },
    { id: 'PREPARING', name: '준비중' }
  ];

  // 현재 사용자의 매장 ID
  $: currentStoreId = $authStore.user?.organizationId || 'IN001001';

  // 탭 활성화 및 데이터 로드
  onMount(async () => {
    tabStore.setActiveTab('BUSINESS_PRODUCTS');
    await loadProducts();
  });

  // 상품 데이터 로드 (페이징)
  async function loadProducts() {
    try {
      loading = true;
      const result = await ProductApi.getProductsWithPaging(
        currentStoreId,
        currentPage,
        pageSize,
        sortBy,
        sortDirection
      );
      
      products = result.products || [];
      totalCount = result.totalCount || 0;
      totalPages = result.totalPages || 0;

    } catch (error) {
      console.error('상품 로드 실패:', error);
      toastStore.add('상품을 불러오는데 실패했습니다.', 'error');
      products = [];
    } finally {
      loading = false;
    }
  }

  // 필터링된 상품 목록
  $: filteredProducts = products.filter(product => {
    const matchesSearch = product.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
                         (product.barcode && product.barcode.includes(searchTerm)) ||
                         (product.sku && product.sku.toLowerCase().includes(searchTerm.toLowerCase()));
    const matchesCategory = selectedCategory === 'all' || product.category === selectedCategory;
    const matchesStatus = selectedStatus === 'all' || product.status === selectedStatus;
    
    return matchesSearch && matchesCategory && matchesStatus;
  });

  // 통계 정보
  $: stats = {
    total: products.length,
    active: products.filter(p => p.isActive).length,
    lowStock: products.filter(p => p.stockQuantity <= p.minStockLevel).length,
    outOfStock: products.filter(p => p.stockQuantity === 0).length
  };

  // 상품 생성
  function handleCreateProduct() {
    showCreateModal = true;
  }

  // 상품 수정
  function handleEditProduct(product) {
    selectedProduct = product;
    showEditModal = true;
  }

  // 상품 상세보기
  function handleViewProduct(product) {
    selectedProduct = product;
    showDetailModal = true;
  }

  // 상품 삭제
  async function handleDeleteProduct(product) {
    if (!confirm(`정말로 "${product.name}" 상품을 삭제하시겠습니까?`)) {
      return;
    }

    try {
      await ProductApi.deleteProduct(product.productId);
      toastStore.add('상품이 삭제되었습니다.', 'success');
      await loadProducts();
    } catch (error) {
      console.error('상품 삭제 실패:', error);
      toastStore.add('상품 삭제에 실패했습니다.', 'error');
    }
  }

  // 재고 조정
  async function handleStockAdjustment(product, adjustment) {
    try {
      await ProductApi.adjustStock(product.productId, adjustment);
      toastStore.add('재고가 조정되었습니다.', 'success');
      await loadProducts();
    } catch (error) {
      console.error('재고 조정 실패:', error);
      toastStore.add('재고 조정에 실패했습니다.', 'error');
    }
  }

  // 상품 상태 변경
  async function handleStatusChange(product, newStatus) {
    try {
      await ProductApi.updateProduct(product.productId, {
        ...product,
        status: newStatus
      });
      toastStore.add('상품 상태가 변경되었습니다.', 'success');
      await loadProducts();
    } catch (error) {
      console.error('상품 상태 변경 실패:', error);
      toastStore.add('상품 상태 변경에 실패했습니다.', 'error');
    }
  }

  // 모달 닫기 후 새로고침
  function handleModalClose() {
    showCreateModal = false;
    showEditModal = false;
    showDetailModal = false;
    selectedProduct = null;
    loadProducts();
  }

  // 페이지 변경
  function changePage(newPage) {
    if (newPage >= 0 && newPage < totalPages) {
      currentPage = newPage;
      loadProducts();
    }
  }

  // 정렬 변경
  function changeSort(newSortBy) {
    if (sortBy === newSortBy) {
      sortDirection = sortDirection === 'ASC' ? 'DESC' : 'ASC';
    } else {
      sortBy = newSortBy;
      sortDirection = 'ASC';
    }
    loadProducts();
  }

  // 상태에 따른 배지 스타일
  function getStatusBadgeClass(status) {
    switch (status) {
      case 'AVAILABLE': return 'bg-green-100 text-green-800';
      case 'OUT_OF_STOCK': return 'bg-red-100 text-red-800';
      case 'DISCONTINUED': return 'bg-gray-100 text-gray-800';
      case 'TEMPORARILY_UNAVAILABLE': return 'bg-yellow-100 text-yellow-800';
      case 'PREPARING': return 'bg-blue-100 text-blue-800';
      default: return 'bg-gray-100 text-gray-800';
    }
  }

  // 상태 이름 변환
  function getStatusName(status) {
    const statusOption = statusOptions.find(opt => opt.id === status);
    return statusOption ? statusOption.name : status;
  }

  // 카테고리 이름 변환
  function getCategoryName(category) {
    const categoryOption = categories.find(cat => cat.id === category);
    return categoryOption ? categoryOption.name : category;
  }
</script>

<svelte:head>
  <title>상품 관리 - WebPos</title>
</svelte:head>

<div class="space-y-6">
  <!-- 헤더 -->
  <div class="flex justify-between items-center">
    <div>
      <h1 class="text-2xl font-bold text-gray-900 flex items-center">
        <Package class="mr-3" size="28" />
        상품 관리
      </h1>
      <p class="text-gray-600 mt-1">매장의 상품을 등록하고 관리합니다</p>
    </div>
    <button
      type="button"
      class="btn btn-primary"
      on:click={handleCreateProduct}
    >
      <Plus size="20" class="mr-2" />
      상품 등록
    </button>
  </div>

  <!-- 통계 카드 -->
  <div class="grid grid-cols-1 md:grid-cols-4 gap-4">
    <div class="card p-4">
      <div class="flex items-center">
        <div class="p-2 bg-blue-100 rounded-lg">
          <Package class="h-6 w-6 text-blue-600" />
        </div>
        <div class="ml-3">
          <p class="text-sm font-medium text-gray-500">전체 상품</p>
          <p class="text-2xl font-bold text-gray-900">{stats.total}</p>
        </div>
      </div>
    </div>

    <div class="card p-4">
      <div class="flex items-center">
        <div class="p-2 bg-green-100 rounded-lg">
          <TrendingUp class="h-6 w-6 text-green-600" />
        </div>
        <div class="ml-3">
          <p class="text-sm font-medium text-gray-500">판매중</p>
          <p class="text-2xl font-bold text-gray-900">{stats.active}</p>
        </div>
      </div>
    </div>

    <div class="card p-4">
      <div class="flex items-center">
        <div class="p-2 bg-yellow-100 rounded-lg">
          <AlertTriangle class="h-6 w-6 text-yellow-600" />
        </div>
        <div class="ml-3">
          <p class="text-sm font-medium text-gray-500">재고부족</p>
          <p class="text-2xl font-bold text-gray-900">{stats.lowStock}</p>
        </div>
      </div>
    </div>

    <div class="card p-4">
      <div class="flex items-center">
        <div class="p-2 bg-red-100 rounded-lg">
          <TrendingDown class="h-6 w-6 text-red-600" />
        </div>
        <div class="ml-3">
          <p class="text-sm font-medium text-gray-500">품절</p>
          <p class="text-2xl font-bold text-gray-900">{stats.outOfStock}</p>
        </div>
      </div>
    </div>
  </div>

  <!-- 필터 및 검색 -->
  <div class="card p-6">
    <div class="grid grid-cols-1 md:grid-cols-4 gap-4">
      <!-- 검색 -->
      <div class="relative">
        <Search size="20" class="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400" />
        <input
          type="text"
          placeholder="상품명, 바코드, SKU 검색..."
          bind:value={searchTerm}
          class="input pl-10"
        />
      </div>

      <!-- 카테고리 필터 -->
      <select bind:value={selectedCategory} class="input">
        {#each categories as category}
          <option value={category.id}>{category.name}</option>
        {/each}
      </select>

      <!-- 상태 필터 -->
      <select bind:value={selectedStatus} class="input">
        {#each statusOptions as status}
          <option value={status.id}>{status.name}</option>
        {/each}
      </select>

      <!-- 정렬 -->
      <select 
        value={`${sortBy}_${sortDirection}`}
        on:change={(e) => {
          const [newSortBy, newDirection] = e.target.value.split('_');
          sortBy = newSortBy;
          sortDirection = newDirection;
          loadProducts();
        }}
        class="input"
      >
        <option value="displayOrder_ASC">진열순 (오름차순)</option>
        <option value="displayOrder_DESC">진열순 (내림차순)</option>
        <option value="name_ASC">상품명 (오름차순)</option>
        <option value="name_DESC">상품명 (내림차순)</option>
        <option value="price_ASC">가격 (낮은순)</option>
        <option value="price_DESC">가격 (높은순)</option>
        <option value="stockQuantity_ASC">재고 (적은순)</option>
        <option value="stockQuantity_DESC">재고 (많은순)</option>
      </select>
    </div>
  </div>

  <!-- 상품 목록 -->
  <div class="card">
    {#if loading}
      <div class="p-12 text-center">
        <div class="animate-spin rounded-full h-12 w-12 border-b-2 border-primary-600 mx-auto mb-4"></div>
        <p class="text-gray-500">상품을 불러오는 중...</p>
      </div>
    {:else if filteredProducts.length === 0}
      <div class="p-12 text-center">
        <Package class="mx-auto h-12 w-12 text-gray-400 mb-4" />
        <h3 class="text-lg font-medium text-gray-900 mb-2">상품이 없습니다</h3>
        <p class="text-gray-500 mb-4">새로운 상품을 등록해보세요.</p>
        <button
          type="button"
          class="btn btn-primary"
          on:click={handleCreateProduct}
        >
          <Plus size="20" class="mr-2" />
          상품 등록
        </button>
      </div>
    {:else}
      <div class="overflow-x-auto">
        <table class="min-w-full divide-y divide-gray-200">
          <thead class="bg-gray-50">
            <tr>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider cursor-pointer"
                  on:click={() => changeSort('name')}>
                상품명
                {#if sortBy === 'name'}
                  <span class="ml-1">{sortDirection === 'ASC' ? '↑' : '↓'}</span>
                {/if}
              </th>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">카테고리</th>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider cursor-pointer"
                  on:click={() => changeSort('price')}>
                가격
                {#if sortBy === 'price'}
                  <span class="ml-1">{sortDirection === 'ASC' ? '↑' : '↓'}</span>
                {/if}
              </th>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider cursor-pointer"
                  on:click={() => changeSort('stockQuantity')}>
                재고
                {#if sortBy === 'stockQuantity'}
                  <span class="ml-1">{sortDirection === 'ASC' ? '↑' : '↓'}</span>
                {/if}
              </th>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">상태</th>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">작업</th>
            </tr>
          </thead>
          <tbody class="bg-white divide-y divide-gray-200">
            {#each filteredProducts as product}
              <tr class="hover:bg-gray-50">
                <td class="px-6 py-4 whitespace-nowrap">
                  <div class="flex items-center">
                    <div class="h-10 w-10 bg-gray-100 rounded-lg flex items-center justify-center mr-3">
                      <Package class="h-5 w-5 text-gray-400" />
                    </div>
                    <div>
                      <div class="text-sm font-medium text-gray-900">{product.name}</div>
                      {#if product.barcode}
                        <div class="text-sm text-gray-500">바코드: {product.barcode}</div>
                      {/if}
                    </div>
                  </div>
                </td>
                <td class="px-6 py-4 whitespace-nowrap">
                  <span class="inline-flex px-2 py-1 text-xs font-semibold rounded-full bg-blue-100 text-blue-800">
                    {getCategoryName(product.category)}
                  </span>
                </td>
                <td class="px-6 py-4 whitespace-nowrap">
                  <div class="text-sm text-gray-900">{product.price.toLocaleString()}원</div>
                  {#if product.originalPrice && product.originalPrice !== product.price}
                    <div class="text-sm text-gray-500 line-through">{product.originalPrice.toLocaleString()}원</div>
                  {/if}
                </td>
                <td class="px-6 py-4 whitespace-nowrap">
                  <div class="text-sm text-gray-900">
                    {product.stockQuantity}개
                    {#if product.stockQuantity <= product.minStockLevel}
                      <span class="ml-1 text-red-500">⚠️</span>
                    {/if}
                  </div>
                  <div class="text-xs text-gray-500">최소: {product.minStockLevel}개</div>
                </td>
                <td class="px-6 py-4 whitespace-nowrap">
                  <span class="inline-flex px-2 py-1 text-xs font-semibold rounded-full {getStatusBadgeClass(product.status)}">
                    {getStatusName(product.status)}
                  </span>
                </td>
                <td class="px-6 py-4 whitespace-nowrap text-sm font-medium">
                  <div class="flex space-x-2">
                    <button
                      type="button"
                      class="text-indigo-600 hover:text-indigo-900"
                      on:click={() => handleViewProduct(product)}
                      title="상세보기"
                    >
                      <Eye size="16" />
                    </button>
                    <button
                      type="button"
                      class="text-yellow-600 hover:text-yellow-900"
                      on:click={() => handleEditProduct(product)}
                      title="수정"
                    >
                      <Edit size="16" />
                    </button>
                    <button
                      type="button"
                      class="text-red-600 hover:text-red-900"
                      on:click={() => handleDeleteProduct(product)}
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

      <!-- 페이지네이션 -->
      {#if totalPages > 1}
        <div class="px-6 py-3 border-t border-gray-200">
          <div class="flex items-center justify-between">
            <div class="text-sm text-gray-700">
              총 {totalCount}개 중 {currentPage * pageSize + 1}-{Math.min((currentPage + 1) * pageSize, totalCount)}개 표시
            </div>
            <div class="flex space-x-1">
              <button
                type="button"
                class="px-3 py-1 border border-gray-300 rounded-md text-sm {currentPage === 0 ? 'bg-gray-100 text-gray-400' : 'bg-white text-gray-700 hover:bg-gray-50'}"
                disabled={currentPage === 0}
                on:click={() => changePage(currentPage - 1)}
              >
                이전
              </button>
              
              {#each Array(Math.min(5, totalPages)) as _, i}
                {#if totalPages <= 5}
                  <button
                    type="button"
                    class="px-3 py-1 border rounded-md text-sm {i === currentPage ? 'bg-primary-600 text-white border-primary-600' : 'bg-white text-gray-700 border-gray-300 hover:bg-gray-50'}"
                    on:click={() => changePage(i)}
                  >
                    {i + 1}
                  </button>
                {:else}
                  <!-- 페이지가 많을 때의 로직 추가 필요 -->
                {/if}
              {/each}
              
              <button
                type="button"
                class="px-3 py-1 border border-gray-300 rounded-md text-sm {currentPage === totalPages - 1 ? 'bg-gray-100 text-gray-400' : 'bg-white text-gray-700 hover:bg-gray-50'}"
                disabled={currentPage === totalPages - 1}
                on:click={() => changePage(currentPage + 1)}
              >
                다음
              </button>
            </div>
          </div>
        </div>
      {/if}
    {/if}
  </div>
</div>

<!-- 모달들 -->
{#if showCreateModal}
  <CreateProductModal 
    bind:open={showCreateModal}
    storeId={currentStoreId}
    on:close={handleModalClose}
  />
{/if}

{#if showEditModal && selectedProduct}
  <EditProductModal 
    bind:open={showEditModal}
    product={selectedProduct}
    on:close={handleModalClose}
  />
{/if}

{#if showDetailModal && selectedProduct}
  <ProductDetailModal 
    bind:open={showDetailModal}
    product={selectedProduct}
    on:close={handleModalClose}
    on:edit={() => {
      showDetailModal = false;
      showEditModal = true;
    }}
    on:delete={() => {
      showDetailModal = false;
      handleDeleteProduct(selectedProduct);
    }}
  />
{/if}
