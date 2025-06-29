<script>
	import { onMount } from 'svelte';
	
	let products = [];
	let loading = true;
	let error = null;
	let searchTerm = '';

	onMount(async () => {
		await loadProducts();
	});

	async function loadProducts() {
		try {
			const response = await fetch('/api/pos/products/catalog', {
				headers: {
					'Authorization': `Bearer ${localStorage.getItem('token')}`
				}
			});

			if (!response.ok) {
				throw new Error('API 호출 실패');
			}

			const data = await response.json();
			products = data.data || [];
		} catch (err) {
			error = err.message;
		} finally {
			loading = false;
		}
	}

	$: filteredProducts = products.filter(product => 
		product.name?.toLowerCase().includes(searchTerm.toLowerCase()) ||
		product.code?.toLowerCase().includes(searchTerm.toLowerCase())
	);
</script>

<svelte:head>
	<title>상품 목록 - WebPOS</title>
</svelte:head>

<div class="p-6">
	<div class="mb-6">
		<h1 class="text-2xl font-bold text-gray-900">상품 목록</h1>
		<p class="text-gray-600 mt-2">POS 상품 목록을 조회합니다.</p>
	</div>

	<div class="mb-6">
		<div class="relative">
			<input 
				type="text" 
				bind:value={searchTerm}
				placeholder="상품명 또는 상품코드로 검색..."
				class="w-full px-4 py-2 pl-10 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
			>
			<div class="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
				<svg class="h-5 w-5 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
					<path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z"></path>
				</svg>
			</div>
		</div>
	</div>

	{#if loading}
		<div class="flex justify-center items-center h-64">
			<div class="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600"></div>
		</div>
	{:else if error}
		<div class="bg-red-50 border border-red-200 text-red-600 px-4 py-3 rounded">
			{error}
		</div>
	{:else}
		<div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6">
			{#each filteredProducts as product}
				<div class="bg-white border border-gray-200 rounded-lg shadow hover:shadow-md transition-shadow">
					<div class="p-4">
						<div class="aspect-square bg-gray-100 rounded-lg mb-4 flex items-center justify-center">
							{#if product.imageUrl}
								<img src={product.imageUrl} alt={product.name} class="w-full h-full object-cover rounded-lg">
							{:else}
								<svg class="w-12 h-12 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
									<path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z"></path>
								</svg>
							{/if}
						</div>
						<h3 class="font-medium text-gray-900 mb-2">{product.name || '상품명 없음'}</h3>
						<p class="text-sm text-gray-500 mb-2">코드: {product.code || '-'}</p>
						<p class="text-lg font-bold text-blue-600">₩{(product.price || 0).toLocaleString()}</p>
						<div class="mt-4">
							<button class="w-full bg-blue-600 hover:bg-blue-700 text-white py-2 px-4 rounded-lg transition-colors">
								장바구니에 추가
							</button>
						</div>
					</div>
				</div>
			{:else}
				<div class="col-span-full text-center py-8">
					<p class="text-gray-500">
						{searchTerm ? '검색 결과가 없습니다.' : '등록된 상품이 없습니다.'}
					</p>
				</div>
			{/each}
		</div>
	{/if}
</div>
