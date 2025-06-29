<script>
	import { onMount } from 'svelte';
	
	let paymentSettings = {};
	let loading = true;
	let error = null;
	let saving = false;

	onMount(async () => {
		await loadPaymentSettings();
	});

	async function loadPaymentSettings() {
		try {
			const response = await fetch('/api/pos/settings/payment', {
				headers: {
					'Authorization': `Bearer ${localStorage.getItem('token')}`
				}
			});

			if (!response.ok) {
				throw new Error('API 호출 실패');
			}

			const data = await response.json();
			paymentSettings = data.data || {};
		} catch (err) {
			error = err.message;
		} finally {
			loading = false;
		}
	}

	async function saveSettings() {
		saving = true;
		try {
			const response = await fetch('/api/pos/settings/payment', {
				method: 'PUT',
				headers: {
					'Content-Type': 'application/json',
					'Authorization': `Bearer ${localStorage.getItem('token')}`
				},
				body: JSON.stringify(paymentSettings)
			});

			if (!response.ok) {
				throw new Error('설정 저장 실패');
			}

			alert('결제 설정이 저장되었습니다.');
		} catch (err) {
			alert(err.message);
		} finally {
			saving = false;
		}
	}

	function togglePaymentMethod(method) {
		if (!paymentSettings.enabledMethods) {
			paymentSettings.enabledMethods = [];
		}
		
		const index = paymentSettings.enabledMethods.indexOf(method);
		if (index > -1) {
			paymentSettings.enabledMethods.splice(index, 1);
		} else {
			paymentSettings.enabledMethods.push(method);
		}
		paymentSettings = { ...paymentSettings };
	}
</script>

<svelte:head>
	<title>결제 설정 - WebPOS</title>
</svelte:head>

<div class="p-6">
	<div class="mb-6">
		<h1 class="text-2xl font-bold text-gray-900">결제 설정</h1>
		<p class="text-gray-600 mt-2">결제 수단을 설정합니다.</p>
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
		<div class="space-y-6">
			<!-- 결제 수단 설정 -->
			<div class="bg-white shadow rounded-lg">
				<div class="px-6 py-4 border-b border-gray-200">
					<h2 class="text-lg font-medium text-gray-900">사용 가능한 결제 수단</h2>
				</div>
				<div class="p-6">
					<div class="grid grid-cols-1 md:grid-cols-2 gap-4">
						{#each [
							{ id: 'cash', name: '현금', icon: '💵' },
							{ id: 'card', name: '신용/체크카드', icon: '💳' },
							{ id: 'mobile', name: '모바일 결제', icon: '📱' },
							{ id: 'gift', name: '상품권', icon: '🎁' },
							{ id: 'point', name: '포인트', icon: '⭐' },
							{ id: 'coupon', name: '쿠폰', icon: '🎫' }
						] as method}
							<label class="flex items-center p-4 border border-gray-200 rounded-lg cursor-pointer hover:bg-gray-50">
								<input 
									type="checkbox"
									checked={paymentSettings.enabledMethods?.includes(method.id) || false}
									on:change={() => togglePaymentMethod(method.id)}
									class="mr-3"
								>
								<span class="text-2xl mr-3">{method.icon}</span>
								<span class="font-medium">{method.name}</span>
							</label>
						{/each}
					</div>
				</div>
			</div>

			<!-- 카드 결제 설정 -->
			<div class="bg-white shadow rounded-lg">
				<div class="px-6 py-4 border-b border-gray-200">
					<h2 class="text-lg font-medium text-gray-900">카드 결제 설정</h2>
				</div>
				<div class="p-6">
					<div class="grid grid-cols-1 md:grid-cols-2 gap-6">
						<div>
							<label class="block text-sm font-medium text-gray-700 mb-2">카드 단말기</label>
							<select 
								bind:value={paymentSettings.cardTerminal}
								class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500"
							>
								<option value="none">없음</option>
								<option value="kicc">KICC</option>
								<option value="kcp">KCP</option>
								<option value="nice">NICE</option>
								<option value="smartro">스마트로</option>
							</select>
						</div>
						<div>
							<label class="block text-sm font-medium text-gray-700 mb-2">가맹점 번호</label>
							<input 
								type="text" 
								bind:value={paymentSettings.merchantId}
								class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500"
								placeholder="가맹점 번호"
							>
						</div>
						<div>
							<label class="block text-sm font-medium text-gray-700 mb-2">최소 결제 금액</label>
							<input 
								type="number" 
								bind:value={paymentSettings.minCardAmount}
								class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500"
								placeholder="1000"
							>
						</div>
						<div>
							<label class="block text-sm font-medium text-gray-700 mb-2">할부 개월 수 제한</label>
							<select 
								bind:value={paymentSettings.maxInstallments}
								class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500"
							>
								<option value="0">일시불만</option>
								<option value="3">3개월까지</option>
								<option value="6">6개월까지</option>
								<option value="12">12개월까지</option>
								<option value="24">24개월까지</option>
							</select>
						</div>
					</div>
				</div>
			</div>

			<!-- 포인트 설정 -->
			<div class="bg-white shadow rounded-lg">
				<div class="px-6 py-4 border-b border-gray-200">
					<h2 class="text-lg font-medium text-gray-900">포인트 설정</h2>
				</div>
				<div class="p-6">
					<div class="grid grid-cols-1 md:grid-cols-2 gap-6">
						<div>
							<label class="block text-sm font-medium text-gray-700 mb-2">포인트 적립률 (%)</label>
							<input 
								type="number" 
								bind:value={paymentSettings.pointRate}
								class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500"
								placeholder="1"
								min="0"
								max="100"
								step="0.1"
							>
						</div>
						<div>
							<label class="block text-sm font-medium text-gray-700 mb-2">포인트 사용 제한</label>
							<select 
								bind:value={paymentSettings.pointUsageLimit}
								class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500"
							>
								<option value="unlimited">제한 없음</option>
								<option value="50">50%까지</option>
								<option value="70">70%까지</option>
								<option value="90">90%까지</option>
							</select>
						</div>
						<div>
							<label class="block text-sm font-medium text-gray-700 mb-2">최소 사용 포인트</label>
							<input 
								type="number" 
								bind:value={paymentSettings.minPointUsage}
								class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500"
								placeholder="100"
							>
						</div>
						<div>
							<label class="block text-sm font-medium text-gray-700 mb-2">포인트 유효기간 (개월)</label>
							<input 
								type="number" 
								bind:value={paymentSettings.pointExpiry}
								class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500"
								placeholder="12"
							>
						</div>
					</div>
				</div>
			</div>

			<!-- 저장 버튼 -->
			<div class="flex justify-end">
				<button 
					on:click={saveSettings}
					disabled={saving}
					class="bg-blue-600 hover:bg-blue-700 disabled:bg-gray-400 text-white px-6 py-2 rounded-lg"
				>
					{saving ? '저장 중...' : '설정 저장'}
				</button>
			</div>
		</div>
	{/if}
</div>
