<script>
	import { onMount } from 'svelte';
	
	let loading = true;
	let error = null;

	onMount(() => {
		// 보고서 메인 페이지로 기능 단순화
		loading = false;
	});
</script>

<svelte:head>
	<title>통계/보고서 - WebPOS</title>
</svelte:head>

<div class="p-6">
	<div class="mb-6">
		<h1 class="text-2xl font-bold text-gray-900">통계/보고서</h1>
		<p class="text-gray-600 mt-2">매출 및 운영 통계를 확인합니다.</p>
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
		<div class="grid grid-cols-1 md:grid-cols-3 gap-6">
			<div class="bg-white shadow rounded-lg p-6">
				<h2 class="text-lg font-medium text-gray-900 mb-4">매출 분석</h2>
				<p class="text-gray-600 mb-4">매출 통계 및 분석 정보를 확인합니다.</p>
				<a href="/business/reports/sales" class="bg-blue-600 hover:bg-blue-700 text-white px-4 py-2 rounded-lg inline-block">
					매출 분석으로 이동
				</a>
			</div>
			
			<div class="bg-white shadow rounded-lg p-6">
				<h2 class="text-lg font-medium text-gray-900 mb-4">상품 분석</h2>
				<p class="text-gray-600 mb-4">상품별 통계 분석 정보를 확인합니다.</p>
				<a href="/business/reports/products" class="bg-green-600 hover:bg-green-700 text-white px-4 py-2 rounded-lg inline-block">
					상품 분석으로 이동
				</a>
			</div>

			<div class="bg-white shadow rounded-lg p-6">
				<h2 class="text-lg font-medium text-gray-900 mb-4">고객 분석</h2>
				<p class="text-gray-600 mb-4">고객 통계 분석 정보를 확인합니다.</p>
				<a href="/business/reports/customers" class="bg-purple-600 hover:bg-purple-700 text-white px-4 py-2 rounded-lg inline-block">
					고객 분석으로 이동
				</a>
			</div>
		</div>
	{/if}
</div>
