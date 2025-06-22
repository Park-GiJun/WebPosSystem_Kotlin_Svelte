<script>
  import { createEventDispatcher } from 'svelte';
  import { ProductApi } from '$lib/api/product.js';
  import { toastStore } from '$lib/stores/toast.js';
  import Modal from '$lib/components/Common/Modal.svelte';
  import { Package, DollarSign, Hash, Barcode } from 'lucide-svelte';

  export let open = false;
  export let storeId;

  const dispatch = createEventDispatcher();

  let loading = false;
  let errors = {};

  let formData = {
    name: '',
    description: '',
    price: '',
    originalPrice: '',
    category: 'FOOD',
    productType: 'FOOD',
    stockQuantity: '',
    minStockLevel: '',
    maxStockLevel: '',
    barcode: '',
    sku: '',
    imageUrl: '',
    displayOrder: ''
  };

  const categories = [
    { id: 'FOOD', name: '음식' },
    { id: 'BEVERAGE', name: '음료' },
    { id: 'DESSERT', name: '디저트' },
    { id: 'MERCHANDISE', name: '상품' },
    { id: 'SERVICE', name: '서비스' },
    { id: 'ETC', name: '기타' }
  ];

  const productTypes = [
    { id: 'FOOD', name: '음식' },
    { id: 'BEVERAGE', name: '음료' },
    { id: 'DESSERT', name: '디저트' },
    { id: 'MERCHANDISE', name: '상품' },
    { id: 'SERVICE', name: '서비스' },
    { id: 'ETC', name: '기타' }
  ];

  function validateForm() {
    errors = {};

    if (!formData.name.trim()) {
      errors.name = '상품명은 필수입니다.';
    }

    if (!formData.price || formData.price <= 0) {
      errors.price = '올바른 가격을 입력해주세요.';
    }

    if (formData.originalPrice && formData.originalPrice < formData.price) {
      errors.originalPrice = '정가는 판매가보다 높아야 합니다.';
    }

    if (!formData.stockQuantity || formData.stockQuantity < 0) {
      errors.stockQuantity = '올바른 재고 수량을 입력해주세요.';
    }

    if (!formData.minStockLevel || formData.minStockLevel < 0) {
      errors.minStockLevel = '올바른 최소 재고를 입력해주세요.';
    }

    if (formData.maxStockLevel && formData.maxStockLevel < formData.minStockLevel) {
      errors.maxStockLevel = '최대 재고는 최소 재고보다 커야 합니다.';
    }

    return Object.keys(errors).length === 0;
  }

  async function handleSubmit() {
    if (!validateForm()) return;

    try {
      loading = true;

      const productData = {
        name: formData.name.trim(),
        description: formData.description.trim() || null,
        price: parseFloat(formData.price),
        originalPrice: formData.originalPrice ? parseFloat(formData.originalPrice) : parseFloat(formData.price),
        category: formData.category,
        productType: formData.productType,
        stockQuantity: parseInt(formData.stockQuantity),
        minStockLevel: parseInt(formData.minStockLevel),
        maxStockLevel: formData.maxStockLevel ? parseInt(formData.maxStockLevel) : 9999,
        barcode: formData.barcode.trim() || null,
        sku: formData.sku.trim() || null,
        imageUrl: formData.imageUrl.trim() || null,
        displayOrder: formData.displayOrder ? parseInt(formData.displayOrder) : 0
      };

      await ProductApi.createProduct(storeId, productData);
      
      toastStore.add('상품이 등록되었습니다.', 'success');
      handleClose();
      
    } catch (error) {
      console.error('상품 등록 실패:', error);
      if (error.message.includes('이미 존재')) {
        errors.submit = '이미 등록된 바코드 또는 SKU입니다.';
      } else {
        errors.submit = '상품 등록에 실패했습니다.';
      }
    } finally {
      loading = false;
    }
  }

  function handleClose() {
    if (!loading) {
      open = false;
      // 폼 초기화
      formData = {
        name: '',
        description: '',
        price: '',
        originalPrice: '',
        category: 'FOOD',
        productType: 'FOOD',
        stockQuantity: '',
        minStockLevel: '',
        maxStockLevel: '',
        barcode: '',
        sku: '',
        imageUrl: '',
        displayOrder: ''
      };
      errors = {};
      dispatch('close');
    }
  }

  // 바코드 생성 (간단한 랜덤)
  function generateBarcode() {
    const timestamp = Date.now().toString();
    const random = Math.floor(Math.random() * 1000).toString().padStart(3, '0');
    formData.barcode = timestamp.slice(-10) + random;
  }

  // SKU 생성
  function generateSku() {
    const categoryCode = formData.category.substring(0, 3).toUpperCase();
    const nameCode = formData.name.replace(/\s/g, '').substring(0, 3).toUpperCase();
    const timestamp = Date.now().toString().slice(-4);
    formData.sku = `${categoryCode}-${nameCode}-${timestamp}`;
  }
</script>

<Modal bind:open title="상품 등록" size="xl" on:close={handleClose}>
  <form on:submit|preventDefault={handleSubmit} class="space-y-6">
    <!-- 기본 정보 -->
    <div class="bg-gray-50 p-4 rounded-lg">
      <h3 class="text-lg font-medium text-gray-900 mb-4 flex items-center">
        <Package class="mr-2 h-5 w-5" />
        기본 정보
      </h3>
      
      <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
        <!-- 상품명 -->
        <div>
          <label for="name" class="block text-sm font-medium text-gray-700 mb-1">
            상품명 <span class="text-red-500">*</span>
          </label>
          <input
            id="name"
            type="text"
            bind:value={formData.name}
            class="input"
            class:border-red-300={errors.name}
            placeholder="상품명을 입력하세요"
            disabled={loading}
            required
          />
          {#if errors.name}
            <p class="text-sm text-red-600 mt-1">{errors.name}</p>
          {/if}
        </div>

        <!-- 카테고리 -->
        <div>
          <label for="category" class="block text-sm font-medium text-gray-700 mb-1">
            카테고리 <span class="text-red-500">*</span>
          </label>
          <select
            id="category"
            bind:value={formData.category}
            class="input"
            disabled={loading}
            required
          >
            {#each categories as category}
              <option value={category.id}>{category.name}</option>
            {/each}
          </select>
        </div>

        <!-- 상품 유형 -->
        <div>
          <label for="productType" class="block text-sm font-medium text-gray-700 mb-1">
            상품 유형 <span class="text-red-500">*</span>
          </label>
          <select
            id="productType"
            bind:value={formData.productType}
            class="input"
            disabled={loading}
            required
          >
            {#each productTypes as type}
              <option value={type.id}>{type.name}</option>
            {/each}
          </select>
        </div>

        <!-- 진열 순서 -->
        <div>
          <label for="displayOrder" class="block text-sm font-medium text-gray-700 mb-1">
            진열 순서
          </label>
          <input
            id="displayOrder"
            type="number"
            bind:value={formData.displayOrder}
            class="input"
            placeholder="0"
            min="0"
            disabled={loading}
          />
        </div>
      </div>

      <!-- 상품 설명 -->
      <div class="mt-4">
        <label for="description" class="block text-sm font-medium text-gray-700 mb-1">
          상품 설명
        </label>
        <textarea
          id="description"
          bind:value={formData.description}
          class="input"
          rows="3"
          placeholder="상품에 대한 설명을 입력하세요"
          disabled={loading}
        ></textarea>
      </div>
    </div>

    <!-- 가격 정보 -->
    <div class="bg-gray-50 p-4 rounded-lg">
      <h3 class="text-lg font-medium text-gray-900 mb-4 flex items-center">
        <DollarSign class="mr-2 h-5 w-5" />
        가격 정보
      </h3>
      
      <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
        <!-- 판매가 -->
        <div>
          <label for="price" class="block text-sm font-medium text-gray-700 mb-1">
            판매가 <span class="text-red-500">*</span>
          </label>
          <div class="relative">
            <input
              id="price"
              type="number"
              bind:value={formData.price}
              class="input pr-8"
              class:border-red-300={errors.price}
              placeholder="0"
              min="0"
              step="1"
              disabled={loading}
              required
            />
            <span class="absolute right-3 top-1/2 transform -translate-y-1/2 text-gray-500">원</span>
          </div>
          {#if errors.price}
            <p class="text-sm text-red-600 mt-1">{errors.price}</p>
          {/if}
        </div>

        <!-- 정가 -->
        <div>
          <label for="originalPrice" class="block text-sm font-medium text-gray-700 mb-1">
            정가
          </label>
          <div class="relative">
            <input
              id="originalPrice"
              type="number"
              bind:value={formData.originalPrice}
              class="input pr-8"
              class:border-red-300={errors.originalPrice}
              placeholder="판매가와 동일"
              min="0"
              step="1"
              disabled={loading}
            />
            <span class="absolute right-3 top-1/2 transform -translate-y-1/2 text-gray-500">원</span>
          </div>
          {#if errors.originalPrice}
            <p class="text-sm text-red-600 mt-1">{errors.originalPrice}</p>
          {/if}
          <p class="text-xs text-gray-500 mt-1">할인 표시용 정가입니다</p>
        </div>
      </div>
    </div>

    <!-- 재고 정보 -->
    <div class="bg-gray-50 p-4 rounded-lg">
      <h3 class="text-lg font-medium text-gray-900 mb-4 flex items-center">
        <Hash class="mr-2 h-5 w-5" />
        재고 정보
      </h3>
      
      <div class="grid grid-cols-1 md:grid-cols-3 gap-4">
        <!-- 초기 재고 -->
        <div>
          <label for="stockQuantity" class="block text-sm font-medium text-gray-700 mb-1">
            초기 재고 <span class="text-red-500">*</span>
          </label>
          <div class="relative">
            <input
              id="stockQuantity"
              type="number"
              bind:value={formData.stockQuantity}
              class="input pr-8"
              class:border-red-300={errors.stockQuantity}
              placeholder="0"
              min="0"
              disabled={loading}
              required
            />
            <span class="absolute right-3 top-1/2 transform -translate-y-1/2 text-gray-500">개</span>
          </div>
          {#if errors.stockQuantity}
            <p class="text-sm text-red-600 mt-1">{errors.stockQuantity}</p>
          {/if}
        </div>

        <!-- 최소 재고 -->
        <div>
          <label for="minStockLevel" class="block text-sm font-medium text-gray-700 mb-1">
            최소 재고 <span class="text-red-500">*</span>
          </label>
          <div class="relative">
            <input
              id="minStockLevel"
              type="number"
              bind:value={formData.minStockLevel}
              class="input pr-8"
              class:border-red-300={errors.minStockLevel}
              placeholder="0"
              min="0"
              disabled={loading}
              required
            />
            <span class="absolute right-3 top-1/2 transform -translate-y-1/2 text-gray-500">개</span>
          </div>
          {#if errors.minStockLevel}
            <p class="text-sm text-red-600 mt-1">{errors.minStockLevel}</p>
          {/if}
          <p class="text-xs text-gray-500 mt-1">재고 부족 알림 기준</p>
        </div>

        <!-- 최대 재고 -->
        <div>
          <label for="maxStockLevel" class="block text-sm font-medium text-gray-700 mb-1">
            최대 재고
          </label>
          <div class="relative">
            <input
              id="maxStockLevel"
              type="number"
              bind:value={formData.maxStockLevel}
              class="input pr-8"
              class:border-red-300={errors.maxStockLevel}
              placeholder="9999"
              min="0"
              disabled={loading}
            />
            <span class="absolute right-3 top-1/2 transform -translate-y-1/2 text-gray-500">개</span>
          </div>
          {#if errors.maxStockLevel}
            <p class="text-sm text-red-600 mt-1">{errors.maxStockLevel}</p>
          {/if}
        </div>
      </div>
    </div>

    <!-- 식별 정보 -->
    <div class="bg-gray-50 p-4 rounded-lg">
      <h3 class="text-lg font-medium text-gray-900 mb-4 flex items-center">
        <Barcode class="mr-2 h-5 w-5" />
        식별 정보
      </h3>
      
      <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
        <!-- 바코드 -->
        <div>
          <label for="barcode" class="block text-sm font-medium text-gray-700 mb-1">
            바코드
          </label>
          <div class="flex">
            <input
              id="barcode"
              type="text"
              bind:value={formData.barcode}
              class="input rounded-r-none"
              placeholder="바코드를 입력하거나 생성하세요"
              disabled={loading}
            />
            <button
              type="button"
              class="px-3 py-2 border border-l-0 border-gray-300 rounded-r-md bg-gray-50 text-gray-700 hover:bg-gray-100"
              on:click={generateBarcode}
              disabled={loading}
            >
              생성
            </button>
          </div>
          <p class="text-xs text-gray-500 mt-1">POS에서 바코드 스캔용</p>
        </div>

        <!-- SKU -->
        <div>
          <label for="sku" class="block text-sm font-medium text-gray-700 mb-1">
            SKU (상품코드)
          </label>
          <div class="flex">
            <input
              id="sku"
              type="text"
              bind:value={formData.sku}
              class="input rounded-r-none"
              placeholder="SKU를 입력하거나 생성하세요"
              disabled={loading}
            />
            <button
              type="button"
              class="px-3 py-2 border border-l-0 border-gray-300 rounded-r-md bg-gray-50 text-gray-700 hover:bg-gray-100"
              on:click={generateSku}
              disabled={loading}
            >
              생성
            </button>
          </div>
          <p class="text-xs text-gray-500 mt-1">재고 관리용 고유 코드</p>
        </div>
      </div>

      <!-- 이미지 URL -->
      <div class="mt-4">
        <label for="imageUrl" class="block text-sm font-medium text-gray-700 mb-1">
          이미지 URL
        </label>
        <input
          id="imageUrl"
          type="url"
          bind:value={formData.imageUrl}
          class="input"
          placeholder="https://example.com/image.jpg"
          disabled={loading}
        />
        <p class="text-xs text-gray-500 mt-1">상품 이미지 URL을 입력하세요</p>
      </div>
    </div>

    <!-- 전체 폼 에러 -->
    {#if errors.submit}
      <div class="p-3 bg-red-50 border border-red-200 rounded-lg">
        <p class="text-sm text-red-700">{errors.submit}</p>
      </div>
    {/if}
  </form>

  <!-- 푸터 버튼들 -->
  <div slot="footer" class="flex justify-end space-x-3 p-4 border-t border-gray-200">
    <button
      type="button"
      class="btn btn-secondary"
      on:click={handleClose}
      disabled={loading}
    >
      취소
    </button>
    <button
      type="submit"
      class="btn btn-primary"
      on:click={handleSubmit}
      disabled={loading}
    >
      {#if loading}
        <div class="flex items-center">
          <div class="animate-spin rounded-full h-4 w-4 border-b-2 border-white mr-2"></div>
          등록 중...
        </div>
      {:else}
        상품 등록
      {/if}
    </button>
  </div>
</Modal>
