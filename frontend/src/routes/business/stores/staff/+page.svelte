<script>
	import { onMount } from 'svelte';
	
	let staffList = [];
	let loading = true;
	let error = null;

	onMount(async () => {
		try {
			const response = await fetch('/api/business/stores/staff', {
				headers: {
					'Authorization': `Bearer ${localStorage.getItem('token')}`
				}
			});

			if (!response.ok) {
				throw new Error('API 호출 실패');
			}

			const data = await response.json();
			staffList = data.data || [];
		} catch (err) {
			error = err.message;
		} finally {
			loading = false;
		}
	});
</script>

<svelte:head>
	<title>매장 직원 - WebPOS</title>
</svelte:head>

<div class="p-6">
	<div class="mb-6">
		<h1 class="text-2xl font-bold text-gray-900">매장 직원</h1>
		<p class="text-gray-600 mt-2">매장 직원 정보를 관리합니다.</p>
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
					<h2 class="text-lg font-medium text-gray-900">직원 목록</h2>
					<button class="bg-blue-600 hover:bg-blue-700 text-white px-4 py-2 rounded-lg">
						직원 추가
					</button>
				</div>
			</div>
			
			{#if staffList.length === 0}
				<div class="text-center py-8">
					<p class="text-gray-500">등록된 직원이 없습니다.</p>
				</div>
			{:else}
				<div class="overflow-x-auto">
					<table class="min-w-full divide-y divide-gray-200">
						<thead class="bg-gray-50">
							<tr>
								<th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
									직원명
								</th>
								<th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
									직급
								</th>
								<th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
									연락처
								</th>
								<th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
									입사일
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
							{#each staffList as staff}
								<tr>
									<td class="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-900">
										{staff.name || '-'}
									</td>
									<td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
										{staff.position || '-'}
									</td>
									<td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
										{staff.phone || '-'}
									</td>
									<td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
										{staff.hireDate || '-'}
									</td>
									<td class="px-6 py-4 whitespace-nowrap">
										<span class="inline-flex px-2 py-1 text-xs font-semibold rounded-full 
											{staff.isActive ? 'bg-green-100 text-green-800' : 'bg-red-100 text-red-800'}">
											{staff.isActive ? '근무중' : '휴직/퇴사'}
										</span>
									</td>
									<td class="px-6 py-4 whitespace-nowrap text-sm font-medium">
										<button class="text-blue-600 hover:text-blue-900 mr-4">수정</button>
										<button class="text-red-600 hover:text-red-900">삭제</button>
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
