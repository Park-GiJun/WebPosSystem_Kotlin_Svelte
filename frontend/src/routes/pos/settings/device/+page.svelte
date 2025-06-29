<script>
	import { onMount } from 'svelte';
	
	let deviceSettings = {};
	let loading = true;
	let error = null;
	let saving = false;

	onMount(async () => {
		await loadDeviceSettings();
	});

	async function loadDeviceSettings() {
		try {
			const response = await fetch('/api/pos/settings/device', {
				headers: {
					'Authorization': `Bearer ${localStorage.getItem('token')}`
				}
			});

			if (!response.ok) {
				throw new Error('API 호출 실패');
			}

			const data = await response.json();
			deviceSettings = data.data || {};
		} catch (err) {
			error = err.message;
		} finally {
			loading = false;
		}
	}

	async function saveSettings() {
		saving = true;
		try {
			const response = await fetch('/api/pos/settings/device', {
				method: 'PUT',
				headers: {
					'Content-Type': 'application/json',
					'Authorization': `Bearer ${localStorage.getItem('token')}`
				},
				body: JSON.stringify(deviceSettings)
			});

			if (!response.ok) {
				throw new Error('설정 저장 실패');
			}

			alert('설정이 저장되었습니다.');
		} catch (err) {
			alert(err.message);
		} finally {
			saving = false;
		}
	}
</script>

<svelte:head>
	<title>기기 설정 - WebPOS</title>
</svelte:head>

<div class="p-6">
	<div class="mb-6">
		<h1 class="text-2xl font-bold text-gray-900">기기 설정</h1>
		<p class="text-gray-600 mt-2">POS 기기 환경을 설정합니다.</p>
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
				<h2 class="text-lg font-medium text-gray-900">기기 환경 설정</h2>
			</div>
			<div class="p-6">
				<div class="grid grid-cols-1 md:grid-cols-2 gap-6">
					<div>
						<label class="block text-sm font-medium text-gray-700 mb-2">기기 이름</label>
						<input 
							type="text" 
							bind:value={deviceSettings.deviceName}
							class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500"
							placeholder="POS-001"
						>
					</div>
					<div>
						<label class="block text-sm font-medium text-gray-700 mb-2">매장 코드</label>
						<input 
							type="text" 
							bind:value={deviceSettings.storeCode}
							class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500"
							placeholder="STORE001"
						>
					</div>
					<div>
						<label class="block text-sm font-medium text-gray-700 mb-2">화면 해상도</label>
						<select 
							bind:value={deviceSettings.screenResolution}
							class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500"
						>
							<option value="1920x1080">1920x1080 (Full HD)</option>
							<option value="1366x768">1366x768 (HD)</option>
							<option value="1024x768">1024x768 (XGA)</option>
						</select>
					</div>
					<div>
						<label class="block text-sm font-medium text-gray-700 mb-2">터치 스크린</label>
						<select 
							bind:value={deviceSettings.touchScreen}
							class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500"
						>
							<option value="enabled">사용</option>
							<option value="disabled">사용 안함</option>
						</select>
					</div>
					<div>
						<label class="block text-sm font-medium text-gray-700 mb-2">바코드 스캐너</label>
						<select 
							bind:value={deviceSettings.barcodeScanner}
							class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500"
						>
							<option value="usb">USB 연결</option>
							<option value="bluetooth">블루투스</option>
							<option value="none">없음</option>
						</select>
					</div>
					<div>
						<label class="block text-sm font-medium text-gray-700 mb-2">현금서랍</label>
						<select 
							bind:value={deviceSettings.cashDrawer}
							class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500"
						>
							<option value="enabled">사용</option>
							<option value="disabled">사용 안함</option>
						</select>
					</div>
					<div>
						<label class="block text-sm font-medium text-gray-700 mb-2">자동 로그아웃 시간 (분)</label>
						<input 
							type="number" 
							bind:value={deviceSettings.autoLogoutTime}
							class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500"
							placeholder="30"
							min="5"
							max="480"
						>
					</div>
					<div>
						<label class="block text-sm font-medium text-gray-700 mb-2">언어</label>
						<select 
							bind:value={deviceSettings.language}
							class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500"
						>
							<option value="ko">한국어</option>
							<option value="en">English</option>
						</select>
					</div>
				</div>
				
				<div class="mt-8 flex justify-end">
					<button 
						on:click={saveSettings}
						disabled={saving}
						class="bg-blue-600 hover:bg-blue-700 disabled:bg-gray-400 text-white px-6 py-2 rounded-lg"
					>
						{saving ? '저장 중...' : '설정 저장'}
					</button>
				</div>
			</div>
		</div>
	{/if}
</div>
