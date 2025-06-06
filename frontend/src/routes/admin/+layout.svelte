<script>
  import { authStore } from '$lib/stores/auth.js';
  import { menuStore } from '$lib/stores/menu.js';
  import { goto } from '$app/navigation';
  import { onMount } from 'svelte';
  import Header from '$lib/components/Layout/Header.svelte';
  import Sidebar from '$lib/components/Layout/Sidebar.svelte';
  import TabContainer from '$lib/components/Layout/TabContainer.svelte';

  let adminMenus = [];
  let loading = true;

  onMount(async () => {
    // 슈퍼어드민 권한 확인
    const unsubscribe = authStore.subscribe(async auth => {
      if (auth.isAuthenticated) {
        if (!auth.user?.roles?.includes('SUPER_ADMIN')) {
          // 권한이 없으면 대시보드로 리다이렉트
          goto('/dashboard');
          return;
        }

        // 메뉴 로드
        if (!$menuStore.menus.length) {
          await menuStore.loadMenus();
        }
        
        // 슈퍼어드민 메뉴 가져오기
        try {
          const systemMenus = await menuStore.getSystemMenus('ADMIN');
          adminMenus = systemMenus.map(menu => ({
            id: menu.menuCode.toLowerCase().replace('admin_', ''),
            title: menu.menuName,
            icon: menu.iconName || 'cog',
            path: menu.menuPath,
            description: getMenuDescription(menu.menuCode)
          }));
          loading = false;
        } catch (error) {
          console.error('Failed to load admin menus:', error);
          loading = false;
        }
      }
    });

    return unsubscribe;
  });

  function getMenuDescription(menuCode) {
    const descriptions = {
      'ADMIN_ORGANIZATIONS': '조직 구조 및 계층 관리',
      'ADMIN_USERS': '시스템 사용자 계정 관리',
      'ADMIN_PERMISSIONS': '역할 및 권한 설정'
    };
    return descriptions[menuCode] || '';
  }
</script>

{#if loading}
  <div class="min-h-screen bg-gray-50 flex items-center justify-center">
    <div class="text-center">
      <div class="animate-spin rounded-full h-12 w-12 border-b-2 border-red-600 mx-auto"></div>
      <p class="mt-4 text-gray-600">메뉴를 로드하는 중...</p>
    </div>
  </div>
{:else}
  <div class="min-h-screen bg-gray-50">
    <Header 
      title="슈퍼어드민 시스템"
      subtitle="시스템 전체 관리"
      backgroundColor="bg-red-600"
    />
    
    <div class="flex h-screen pt-16">
      <Sidebar 
        menus={adminMenus}
        backgroundColor="bg-red-50"
        borderColor="border-red-200"
        textColor="text-red-800"
        hoverColor="hover:bg-red-100"
      />
      
      <main class="flex-1 ml-64 overflow-hidden">
        <TabContainer>
          <slot />
        </TabContainer>
      </main>
    </div>
  </div>
{/if}
