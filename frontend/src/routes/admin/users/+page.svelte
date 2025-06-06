<script>
  import { onMount } from 'svelte';
  import { authStore } from '$lib/stores/auth.js';
  import { tabStore } from '$lib/stores/tabs.js';
  import { Plus, Search, Filter, Edit, Trash2, Shield } from 'lucide-svelte';

  let users = [];
  let loading = true;
  let searchTerm = '';
  let filterStatus = 'all';
  let filterRole = 'all';

  // 탭 활성화
  onMount(() => {
    tabStore.setActiveTab('ADMIN_USERS');
    loadUsers();
  });

  async function loadUsers() {
    try {
      const response = await fetch('/api/v1/admin/users', {
        headers: {
          'Authorization': `Bearer ${$authStore.token}`
        }
      });
      
      if (response.ok) {
        const data = await response.json();
        users = data.users || [];
      }
    } catch (error) {
      console.error('Failed to load users:', error);
    } finally {
      loading = false;
    }
  }

  // 필터링된 사용자 목록
  $: filteredUsers = users.filter(user => {
    const matchesSearch = user.username.toLowerCase().includes(searchTerm.toLowerCase()) ||
                         user.email.toLowerCase().includes(searchTerm.toLowerCase());
    const matchesStatus = filterStatus === 'all' || user.userStatus === filterStatus;
    const matchesRole = filterRole === 'all' || user.roles.includes(filterRole);
    
    return matchesSearch && matchesStatus && matchesRole;
  });

  function getRoleColor(role) {
    const colors = {
      'SUPER_ADMIN': 'bg-red-100 text-red-800',
      'SYSTEM_ADMIN': 'bg-orange-100 text-orange-800',
      'HQ_MANAGER': 'bg-blue-100 text-blue-800',
      'STORE_MANAGER': 'bg-green-100 text-green-800',
      'AREA_MANAGER': 'bg-purple-100 text-purple-800',
      'USER': 'bg-gray-100 text-gray-800'
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

  function editUser(user) {
    // TODO: 사용자 편집 모달 또는 페이지로 이동
    console.log('Edit user:', user);
  }

  function deleteUser(user) {
    // TODO: 사용자 삭제 확인 모달
    console.log('Delete user:', user);
  }

  function createUser() {
    // TODO: 사용자 생성 모달 또는 페이지로 이동
    console.log('Create user');
  }
</script>

<svelte:head>
  <title>사용자 관리 - WebPos</title>
</svelte:head>

<div slot="title">사용자 관리</div>

<div class="space-y-6">
  <!-- 헤더 -->
  <div class="flex items-center justify-between">
    <div>
      <h1 class="text-2xl font-bold text-gray-900">사용자 관리</h1>
      <p class="text-gray-600 mt-1">시스템 사용자를 관리합니다.</p>
    </div>
    <button 
      type="button" 
      class="btn btn-primary"
      on:click={createUser}
    >
      <Plus size="16" class="mr-2" />
      사용자 추가
    </button>
  </div>

  <!-- 검색 및 필터 -->
  <div class="card p-6">
    <div class="grid grid-cols-1 md:grid-cols-3 gap-4">
      <!-- 검색 -->
      <div class="relative">
        <Search size="20" class="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400" />
        <input
          type="text"
          placeholder="사용자명 또는 이메일 검색..."
          bind:value={searchTerm}
          class="input pl-10"
        />
      </div>

      <!-- 상태 필터 -->
      <select bind:value={filterStatus} class="input">
        <option value="all">모든 상태</option>
        <option value="ACTIVE">활성</option>
        <option value="INACTIVE">비활성</option>
        <option value="SUSPENDED">정지</option>
        <option value="PENDING_VERIFICATION">인증 대기</option>
        <option value="LOCKED">잠김</option>
      </select>

      <!-- 역할 필터 -->
      <select bind:value={filterRole} class="input">
        <option value="all">모든 역할</option>
        <option value="SUPER_ADMIN">슈퍼어드민</option>
        <option value="SYSTEM_ADMIN">시스템어드민</option>
        <option value="HQ_MANAGER">본사관리자</option>
        <option value="STORE_MANAGER">매장관리자</option>
        <option value="AREA_MANAGER">지역관리자</option>
        <option value="USER">일반사용자</option>
      </select>
    </div>
  </div>

  <!-- 사용자 목록 -->
  <div class="card overflow-hidden">
    {#if loading}
      <div class="p-12 text-center">
        <div class="animate-spin rounded-full h-8 w-8 border-b-2 border-primary-600 mx-auto"></div>
        <p class="mt-4 text-gray-600">로딩 중...</p>
      </div>
    {:else if filteredUsers.length === 0}
      <div class="p-12 text-center">
        <p class="text-gray-500">사용자가 없습니다.</p>
      </div>
    {:else}
      <div class="overflow-x-auto">
        <table class="min-w-full divide-y divide-gray-200">
          <thead class="bg-gray-50">
            <tr>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                사용자
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
                생성일
              </th>
              <th class="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase tracking-wider">
                작업
              </th>
            </tr>
          </thead>
          <tbody class="bg-white divide-y divide-gray-200">
            {#each filteredUsers as user}
              <tr class="hover:bg-gray-50">
                <td class="px-6 py-4 whitespace-nowrap">
                  <div class="flex items-center">
                    <div class="w-10 h-10 bg-primary-600 rounded-full flex items-center justify-center">
                      <span class="text-white font-medium text-sm">
                        {user.username.charAt(0).toUpperCase()}
                      </span>
                    </div>
                    <div class="ml-4">
                      <div class="text-sm font-medium text-gray-900">{user.username}</div>
                      <div class="text-sm text-gray-500">{user.email}</div>
                    </div>
                  </div>
                </td>
                <td class="px-6 py-4 whitespace-nowrap">
                  <div class="flex flex-wrap gap-1">
                    {#each user.roles as role}
                      <span class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium {getRoleColor(role)}">
                        {role}
                      </span>
                    {/each}
                  </div>
                </td>
                <td class="px-6 py-4 whitespace-nowrap">
                  <span class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium {getStatusColor(user.userStatus)}">
                    {user.userStatus}
                  </span>
                </td>
                <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                  {user.lastLoginAt ? new Date(user.lastLoginAt).toLocaleDateString('ko-KR') : '없음'}
                </td>
                <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                  {new Date(user.createdAt).toLocaleDateString('ko-KR')}
                </td>
                <td class="px-6 py-4 whitespace-nowrap text-right text-sm font-medium">
                  <div class="flex justify-end space-x-2">
                    <button
                      type="button"
                      class="text-indigo-600 hover:text-indigo-900"
                      on:click={() => editUser(user)}
                      title="편집"
                    >
                      <Edit size="16" />
                    </button>
                    <button
                      type="button"
                      class="text-red-600 hover:text-red-900"
                      on:click={() => deleteUser(user)}
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
