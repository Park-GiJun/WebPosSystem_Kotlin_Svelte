<script>
	import { onMount } from 'svelte';
	
	let salesHistory = [];
	let loading = true;
	let error = null;
	let selectedDate = new Date().toISOString().split('T')[0];

	onMount(async () => {
		await loadSalesHistory();
	});

	async function loadSalesHistory() {
		loading = true;
		try {
			const response = await fetch(`/api/pos/sales/history?date=${selectedDate}`, {
				headers: {
					'Authorization': `Bearer ${localStorage.getItem('token')}`
				}
			});

			if (!response.ok) {
				throw new Error('API 호출 실패');
			}

			const data = await response.json();
			salesHistory = data.data || [];
		} catch (err) {
			error = err.message;
		} finally {
			loading = false;
		}
	}

	$: if (selectedDate) {
		loadSalesHistory();
	}
</script>

<svelte:head>
	<title>판매 내역 - WebPOS</title>
</svelte:head>

<div class="p-6">
	<div class="mb-6">
		<h1 class="text-2xl font-bold text-gray-900">판매 내역</h1>
		<p class="text-gray-600 mt-2">판매 이력을 조회합니다.</p>
	</div>

	<div class="mb-6">
		<div class="flex items-center space-x-4">
			<div>
				<label class="block text-sm font-medium text-gray-700 mb-2">조회 날짜</label>
				<input 
					type="date" 
					bind:value={selectedDate}
					class="px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500"
				>
			</div>
			<button 
				on:click={loadSalesHistory}
				class="bg-blue-600 hover:bg-blue-700 text-white px-4 py-2 rounded-lg mt-7"
			>
				조회
			</button>
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
		<div class="bg-white shadow rounded-lg">
			<div class="px-6 py-4 border-b border-gray-200">
				<h2 class="text-lg font-medium text-gray-900">
					판매 내역 ({salesHistory.length}건)
				</h2>
			</div>
			
			{#if salesHistory.length === 0}
				<div class="text-center py-8">
					<p class="text-gray-500">선택한 날짜에 판매 내역이 없습니다.</p>
				</div>
			{:else}
				<div class="overflow-x-auto">
					<table class="min-w-full divide-y divide-gray-200">
						<thead class="bg-gray-50">
							<tr>
								<th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
									판매 번호
								</th>
								<th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
									판매 시간
								</th>
								<th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
									상품 수
								</th>
								<th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
									총 금액
								</th>
								<th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
									결제 방법
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
							{#each salesHistory as sale}
								<tr>
									<td class="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-900">
										{sale.saleNumber || '-'}
									</td>
									<td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
										{sale.saleTime || '-'}
									</td>
									<td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
										{sale.itemCount || 0}개
									</td>
									<td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
										₩{(sale.totalAmount || 0).toLocaleString()}
									</td>
									<td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
										{sale.paymentMethod || '-'}
									</td>
									<td class="px-6 py-4 whitespace-nowrap">
										<span class="inline-flex px-2 py-1 text-xs font-semibold rounded-full 
											{sale.status === '완료' ? 'bg-green-100 text-green-800' : 
											 sale.status === '취소' ? 'bg-red-100 text-red-800' : 'bg-yellow-100 text-yellow-800'}">
											{sale.status || '대기'}
										</span>
									</td>
									<td class="px-6 py-4 whitespace-nowrap text-sm font-medium">
										<button class="text-blue-600 hover:text-blue-900 mr-4">상세</button>
										{#if sale.status === '완료'}
											<button class="text-red-600 hover:text-red-900">취소</button>
										{/if}
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
