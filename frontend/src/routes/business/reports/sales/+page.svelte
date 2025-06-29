<script>
	import { onMount } from 'svelte';
	
	let salesData = [];
	let loading = true;
	let error = null;

	onMount(async () => {
		try {
			const response = await fetch('/api/business/reports/sales', {
				headers: {
					'Authorization': `Bearer ${localStorage.getItem('token')}`
				}
			});

			if (!response.ok) {
				throw new Error('API 호출 실패');
			}

			const data = await response.json();
			salesData = data.data || [];
		} catch (err) {
			error = err.message;
		} finally {
			loading = false;
		}
	});
</script>

<svelte:head>
	<title>매출 분석 - WebPOS</title>
</svelte:head>

<div class="p-6">
	<div class="mb-6">
		<h1 class="text-2xl font-bold text-gray-900">매출 분석</h1>
		<p class="text-gray-600 mt-2">매출 통계 및 분석 정보를 확인합니다.</p>
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
		<div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-6">
			<div class="bg-white p-6 rounded-lg shadow">
				<h3 class="text-sm font-medium text-gray-500">일일 매출</h3>
				<p class="text-2xl font-bold text-gray-900">₩0</p>
			</div>
			<div class="bg-white p-6 rounded-lg shadow">
				<h3 class="text-sm font-medium text-gray-500">주간 매출</h3>
				<p class="text-2xl font-bold text-gray-900">₩0</p>
			</div>
			<div class="bg-white p-6 rounded-lg shadow">
				<h3 class="text-sm font-medium text-gray-500">월간 매출</h3>
				<p class="text-2xl font-bold text-gray-900">₩0</p>
			</div>
			<div class="bg-white p-6 rounded-lg shadow">
				<h3 class="text-sm font-medium text-gray-500">연간 매출</h3>
				<p class="text-2xl font-bold text-gray-900">₩0</p>
			</div>
		</div>

		<div class="bg-white shadow rounded-lg">
			<div class="px-6 py-4 border-b border-gray-200">
				<h2 class="text-lg font-medium text-gray-900">매출 분석 차트</h2>
			</div>
			<div class="p-6">
				<div class="h-64 flex items-center justify-center text-gray-500">
					매출 차트가 표시될 영역입니다.
				</div>
			</div>
		</div>
	{/if}
</div>
