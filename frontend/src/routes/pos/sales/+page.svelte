<script>
  import { onMount } from 'svelte';
  import { authStore } from '$lib/stores/auth.js';
  import { tabStore } from '$lib/stores/tabs.js';
  import { ShoppingCart, Package, CreditCard, Receipt, Plus, Minus, X } from 'lucide-svelte';

  let products = [];
  let cart = [];
  let loading = false;
  let searchTerm = '';
  let selectedCategory = 'all';
  let categories = [
    { id: 'all', name: '전체' },
    { id: 'food', name: '음식' },
    { id: 'beverage', name: '음료' },
    { id: 'dessert', name: '디저트' },
    { id: 'etc', name: '기타' }
  ];

  // 탭 활성화
  onMount(() => {
    tabStore.setActiveTab('POS_SALES');
    // 임시 상품 데이터
    products = [
      { id: 1, name: '아메리카노', price: 4500, category: 'beverage', stock: 50, isActive: true },
      { id: 2, name: '라떼', price: 5000, category: 'beverage', stock: 30, isActive: true },
      { id: 3, name: '카푸치노', price: 5500, category: 'beverage', stock: 25, isActive: true },
      { id: 4, name: '에스프레소', price: 4000, category: 'beverage', stock: 40, isActive: true },
      { id: 5, name: '크로와상', price: 3000, category: 'food', stock: 15, isActive: true },
      { id: 6, name: '베이글', price: 3500, category: 'food', stock: 20, isActive: true },
      { id: 7, name: '치즈케이크', price: 6000, category: 'dessert', stock: 10, isActive: true },
      { id: 8, name: '초콜릿쿠키', price: 2500, category: 'dessert', stock: 25, isActive: true },
      { id: 9, name: '프라푸치노', price: 6500, category: 'beverage', stock: 20, isActive: true },
      { id: 10, name: '샌드위치', price: 7000, category: 'food', stock: 12, isActive: true },
      { id: 11, name: '마카롱', price: 2000, category: 'dessert', stock: 30, isActive: true },
      { id: 12, name: '스무디', price: 5500, category: 'beverage', stock: 18, isActive: true }
    ];
  });

  // 필터링된 상품 목록
  $: filteredProducts = products.filter(product => {
    const matchesSearch = product.name.toLowerCase().includes(searchTerm.toLowerCase());
    const matchesCategory = selectedCategory === 'all' || product.category === selectedCategory;
    
    return matchesSearch && matchesCategory && product.isActive;
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

  function processPayment() {
    if (cart.length === 0) {
      alert('장바구니가 비어있습니다.');
      return;
    }

    alert(`결제가 완료되었습니다. 총 금액: ${totalAmount.toLocaleString()}원`);
    clearCart();
  }
</script>

<svelte:head>
  <title>POS 판매 - WebPos</title>
</svelte:head>

<div slot="title">POS 판매</div>

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
      {#if filteredProducts.length === 0}
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
          disabled={cart.length === 0}
          on:click={processPayment}
        >
          <CreditCard class="mr-2" size="20" />
          결제하기
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
