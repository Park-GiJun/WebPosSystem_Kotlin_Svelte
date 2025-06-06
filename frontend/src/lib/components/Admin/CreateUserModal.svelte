<script>
  import { createEventDispatcher } from 'svelte';
  import { Eye, EyeOff, X } from 'lucide-svelte';
  import Modal from '$lib/components/Common/Modal.svelte';

  export let open = false;

  const dispatch = createEventDispatcher();

  let form = {
    username: '',
    email: '',
    password: '',
    confirmPassword: '',
    roles: ['USER']
  };

  let showPassword = false;
  let showConfirmPassword = false;
  let loading = false;
  let errors = {};

  const availableRoles = [
    { value: 'USER', label: '일반 사용자' },
    { value: 'STORE_MANAGER', label: '매장 관리자' },
    { value: 'AREA_MANAGER', label: '지역 관리자' },
    { value: 'HQ_MANAGER', label: '본사 관리자' },
    { value: 'SYSTEM_ADMIN', label: '시스템 관리자' },
    { value: 'SUPER_ADMIN', label: '슈퍼 관리자' }
  ];

  function validateForm() {
    errors = {};

    if (!form.username.trim()) {
      errors.username = '사용자명은 필수입니다.';
    } else if (form.username.length < 3) {
      errors.username = '사용자명은 최소 3자 이상이어야 합니다.';
    }

    if (!form.email.trim()) {
      errors.email = '이메일은 필수입니다.';
    } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(form.email)) {
      errors.email = '올바른 이메일 형식이 아닙니다.';
    }

    if (!form.password) {
      errors.password = '비밀번호는 필수입니다.';
    } else if (form.password.length < 6) {
      errors.password = '비밀번호는 최소 6자 이상이어야 합니다.';
    }

    if (form.password !== form.confirmPassword) {
      errors.confirmPassword = '비밀번호가 일치하지 않습니다.';
    }

    if (form.roles.length === 0) {
      errors.roles = '최소 하나의 역할을 선택해야 합니다.';
    }

    return Object.keys(errors).length === 0;
  }

  function toggleRole(role) {
    if (form.roles.includes(role)) {
      form.roles = form.roles.filter(r => r !== role);
    } else {
      form.roles = [...form.roles, role];
    }
  }

  async function handleSubmit() {
    if (!validateForm()) {
      return;
    }

    loading = true;

    try {
      // TODO: API 호출
      await new Promise(resolve => setTimeout(resolve, 1000)); // 임시 딜레이

      dispatch('user-created', {
        ...form,
        id: Date.now().toString(),
        userStatus: 'PENDING_VERIFICATION',
        isEmailVerified: false,
        lastLoginAt: null,
        failedLoginAttempts: 0,
        isLocked: false,
        lockedUntil: null,
        createdAt: new Date().toISOString(),
        updatedAt: new Date().toISOString()
      });

      handleClose();
    } catch (error) {
      console.error('Failed to create user:', error);
      errors.submit = '사용자 생성에 실패했습니다.';
    } finally {
      loading = false;
    }
  }

  function handleClose() {
    if (!loading) {
      open = false;
      form = {
        username: '',
        email: '',
        password: '',
        confirmPassword: '',
        roles: ['USER']
      };
      errors = {};
      showPassword = false;
      showConfirmPassword = false;
      dispatch('close');
    }
  }
</script>

<Modal bind:open title="사용자 추가" size="md" on:close={handleClose}>
  <form on:submit|preventDefault={handleSubmit} class="space-y-4">
    <!-- 사용자명 -->
    <div>
      <label for="username" class="block text-sm font-medium text-gray-700 mb-1">
        사용자명 <span class="text-red-500">*</span>
      </label>
      <input
        id="username"
        type="text"
        bind:value={form.username}
        class="input"
        class:border-red-300={errors.username}
        placeholder="사용자명을 입력하세요"
        disabled={loading}
        required
      />
      {#if errors.username}
        <p class="text-sm text-red-600 mt-1">{errors.username}</p>
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
        bind:value={form.email}
        class="input"
        class:border-red-300={errors.email}
        placeholder="이메일을 입력하세요"
        disabled={loading}
        required
      />
      {#if errors.email}
        <p class="text-sm text-red-600 mt-1">{errors.email}</p>
      {/if}
    </div>

    <!-- 비밀번호 -->
    <div>
      <label for="password" class="block text-sm font-medium text-gray-700 mb-1">
        비밀번호 <span class="text-red-500">*</span>
      </label>
      <div class="relative">
        {#if showPassword}
          <input
            id="password"
            type="text"
            bind:value={form.password}
            class="input pr-10"
            class:border-red-300={errors.password}
            placeholder="비밀번호를 입력하세요"
            disabled={loading}
            required
          />
        {:else}
          <input
            id="password"
            type="password"
            bind:value={form.password}
            class="input pr-10"
            class:border-red-300={errors.password}
            placeholder="비밀번호를 입력하세요"
            disabled={loading}
            required
          />
        {/if}
        <button
          type="button"
          class="absolute inset-y-0 right-0 pr-3 flex items-center"
          on:click={() => showPassword = !showPassword}
        >
          {#if showPassword}
            <EyeOff class="h-4 w-4 text-gray-400" />
          {:else}
            <Eye class="h-4 w-4 text-gray-400" />
          {/if}
        </button>
      </div>
      {#if errors.password}
        <p class="text-sm text-red-600 mt-1">{errors.password}</p>
      {/if}
    </div>

    <!-- 비밀번호 확인 -->
    <div>
      <label for="confirmPassword" class="block text-sm font-medium text-gray-700 mb-1">
        비밀번호 확인 <span class="text-red-500">*</span>
      </label>
      <div class="relative">
        {#if showConfirmPassword}
          <input
            id="confirmPassword"
            type="text"
            bind:value={form.confirmPassword}
            class="input pr-10"
            class:border-red-300={errors.confirmPassword}
            placeholder="비밀번호를 다시 입력하세요"
            disabled={loading}
            required
          />
        {:else}
          <input
            id="confirmPassword"
            type="password"
            bind:value={form.confirmPassword}
            class="input pr-10"
            class:border-red-300={errors.confirmPassword}
            placeholder="비밀번호를 다시 입력하세요"
            disabled={loading}
            required
          />
        {/if}
        <button
          type="button"
          class="absolute inset-y-0 right-0 pr-3 flex items-center"
          on:click={() => showConfirmPassword = !showConfirmPassword}
        >
          {#if showConfirmPassword}
            <EyeOff class="h-4 w-4 text-gray-400" />
          {:else}
            <Eye class="h-4 w-4 text-gray-400" />
          {/if}
        </button>
      </div>
      {#if errors.confirmPassword}
        <p class="text-sm text-red-600 mt-1">{errors.confirmPassword}</p>
      {/if}
    </div>

    <!-- 역할 선택 -->
    <div>
      <label class="block text-sm font-medium text-gray-700 mb-2">
        역할 <span class="text-red-500">*</span>
      </label>
      <div class="space-y-2 max-h-40 overflow-y-auto border border-gray-200 rounded-lg p-3">
        {#each availableRoles as role}
          <label class="flex items-center">
            <input
              type="checkbox"
              checked={form.roles.includes(role.value)}
              on:change={() => toggleRole(role.value)}
              class="rounded border-gray-300 text-primary-600 shadow-sm focus:border-primary-300 focus:ring focus:ring-primary-200 focus:ring-opacity-50"
              disabled={loading}
            />
            <span class="ml-2 text-sm text-gray-700">{role.label}</span>
          </label>
        {/each}
      </div>
      {#if errors.roles}
        <p class="text-sm text-red-600 mt-1">{errors.roles}</p>
      {/if}
    </div>

    <!-- 전체 폼 에러 -->
    {#if errors.submit}
      <div class="p-3 bg-red-50 border border-red-200 rounded-lg">
        <p class="text-sm text-red-700">{errors.submit}</p>
      </div>
    {/if}
  </form>

  <!-- 푸터 버튼들 -->
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
      type="submit"
      class="btn btn-primary"
      on:click={handleSubmit}
      disabled={loading}
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
