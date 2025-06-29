<script>
	import { onMount } from 'svelte';
	
	let returns = [];
	let loading = true;
	let error = null;
	let searchSaleNumber = '';
	let returnItems = [];

	onMount(async () => {
		await loadReturns();
	});

	async function loadReturns() {
		try {
			const response = await fetch('/api/pos/sales/returns', {
				headers: {
					'Authorization': `Bearer ${localStorage.getItem('token')}`
				}
			});

			if (!response.ok) {
				throw new Error('API 호출 실패');
			}

			const data = await response.json();
			returns = data.data || [];
		} catch (err) {
			error = err.message;
		} finally {
			loading = false;
		}
	}

	async function searchSale() {
		if (!searchSaleNumber.trim()) return;

		try {
			const response = await fetch(`/api/pos/sales/${searchSaleNumber}`, {
				headers: {
					'Authorization': `Bearer ${localStorage.getItem('token')}`
				}
			});

			if (!response.ok) {
				throw new Error('판매 정보를 찾을 수 없습니다');
			}

			const data = await response.json();
			returnItems = data.data.items || [];
		} catch (err) {
			error = err.message;
			returnItems = [];
		}
	}

	async function processReturn(items) {
		try {
			const response = await fetch('/api/pos/sales/returns', {
				method: 'POST',
				headers: {
					'Content-Type': 'application/json',
					'Authorization': `Bearer ${localStorage.getItem('token')}`
				},
				body: JSON.stringify({
					saleNumber: searchSaleNumber,
					items: items
				})
			});

			if (!response.ok) {
				throw new Error('반품 처리 실패');
			}

			alert('반품이 완료되었습니다.');
			searchSaleNumber = '';
			returnItems = [];
			await loadReturns();
		} catch (err) {
			alert(err.message);
		}
	}
</script>

<svelte:head>
	<title>반품/교환 - WebPOS</title>
</svelte:head>

<div class="p-6">
	<div class="mb-6">
		<h1 class="text-2xl font-bold text-gray-900">반품/교환</h1>
		<p class="text-gray-600 mt-2">반품 및 교환을 처리합니다.</p>
	</div>

	<div class="grid grid-cols-1 lg:grid-cols-2 gap-6">
		<!-- 반품 처리 영역 -->
		<div class="bg-white shadow rounded-lg">
			<div class="px-6 py-4 border-b border-gray-200">
				<h2 class="text-lg font-medium text-gray-900">반품 처리</h2>
			</div>
			<div class="p-6">
				<div class="mb-4">
					<label class="block text-sm font-medium text-gray-700 mb-2">판매 번호</label>
					<div class="flex space-x-2">
						<input 
							type="text" 
							bind:value={searchSaleNumber}
							placeholder="판매 번호를 입력하세요"
							class="flex-1 px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500"
						>
						<button 
							on:click={searchSale}
							class="bg-blue-600 hover:bg-blue-700 text-white px-4 py-2 rounded-lg"
						>
							조회
						</button>
					</div>
				</div>

				{#if returnItems.length > 0}
					<div class="space-y-3 mb-4">
						<h3 class="font-medium text-gray-900">반품 가능 상품</h3>
						{#each returnItems as item}
							<div class="flex items-center justify-between p-3 border border-gray-200 rounded-lg">
								<div class="flex-1">
									<h4 class="font-medium">{item.productName}</h4>
									<p class="text-sm text-gray-500">수량: {item.quantity}개 | 단가: ₩{item.price?.toLocaleString()}</p>
								</div>
								<button 
									on:click={() => processReturn([item])}
									class="bg-red-600 hover:bg-red-700 text-white px-3 py-1 rounded text-sm"
								>
									반품
								</button>
							</div>
						{/each}
						<button 
							on:click={() => processReturn(returnItems)}
							class="w-full bg-red-600 hover:bg-red-700 text-white py-2 px-4 rounded-lg"
						>
							전체 반품
						</button>
					</div>
				{/if}
			</div>
		</div>

		<!-- 반품 내역 영역 -->
		<div class="bg-white shadow rounded-lg">
			<div class="px-6 py-4 border-b border-gray-200">
				<h2 class="text-lg font-medium text-gray-900">반품 내역</h2>
			</div>
			<div class="p-6">
				{#if loading}
					<div class="text-center py-8">
						<div class="animate-spin rounded-full h-6 w-6 border-b-2 border-blue-600 mx-auto"></div>
					</div>
				{:else if error}
					<div class="bg-red-50 border border-red-200 text-red-600 px-4 py-3 rounded">
						{error}
					</div>
				{:else if returns.length === 0}
					<div class="text-center py-8 text-gray-500">
						반품 내역이 없습니다.
					</div>
				{:else}
					<div class="space-y-3 max-h-64 overflow-y-auto">
						{#each returns as returnItem}
							<div class="p-3 border border-gray-200 rounded-lg">
								<div class="flex justify-between items-start">
									<div>
										<h4 class="font-medium">판매번호: {returnItem.saleNumber}</h4>
										<p class="text-sm text-gray-500">반품일: {returnItem.returnDate}</p>
										<p class="text-sm text-gray-500">반품금액: ₩{returnItem.returnAmount?.toLocaleString()}</p>
									</div>
									<span class="inline-flex px-2 py-1 text-xs font-semibold rounded-full bg-red-100 text-red-800">
										반품완료
									</span>
								</div>
							</div>
						{/each}
					</div>
				{/if}
			</div>
		</div>
	</div>
</div>
