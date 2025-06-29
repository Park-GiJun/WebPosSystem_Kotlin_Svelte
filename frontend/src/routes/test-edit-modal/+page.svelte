<script>
  import EditUserModal from '$lib/components/SuperAdmin/EditUserModal.svelte';

  let showEditModal = false;
  let selectedUser = {
    id: 'test-user-id',
    username: 'testuser',
    email: 'test@example.com',
    roles: ['USER', 'STORE_MANAGER'],
    userStatus: 'ACTIVE',
    isEmailVerified: true,
    lastLoginAt: new Date().toISOString(),
    failedLoginAttempts: 0,
    isLocked: false,
    createdAt: new Date().toISOString(),
    updatedAt: new Date().toISOString()
  };

  function openEditModal() {
    showEditModal = true;
  }

  function handleUserUpdated(event) {
    console.log('User updated:', event.detail);
    alert('사용자가 수정되었습니다!');
  }
</script>

<svelte:head>
  <title>사용자 편집 모달 테스트</title>
</svelte:head>

<div class="p-8">
  <h1 class="text-2xl font-bold mb-4">사용자 편집 모달 테스트</h1>
  
  <div class="space-y-4">
    <div class="card p-6">
      <h2 class="text-lg font-semibold mb-4">테스트 사용자 정보</h2>
      <div class="grid grid-cols-2 gap-4 text-sm">
        <div><strong>ID:</strong> {selectedUser.id}</div>
        <div><strong>사용자명:</strong> {selectedUser.username}</div>
        <div><strong>이메일:</strong> {selectedUser.email}</div>
        <div><strong>역할:</strong> {selectedUser.roles.join(', ')}</div>
        <div><strong>상태:</strong> {selectedUser.userStatus}</div>
        <div><strong>이메일 인증:</strong> {selectedUser.isEmailVerified ? '완료' : '대기'}</div>
      </div>
    </div>
    
    <button 
      class="btn btn-primary"
      on:click={openEditModal}
    >
      편집 모달 열기
    </button>
  </div>
</div>

<!-- 사용자 편집 모달 -->
<EditUserModal
  bind:open={showEditModal}
  bind:user={selectedUser}
  on:user-updated={handleUserUpdated}
  on:close={() => {
    showEditModal = false;
  }}
/>
