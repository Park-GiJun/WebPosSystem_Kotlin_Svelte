<script>
	import { onMount } from 'svelte';
	
	let loading = true;
	let error = null;

	onMount(() => {
		// 상품 관리 메인 페이지로 기능 단순화
		loading = false;
	});
</script>

<svelte:head>
	<title>상품 관리 - WebPOS</title>
</svelte:head>

<div class="p-6">
	<div class="mb-6">
		<h1 class="text-2xl font-bold text-gray-900">상품 관리</h1>
		<p class="text-gray-600 mt-2">상품 정보를 관리합니다.</p>
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
		<div class="grid grid-cols-1 md:grid-cols-2 gap-6">
			<div class="bg-white shadow rounded-lg p-6">
				<h2 class="text-lg font-medium text-gray-900 mb-4">상품 목록</h2>
				<p class="text-gray-600 mb-4">POS 상품 목록을 조회합니다.</p>
				<a href="/pos/products/catalog" class="bg-blue-600 hover:bg-blue-700 text-white px-4 py-2 rounded-lg inline-block">
					상품 목록으로 이동
				</a>
			</div>
			
			<div class="bg-white shadow rounded-lg p-6">
				<h2 class="text-lg font-medium text-gray-900 mb-4">상품 검색</h2>
				<p class="text-gray-600 mb-4">상품을 빠르게 검색합니다.</p>
				<a href="/pos/products/search" class="bg-green-600 hover:bg-green-700 text-white px-4 py-2 rounded-lg inline-block">
					상품 검색으로 이동
				</a>
			</div>
		</div>
	{/if}
</div>
