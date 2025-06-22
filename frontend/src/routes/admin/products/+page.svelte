<script>
  import { onMount } from 'svelte';
  import { authStore } from '$lib/stores/auth.js';
  import { tabStore } from '$lib/stores/tabs.js';
  import { toastStore } from '$lib/stores/toast.js';
  import { ProductApi } from '$lib/api/product.js';
  import { 
    Package, 
    Plus, 
    Search, 
    Edit, 
    Trash2, 
    AlertTriangle,
    CheckCircle,
    XCircle,
    Eye
  } from 'lucide-svelte';
  import Modal from '$lib/components/Common/Modal.svelte';
  import LoadingSpinner from '$lib/components/Common/LoadingSpinner.svelte';

  let products = [];
  let loading = false;
  let searchTerm = '';
  let selectedCategory = 'all';
  let selectedStatus = 'all';
  let currentPage = 0;
  let pageSize = 10;
  let totalCount = 0;
  let totalPages = 0;
  let sortBy = 'displayOrder';
  let sortDirection = 'ASC';

  let categories = [
    { id: 'all', name: '전체' }
  ];

  let showCreateModal = false;
  let showEditModal = false;
  let showDeleteModal = false;
  let selectedProduct = null;

  // 현재 사용자의 매장 ID (인증된 사용자 정보에서 가져오기)  
  $: currentStoreId = $authStore.user?.storeId || '550e8400-e29b-41d4-a716-446655440000';

  let newProduct = {
    name: '',
    description: '',
    price: 0,
    originalPrice: 0,
    category: '',
    productType: 'FOOD',
    stockQuantity: 0,
    minStockLevel: 0,
    maxStockLevel: 100,
    barcode: '',
    sku: '',
    imageUrl: '',
    displayOrder: 0
  };

  const productTypes = [
    { value: 'FOOD', label: '음식' },
    { value: 'BEVERAGE', label: '음료' },
    { value: 'DESSERT', label: '디저트' },
    { value: 'MERCHANDISE', label: '상품/굿즈' },
    { value: 'SERVICE', label: '서비스' },
    { value: 'ETC', label: '기타' }
  ];

  const statusOptions = [
    { value: 'all', label: '전체' },
    { value: 'AVAILABLE', label: '판매 가능' },
    { value: 'OUT_OF_STOCK', label: '품절' },
    { value: 'DISCONTINUED', label: '판매 중단' },
    { value: 'TEMPORARILY_UNAVAILABLE', label: '임시 비활성화' },
    { value: 'PREPARING', label: '준비 중' }
  ];

  onMount(async () => {
    tabStore.setActiveTab('ADMIN_PRODUCTS');
    await loadProducts();
    await loadCategories();
  });

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
      
      products = result.products;
      totalCount = result.totalCount;
      totalPages = result.totalPages;
    } catch (error) {
      console.error('상품 목록 로드 실패:', error);
      toastStore.add('상품 목록을 불러오는데 실패했습니다.', 'error');
      products = [];
    } finally {
      loading = false;
    }
  }

  async function loadCategories() {
    try {
      const allProducts = await ProductApi.getProductsByStore(currentStoreId);
      const uniqueCategories = [...new Set(allProducts.map(p => p.category))];
      categories = [
        { id: 'all', name: '전체' },
        ...uniqueCategories.map(cat => ({ id: cat, name: cat }))
      ];
    } catch (error) {
      console.error('카테고리 로드 실패:', error);
    }
  }

  function getStatusColor(status) {
    switch (status) {
      case 'AVAILABLE':
        return 'text-green-600 bg-green-100';
      case 'OUT_OF_STOCK':
        return 'text-red-600 bg-red-100';
      case 'DISCONTINUED':
        return 'text-gray-600 bg-gray-100';
      case 'TEMPORARILY_UNAVAILABLE':
        return 'text-yellow-600 bg-yellow-100';
      case 'PREPARING':
        return 'text-blue-600 bg-blue-100';
      default:
        return 'text-gray-600 bg-gray-100';
    }
  }

  function getStatusLabel(status) {
    const option = statusOptions.find(opt => opt.value === status);
    return option ? option.label : status;
  }

  function getProductTypeLabel(type) {
    const productType = productTypes.find(pt => pt.value === type);
    return productType ? productType.label : type;
  }

  // 검색 및 필터링
  $: filteredProducts = products.filter(product => {
    const matchesSearch = product.name.toLowerCase().includes(searchTerm.toLowerCase());
    const matchesCategory = selectedCategory === 'all' || product.category === selectedCategory;
    const matchesStatus = selectedStatus === 'all' || product.status === selectedStatus;
    
    return matchesSearch && matchesCategory && matchesStatus;
  });
</script>

<svelte:head>
  <title>상품 관리 - WebPos Admin</title>
</svelte:head>

<div class="space-y-6">
  <!-- 헤더 -->
  <div class="flex items-center justify-between">
    <div>
      <h1 class="text-2xl font-bold text-gray-900">상품 관리</h1>
      <p class="mt-1 text-sm text-gray-500">매장의 상품을 관리하고 재고를 확인할 수 있습니다.</p>
    </div>
    <button
      type="button"
      class="btn btn-primary"
      on:click={() => showCreateModal = true}
    >
      <Plus class="mr-2" size="16" />
      새 상품 추가
    </button>
  </div>

  <!-- 검색 및 필터 -->
  <div class="card p-6">
    <div class="grid grid-cols-1 md:grid-cols-4 gap-4">
      <!-- 검색 -->
      <div class="relative">
        <Search size="20" class="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400" />
        <input
          type="text"
          placeholder="상품명 검색..."
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
          <option value={status.value}>{status.label}</option>
        {/each}
      </select>

      <!-- 새로고침 버튼 -->
      <button
        type="button"
        class="btn btn-secondary"
        on:click={loadProducts}
        disabled={loading}
      >
        {#if loading}
          로딩...
        {:else}
          새로고침
        {/if}
      </button>
    </div>
  </div>

  <!-- 상품 목록 -->
  <div class="card">
    {#if loading}
      <div class="flex justify-center py-12">
        <LoadingSpinner />
      </div>
    {:else if filteredProducts.length === 0}
      <div class="text-center py-12">
        <Package class="mx-auto h-12 w-12 text-gray-400" />
        <p class="mt-4 text-gray-500">상품이 없습니다.</p>
      </div>
    {:else}
      <!-- 테이블 -->
      <div class="overflow-x-auto">
        <table class="min-w-full divide-y divide-gray-200">
          <thead class="bg-gray-50">
            <tr>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                상품명
              </th>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                카테고리
              </th>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                가격
              </th>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                재고
              </th>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                상태
              </th>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                활성화
              </th>
              <th class="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase tracking-wider">
                작업
              </th>
            </tr>
          </thead>
          <tbody class="bg-white divide-y divide-gray-200">
            {#each filteredProducts as product}
              <tr class="hover:bg-gray-50">
                <td class="px-6 py-4 whitespace-nowrap">
                  <div class="flex items-center">
                    <div class="flex-shrink-0 h-10 w-10">
                      <div class="h-10 w-10 rounded-full bg-gray-200 flex items-center justify-center">
                        <Package class="h-5 w-5 text-gray-500" />
                      </div>
                    </div>
                    <div class="ml-4">
                      <div class="text-sm font-medium text-gray-900">{product.name}</div>
                      <div class="text-sm text-gray-500">{getProductTypeLabel(product.productType)}</div>
                    </div>
                  </div>
                </td>
                <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                  {product.category}
                </td>
                <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                  {product.price.toLocaleString()}원
                  {#if product.discountRate > 0}
                    <div class="text-xs text-red-500">
                      {product.discountRate.toFixed(1)}% 할인
                    </div>
                  {/if}
                </td>
                <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                  <span class={product.isLowStock ? 'text-red-600 font-medium' : ''}>
                    {product.stockQuantity}
                  </span>
                  {#if product.isLowStock}
                    <AlertTriangle class="inline ml-1 h-4 w-4 text-red-500" />
                  {/if}
                </td>
                <td class="px-6 py-4 whitespace-nowrap">
                  <span class={`inline-flex px-2 py-1 text-xs font-semibold rounded-full ${getStatusColor(product.status)}`}>
                    {getStatusLabel(product.status)}
                  </span>
                </td>
                <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                  {#if product.isActive}
                    <CheckCircle class="h-5 w-5 text-green-500" />
                  {:else}
                    <XCircle class="h-5 w-5 text-red-500" />
                  {/if}
                </td>
                <td class="px-6 py-4 whitespace-nowrap text-right text-sm font-medium">
                  <div class="flex items-center justify-end space-x-2">
                    <button
                      type="button"
                      class="text-blue-600 hover:text-blue-900"
                      title="수정"
                    >
                      <Edit class="h-4 w-4" />
                    </button>
                    <button
                      type="button"
                      class="text-red-600 hover:text-red-900"
                      title="삭제"
                    >
                      <Trash2 class="h-4 w-4" />
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

<!-- 상품 생성 모달 (기본 구조만) -->
<Modal bind:showModal={showCreateModal} title="새 상품 추가">
  <div class="text-center py-8">
    <p class="text-gray-500">상품 생성 폼이 여기에 들어갑니다.</p>
  </div>
</Modal>
