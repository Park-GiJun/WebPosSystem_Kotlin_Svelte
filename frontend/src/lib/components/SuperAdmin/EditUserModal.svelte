<script>
  import { createEventDispatcher } from 'svelte';
  import { userApi } from '$lib/api/admin.js';
  import { authStore } from '$lib/stores/auth.js';
  import { toastStore } from '$lib/stores/toast.js';
  import { X, Save, Shield, Mail, User, Lock } from 'lucide-svelte';

  export let open = false;
  export let user = null;

  const dispatch = createEventDispatcher();

  // 폼 데이터
  let formData = {
    username: '',
    email: '',
    roles: [],
    userStatus: 'ACTIVE'
  };

  let loading = false;
  let errors = {};

  // 사용 가능한 역할 목록
  const availableRoles = [
    { value: 'SUPER_ADMIN', label: '최고관리자', description: '모든 시스템 권한' },
    { value: 'SYSTEM_ADMIN', label: '시스템관리자', description: '시스템 관리 권한' },
    { value: 'HQ_MANAGER', label: '본사관리자', description: '본사 관리 권한' },
    { value: 'STORE_MANAGER', label: '매장관리자', description: '매장 관리 권한' },
    { value: 'AREA_MANAGER', label: '지역관리자', description: '지역 관리 권한' },
    { value: 'USER', label: '일반사용자', description: '기본 사용자 권한' }
  ];

  // 사용자 상태 목록
  const statusOptions = [
    { value: 'ACTIVE', label: '활성' },
    { value: 'INACTIVE', label: '비활성' },
    { value: 'SUSPENDED', label: '정지' },
    { value: 'PENDING_VERIFICATION', label: '인증대기' }
  ];

  let initializing = false;
  let lastInitializedUserId = null;

  // 사용자 정보가 변경될 때 폼 데이터 초기화 (사용자 ID 기준으로 중복 방지)
  $: if (user && open && user.id && user.id !== lastInitializedUserId && !initializing) {
    initializeForm();
  }

  function initializeForm() {
    if (!user || initializing) return;
    
    initializing = true;
    lastInitializedUserId = user.id;
    
    try {
      // 전달받은 사용자 정보로 폼 초기화
      formData = {
        username: user.username || '',
        email: user.email || '',
        roles: user.roles || [],
        userStatus: user.userStatus || 'ACTIVE'
      };
      errors = {};
      console.log('✅ 사용자 정보로 폼 초기화 완료:', user.username);
      
    } catch (error) {
      console.error('❌ 폼 초기화 실패:', error);
      // 에러가 발생해도 기본값으로 초기화
      formData = {
        username: user?.username || '',
        email: user?.email || '',
        roles: user?.roles || [],
        userStatus: user?.userStatus || 'ACTIVE'
      };
      errors = {};
    } finally {
      initializing = false;
    }
  }

  function validateForm() {
    errors = {};

    if (!formData.username.trim()) {
      errors.username = '사용자명은 필수입니다';
    } else if (formData.username.length < 3) {
      errors.username = '사용자명은 3자 이상이어야 합니다';
    } else if (!/^[a-zA-Z0-9_-]+$/.test(formData.username)) {
      errors.username = '사용자명은 영문, 숫자, -, _만 사용 가능합니다';
    }

    if (!formData.email.trim()) {
      errors.email = '이메일은 필수입니다';
    } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(formData.email)) {
      errors.email = '올바른 이메일 형식이 아닙니다';
    }

    if (!formData.roles || formData.roles.length === 0) {
      errors.roles = '최소 하나의 역할을 선택해야 합니다';
    }

    return Object.keys(errors).length === 0;
  }

  async function handleSubmit() {
    if (!validateForm()) {
      console.warn('⚠️ 폼 유효성 검사 실패:', errors);
      return;
    }

    const token = $authStore.token;
    if (!token) {
      toastStore.error('인증이 필요합니다.');
      return;
    }

    try {
      loading = true;
      
      // 전송할 데이터 검증
      const requestData = {
        username: formData.username.trim(),
        email: formData.email.trim(),
        roles: formData.roles || [],
        userStatus: formData.userStatus || 'ACTIVE'
      };
      
      console.log('✏️ 사용자 수정 요청 데이터:', {
        userId: user.id,
        requestData: requestData,
        originalFormData: formData,
        token: token ? token.substring(0, 20) + '...' : 'null'
      });

      const response = await userApi.updateUser(user.id, requestData, token);
      
      if (response) {
        dispatch('user-updated', response);
        toastStore.success('사용자가 성공적으로 수정되었습니다.');
        close();
        console.log('✅ 사용자 수정 완료:', response);
      }
    } catch (error) {
      console.error('❌ 사용자 수정 실패:', {
        error: error,
        message: error.message,
        stack: error.stack
      });

      // 에러 메시지 개선
      let errorMessage = '사용자 수정에 실패했습니다.';
      
      if (error.message?.includes('Version does not match')) {
        errorMessage = '다른 사용자가 동시에 수정했습니다. 페이지를 새로고침 후 다시 시도해주세요.';
      } else if (error.message?.includes('동시 수정 충돌')) {
        errorMessage = '동시 수정으로 인한 충돌이 발생했습니다. 잠시 후 다시 시도해주세요.';
      } else if (error.message?.includes('이미 존재하는')) {
        errorMessage = error.message;
      } else if (error.message) {
        errorMessage = error.message;
      }
      
      toastStore.error(errorMessage);
    } finally {
      loading = false;
    }
  }

  function handleRoleChange(roleValue, checked) {
    if (checked) {
      formData.roles = [...formData.roles, roleValue];
    } else {
      formData.roles = formData.roles.filter(role => role !== roleValue);
    }
    // 역할 변경 시 에러 초기화
    if (errors.roles) {
      delete errors.roles;
      errors = { ...errors };
    }
  }

  function close() {
    open = false;
    initializing = false;
    lastInitializedUserId = null;
    formData = {
      username: '',
      email: '',
      roles: [],
      userStatus: 'ACTIVE'
    };
    errors = {};
    dispatch('close');
  }

  function handleKeydown(event) {
    if (event.key === 'Escape') {
      close();
    }
  }

  function getRoleLabel(roleValue) {
    const role = availableRoles.find(r => r.value === roleValue);
    return role ? role.label : roleValue;
  }

  function getStatusLabel(statusValue) {
    const status = statusOptions.find(s => s.value === statusValue);
    return status ? status.label : statusValue;
  }
</script>

<svelte:window on:keydown={handleKeydown} />

{#if open}
  <div class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center p-4 z-50">
    <div class="bg-white rounded-lg shadow-xl max-w-2xl w-full max-h-[90vh] overflow-y-auto">
      <!-- 헤더 -->
      <div class="flex items-center justify-between p-6 border-b">
        <div class="flex items-center space-x-3">
          <div class="p-2 bg-blue-100 rounded-lg">
            <User class="w-5 h-5 text-blue-600" />
          </div>
          <div>
            <h3 class="text-lg font-semibold text-gray-900">사용자 수정</h3>
            <p class="text-sm text-gray-600">"{user?.username || ''}" 사용자의 정보를 수정합니다</p>
          </div>
        </div>
        <button
          type="button"
          on:click={close}
          class="text-gray-400 hover:text-gray-600 transition-colors"
        >
          <X size="24" />
        </button>
      </div>

      <!-- 폼 -->
      <form on:submit|preventDefault={handleSubmit} class="p-6 space-y-6">
        <!-- 기본 정보 섹션 -->
        <div class="space-y-4">
          <h4 class="text-md font-medium text-gray-900 flex items-center">
            <User class="w-4 h-4 mr-2" />
            기본 정보
          </h4>

          <!-- 사용자명 -->
          <div>
            <label for="username" class="block text-sm font-medium text-gray-700 mb-1">
              사용자명 *
            </label>
            <input
              id="username"
              type="text"
              bind:value={formData.username}
              class="input {errors.username ? 'border-red-500' : ''}"
              placeholder="사용자명을 입력하세요"
              required
            />
            {#if errors.username}
              <p class="mt-1 text-sm text-red-600">{errors.username}</p>
            {/if}
          </div>

          <!-- 이메일 -->
          <div>
            <label for="email" class="block text-sm font-medium text-gray-700 mb-1">
              <Mail class="w-4 h-4 inline mr-1" />
              이메일 *
            </label>
            <input
              id="email"
              type="email"
              bind:value={formData.email}
              class="input {errors.email ? 'border-red-500' : ''}"
              placeholder="이메일을 입력하세요"
              required
            />
            {#if errors.email}
              <p class="mt-1 text-sm text-red-600">{errors.email}</p>
            {/if}
          </div>

          <!-- 사용자 상태 -->
          <div>
            <label for="userStatus" class="block text-sm font-medium text-gray-700 mb-1">
              <Lock class="w-4 h-4 inline mr-1" />
              계정 상태
            </label>
            <select
              id="userStatus"
              bind:value={formData.userStatus}
              class="input"
            >
              {#each statusOptions as status}
                <option value={status.value}>{status.label}</option>
              {/each}
            </select>
          </div>
        </div>

        <!-- 역할 권한 섹션 -->
        <div class="space-y-4">
          <h4 class="text-md font-medium text-gray-900 flex items-center">
            <Shield class="w-4 h-4 mr-2" />
            역할 및 권한 *
          </h4>

          <div class="grid grid-cols-1 md:grid-cols-2 gap-3">
            {#each availableRoles as role}
              <label class="flex items-start space-x-3 p-3 border rounded-lg hover:bg-gray-50 cursor-pointer">
                <input
                  type="checkbox"
                  checked={formData.roles.includes(role.value)}
                  on:change={(e) => handleRoleChange(role.value, e.target.checked)}
                  class="mt-1"
                />
                <div class="flex-1">
                  <div class="font-medium text-sm text-gray-900">{role.label}</div>
                  <div class="text-xs text-gray-500">{role.description}</div>
                </div>
              </label>
            {/each}
          </div>

          {#if errors.roles}
            <p class="text-sm text-red-600">{errors.roles}</p>
          {/if}

          {#if formData.roles.length > 0}
            <div class="p-3 bg-blue-50 rounded-lg">
              <div class="text-sm text-blue-800 font-medium mb-2">선택된 역할:</div>
              <div class="flex flex-wrap gap-2">
                {#each formData.roles as role}
                  <span class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-blue-100 text-blue-800">
                    {getRoleLabel(role)}
                  </span>
                {/each}
              </div>
            </div>
          {/if}
        </div>

        <!-- 현재 계정 상태 정보 -->
        {#if user}
          <div class="bg-gray-50 p-4 rounded-lg space-y-2">
            <h5 class="text-sm font-medium text-gray-900">현재 계정 정보</h5>
            <div class="grid grid-cols-2 gap-4 text-sm">
              <div>
                <span class="text-gray-600">상태:</span>
                <span class="ml-2 px-2 py-1 rounded text-xs font-medium {user.userStatus === 'ACTIVE' ? 'bg-green-100 text-green-800' : 'bg-gray-100 text-gray-800'}">
                  {getStatusLabel(user.userStatus)}
                </span>
              </div>
              <div>
                <span class="text-gray-600">이메일 인증:</span>
                <span class="ml-2 px-2 py-1 rounded text-xs font-medium {user.isEmailVerified ? 'bg-green-100 text-green-800' : 'bg-yellow-100 text-yellow-800'}">
                  {user.isEmailVerified ? '인증완료' : '인증대기'}
                </span>
              </div>
              <div>
                <span class="text-gray-600">계정 잠금:</span>
                <span class="ml-2 px-2 py-1 rounded text-xs font-medium {user.isLocked ? 'bg-red-100 text-red-800' : 'bg-green-100 text-green-800'}">
                  {user.isLocked ? '잠김' : '정상'}
                </span>
              </div>
              <div>
                <span class="text-gray-600">로그인 실패:</span>
                <span class="ml-2 text-gray-900">{user.failedLoginAttempts}회</span>
              </div>
            </div>
            {#if user.lastLoginAt}
              <div class="text-sm">
                <span class="text-gray-600">마지막 로그인:</span>
                <span class="ml-2 text-gray-900">{new Date(user.lastLoginAt).toLocaleString('ko-KR')}</span>
              </div>
            {/if}
          </div>
        {/if}

        <!-- 버튼 -->
        <div class="flex justify-end space-x-3 pt-4 border-t">
          <button
            type="button"
            on:click={close}
            class="btn btn-secondary"
            disabled={loading}
          >
            취소
          </button>
          <button
            type="submit"
            class="btn btn-primary"
            disabled={loading}
          >
            <Save size="16" class="mr-2" />
            {loading ? '수정 중...' : '수정하기'}
          </button>
        </div>
      </form>
    </div>
  </div>
{/if}
