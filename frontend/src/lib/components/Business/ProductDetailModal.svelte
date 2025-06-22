<script>
  import { createEventDispatcher } from 'svelte';
  import { ProductApi } from '$lib/api/product.js';
  import { toastStore } from '$lib/stores/toast.js';
  import Modal from '$lib/components/Common/Modal.svelte';
  import { Package, Edit, Trash2, Plus, Minus, Barcode, Hash, DollarSign, Calendar, User } from 'lucide-svelte';

  export let open = false;
  export let product;

  const dispatch = createEventDispatcher();

  let loading = false;
  let stockAdjustmentValue = '';
  let showStockAdjustment = false;

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
    const statusMap = {
      'AVAILABLE': '판매중',
      'OUT_OF_STOCK': '품절',
      'DISCONTINUED': '단종',
      'TEMPORARILY_UNAVAILABLE': '일시품절',
      'PREPARING': '준비중'
    };
    return statusMap[status] || status;
  }

  // 카테고리 이름 변환
  function getCategoryName(category) {
    const categoryMap = {
      'FOOD': '음식',
      'BEVERAGE': '음료',
      'DESSERT': '디저트',
      'MERCHANDISE': '상품',
      'SERVICE': '서비스',
      'ETC': '기타'
    };
    return categoryMap[category] || category;
  }

  // 재고 조정
  async function handleStockAdjustment() {
    if (!stockAdjustmentValue || stockAdjustmentValue == 0) {
      toastStore.add('조정할 수량을 입력하세요.', 'error');
      return;
    }

    try {
      loading = true;
      await ProductApi.adjustStock(product.productId, parseInt(stockAdjustmentValue));
      toastStore.add('재고가 조정되었습니다.', 'success');
      
      // 상품 정보 업데이트
      product.stockQuantity += parseInt(stockAdjustmentValue);
      stockAdjustmentValue = '';
      showStockAdjustment = false;
      
    } catch (error) {
      console.error('재고 조정 실패:', error);
      toastStore.add('재고 조정에 실패했습니다.', 'error');
    } finally {
      loading = false;
    }
  }

  function handleClose() {
    if (!loading) {
      open = false;
      showStockAdjustment = false;
      stockAdjustmentValue = '';
      dispatch('close');
    }
  }

  function handleEdit() {
    dispatch('edit');
  }

  function handleDelete() {
    dispatch('delete');
  }

  // 날짜 포맷팅
  function formatDate(dateString) {
    if (!dateString) return '-';
    return new Date(dateString).toLocaleDateString('ko-KR', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit'
    });
  }
</script>

<Modal bind:open title="상품 상세정보" size="xl" on:close={handleClose}>
  {#if product}
    <div class="space-y-6">
      <!-- 상품 헤더 -->
      <div class="flex items-start justify-between">
        <div class="flex items-center space-x-4">
          <div class="h-16 w-16 bg-gray-100 rounded-lg flex items-center justify-center">
            {#if product.imageUrl}
              <img src={product.imageUrl} alt={product.name} class="h-full w-full object-cover rounded-lg" />
            {:else}
              <Package class="h-8 w-8 text-gray-400" />
            {/if}
          </div>
          <div>
            <h2 class="text-xl font-bold text-gray-900">{product.name}</h2>
            <div class="flex items-center space-x-2 mt-1">
              <span class="inline-flex px-2 py-1 text-xs font-semibold rounded-full bg-blue-100 text-blue-800">
                {getCategoryName(product.category)}
              </span>
              <span class="inline-flex px-2 py-1 text-xs font-semibold rounded-full {getStatusBadgeClass(product.status)}">
                {getStatusName(product.status)}
              </span>
              {#if !product.isActive}
                <span class="inline-flex px-2 py-1 text-xs font-semibold rounded-full bg-gray-100 text-gray-800">
                  비활성
                </span>
              {/if}
            </div>
          </div>
        </div>
        
        <div class="flex space-x-2">
          <button
            type="button"
            class="btn btn-secondary"
            on:click={handleEdit}
            disabled={loading}
          >
            <Edit size="16" class="mr-2" />
            수정
          </button>
          <button
            type="button"
            class="btn btn-danger"
            on:click={handleDelete}
            disabled={loading}
          >
            <Trash2 size="16" class="mr-2" />
            삭제
          </button>
        </div>
      </div>

      <!-- 기본 정보 -->
      <div class="bg-gray-50 p-4 rounded-lg">
        <h3 class="text-lg font-medium text-gray-900 mb-4 flex items-center">
          <Package class="mr-2 h-5 w-5" />
          기본 정보
        </h3>
        
        <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
          <div>
            <label class="block text-sm font-medium text-gray-500">상품명</label>
            <p class="mt-1 text-sm text-gray-900">{product.name}</p>
          </div>
          
          <div>
            <label class="block text-sm font-medium text-gray-500">카테고리</label>
            <p class="mt-1 text-sm text-gray-900">{getCategoryName(product.category)}</p>
          </div>
          
          <div>
            <label class="block text-sm font-medium text-gray-500">상품 유형</label>
            <p class="mt-1 text-sm text-gray-900">{getCategoryName(product.productType)}</p>
          </div>
          
          <div>
            <label class="block text-sm font-medium text-gray-500">진열 순서</label>
            <p class="mt-1 text-sm text-gray-900">{product.displayOrder || 0}</p>
          </div>
        </div>

        {#if product.description}
          <div class="mt-4">
            <label class="block text-sm font-medium text-gray-500">상품 설명</label>
            <p class="mt-1 text-sm text-gray-900 whitespace-pre-wrap">{product.description}</p>
          </div>
        {/if}
      </div>

      <!-- 가격 정보 -->
      <div class="bg-gray-50 p-4 rounded-lg">
        <h3 class="text-lg font-medium text-gray-900 mb-4 flex items-center">
          <DollarSign class="mr-2 h-5 w-5" />
          가격 정보
        </h3>
        
        <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
          <div>
            <label class="block text-sm font-medium text-gray-500">판매가</label>
            <p class="mt-1 text-lg font-bold text-gray-900">{product.price.toLocaleString()}원</p>
          </div>
          
          <div>
            <label class="block text-sm font-medium text-gray-500">정가</label>
            <p class="mt-1 text-sm text-gray-900">
              {product.originalPrice.toLocaleString()}원
              {#if product.originalPrice > product.price}
                <span class="ml-2 text-red-600 text-xs">
                  ({Math.round((1 - product.price / product.originalPrice) * 100)}% 할인)
                </span>
              {/if}
            </p>
          </div>
        </div>
      </div>

      <!-- 재고 정보 -->
      <div class="bg-gray-50 p-4 rounded-lg">
        <div class="flex justify-between items-center mb-4">
          <h3 class="text-lg font-medium text-gray-900 flex items-center">
            <Hash class="mr-2 h-5 w-5" />
            재고 정보
          </h3>
          <button
            type="button"
            class="btn btn-sm btn-secondary"
            on:click={() => showStockAdjustment = !showStockAdjustment}
            disabled={loading}
          >
            재고 조정
          </button>
        </div>
        
        <div class="grid grid-cols-1 md:grid-cols-3 gap-4">
          <div>
            <label class="block text-sm font-medium text-gray-500">현재 재고</label>
            <p class="mt-1 text-lg font-bold text-gray-900">
              {product.stockQuantity}개
              {#if product.stockQuantity <= product.minStockLevel}
                <span class="ml-2 text-red-500 text-sm">⚠️ 재고부족</span>
              {/if}
            </p>
          </div>
          
          <div>
            <label class="block text-sm font-medium text-gray-500">최소 재고</label>
            <p class="mt-1 text-sm text-gray-900">{product.minStockLevel}개</p>
          </div>
          
          <div>
            <label class="block text-sm font-medium text-gray-500">최대 재고</label>
            <p class="mt-1 text-sm text-gray-900">{product.maxStockLevel}개</p>
          </div>
        </div>

        <!-- 재고 조정 섹션 -->
        {#if showStockAdjustment}
          <div class="mt-4 p-3 bg-blue-50 border border-blue-200 rounded">
            <h4 class="text-sm font-medium text-blue-900 mb-2">재고 조정</h4>
            <div class="flex items-center space-x-3">
              <div class="flex-1">
                <input
                  type="number"
                  bind:value={stockAdjustmentValue}
                  class="input"
                  placeholder="조정할 수량 (+ 증가, - 감소)"
                  disabled={loading}
                />
              </div>
              <button
                type="button"
                class="btn btn-sm btn-primary"
                on:click={handleStockAdjustment}
                disabled={loading || !stockAdjustmentValue}
              >
                {#if loading}
                  조정 중...
                {:else}
                  조정
                {/if}
              </button>
              <button
                type="button"
                class="btn btn-sm btn-secondary"
                on:click={() => showStockAdjustment = false}
                disabled={loading}
              >
                취소
              </button>
            </div>
            <p class="text-xs text-blue-600 mt-1">
              예: +10 (10개 증가), -5 (5개 감소)
            </p>
          </div>
        {/if}
      </div>

      <!-- 식별 정보 -->
      <div class="bg-gray-50 p-4 rounded-lg">
        <h3 class="text-lg font-medium text-gray-900 mb-4 flex items-center">
          <Barcode class="mr-2 h-5 w-5" />
          식별 정보
        </h3>
        
        <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
          <div>
            <label class="block text-sm font-medium text-gray-500">상품 ID</label>
            <p class="mt-1 text-sm text-gray-900 font-mono">{product.productId}</p>
          </div>
          
          <div>
            <label class="block text-sm font-medium text-gray-500">바코드</label>
            <p class="mt-1 text-sm text-gray-900 font-mono">{product.barcode || '-'}</p>
          </div>
          
          <div>
            <label class="block text-sm font-medium text-gray-500">SKU</label>
            <p class="mt-1 text-sm text-gray-900 font-mono">{product.sku || '-'}</p>
          </div>
          
          <div>
            <label class="block text-sm font-medium text-gray-500">상태</label>
            <p class="mt-1 text-sm text-gray-900">{getStatusName(product.status)}</p>
          </div>
        </div>

        {#if product.imageUrl}
          <div class="mt-4">
            <label class="block text-sm font-medium text-gray-500">이미지 URL</label>
            <p class="mt-1 text-sm text-gray-900 break-all">{product.imageUrl}</p>
          </div>
        {/if}
      </div>

      <!-- 생성/수정 정보 -->
      <div class="bg-gray-50 p-4 rounded-lg">
        <h3 class="text-lg font-medium text-gray-900 mb-4 flex items-center">
          <Calendar class="mr-2 h-5 w-5" />
          생성/수정 정보
        </h3>
        
        <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
          <div>
            <label class="block text-sm font-medium text-gray-500">생성일</label>
            <p class="mt-1 text-sm text-gray-900">{formatDate(product.createdAt)}</p>
          </div>
          
          <div>
            <label class="block text-sm font-medium text-gray-500">수정일</label>
            <p class="mt-1 text-sm text-gray-900">{formatDate(product.updatedAt)}</p>
          </div>
          
          {#if product.createdBy}
            <div>
              <label class="block text-sm font-medium text-gray-500">생성자</label>
              <p class="mt-1 text-sm text-gray-900">{product.createdBy}</p>
            </div>
          {/if}
          
          {#if product.updatedBy}
            <div>
              <label class="block text-sm font-medium text-gray-500">수정자</label>
              <p class="mt-1 text-sm text-gray-900">{product.updatedBy}</p>
            </div>
          {/if}
        </div>
      </div>
    </div>
  {/if}

  <!-- 푸터 버튼들 -->
  <div slot="footer" class="flex justify-end space-x-3 p-4 border-t border-gray-200">
    <button
      type="button"
      class="btn btn-secondary"
      on:click={handleClose}
      disabled={loading}
    >
      닫기
    </button>
    <button
      type="button"
      class="btn btn-primary"
      on:click={handleEdit}
      disabled={loading}
    >
      <Edit size="16" class="mr-2" />
      수정
    </button>
  </div>
</Modal>
