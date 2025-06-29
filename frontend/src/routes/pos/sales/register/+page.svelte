<script>
	import { onMount } from 'svelte';
	
	let cart = [];
	let searchTerm = '';
	let searchResults = [];
	let loading = false;
	let error = null;
	let total = 0;

	$: total = cart.reduce((sum, item) => sum + (item.price * item.quantity), 0);

	async function searchProducts() {
		if (!searchTerm.trim()) {
			searchResults = [];
			return;
		}

		loading = true;
		try {
			const response = await fetch(`/api/pos/products/search?q=${encodeURIComponent(searchTerm)}`, {
				headers: {
					'Authorization': `Bearer ${localStorage.getItem('token')}`
				}
			});

			if (!response.ok) {
				throw new Error('API 호출 실패');
			}

			const data = await response.json();
			searchResults = data.data || [];
		} catch (err) {
			error = err.message;
		} finally {
			loading = false;
		}
	}

	function addToCart(product) {
		const existingItem = cart.find(item => item.id === product.id);
		if (existingItem) {
			existingItem.quantity += 1;
		} else {
			cart = [...cart, { ...product, quantity: 1 }];
		}
		searchTerm = '';
		searchResults = [];
	}

	function removeFromCart(index) {
		cart = cart.filter((_, i) => i !== index);
	}

	function updateQuantity(index, quantity) {
		if (quantity <= 0) {
			removeFromCart(index);
		} else {
			cart[index].quantity = quantity;
			cart = [...cart];
		}
	}

	async function processSale() {
		if (cart.length === 0) return;

		try {
			const response = await fetch('/api/pos/sales/register', {
				method: 'POST',
				headers: {
					'Content-Type': 'application/json',
					'Authorization': `Bearer ${localStorage.getItem('token')}`
				},
				body: JSON.stringify({
					items: cart.map(item => ({
						productId: item.id,
						quantity: item.quantity,
						price: item.price
					})),
					total: total
				})
			});

			if (!response.ok) {
				throw new Error('판매 등록 실패');
			}

			// 성공시 장바구니 비우기
			cart = [];
			alert('판매가 완료되었습니다.');
		} catch (err) {
			alert(err.message);
		}
	}

	// 검색어 디바운싱
	let searchTimeout;
	$: {
		clearTimeout(searchTimeout);
		searchTimeout = setTimeout(() => {
			searchProducts();
		}, 300);
	}
</script>

<svelte:head>
	<title>판매 등록 - WebPOS</title>
</svelte:head>

<div class="p-6">
	<div class="mb-6">
		<h1 class="text-2xl font-bold text-gray-900">판매 등록</h1>
		<p class="text-gray-600 mt-2">상품을 스캔하거나 검색하여 판매를 등록합니다.</p>
	</div>

	<div class="grid grid-cols-1 lg:grid-cols-2 gap-6">
		<!-- 상품 검색 영역 -->
		<div class="bg-white shadow rounded-lg">
			<div class="px-6 py-4 border-b border-gray-200">
				<h2 class="text-lg font-medium text-gray-900">상품 검색</h2>
			</div>
			<div class="p-6">
				<div class="relative mb-4">
					<input 
						type="text" 
						bind:value={searchTerm}
						placeholder="상품명, 바코드 검색..."
						class="w-full px-4 py-3 pl-10 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500"
						autofocus
					>
					<div class="absolute inset-y-0 left-0 pl-3 flex items-center">
						<svg class="h-5 w-5 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
							<path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z"></path>
						</svg>
					</div>
				</div>

				{#if loading}
					<div class="text-center py-4">
						<div class="animate-spin rounded-full h-6 w-6 border-b-2 border-blue-600 mx-auto"></div>
					</div>
				{:else if searchResults.length > 0}
					<div class="space-y-2 max-h-64 overflow-y-auto">
						{#each searchResults as product}
							<div class="flex items-center p-3 border border-gray-200 rounded-lg hover:bg-gray-50 cursor-pointer" 
								on:click={() => addToCart(product)}>
								<div class="flex-1">
									<h4 class="font-medium">{product.name}</h4>
									<p class="text-sm text-gray-500">₩{product.price?.toLocaleString()}</p>
								</div>
								<button class="bg-blue-600 text-white px-3 py-1 rounded text-sm">
									추가
								</button>
							</div>
						{/each}
					</div>
				{/if}
			</div>
		</div>

		<!-- 장바구니 영역 -->
		<div class="bg-white shadow rounded-lg">
			<div class="px-6 py-4 border-b border-gray-200">
				<h2 class="text-lg font-medium text-gray-900">장바구니</h2>
			</div>
			<div class="p-6">
				{#if cart.length === 0}
					<div class="text-center py-8 text-gray-500">
						장바구니가 비어있습니다.
					</div>
				{:else}
					<div class="space-y-3 mb-4 max-h-64 overflow-y-auto">
						{#each cart as item, index}
							<div class="flex items-center justify-between p-3 border border-gray-200 rounded-lg">
								<div class="flex-1">
									<h4 class="font-medium">{item.name}</h4>
									<p class="text-sm text-gray-500">₩{item.price?.toLocaleString()}</p>
								</div>
								<div class="flex items-center space-x-2">
									<button 
										on:click={() => updateQuantity(index, item.quantity - 1)}
										class="w-8 h-8 flex items-center justify-center border border-gray-300 rounded"
									>
										-
									</button>
									<span class="w-8 text-center">{item.quantity}</span>
									<button 
										on:click={() => updateQuantity(index, item.quantity + 1)}
										class="w-8 h-8 flex items-center justify-center border border-gray-300 rounded"
									>
										+
									</button>
									<button 
										on:click={() => removeFromCart(index)}
										class="text-red-600 hover:text-red-800 ml-2"
									>
										삭제
									</button>
								</div>
							</div>
						{/each}
					</div>
					
					<div class="border-t pt-4">
						<div class="flex justify-between items-center mb-4">
							<span class="text-lg font-medium">총 금액:</span>
							<span class="text-2xl font-bold text-blue-600">₩{total.toLocaleString()}</span>
						</div>
						<button 
							on:click={processSale}
							class="w-full bg-green-600 hover:bg-green-700 text-white py-3 px-4 rounded-lg font-medium"
						>
							결제하기
						</button>
					</div>
				{/if}
			</div>
		</div>
	</div>
</div>
