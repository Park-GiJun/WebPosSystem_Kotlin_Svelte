<script>
	import { onMount } from 'svelte';
	
	let searchTerm = '';
	let searchResults = [];
	let loading = false;
	let error = null;

	async function searchProducts() {
		if (!searchTerm.trim()) {
			searchResults = [];
			return;
		}

		loading = true;
		error = null;

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

	// 검색어가 변경되면 자동으로 검색 (디바운싱 적용)
	let searchTimeout;
	$: {
		clearTimeout(searchTimeout);
		searchTimeout = setTimeout(() => {
			searchProducts();
		}, 300);
	}
</script>

<svelte:head>
	<title>상품 검색 - WebPOS</title>
</svelte:head>

<div class="p-6">
	<div class="mb-6">
		<h1 class="text-2xl font-bold text-gray-900">상품 검색</h1>
		<p class="text-gray-600 mt-2">상품을 빠르게 검색합니다.</p>
	</div>

	<div class="mb-6">
		<div class="relative">
			<input 
				type="text" 
				bind:value={searchTerm}
				placeholder="상품명, 상품코드, 바코드로 검색..."
				class="w-full px-4 py-3 pl-12 text-lg border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
				autofocus
			>
			<div class="absolute inset-y-0 left-0 pl-4 flex items-center pointer-events-none">
				<svg class="h-6 w-6 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
					<path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z"></path>
				</svg>
			</div>
		</div>
	</div>

	{#if loading}
		<div class="flex justify-center items-center h-32">
			<div class="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600"></div>
		</div>
	{:else if error}
		<div class="bg-red-50 border border-red-200 text-red-600 px-4 py-3 rounded">
			{error}
		</div>
	{:else if searchTerm && searchResults.length === 0}
		<div class="text-center py-8">
			<p class="text-gray-500">'{searchTerm}'에 대한 검색 결과가 없습니다.</p>
		</div>
	{:else if searchResults.length > 0}
		<div class="bg-white shadow rounded-lg">
			<div class="px-6 py-4 border-b border-gray-200">
				<h2 class="text-lg font-medium text-gray-900">검색 결과 ({searchResults.length}개)</h2>
			</div>
			<div class="divide-y divide-gray-200">
				{#each searchResults as product}
					<div class="p-6 hover:bg-gray-50 cursor-pointer" on:click={() => {}}>
						<div class="flex items-center">
							<div class="w-16 h-16 bg-gray-100 rounded-lg flex items-center justify-center mr-4">
								{#if product.imageUrl}
									<img src={product.imageUrl} alt={product.name} class="w-full h-full object-cover rounded-lg">
								{:else}
									<svg class="w-8 h-8 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
										<path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z"></path>
									</svg>
								{/if}
							</div>
							<div class="flex-1">
								<h3 class="text-lg font-medium text-gray-900">{product.name || '상품명 없음'}</h3>
								<p class="text-sm text-gray-500">코드: {product.code || '-'}</p>
								{#if product.barcode}
									<p class="text-sm text-gray-500">바코드: {product.barcode}</p>
								{/if}
								<p class="text-lg font-bold text-blue-600 mt-1">₩{(product.price || 0).toLocaleString()}</p>
							</div>
							<div class="ml-4">
								<button class="bg-blue-600 hover:bg-blue-700 text-white px-4 py-2 rounded-lg">
									선택
								</button>
							</div>
						</div>
					</div>
				{/each}
			</div>
		</div>
	{:else}
		<div class="text-center py-12">
			<svg class="mx-auto h-12 w-12 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
				<path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z"></path>
			</svg>
			<h3 class="mt-2 text-sm font-medium text-gray-900">상품 검색</h3>
			<p class="mt-1 text-sm text-gray-500">상품명, 상품코드 또는 바코드를 입력하여 검색하세요.</p>
		</div>
	{/if}
</div>
