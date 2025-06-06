<script>
  import { createEventDispatcher } from 'svelte';
  import { Eye, EyeOff, X, User, Shield } from 'lucide-svelte';
  import Modal from '$lib/components/Common/Modal.svelte';
  import { userApi } from '$lib/api/superAdmin.js';
  import { toastStore } from '$lib/stores/toast.js';

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
    { value: 'USER', label: '일반 사용자', description: '기본 사용자 권한' },
    { value: 'STORE_MANAGER', label: '매장 관리자', description: '매장 운영 및 관리' },
    { value: 'AREA_MANAGER', label: '지역 관리자', description: '지역별 매장 관리' },
    { value: 'HQ_MANAGER', label: '본사 관리자', description: '본사 및 전체 매장 관리' },
    { value: 'SYSTEM_ADMIN', label: '시스템 관리자', description: '시스템 설정 및 관리' },
    { value: 'SUPER_ADMIN', label: '슈퍼 관리자', description: '모든 권한 (주의!)' }
  ];

  function validateForm() {
    errors = {};

    if (!form.username.trim()) {
      errors.username = '사용자명은 필수입니다.';
    } else if (form.username.length < 3) {
      errors.username = '사용자명은 최소 3자 이상이어야 합니다.';
    } else if (!/^[a-zA-Z0-9_]+$/.test(form.username)) {
      errors.username = '사용자명은 영문, 숫자, 밑줄(_)만 사용할 수 있습니다.';
    }

    if (!form.email.trim()) {
      errors.email = '이메일은 필수입니다.';
    } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(form.email)) {
      errors.email = '올바른 이메일 형식이 아닙니다.';
    }

    if (!form.password) {
      errors.password = '비밀번호는 필수입니다.';
    } else if (form.password.length < 8) {
      errors.password = '비밀번호는 최소 8자 이상이어야 합니다.';
    } else if (!/^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)/.test(form.password)) {
      errors.password = '비밀번호는 대문자, 소문자, 숫자를 포함해야 합니다.';
    }

    if (form.password !== form.confirmPassword) {
      errors.confirmPassword = '비밀번호가 일치하지 않습니다.';
    }

    if (form.roles.length === 0) {
      errors.roles = '최소 하나의 역할을 선택해야 합니다.';
    }

    // 슈퍼 관리자 역할 체크
    if (form.roles.includes('SUPER_ADMIN') && form.roles.length > 1) {
      errors.roles = '슈퍼 관리자는 단독으로만 설정할 수 있습니다.';
    }

    return Object.keys(errors).length === 0;
  }

  function toggleRole(role) {
    if (role === 'SUPER_ADMIN') {
      // 슈퍼 관리자 선택시 다른 역할 모두 제거
      if (form.roles.includes('SUPER_ADMIN')) {
        form.roles = form.roles.filter(r => r !== 'SUPER_ADMIN');
      } else {
        form.roles = ['SUPER_ADMIN'];
      }
    } else {
      // 다른 역할 선택시 슈퍼 관리자 제거
      form.roles = form.roles.filter(r => r !== 'SUPER_ADMIN');
      
      if (form.roles.includes(role)) {
        form.roles = form.roles.filter(r => r !== role);
      } else {
        form.roles = [...form.roles, role];
      }
    }
  }

  function getRoleColor(role) {
    const colors = {
      'SUPER_ADMIN': 'bg-red-100 text-red-800 border-red-200',
      'SYSTEM_ADMIN': 'bg-orange-100 text-orange-800 border-orange-200',
      'HQ_MANAGER': 'bg-purple-100 text-purple-800 border-purple-200',
      'AREA_MANAGER': 'bg-blue-100 text-blue-800 border-blue-200',
      'STORE_MANAGER': 'bg-green-100 text-green-800 border-green-200',
      'USER': 'bg-gray-100 text-gray-800 border-gray-200'
    };
    return colors[role] || 'bg-gray-100 text-gray-800 border-gray-200';
  }

  async function handleSubmit() {
    if (!validateForm()) {
      return;
    }

    loading = true;

    try {
      const userData = {
        username: form.username.trim(),
        email: form.email.trim(),
        password: form.password,
        roles: form.roles
      };

      const response = await userApi.createUser(userData);

      if (response.success) {
        dispatch('user-created', response.data);
        handleClose();
        toastStore.success('사용자가 성공적으로 생성되었습니다.');
      } else if (response.error === 'API_NOT_FOUND') {
        // API가 없는 경우 더미 데이터로 응답
        const dummyUser = {
          id: Date.now().toString(),
          ...userData,
          userStatus: 'PENDING_VERIFICATION',
          isEmailVerified: false,
          lastLoginAt: null,
          failedLoginAttempts: 0,
          isLocked: false,
          lockedUntil: null,
          createdAt: new Date().toISOString(),
          updatedAt: new Date().toISOString()
        };
        
        dispatch('user-created', dummyUser);
        handleClose();
        toastStore.success('사용자가 성공적으로 생성되었습니다. (데모 모드)');
      } else {
        errors.submit = response.message || '사용자 생성에 실패했습니다.';
      }
    } catch (error) {
      console.error('사용자 생성 오류:', error);
      errors.submit = '사용자 생성 중 오류가 발생했습니다.';
    } finally {
      loading = false;
    }
  }

  function generatePassword() {
    const chars = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*';
    let password = '';
    
    // 최소 요구사항 충족을 위해 각 타입에서 하나씩 선택
    password += 'ABCDEFGHIJKLMNOPQRSTUVWXYZ'[Math.floor(Math.random() * 26)]; // 대문자
    password += 'abcdefghijklmnopqrstuvwxyz'[Math.floor(Math.random() * 26)]; // 소문자
    password += '0123456789'[Math.floor(Math.random() * 10)]; // 숫자
    password += '!@#$%^&*'[Math.floor(Math.random() * 8)]; // 특수문자
    
    // 나머지 4자리 랜덤 생성
    for (let i = 0; i < 4; i++) {
      password += chars[Math.floor(Math.random() * chars.length)];
    }
    
    // 문자열을 섞어서 패턴을 예측하기 어렵게 만듦
    form.password = password.split('').sort(() => Math.random() - 0.5).join('');
    form.confirmPassword = form.password;
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

<Modal bind:open title="사용자 생성" size="lg" on:close={handleClose}>
  <form on:submit|preventDefault={handleSubmit} class="space-y-6">
    <!-- 기본 정보 섹션 -->
    <div class="bg-gray-50 p-4 rounded-lg">
      <h3 class="text-lg font-medium text-gray-900 mb-4 flex items-center">
        <User class="mr-2 h-5 w-5" />
        기본 정보
      </h3>
      
      <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
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
            placeholder="영문, 숫자, 밑줄(_)만 사용"
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
            placeholder="user@example.com"
            disabled={loading}
            required
          />
          {#if errors.email}
            <p class="text-sm text-red-600 mt-1">{errors.email}</p>
          {/if}
        </div>
      </div>
    </div>

    <!-- 비밀번호 섹션 -->
    <div class="bg-gray-50 p-4 rounded-lg">
      <div class="flex items-center justify-between mb-4">
        <h3 class="text-lg font-medium text-gray-900 flex items-center">
          <Shield class="mr-2 h-5 w-5" />
          비밀번호 설정
        </h3>
        <button
          type="button"
          class="btn btn-secondary btn-sm"
          on:click={generatePassword}
          disabled={loading}
        >
          자동 생성
        </button>
      </div>
      
      <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
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
                placeholder="최소 8자, 대소문자+숫자 포함"
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
                placeholder="최소 8자, 대소문자+숫자 포함"
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
                placeholder="비밀번호를 다시 입력"
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
                placeholder="비밀번호를 다시 입력"
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
      </div>
    </div>

    <!-- 역할 선택 섹션 -->
    <div class="bg-gray-50 p-4 rounded-lg">
      <h3 class="text-lg font-medium text-gray-900 mb-4 flex items-center">
        <Shield class="mr-2 h-5 w-5" />
        역할 및 권한
      </h3>
      
      <div class="space-y-3">
        {#each availableRoles as role}
          <label class="flex items-start space-x-3 p-3 border rounded-lg hover:bg-white transition-colors {form.roles.includes(role.value) ? getRoleColor(role.value) : 'border-gray-200 bg-white'}">
            <input
              type="checkbox"
              checked={form.roles.includes(role.value)}
              on:change={() => toggleRole(role.value)}
              class="mt-1 rounded border-gray-300 text-primary-600 shadow-sm focus:border-primary-300 focus:ring focus:ring-primary-200 focus:ring-opacity-50"
              disabled={loading}
            />
            <div class="flex-1">
              <div class="flex items-center space-x-2">
                <span class="font-medium text-sm">{role.label}</span>
                {#if role.value === 'SUPER_ADMIN'}
                  <span class="inline-flex items-center px-2 py-0.5 rounded text-xs font-medium bg-red-100 text-red-800">
                    위험
                  </span>
                {/if}
              </div>
              <p class="text-xs text-gray-600 mt-1">{role.description}</p>
            </div>
          </label>
        {/each}
      </div>
      
      {#if errors.roles}
        <p class="text-sm text-red-600 mt-2">{errors.roles}</p>
      {/if}
      
      {#if form.roles.includes('SUPER_ADMIN')}
        <div class="mt-3 p-3 bg-red-50 border border-red-200 rounded-lg">
          <p class="text-sm text-red-700">
            ⚠️ <strong>주의:</strong> 슈퍼 관리자는 시스템의 모든 권한을 가집니다. 신중하게 부여하세요.
          </p>
        </div>
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
