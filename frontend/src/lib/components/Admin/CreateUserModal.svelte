<script>
  import { createEventDispatcher } from 'svelte';
  import { authStore } from '$lib/stores/auth.js';
  import { toastStore } from '$lib/stores/toast.js';
  import Modal from '$lib/components/Common/Modal.svelte';
  import { Eye, EyeOff } from 'lucide-svelte';

  export let open = false;

  const dispatch = createEventDispatcher();

  let formData = {
    username: '',
    email: '',
    password: '',
    roles: ['USER']
  };
  let loading = false;
  let showPassword = false;
  let errors = {};

  const availableRoles = [
    { value: 'USER', label: '일반 사용자', description: 'POS 판매 기능만 사용 가능' },
    { value: 'STORE_MANAGER', label: '매장 관리자', description: 'POS 시스템 관리' },
    { value: 'AREA_MANAGER', label: '지역 관리자', description: '담당 지역 매장 관리' },
    { value: 'HQ_MANAGER', label: '본사 관리자', description: '영업정보시스템 관리' },
    { value: 'SYSTEM_ADMIN', label: '시스템 관리자', description: '시스템 전반 관리' },
    { value: 'SUPER_ADMIN', label: '최고 관리자', description: '모든 시스템 관리' }
  ];

  function resetForm() {
    formData = {
      username: '',
      email: '',
      password: '',
      roles: ['USER']
    };
    errors = {};
    showPassword = false;
  }

  function validateForm() {
    errors = {};

    if (!formData.username.trim()) {
      errors.username = '사용자명을 입력해주세요.';
    } else if (formData.username.length < 3) {
      errors.username = '사용자명은 3자 이상이어야 합니다.';
    }

    if (!formData.email.trim()) {
      errors.email = '이메일을 입력해주세요.';
    } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(formData.email)) {
      errors.email = '올바른 이메일 형식이 아닙니다.';
    }

    if (!formData.password) {
      errors.password = '비밀번호를 입력해주세요.';
    } else if (formData.password.length < 6) {
      errors.password = '비밀번호는 6자 이상이어야 합니다.';
    }

    if (formData.roles.length === 0) {
      errors.roles = '최소 하나의 역할을 선택해주세요.';
    }

    return Object.keys(errors).length === 0;
  }

  async function handleSubmit() {
    if (!validateForm()) return;

    loading = true;

    try {
      const response = await fetch('/api/v1/admin/users', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${$authStore.token}`
        },
        body: JSON.stringify(formData)
      });

      if (!response.ok) {
        const error = await response.json();
        throw new Error(error.message || '사용자 생성에 실패했습니다.');
      }

      const newUser = await response.json();
      
      toastStore.success('사용자가 성공적으로 생성되었습니다.');
      dispatch('user-created', newUser);
      
      resetForm();
      open = false;
    } catch (error) {
      console.error('Create user error:', error);
      toastStore.error(error.message);
    } finally {
      loading = false;
    }
  }

  function handleClose() {
    resetForm();
    open = false;
    dispatch('close');
  }

  function toggleRole(role) {
    if (formData.roles.includes(role)) {
      formData.roles = formData.roles.filter(r => r !== role);
    } else {
      formData.roles = [...formData.roles, role];
    }
  }

  $: if (!open) {
    resetForm();
  }
</script>

<Modal bind:open title="새 사용자 생성" size="lg" on:close={handleClose}>
  <form on:submit|preventDefault={handleSubmit} class="space-y-6">
    <!-- 기본 정보 -->
    <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
      <!-- 사용자명 -->
      <div>
        <label for="username" class="block text-sm font-medium text-gray-700 mb-1">
          사용자명 <span class="text-red-500">*</span>
        </label>
        <input
          id="username"
          type="text"
          bind:value={formData.username}
          class="input"
          class:border-red-300={errors.username}
          placeholder="사용자명을 입력하세요"
          disabled={loading}
        />
        {#if errors.username}
          <p class="mt-1 text-sm text-red-600">{errors.username}</p>
        {/if}
      </div>

      <!-- 이메일 -->
      <div>
        <label for="email" class="block text-sm font-medium text-gray-700 mb-1">
          이메일 <span class="text-red-500">*</span>
        </label>
        <input
          id="email"
          type="email"
          bind:value={formData.email}
          class="input"
          class:border-red-300={errors.email}
          placeholder="이메일을 입력하세요"
          disabled={loading}
        />
        {#if errors.email}
          <p class="mt-1 text-sm text-red-600">{errors.email}</p>
        {/if}
      </div>
    </div>

    <!-- 비밀번호 -->
    <div>
      <label for="password" class="block text-sm font-medium text-gray-700 mb-1">
        임시 비밀번호 <span class="text-red-500">*</span>
      </label>
      <div class="relative">
        <input
          id="password"
          type={showPassword ? 'text' : 'password'}
          bind:value={formData.password}
          class="input pr-10"
          class:border-red-300={errors.password}
          placeholder="임시 비밀번호를 입력하세요"
          disabled={loading}
        />
        <button
          type="button"
          class="absolute inset-y-0 right-0 pr-3 flex items-center"
          on:click={() => showPassword = !showPassword}
        >
          {#if showPassword}
            <EyeOff size="16" class="text-gray-400" />
          {:else}
            <Eye size="16" class="text-gray-400" />
          {/if}
        </button>
      </div>
      {#if errors.password}
        <p class="mt-1 text-sm text-red-600">{errors.password}</p>
      {/if}
      <p class="mt-1 text-sm text-gray-500">
        사용자는 첫 로그인 시 비밀번호를 변경해야 합니다.
      </p>
    </div>

    <!-- 역할 선택 -->
    <div>
      <label class="block text-sm font-medium text-gray-700 mb-3">
        역할 <span class="text-red-500">*</span>
      </label>
      <div class="space-y-3">
        {#each availableRoles as role}
          <label class="flex items-start space-x-3 p-3 border border-gray-200 rounded-lg hover:bg-gray-50 cursor-pointer">
            <input
              type="checkbox"
              checked={formData.roles.includes(role.value)}
              on:change={() => toggleRole(role.value)}
              class="mt-1 h-4 w-4 text-primary-600 focus:ring-primary-500 border-gray-300 rounded"
              disabled={loading}
            />
            <div class="flex-1 min-w-0">
              <div class="text-sm font-medium text-gray-900">{role.label}</div>
              <div class="text-sm text-gray-500">{role.description}</div>
            </div>
          </label>
        {/each}
      </div>
      {#if errors.roles}
        <p class="mt-1 text-sm text-red-600">{errors.roles}</p>
      {/if}
    </div>

    <!-- 선택된 역할 미리보기 -->
    {#if formData.roles.length > 0}
      <div class="p-3 bg-blue-50 border border-blue-200 rounded-lg">
        <h4 class="text-sm font-medium text-blue-900 mb-2">선택된 역할:</h4>
        <div class="flex flex-wrap gap-2">
          {#each formData.roles as selectedRole}
            {@const roleInfo = availableRoles.find(r => r.value === selectedRole)}
            <span class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-blue-100 text-blue-800">
              {roleInfo?.label || selectedRole}
            </span>
          {/each}
        </div>
      </div>
    {/if}
  </form>

  <!-- 버튼 -->
  <div slot="footer" class="flex justify-end space-x-3 p-4 border-t border-gray-200">
    <button
      type="button"
      class="btn btn-secondary"
      on:click={handleClose}
      disabled={loading}
    >
      취소
    </button>
    <button
      type="button"
      class="btn btn-primary"
      on:click={handleSubmit}
      disabled={loading || !formData.username || !formData.email || !formData.password}
    >
      {#if loading}
        <div class="flex items-center">
          <div class="animate-spin rounded-full h-4 w-4 border-b-2 border-white mr-2"></div>
          생성 중...
        </div>
      {:else}
        사용자 생성
      {/if}
    </button>
  </div>
</Modal>
