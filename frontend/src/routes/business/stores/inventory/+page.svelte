<script>
	import { onMount } from 'svelte';
	
	let inventory = [];
	let loading = true;
	let error = null;

	onMount(async () => {
		try {
			const response = await fetch('/api/business/stores/inventory', {
				headers: {
					'Authorization': `Bearer ${localStorage.getItem('token')}`
				}
			});

			if (!response.ok) {
				throw new Error('API 호출 실패');
			}

			const data = await response.json();
			inventory = data.data || [];
		} catch (err) {
			error = err.message;
		} finally {
			loading = false;
		}
	});
</script>

<svelte:head>
	<title>재고 관리 - WebPOS</title>
</svelte:head>

<div class="p-6">
	<div class="mb-6">
		<h1 class="text-2xl font-bold text-gray-900">재고 관리</h1>
		<p class="text-gray-600 mt-2">매장 재고 정보를 관리합니다.</p>
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
		<div class="bg-white shadow rounded-lg">
			<div class="px-6 py-4 border-b border-gray-200">
				<div class="flex justify-between items-center">
					<h2 class="text-lg font-medium text-gray-900">재고 현황</h2>
					<div class="flex gap-2">
						<button class="bg-green-600 hover:bg-green-700 text-white px-4 py-2 rounded-lg">
							입고 등록
						</button>
						<button class="bg-blue-600 hover:bg-blue-700 text-white px-4 py-2 rounded-lg">
							재고 조정
						</button>
					</div>
				</div>
			</div>
			
			{#if inventory.length === 0}
				<div class="text-center py-8">
					<p class="text-gray-500">재고 정보가 없습니다.</p>
				</div>
			{:else}
				<div class="overflow-x-auto">
					<table class="min-w-full divide-y divide-gray-200">
						<thead class="bg-gray-50">
							<tr>
								<th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
									상품명
								</th>
								<th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
									상품 코드
								</th>
								<th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
									현재 재고
								</th>
								<th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
									안전 재고
								</th>
								<th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
									상태
								</th>
								<th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
									작업
								</th>
							</tr>
						</thead>
						<tbody class="bg-white divide-y divide-gray-200">
							{#each inventory as item}
								<tr>
									<td class="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-900">
										{item.productName || '-'}
									</td>
									<td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
										{item.productCode || '-'}
									</td>
									<td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
										{item.currentStock || 0}개
									</td>
									<td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
										{item.safetyStock || 0}개
									</td>
									<td class="px-6 py-4 whitespace-nowrap">
										<span class="inline-flex px-2 py-1 text-xs font-semibold rounded-full 
											{item.currentStock > item.safetyStock ? 'bg-green-100 text-green-800' : 
											 item.currentStock > 0 ? 'bg-yellow-100 text-yellow-800' : 'bg-red-100 text-red-800'}">
											{item.currentStock > item.safetyStock ? '충분' : 
											 item.currentStock > 0 ? '부족' : '품절'}
										</span>
									</td>
									<td class="px-6 py-4 whitespace-nowrap text-sm font-medium">
										<button class="text-blue-600 hover:text-blue-900 mr-4">수정</button>
										<button class="text-green-600 hover:text-green-900">입고</button>
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
