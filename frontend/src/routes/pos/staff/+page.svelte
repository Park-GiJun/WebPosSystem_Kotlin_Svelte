<script>
  import { onMount } from 'svelte';
  import { authStore } from '$lib/stores/auth.js';
  import { tabStore } from '$lib/stores/tabs.js';
  import { toastStore } from '$lib/stores/toast.js';
  import { Plus, Users, Edit, Trash2, Shield, UserCheck, UserX } from 'lucide-svelte';
  import Modal from '$lib/components/Common/Modal.svelte';

  let staff = [];
  let loading = true;
  let showCreateModal = false;

  // 직원 생성 폼
  let staffForm = {
    username: '',
    email: '',
    password: '',
    roles: ['USER']
  };

  const availableRoles = [
    { value: 'USER', label: '일반 직원', description: 'POS 판매 기능만 사용' },
    { value: 'STORE_MANAGER', label: '매장 관리자', description: '매장 전체 관리' }
  ];

  onMount(() => {
    tabStore.setActiveTab('POS_STAFF');
    loadStaff();
  });

  async function loadStaff() {
    try {
      const response = await fetch('/api/v1/stores/users', {
        headers: {
          'Authorization': `Bearer ${$authStore.token}`
        }
      });
      
      if (response.ok) {
        const data = await response.json();
        staff = data.users || [];
      } else {
        throw new Error('직원 목록을 불러올 수 없습니다.');
      }
    } catch (error) {
      console.error('Failed to load staff:', error);
      toastStore.error(error.message);
    } finally {
      loading = false;
    }
  }

  async function createStaff() {
    try {
      const response = await fetch('/api/v1/stores/users', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${$authStore.token}`
        },
        body: JSON.stringify(staffForm)
      });

      if (response.ok) {
        toastStore.success('직원이 성공적으로 추가되었습니다.');
        showCreateModal = false;
        resetForm();
        await loadStaff();
      } else {
        const error = await response.json();
        throw new Error(error.message || '직원 추가에 실패했습니다.');
      }
    } catch (error) {
      console.error('Create staff error:', error);
      toastStore.error(error.message);
    }
  }

  function resetForm() {
    staffForm = {
      username: '',
      email: '',
      password: '',
      roles: ['USER']
    };
  }

  function getRoleColor(role) {
    const colors = {
      'STORE_MANAGER': 'bg-blue-100 text-blue-800',
      'USER': 'bg-green-100 text-green-800'
    };
    return colors[role] || 'bg-gray-100 text-gray-800';
  }

  function getStatusColor(status) {
    const colors = {
      'ACTIVE': 'bg-green-100 text-green-800',
      'INACTIVE': 'bg-gray-100 text-gray-800',
      'SUSPENDED': 'bg-red-100 text-red-800',
      'PENDING_VERIFICATION': 'bg-yellow-100 text-yellow-800',
      'LOCKED': 'bg-orange-100 text-orange-800'
    };
    return colors[status] || 'bg-gray-100 text-gray-800';
  }

  function editStaff(member) {
    console.log('Edit staff:', member);
  }

  function deleteStaff(member) {
    console.log('Delete staff:', member);
  }

  function toggleStaffStatus(member) {
    console.log('Toggle staff status:', member);
  }
</script>

<svelte:head>
  <title>직원 관리 - WebPos</title>
</svelte:head>



<div class="space-y-6">
  <!-- 헤더 -->
  <div class="flex items-center justify-between">
    <div>
      <h1 class="text-2xl font-bold text-gray-900">직원 관리</h1>
      <p class="text-gray-600 mt-1">매장 직원을 관리합니다.</p>
    </div>
    <button 
      type="button" 
      class="btn btn-primary"
      on:click={() => showCreateModal = true}
    >
      <Plus size="16" class="mr-2" />
      직원 추가
    </button>
  </div>

  <!-- 통계 카드 -->
  <div class="grid grid-cols-1 md:grid-cols-4 gap-6">
    <div class="card p-6">
      <div class="flex items-center">
        <div class="p-3 rounded-full bg-blue-100">
          <Users class="h-6 w-6 text-blue-600" />
        </div>
        <div class="ml-4">
          <p class="text-sm font-medium text-gray-600">총 직원</p>
          <p class="text-2xl font-bold text-gray-900">{staff.length}</p>
        </div>
      </div>
    </div>

    <div class="card p-6">
      <div class="flex items-center">
        <div class="p-3 rounded-full bg-green-100">
          <UserCheck class="h-6 w-6 text-green-600" />
        </div>
        <div class="ml-4">
          <p class="text-sm font-medium text-gray-600">활성 직원</p>
          <p class="text-2xl font-bold text-gray-900">
            {staff.filter(s => s.userStatus === 'ACTIVE').length}
          </p>
        </div>
      </div>
    </div>

    <div class="card p-6">
      <div class="flex items-center">
        <div class="p-3 rounded-full bg-purple-100">
          <Shield class="h-6 w-6 text-purple-600" />
        </div>
        <div class="ml-4">
          <p class="text-sm font-medium text-gray-600">관리자</p>
          <p class="text-2xl font-bold text-gray-900">
            {staff.filter(s => s.roles.includes('STORE_MANAGER')).length}
          </p>
        </div>
      </div>
    </div>

    <div class="card p-6">
      <div class="flex items-center">
        <div class="p-3 rounded-full bg-yellow-100">
          <UserX class="h-6 w-6 text-yellow-600" />
        </div>
        <div class="ml-4">
          <p class="text-sm font-medium text-gray-600">비활성 직원</p>
          <p class="text-2xl font-bold text-gray-900">
            {staff.filter(s => s.userStatus !== 'ACTIVE').length}
          </p>
        </div>
      </div>
    </div>
  </div>

  <!-- 직원 목록 -->
  <div class="card overflow-hidden">
    <div class="px-6 py-4 border-b border-gray-200">
      <h3 class="text-lg font-medium text-gray-900">직원 목록</h3>
    </div>

    {#if loading}
      <div class="p-12 text-center">
        <div class="animate-spin rounded-full h-8 w-8 border-b-2 border-primary-600 mx-auto"></div>
        <p class="mt-4 text-gray-600">로딩 중...</p>
      </div>
    {:else if staff.length === 0}
      <div class="p-12 text-center">
        <Users class="mx-auto h-12 w-12 text-gray-400" />
        <p class="mt-4 text-gray-500">등록된 직원이 없습니다.</p>
        <button 
          type="button" 
          class="mt-4 btn btn-primary"
          on:click={() => showCreateModal = true}
        >
          <Plus size="16" class="mr-2" />
          첫 번째 직원 추가
        </button>
      </div>
    {:else}
      <div class="overflow-x-auto">
        <table class="min-w-full divide-y divide-gray-200">
          <thead class="bg-gray-50">
            <tr>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                직원
              </th>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                역할
              </th>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                상태
              </th>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                최근 로그인
              </th>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                등록일
              </th>
              <th class="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase tracking-wider">
                작업
              </th>
            </tr>
          </thead>
          <tbody class="bg-white divide-y divide-gray-200">
            {#each staff as member}
              <tr class="hover:bg-gray-50">
                <td class="px-6 py-4 whitespace-nowrap">
                  <div class="flex items-center">
                    <div class="w-10 h-10 bg-primary-600 rounded-full flex items-center justify-center">
                      <span class="text-white font-medium text-sm">
                        {member.username.charAt(0).toUpperCase()}
                      </span>
                    </div>
                    <div class="ml-4">
                      <div class="text-sm font-medium text-gray-900">{member.username}</div>
                      <div class="text-sm text-gray-500">{member.email}</div>
                    </div>
                  </div>
                </td>
                <td class="px-6 py-4 whitespace-nowrap">
                  <div class="flex flex-wrap gap-1">
                    {#each member.roles as role}
                      <span class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium {getRoleColor(role)}">
                        {availableRoles.find(r => r.value === role)?.label || role}
                      </span>
                    {/each}
                  </div>
                </td>
                <td class="px-6 py-4 whitespace-nowrap">
                  <span class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium {getStatusColor(member.userStatus)}">
                    {member.userStatus === 'ACTIVE' ? '활성' : 
                     member.userStatus === 'INACTIVE' ? '비활성' :
                     member.userStatus === 'SUSPENDED' ? '정지' :
                     member.userStatus === 'PENDING_VERIFICATION' ? '인증대기' :
                     member.userStatus === 'LOCKED' ? '잠김' : member.userStatus}
                  </span>
                </td>
                <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                  {member.lastLoginAt ? new Date(member.lastLoginAt).toLocaleDateString('ko-KR') : '없음'}
                </td>
                <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                  {member.createdAt ? new Date(member.createdAt).toLocaleDateString('ko-KR') : ''}
                </td>
                <td class="px-6 py-4 whitespace-nowrap text-right text-sm font-medium">
                  <div class="flex justify-end space-x-2">
                    <button
                      type="button"
                      class="text-indigo-600 hover:text-indigo-900"
                      on:click={() => editStaff(member)}
                      title="편집"
                    >
                      <Edit size="16" />
                    </button>
                    <button
                      type="button"
                      class="text-green-600 hover:text-green-900"
                      on:click={() => toggleStaffStatus(member)}
                      title={member.userStatus === 'ACTIVE' ? '비활성화' : '활성화'}
                    >
                      {#if member.userStatus === 'ACTIVE'}
                        <UserX size="16" />
                      {:else}
                        <UserCheck size="16" />
                      {/if}
                    </button>
                    <button
                      type="button"
                      class="text-red-600 hover:text-red-900"
                      on:click={() => deleteStaff(member)}
                      title="삭제"
                    >
                      <Trash2 size="16" />
                    </button>
                  </div>
                </td>
              </tr>
            {/each}
          </tbody>
        </table>
      </div>
    {/if}
  </div>
</div>

<!-- 직원 추가 모달 -->
<Modal bind:open={showCreateModal} title="직원 추가" size="md">
  <form on:submit|preventDefault={createStaff} class="space-y-6">
    <div>
      <label for="username" class="block text-sm font-medium text-gray-700">사용자 아이디 *</label>
      <input
        id="username"
        type="text"
        required
        bind:value={staffForm.username}
        class="mt-1 input"
        placeholder="사용자 아이디를 입력하세요"
      />
    </div>

    <div>
      <label for="email" class="block text-sm font-medium text-gray-700">이메일 *</label>
      <input
        id="email"
        type="email"
        required
        bind:value={staffForm.email}
        class="mt-1 input"
        placeholder="user@example.com"
      />
    </div>

    <div>
      <label for="password" class="block text-sm font-medium text-gray-700">비밀번호 *</label>
      <input
        id="password"
        type="password"
        required
        bind:value={staffForm.password}
        class="mt-1 input"
        placeholder="비밀번호를 입력하세요"
      />
    </div>

    <div>
      <label for="roles" class="block text-sm font-medium text-gray-700">역할 *</label>
      <select
        id="roles"
        required
        bind:value={staffForm.roles[0]}
        on:change={(e) => staffForm.roles = [e.target.value]}
        class="mt-1 input"
      >
        {#each availableRoles as role}
          <option value={role.value}>{role.label}</option>
        {/each}
      </select>
      <p class="mt-1 text-sm text-gray-500">
        {availableRoles.find(r => r.value === staffForm.roles[0])?.description}
      </p>
    </div>

    <div class="flex justify-end space-x-3 pt-4">
      <button
        type="button"
        class="btn btn-secondary"
        on:click={() => showCreateModal = false}
      >
        취소
      </button>
      <button type="submit" class="btn btn-primary">
        직원 추가
      </button>
    </div>
  </form>
</Modal>
