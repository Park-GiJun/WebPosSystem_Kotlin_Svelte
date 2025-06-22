<script>
  import { onMount } from 'svelte';
  import { authStore } from '$lib/stores/auth.js';
  import { tabStore } from '$lib/stores/tabs.js';
  import { toastStore } from '$lib/stores/toast.js';
  import { ProductApi } from '$lib/api/product.js';
  import { ShoppingCart, Package, CreditCard, Receipt, Plus, Minus, X } from 'lucide-svelte';

  let products = [];
  let allProducts = [];
  let cart = [];
  let loading = false;
  let searchTerm = '';
  let selectedCategory = 'all';
  let categories = [
    { id: 'all', name: '전체' }
  ];

  // 현재 사용자의 매장 ID (인증된 사용자 정보에서 가져오기)
  $: currentStoreId = $authStore.user?.organizationId || 'IN001001';

  // 탭 활성화 및 데이터 로드
  onMount(async () => {
    tabStore.setActiveTab('POS_SALES');
    await loadProducts();
  });

  // 상품 데이터 로드
  async function loadProducts() {
    try {
      loading = true;
      const productsData = await ProductApi.getProductsForPos(currentStoreId);
      
      allProducts = productsData.map(product => ({
        id: product.productId,
        name: product.name,
        price: product.price,
        category: product.category,
        stock: product.stockQuantity,
        isActive: product.isActive,
        isAvailableForSale: product.isAvailableForSale
      }));

      products = allProducts;
      
      // 카테고리 목록 생성
      const uniqueCategories = [...new Set(allProducts.map(p => p.category))];
      categories = [
        { id: 'all', name: '전체' },
        ...uniqueCategories.map(cat => ({ id: cat, name: cat }))
      ];

    } catch (error) {
      console.error('상품 로드 실패:', error);
      toastStore.add('상품을 불러오는데 실패했습니다.', 'error');
      // 에러 시 빈 배열로 설정
      products = [];
      allProducts = [];
    } finally {
      loading = false;
    }
  }

  // 필터링된 상품 목록
  $: filteredProducts = products.filter(product => {
    const matchesSearch = product.name.toLowerCase().includes(searchTerm.toLowerCase());
    const matchesCategory = selectedCategory === 'all' || product.category === selectedCategory;
    
    return matchesSearch && matchesCategory && product.isActive && product.isAvailableForSale;
  });

  // 장바구니 총액
  $: totalAmount = cart.reduce((sum, item) => sum + (item.price * item.quantity), 0);

  function addToCart(product) {
    const existingItem = cart.find(item => item.id === product.id);
    
    if (existingItem) {
      existingItem.quantity += 1;
      cart = [...cart];
    } else {
      cart = [...cart, { ...product, quantity: 1 }];
    }
  }

  function updateQuantity(productId, change) {
    cart = cart.map(item => {
      if (item.id === productId) {
        const newQuantity = item.quantity + change;
        return newQuantity > 0 ? { ...item, quantity: newQuantity } : null;
      }
      return item;
    }).filter(Boolean);
  }

  function removeFromCart(productId) {
    cart = cart.filter(item => item.id !== productId);
  }

  function clearCart() {
    cart = [];
  }

  async function processPayment() {
    if (cart.length === 0) {
      toastStore.add('장바구니가 비어있습니다.', 'error');
      return;
    }

    try {
      loading = true;
      
      // 판매할 상품 목록 생성 (productId: quantity 형태)
      const salesItems = {};
      cart.forEach(item => {
        salesItems[item.id] = item.quantity;
      });

      // 실제 판매 처리 (재고 차감)
      await ProductApi.sellProducts(salesItems);
      
      toastStore.add(`결제가 완료되었습니다. 총 금액: ${totalAmount.toLocaleString()}원`, 'success');
      clearCart();
      
      // 상품 목록 새로고침 (재고 정보 업데이트)
      await loadProducts();
      
    } catch (error) {
      console.error('결제 처리 실패:', error);
      toastStore.add('결제 처리 중 오류가 발생했습니다.', 'error');
    } finally {
      loading = false;
    }
  }
</script>

<svelte:head>
  <title>POS 판매 - WebPos</title>
</svelte:head>

<div class="grid grid-cols-1 lg:grid-cols-3 gap-6 h-full">
  <!-- 상품 목록 -->
  <div class="lg:col-span-2 space-y-6">
    <!-- 검색 및 카테고리 필터 -->
    <div class="card p-6">
      <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
        <!-- 검색 -->
        <div class="relative">
          <Package size="20" class="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400" />
          <input
            type="text"
            placeholder="상품명 검색..."
            bind:value={searchTerm}
            class="input pl-10"
          />
        </div>

        <!-- 카테고리 -->
        <select bind:value={selectedCategory} class="input">
          {#each categories as category}
            <option value={category.id}>{category.name}</option>
          {/each}
        </select>
      </div>
    </div>

    <!-- 상품 그리드 -->
    <div class="card p-6">
      {#if loading}
        <div class="text-center py-12">
          <div class="animate-spin rounded-full h-12 w-12 border-b-2 border-primary-600 mx-auto mb-4"></div>
          <p class="text-gray-500">상품을 불러오는 중...</p>
        </div>
      {:else if filteredProducts.length === 0}
        <div class="text-center py-12">
          <Package class="mx-auto h-12 w-12 text-gray-400" />
          <p class="mt-4 text-gray-500">상품이 없습니다.</p>
        </div>
      {:else}
        <div class="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-4">
          {#each filteredProducts as product}
            <button
              type="button"
              class="p-4 border border-gray-200 rounded-lg hover:border-primary-300 hover:shadow-md transition-all duration-200 text-left"
              on:click={() => addToCart(product)}
            >
              <!-- 상품 이미지 -->
              <div class="w-full h-24 bg-gray-100 rounded mb-3 flex items-center justify-center">
                <Package class="h-8 w-8 text-gray-400" />
              </div>
              
              <!-- 상품 정보 -->
              <h3 class="font-medium text-gray-900 text-sm mb-1 line-clamp-2">{product.name}</h3>
              <p class="text-primary-600 font-semibold">{product.price.toLocaleString()}원</p>
              <p class="text-xs text-gray-500 mt-1">재고: {product.stock}</p>
            </button>
          {/each}
        </div>
      {/if}
    </div>
  </div>

  <!-- 장바구니 및 결제 -->
  <div class="lg:col-span-1">
    <div class="card p-6 sticky top-6">
      <!-- 장바구니 헤더 -->
      <div class="flex items-center justify-between mb-4">
        <h3 class="text-lg font-semibold text-gray-900 flex items-center">
          <ShoppingCart class="mr-2" size="20" />
          장바구니
        </h3>
        {#if cart.length > 0}
          <button
            type="button"
            class="text-sm text-red-600 hover:text-red-800"
            on:click={clearCart}
          >
            전체 삭제
          </button>
        {/if}
      </div>

      <!-- 장바구니 아이템들 -->
      <div class="space-y-3 mb-6 max-h-96 overflow-y-auto">
        {#if cart.length === 0}
          <div class="text-center py-8">
            <ShoppingCart class="mx-auto h-8 w-8 text-gray-400 mb-2" />
            <p class="text-gray-500 text-sm">장바구니가 비어있습니다</p>
          </div>
        {:else}
          {#each cart as item}
            <div class="flex items-center justify-between p-3 bg-gray-50 rounded-lg">
              <div class="flex-1 min-w-0">
                <h4 class="text-sm font-medium text-gray-900 truncate">{item.name}</h4>
                <p class="text-sm text-gray-600">{item.price.toLocaleString()}원</p>
              </div>
              
              <div class="flex items-center space-x-2 ml-3">
                <!-- 수량 조절 -->
                <button
                  type="button"
                  class="p-1 rounded-full hover:bg-gray-200"
                  on:click={() => updateQuantity(item.id, -1)}
                >
                  <Minus size="14" />
                </button>
                
                <span class="w-8 text-center text-sm font-medium">{item.quantity}</span>
                
                <button
                  type="button"
                  class="p-1 rounded-full hover:bg-gray-200"
                  on:click={() => updateQuantity(item.id, 1)}
                >
                  <Plus size="14" />
                </button>
                
                <!-- 삭제 버튼 -->
                <button
                  type="button"
                  class="p-1 rounded-full hover:bg-red-100 text-red-600 ml-2"
                  on:click={() => removeFromCart(item.id)}
                >
                  <X size="14" />
                </button>
              </div>
            </div>
            
            <!-- 소계 -->
            <div class="flex justify-between text-sm text-gray-600 px-3">
              <span>소계:</span>
              <span>{(item.price * item.quantity).toLocaleString()}원</span>
            </div>
          {/each}
        {/if}
      </div>

      <!-- 총액 -->
      <div class="border-t border-gray-200 pt-4 mb-6">
        <div class="flex justify-between items-center">
          <span class="text-lg font-semibold text-gray-900">총액:</span>
          <span class="text-xl font-bold text-primary-600">
            {totalAmount.toLocaleString()}원
          </span>
        </div>
      </div>

      <!-- 결제 버튼 -->
      <div class="space-y-3">
        <button
          type="button"
          class="w-full btn btn-primary py-3 text-lg"
          disabled={cart.length === 0 || loading}
          on:click={processPayment}
        >
          {#if loading}
            <div class="flex items-center justify-center">
              <div class="animate-spin rounded-full h-5 w-5 border-b-2 border-white mr-2"></div>
              처리 중...
            </div>
          {:else}
            <CreditCard class="mr-2" size="20" />
            결제하기
          {/if}
        </button>
        
        <button
          type="button"
          class="w-full btn btn-secondary"
          disabled={cart.length === 0}
        >
          <Receipt class="mr-2" size="16" />
          영수증 미리보기
        </button>
      </div>

      <!-- 주문 정보 -->
      {#if cart.length > 0}
        <div class="mt-6 pt-4 border-t border-gray-200 text-sm text-gray-600">
          <div class="flex justify-between mb-1">
            <span>상품 수:</span>
            <span>{cart.reduce((sum, item) => sum + item.quantity, 0)}개</span>
          </div>
          <div class="flex justify-between">
            <span>항목 수:</span>
            <span>{cart.length}종</span>
          </div>
        </div>
      {/if}
    </div>
  </div>
</div>
