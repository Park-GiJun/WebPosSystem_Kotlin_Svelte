<script>
	import { onMount } from 'svelte';
	
	let storeInfo = {};
	let loading = true;
	let error = null;

	onMount(async () => {
		try {
			const response = await fetch('/api/business/stores/info', {
				headers: {
					'Authorization': `Bearer ${localStorage.getItem('token')}`
				}
			});

			if (!response.ok) {
				throw new Error('API 호출 실패');
			}

			const data = await response.json();
			storeInfo = data.data || {};
		} catch (err) {
			error = err.message;
		} finally {
			loading = false;
		}
	});
</script>

<svelte:head>
	<title>매장 정보 - WebPOS</title>
</svelte:head>

<div class="p-6">
	<div class="mb-6">
		<h1 class="text-2xl font-bold text-gray-900">매장 정보</h1>
		<p class="text-gray-600 mt-2">매장 기본 정보를 관리합니다.</p>
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
					<h2 class="text-lg font-medium text-gray-900">매장 기본 정보</h2>
					<button class="bg-blue-600 hover:bg-blue-700 text-white px-4 py-2 rounded-lg">
						정보 수정
					</button>
				</div>
			</div>
			
			<div class="p-6">
				<div class="grid grid-cols-1 md:grid-cols-2 gap-6">
					<div>
						<label class="block text-sm font-medium text-gray-700 mb-2">매장명</label>
						<input type="text" value={storeInfo.storeName || ''} readonly 
							class="w-full px-3 py-2 border border-gray-300 rounded-lg bg-gray-50">
					</div>
					<div>
						<label class="block text-sm font-medium text-gray-700 mb-2">매장 코드</label>
						<input type="text" value={storeInfo.storeCode || ''} readonly 
							class="w-full px-3 py-2 border border-gray-300 rounded-lg bg-gray-50">
					</div>
					<div class="md:col-span-2">
						<label class="block text-sm font-medium text-gray-700 mb-2">매장 주소</label>
						<input type="text" value={storeInfo.address || ''} readonly 
							class="w-full px-3 py-2 border border-gray-300 rounded-lg bg-gray-50">
					</div>
					<div>
						<label class="block text-sm font-medium text-gray-700 mb-2">전화번호</label>
						<input type="text" value={storeInfo.phone || ''} readonly 
							class="w-full px-3 py-2 border border-gray-300 rounded-lg bg-gray-50">
					</div>
					<div>
						<label class="block text-sm font-medium text-gray-700 mb-2">영업 시간</label>
						<input type="text" value={storeInfo.businessHours || ''} readonly 
							class="w-full px-3 py-2 border border-gray-300 rounded-lg bg-gray-50">
					</div>
					<div>
						<label class="block text-sm font-medium text-gray-700 mb-2">매장 관리자</label>
						<input type="text" value={storeInfo.manager || ''} readonly 
							class="w-full px-3 py-2 border border-gray-300 rounded-lg bg-gray-50">
					</div>
					<div>
						<label class="block text-sm font-medium text-gray-700 mb-2">개점일</label>
						<input type="text" value={storeInfo.openDate || ''} readonly 
							class="w-full px-3 py-2 border border-gray-300 rounded-lg bg-gray-50">
					</div>
				</div>
			</div>
		</div>
	{/if}
</div>
