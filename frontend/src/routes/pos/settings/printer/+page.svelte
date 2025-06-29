<script>
	import { onMount } from 'svelte';
	
	let printerSettings = {};
	let loading = true;
	let error = null;
	let saving = false;
	let testing = false;

	onMount(async () => {
		await loadPrinterSettings();
	});

	async function loadPrinterSettings() {
		try {
			const response = await fetch('/api/pos/settings/printer', {
				headers: {
					'Authorization': `Bearer ${localStorage.getItem('token')}`
				}
			});

			if (!response.ok) {
				throw new Error('API 호출 실패');
			}

			const data = await response.json();
			printerSettings = data.data || {};
		} catch (err) {
			error = err.message;
		} finally {
			loading = false;
		}
	}

	async function saveSettings() {
		saving = true;
		try {
			const response = await fetch('/api/pos/settings/printer', {
				method: 'PUT',
				headers: {
					'Content-Type': 'application/json',
					'Authorization': `Bearer ${localStorage.getItem('token')}`
				},
				body: JSON.stringify(printerSettings)
			});

			if (!response.ok) {
				throw new Error('설정 저장 실패');
			}

			alert('프린터 설정이 저장되었습니다.');
		} catch (err) {
			alert(err.message);
		} finally {
			saving = false;
		}
	}

	async function testPrint() {
		testing = true;
		try {
			const response = await fetch('/api/pos/settings/printer/test', {
				method: 'POST',
				headers: {
					'Authorization': `Bearer ${localStorage.getItem('token')}`
				}
			});

			if (!response.ok) {
				throw new Error('테스트 인쇄 실패');
			}

			alert('테스트 영수증이 출력되었습니다.');
		} catch (err) {
			alert(err.message);
		} finally {
			testing = false;
		}
	}
</script>

<svelte:head>
	<title>프린터 설정 - WebPOS</title>
</svelte:head>

<div class="p-6">
	<div class="mb-6">
		<h1 class="text-2xl font-bold text-gray-900">프린터 설정</h1>
		<p class="text-gray-600 mt-2">영수증 프린터를 설정합니다.</p>
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
			<!-- 프린터 연결 설정 -->
			<div class="bg-white shadow rounded-lg">
				<div class="px-6 py-4 border-b border-gray-200">
					<h2 class="text-lg font-medium text-gray-900">프린터 연결 설정</h2>
				</div>
				<div class="p-6">
					<div class="grid grid-cols-1 md:grid-cols-2 gap-6">
						<div>
							<label class="block text-sm font-medium text-gray-700 mb-2">프린터 이름</label>
							<input 
								type="text" 
								bind:value={printerSettings.printerName}
								class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500"
								placeholder="영수증 프린터"
							>
						</div>
						<div>
							<label class="block text-sm font-medium text-gray-700 mb-2">연결 방식</label>
							<select 
								bind:value={printerSettings.connectionType}
								class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500"
							>
								<option value="usb">USB</option>
								<option value="network">네트워크</option>
								<option value="bluetooth">블루투스</option>
							</select>
						</div>
						<div>
							<label class="block text-sm font-medium text-gray-700 mb-2">프린터 모델</label>
							<select 
								bind:value={printerSettings.printerModel}
								class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500"
							>
								<option value="generic">일반 영수증 프린터</option>
								<option value="epson-tm">EPSON TM 시리즈</option>
								<option value="star-tsp">STAR TSP 시리즈</option>
								<option value="citizen-ct">CITIZEN CT 시리즈</option>
							</select>
						</div>
						<div>
							<label class="block text-sm font-medium text-gray-700 mb-2">용지 크기</label>
							<select 
								bind:value={printerSettings.paperSize}
								class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500"
							>
								<option value="58mm">58mm</option>
								<option value="80mm">80mm</option>
							</select>
						</div>
					</div>
				</div>
			</div>

			<!-- 영수증 설정 -->
			<div class="bg-white shadow rounded-lg">
				<div class="px-6 py-4 border-b border-gray-200">
					<h2 class="text-lg font-medium text-gray-900">영수증 설정</h2>
				</div>
				<div class="p-6">
					<div class="grid grid-cols-1 md:grid-cols-2 gap-6">
						<div>
							<label class="block text-sm font-medium text-gray-700 mb-2">매장명</label>
							<input 
								type="text" 
								bind:value={printerSettings.storeName}
								class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500"
								placeholder="매장명"
							>
						</div>
						<div>
							<label class="block text-sm font-medium text-gray-700 mb-2">사업자 번호</label>
							<input 
								type="text" 
								bind:value={printerSettings.businessNumber}
								class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500"
								placeholder="000-00-00000"
							>
						</div>
						<div class="md:col-span-2">
							<label class="block text-sm font-medium text-gray-700 mb-2">매장 주소</label>
							<input 
								type="text" 
								bind:value={printerSettings.storeAddress}
								class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500"
								placeholder="매장 주소"
							>
						</div>
						<div>
							<label class="block text-sm font-medium text-gray-700 mb-2">전화번호</label>
							<input 
								type="text" 
								bind:value={printerSettings.phoneNumber}
								class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500"
								placeholder="000-0000-0000"
							>
						</div>
						<div>
							<label class="block text-sm font-medium text-gray-700 mb-2">자동 절단</label>
							<select 
								bind:value={printerSettings.autoCut}
								class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500"
							>
								<option value="enabled">사용</option>
								<option value="disabled">사용 안함</option>
							</select>
						</div>
					</div>
					
					<div class="mt-6">
						<label class="block text-sm font-medium text-gray-700 mb-2">영수증 하단 메시지</label>
						<textarea 
							bind:value={printerSettings.footerMessage}
							rows="3"
							class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500"
							placeholder="감사합니다. 또 오세요!"
						></textarea>
					</div>
				</div>
			</div>

			<!-- 저장 및 테스트 버튼 -->
			<div class="flex justify-between">
				<button 
					on:click={testPrint}
					disabled={testing}
					class="bg-green-600 hover:bg-green-700 disabled:bg-gray-400 text-white px-6 py-2 rounded-lg"
				>
					{testing ? '테스트 중...' : '테스트 인쇄'}
				</button>
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
