<script>
	import { onMount } from 'svelte';
	
	let customerData = [];
	let loading = true;
	let error = null;

	onMount(async () => {
		try {
			const response = await fetch('/api/business/reports/customers', {
				headers: {
					'Authorization': `Bearer ${localStorage.getItem('token')}`
				}
			});

			if (!response.ok) {
				throw new Error('API 호출 실패');
			}

			const data = await response.json();
			customerData = data.data || [];
		} catch (err) {
			error = err.message;
		} finally {
			loading = false;
		}
	});
</script>

<svelte:head>
	<title>고객 분석 - WebPOS</title>
</svelte:head>

<div class="p-6">
	<div class="mb-6">
		<h1 class="text-2xl font-bold text-gray-900">고객 분석</h1>
		<p class="text-gray-600 mt-2">고객 통계 분석 정보를 확인합니다.</p>
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
		<div class="grid grid-cols-1 md:grid-cols-4 gap-6 mb-6">
			<div class="bg-white p-6 rounded-lg shadow">
				<h3 class="text-sm font-medium text-gray-500">총 고객 수</h3>
				<p class="text-2xl font-bold text-gray-900">0명</p>
			</div>
			<div class="bg-white p-6 rounded-lg shadow">
				<h3 class="text-sm font-medium text-gray-500">신규 고객</h3>
				<p class="text-2xl font-bold text-gray-900">0명</p>
			</div>
			<div class="bg-white p-6 rounded-lg shadow">
				<h3 class="text-sm font-medium text-gray-500">재방문 고객</h3>
				<p class="text-2xl font-bold text-gray-900">0명</p>
			</div>
			<div class="bg-white p-6 rounded-lg shadow">
				<h3 class="text-sm font-medium text-gray-500">평균 구매액</h3>
				<p class="text-2xl font-bold text-gray-900">₩0</p>
			</div>
		</div>

		<div class="bg-white shadow rounded-lg">
			<div class="px-6 py-4 border-b border-gray-200">
				<h2 class="text-lg font-medium text-gray-900">고객별 구매 분석</h2>
			</div>
			
			{#if customerData.length === 0}
				<div class="text-center py-8">
					<p class="text-gray-500">분석할 고객 데이터가 없습니다.</p>
				</div>
			{:else}
				<div class="overflow-x-auto">
					<table class="min-w-full divide-y divide-gray-200">
						<thead class="bg-gray-50">
							<tr>
								<th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
									고객명
								</th>
								<th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
									구매 횟수
								</th>
								<th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
									총 구매액
								</th>
								<th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
									평균 구매액
								</th>
								<th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
									최근 방문
								</th>
							</tr>
						</thead>
						<tbody class="bg-white divide-y divide-gray-200">
							{#each customerData as customer}
								<tr>
									<td class="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-900">
										{customer.customerName || '-'}
									</td>
									<td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
										{customer.purchaseCount || 0}회
									</td>
									<td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
										₩{(customer.totalAmount || 0).toLocaleString()}
									</td>
									<td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
										₩{(customer.avgAmount || 0).toLocaleString()}
									</td>
									<td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
										{customer.lastVisit || '-'}
									</td>
								</tr>
							{/each}
						</tbody>
					</table>
				</div>
			{/if}
		</div>
	{/if}
</div>
