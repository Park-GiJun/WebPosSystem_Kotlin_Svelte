<script>
	import { onMount } from 'svelte';
	import { ChevronLeft, Plus, Search, Edit, Trash, Building, MapPin, Phone, Mail, Calendar } from 'lucide-svelte';

	let headquarters = [];
	let filteredHeadquarters = [];
	let searchTerm = '';
	let showAddModal = false;
	let showEditModal = false;
	let selectedHQ = null;
	let newHQ = {
		name: '',
		code: '',
		address: '',
		phone: '',
		email: '',
		managerName: '',
		managerPhone: '',
		establishedDate: '',
		businessType: '',
		isActive: true
	};

	const businessTypes = [
		{ value: 'RETAIL', label: '소매업' },
		{ value: 'WHOLESALE', label: '도매업' },
		{ value: 'FRANCHISE', label: '프랜차이즈' },
		{ value: 'MANUFACTURING', label: '제조업' },
		{ value: 'SERVICE', label: '서비스업' },
		{ value: 'OTHER', label: '기타' }
	];

	onMount(() => {
		loadHeadquarters();
	});

	async function loadHeadquarters() {
		try {
			// API 호출 시뮬레이션
			headquarters = [
				{
					id: 1,
					name: '메인 본사',
					code: 'HQ001',
					address: '서울시 강남구 테헤란로 123, 본사빌딩 10-15층',
					phone: '02-1234-5678',
					email: 'main@company.com',
					managerName: '김본사',
					managerPhone: '010-1111-2222',
					establishedDate: '2010-01-15',
					businessType: 'RETAIL',
					isActive: true,
					totalStores: 25,
					totalEmployees: 150,
					createdAt: '2024-01-01'
				},
				{
					id: 2,
					name: '부산 지역본부',
					code: 'HQ002',
					address: '부산시 해운대구 센텀중앙로 456, 센텀빌딩 8층',
					phone: '051-2345-6789',
					email: 'busan@company.com',
					managerName: '이지역',
					managerPhone: '010-2222-3333',
					establishedDate: '2015-03-20',
					businessType: 'RETAIL',
					isActive: true,
					totalStores: 12,
					totalEmployees: 75,
					createdAt: '2024-02-15'
				},
				{
					id: 3,
					name: '대구 지사',
					code: 'HQ003',
					address: '대구시 중구 달구벌대로 789, 대구타워 5층',
					phone: '053-3456-7890',
					email: 'daegu@company.com',
					managerName: '박지사',
					managerPhone: '010-3333-4444',
					establishedDate: '2018-06-10',
					businessType: 'RETAIL',
					isActive: false,
					totalStores: 8,
					totalEmployees: 45,
					createdAt: '2024-03-10'
				}
			];
			filteredHeadquarters = [...headquarters];
		} catch (error) {
			console.error('본사 로딩 실패:', error);
		}
	}

	function filterHeadquarters() {
		if (!searchTerm.trim()) {
			filteredHeadquarters = [...headquarters];
		} else {
			filteredHeadquarters = headquarters.filter(hq => 
				hq.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
				hq.code.toLowerCase().includes(searchTerm.toLowerCase()) ||
				hq.address.toLowerCase().includes(searchTerm.toLowerCase()) ||
				hq.managerName.toLowerCase().includes(searchTerm.toLowerCase())
			);
		}
	}

	function openAddModal() {
		newHQ = {
			name: '',
			code: '',
			address: '',
			phone: '',
			email: '',
			managerName: '',
			managerPhone: '',
			establishedDate: '',
			businessType: 'RETAIL',
			isActive: true
		};
		showAddModal = true;
	}

	function openEditModal(hq) {
		selectedHQ = { ...hq };
		showEditModal = true;
	}

	function closeModals() {
		showAddModal = false;
		showEditModal = false;
		selectedHQ = null;
	}

	async function saveHeadquarter() {
		try {
			// API 호출 시뮬레이션
			const newId = Math.max(...headquarters.map(h => h.id)) + 1;
			const hqToAdd = {
				...newHQ,
				id: newId,
				totalStores: 0,
				totalEmployees: 0,
				createdAt: new Date().toISOString().split('T')[0]
			};
			
			headquarters = [...headquarters, hqToAdd];
			filterHeadquarters();
			closeModals();
			alert('본사가 추가되었습니다.');
		} catch (error) {
			console.error('본사 저장 실패:', error);
			alert('본사 저장에 실패했습니다.');
		}
	}

	async function updateHeadquarter() {
		try {
			// API 호출 시뮬레이션
			headquarters = headquarters.map(hq => 
				hq.id === selectedHQ.id ? { ...selectedHQ } : hq
			);
			filterHeadquarters();
			closeModals();
			alert('본사 정보가 수정되었습니다.');
		} catch (error) {
			console.error('본사 수정 실패:', error);
			alert('본사 수정에 실패했습니다.');
		}
	}

	async function deleteHeadquarter(hqId) {
		if (!confirm('정말로 이 본사를 삭제하시겠습니까?\n연결된 모든 매장 정보도 함께 삭제됩니다.')) return;
		
		try {
			// API 호출 시뮬레이션
			headquarters = headquarters.filter(hq => hq.id !== hqId);
			filterHeadquarters();
			alert('본사가 삭제되었습니다.');
		} catch (error) {
			console.error('본사 삭제 실패:', error);
			alert('본사 삭제에 실패했습니다.');
		}
	}

	function getBusinessTypeName(type) {
		const found = businessTypes.find(t => t.value === type);
		return found ? found.label : type;
	}

	$: searchTerm, filterHeadquarters();
</script>

<svelte:head>
	<title>본사 관리 - WebPOS</title>
</svelte:head>

<div class="p-6">
	<!-- 헤더 -->
	<div class="flex items-center justify-between mb-6">
		<div class="flex items-center gap-4">
			<button 
				class="p-2 hover:bg-gray-100 rounded-lg transition-colors"
				on:click={() => history.back()}
			>
				<ChevronLeft class="w-5 h-5" />
			</button>
			<div>
				<h1 class="text-2xl font-bold text-gray-900 flex items-center gap-2">
					<Building class="w-6 h-6" />
					본사 관리
				</h1>
				<p class="text-gray-600 mt-1">본사 및 지역본부 조직을 관리합니다</p>
			</div>
		</div>
		<button 
			class="bg-blue-600 text-white px-4 py-2 rounded-lg hover:bg-blue-700 transition-colors flex items-center gap-2"
			on:click={openAddModal}
		>
			<Plus class="w-4 h-4" />
			본사 추가
		</button>
	</div>

	<!-- 검색 -->
	<div class="bg-white rounded-lg shadow-sm border border-gray-200 p-4 mb-6">
		<div class="flex items-center gap-4">
			<div class="flex-1">
				<div class="relative">
					<Search class="w-5 h-5 absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400" />
					<input
						type="text"
						placeholder="본사명, 코드, 주소, 담당자로 검색..."
						class="w-full pl-10 pr-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
						bind:value={searchTerm}
					/>
				</div>
			</div>
		</div>
	</div>

	<!-- 본사 통계 -->
	<div class="grid grid-cols-1 md:grid-cols-4 gap-4 mb-6">
		<div class="bg-white p-4 rounded-lg shadow-sm border border-gray-200">
			<div class="flex items-center justify-between">
				<div>
					<p class="text-sm text-gray-600">전체 본사</p>
					<p class="text-2xl font-bold text-gray-900">{headquarters.length}</p>
				</div>
				<div class="w-12 h-12 bg-blue-100 rounded-lg flex items-center justify-center">
					<Building class="w-6 h-6 text-blue-600" />
				</div>
			</div>
		</div>
		<div class="bg-white p-4 rounded-lg shadow-sm border border-gray-200">
			<div class="flex items-center justify-between">
				<div>
					<p class="text-sm text-gray-600">활성 본사</p>
					<p class="text-2xl font-bold text-green-600">{headquarters.filter(h => h.isActive).length}</p>
				</div>
				<div class="w-12 h-12 bg-green-100 rounded-lg flex items-center justify-center">
					<Building class="w-6 h-6 text-green-600" />
				</div>
			</div>
		</div>
		<div class="bg-white p-4 rounded-lg shadow-sm border border-gray-200">
			<div class="flex items-center justify-between">
				<div>
					<p class="text-sm text-gray-600">총 매장 수</p>
					<p class="text-2xl font-bold text-purple-600">{headquarters.reduce((sum, h) => sum + h.totalStores, 0)}</p>
				</div>
				<div class="w-12 h-12 bg-purple-100 rounded-lg flex items-center justify-center">
					<Building class="w-6 h-6 text-purple-600" />
				</div>
			</div>
		</div>
		<div class="bg-white p-4 rounded-lg shadow-sm border border-gray-200">
			<div class="flex items-center justify-between">
				<div>
					<p class="text-sm text-gray-600">총 직원 수</p>
					<p class="text-2xl font-bold text-indigo-600">{headquarters.reduce((sum, h) => sum + h.totalEmployees, 0)}</p>
				</div>
				<div class="w-12 h-12 bg-indigo-100 rounded-lg flex items-center justify-center">
					<Building class="w-6 h-6 text-indigo-600" />
				</div>
			</div>
		</div>
	</div>

	<!-- 본사 목록 -->
	<div class="bg-white rounded-lg shadow-sm border border-gray-200 overflow-hidden">
		<div class="overflow-x-auto">
			<table class="w-full">
				<thead class="bg-gray-50">
					<tr>
						<th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">본사정보</th>
						<th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">주소</th>
						<th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">연락처</th>
						<th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">담당자</th>
						<th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">업종</th>
						<th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">매장/직원</th>
						<th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">상태</th>
						<th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">작업</th>
					</tr>
				</thead>
				<tbody class="bg-white divide-y divide-gray-200">
					{#each filteredHeadquarters as hq}
						<tr class="hover:bg-gray-50">
							<td class="px-6 py-4 whitespace-nowrap">
								<div>
									<div class="text-sm font-medium text-gray-900">{hq.name}</div>
									<div class="text-sm text-gray-500">코드: {hq.code}</div>
									<div class="text-sm text-gray-500 flex items-center gap-1">
										<Calendar class="w-3 h-3" />
										설립: {new Date(hq.establishedDate).toLocaleDateString('ko-KR')}
									</div>
								</div>
							</td>
							<td class="px-6 py-4">
								<div class="flex items-start gap-1 text-sm text-gray-900">
									<MapPin class="w-3 h-3 mt-1 flex-shrink-0" />
									<span class="break-words">{hq.address}</span>
								</div>
							</td>
							<td class="px-6 py-4 whitespace-nowrap">
								<div class="flex flex-col gap-1">
									<div class="flex items-center gap-1 text-sm text-gray-900">
										<Phone class="w-3 h-3" />
										{hq.phone}
									</div>
									<div class="flex items-center gap-1 text-sm text-gray-500">
										<Mail class="w-3 h-3" />
										{hq.email}
									</div>
								</div>
							</td>
							<td class="px-6 py-4 whitespace-nowrap">
								<div class="text-sm text-gray-900">
									<div class="font-medium">{hq.managerName}</div>
									<div class="text-gray-500">{hq.managerPhone}</div>
								</div>
							</td>
							<td class="px-6 py-4 whitespace-nowrap">
								<span class="inline-flex px-2 py-1 text-xs font-semibold rounded-full bg-blue-100 text-blue-800">
									{getBusinessTypeName(hq.businessType)}
								</span>
							</td>
							<td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
								<div>매장: {hq.totalStores}개</div>
								<div>직원: {hq.totalEmployees}명</div>
							</td>
							<td class="px-6 py-4 whitespace-nowrap">
								<span class="inline-flex px-2 py-1 text-xs font-semibold rounded-full {hq.isActive ? 'bg-green-100 text-green-800' : 'bg-red-100 text-red-800'}">
									{hq.isActive ? '활성' : '비활성'}
								</span>
							</td>
							<td class="px-6 py-4 whitespace-nowrap text-sm font-medium">
								<div class="flex items-center gap-2">
									<button 
										class="text-blue-600 hover:text-blue-900 p-1 rounded"
										on:click={() => openEditModal(hq)}
									>
										<Edit class="w-4 h-4" />
									</button>
									<button 
										class="text-red-600 hover:text-red-900 p-1 rounded"
										on:click={() => deleteHeadquarter(hq.id)}
									>
										<Trash class="w-4 h-4" />
									</button>
								</div>
							</td>
						</tr>
					{/each}
				</tbody>
			</table>
		</div>
	</div>
</div>

<!-- 본사 추가 모달 -->
{#if showAddModal}
	<div class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
		<div class="bg-white rounded-lg shadow-xl w-full max-w-2xl mx-4 max-h-96 overflow-y-auto">
			<div class="px-6 py-4 border-b border-gray-200">
				<h2 class="text-xl font-semibold text-gray-900">새 본사 추가</h2>
			</div>
			<div class="px-6 py-4 grid grid-cols-1 md:grid-cols-2 gap-4">
				<div>
					<label class="block text-sm font-medium text-gray-700 mb-1">본사명</label>
					<input
						type="text"
						class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
						bind:value={newHQ.name}
						placeholder="본사명을 입력하세요"
					/>
				</div>
				<div>
					<label class="block text-sm font-medium text-gray-700 mb-1">본사 코드</label>
					<input
						type="text"
						class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
						bind:value={newHQ.code}
						placeholder="HQ001"
					/>
				</div>
				<div class="md:col-span-2">
					<label class="block text-sm font-medium text-gray-700 mb-1">주소</label>
					<input
						type="text"
						class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
						bind:value={newHQ.address}
						placeholder="주소를 입력하세요"
					/>
				</div>
				<div>
					<label class="block text-sm font-medium text-gray-700 mb-1">전화번호</label>
					<input
						type="tel"
						class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
						bind:value={newHQ.phone}
						placeholder="02-0000-0000"
					/>
				</div>
				<div>
					<label class="block text-sm font-medium text-gray-700 mb-1">이메일</label>
					<input
						type="email"
						class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
						bind:value={newHQ.email}
						placeholder="email@company.com"
					/>
				</div>
				<div>
					<label class="block text-sm font-medium text-gray-700 mb-1">담당자명</label>
					<input
						type="text"
						class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
						bind:value={newHQ.managerName}
						placeholder="담당자명을 입력하세요"
					/>
				</div>
				<div>
					<label class="block text-sm font-medium text-gray-700 mb-1">담당자 연락처</label>
					<input
						type="tel"
						class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
						bind:value={newHQ.managerPhone}
						placeholder="010-0000-0000"
					/>
				</div>
				<div>
					<label class="block text-sm font-medium text-gray-700 mb-1">설립일</label>
					<input
						type="date"
						class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
						bind:value={newHQ.establishedDate}
					/>
				</div>
				<div>
					<label class="block text-sm font-medium text-gray-700 mb-1">업종</label>
					<select
						class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
						bind:value={newHQ.businessType}
					>
						{#each businessTypes as type}
							<option value={type.value}>{type.label}</option>
						{/each}
					</select>
				</div>
				<div class="md:col-span-2">
					<label class="flex items-center">
						<input
							type="checkbox"
							class="rounded border-gray-300 text-blue-600 focus:ring-blue-500"
							bind:checked={newHQ.isActive}
						/>
						<span class="ml-2 text-sm text-gray-700">활성 상태</span>
					</label>
				</div>
			</div>
			<div class="px-6 py-4 border-t border-gray-200 flex justify-end gap-3">
				<button
					class="px-4 py-2 text-gray-700 bg-gray-100 rounded-lg hover:bg-gray-200 transition-colors"
					on:click={closeModals}
				>
					취소
				</button>
				<button
					class="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors"
					on:click={saveHeadquarter}
				>
					저장
				</button>
			</div>
		</div>
	</div>
{/if}

<!-- 본사 수정 모달 -->
{#if showEditModal && selectedHQ}
	<div class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
		<div class="bg-white rounded-lg shadow-xl w-full max-w-2xl mx-4 max-h-96 overflow-y-auto">
			<div class="px-6 py-4 border-b border-gray-200">
				<h2 class="text-xl font-semibold text-gray-900">본사 정보 수정</h2>
			</div>
			<div class="px-6 py-4 grid grid-cols-1 md:grid-cols-2 gap-4">
				<div>
					<label class="block text-sm font-medium text-gray-700 mb-1">본사명</label>
					<input
						type="text"
						class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
						bind:value={selectedHQ.name}
						placeholder="본사명을 입력하세요"
					/>
				</div>
				<div>
					<label class="block text-sm font-medium text-gray-700 mb-1">본사 코드</label>
					<input
						type="text"
						class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
						bind:value={selectedHQ.code}
						placeholder="HQ001"
					/>
				</div>
				<div class="md:col-span-2">
					<label class="block text-sm font-medium text-gray-700 mb-1">주소</label>
					<input
						type="text"
						class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
						bind:value={selectedHQ.address}
						placeholder="주소를 입력하세요"
					/>
				</div>
				<div>
					<label class="block text-sm font-medium text-gray-700 mb-1">전화번호</label>
					<input
						type="tel"
						class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
						bind:value={selectedHQ.phone}
						placeholder="02-0000-0000"
					/>
				</div>
				<div>
					<label class="block text-sm font-medium text-gray-700 mb-1">이메일</label>
					<input
						type="email"
						class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
						bind:value={selectedHQ.email}
						placeholder="email@company.com"
					/>
				</div>
				<div>
					<label class="block text-sm font-medium text-gray-700 mb-1">담당자명</label>
					<input
						type="text"
						class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
						bind:value={selectedHQ.managerName}
						placeholder="담당자명을 입력하세요"
					/>
				</div>
				<div>
					<label class="block text-sm font-medium text-gray-700 mb-1">담당자 연락처</label>
					<input
						type="tel"
						class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
						bind:value={selectedHQ.managerPhone}
						placeholder="010-0000-0000"
					/>
				</div>
				<div>
					<label class="block text-sm font-medium text-gray-700 mb-1">설립일</label>
					<input
						type="date"
						class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
						bind:value={selectedHQ.establishedDate}
					/>
				</div>
				<div>
					<label class="block text-sm font-medium text-gray-700 mb-1">업종</label>
					<select
						class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
						bind:value={selectedHQ.businessType}
					>
						{#each businessTypes as type}
							<option value={type.value}>{type.label}</option>
						{/each}
					</select>
				</div>
				<div class="md:col-span-2">
					<label class="flex items-center">
						<input
							type="checkbox"
							class="rounded border-gray-300 text-blue-600 focus:ring-blue-500"
							bind:checked={selectedHQ.isActive}
						/>
						<span class="ml-2 text-sm text-gray-700">활성 상태</span>
					</label>
				</div>
			</div>
			<div class="px-6 py-4 border-t border-gray-200 flex justify-end gap-3">
				<button
					class="px-4 py-2 text-gray-700 bg-gray-100 rounded-lg hover:bg-gray-200 transition-colors"
					on:click={closeModals}
				>
					취소
				</button>
				<button
					class="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors"
					on:click={updateHeadquarter}
				>
					수정
				</button>
			</div>
		</div>
	</div>
{/if}
