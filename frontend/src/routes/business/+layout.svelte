<script>
  import { authStore } from '$lib/stores/auth.js';
  import { menuStore } from '$lib/stores/menu.js';
  import { goto } from '$app/navigation';
  import { onMount } from 'svelte';
  import Header from '$lib/components/Layout/Header.svelte';
  import Sidebar from '$lib/components/Layout/Sidebar.svelte';
  import TabContainer from '$lib/components/Layout/TabContainer.svelte';

  let businessMenus = [];
  let loading = true;

  onMount(async () => {
    // 영업정보시스템 접근 권한 확인
    const unsubscribe = authStore.subscribe(async auth => {
      if (auth.isAuthenticated) {
        // 본사 관리자 권한 확인 또는 메뉴 권한 확인
        const hasBusinessAccess = auth.user?.roles?.some(role => 
          ['SUPER_ADMIN', 'SYSTEM_ADMIN', 'HQ_MANAGER'].includes(role)
        );
        
        if (!hasBusinessAccess) {
          // 메뉴별 권한도 확인
          const hasMenuPermission = await menuStore.hasPermission('BUSINESS', 'READ');
          if (!hasMenuPermission) {
            goto('/dashboard');
            return;
          }
        }

        // 메뉴 로드
        if (!$menuStore.menus.length) {
          await menuStore.loadMenus();
        }
        
        // 영업정보시스템 메뉴 가져오기
        try {
          const systemMenus = await menuStore.getSystemMenus('BUSINESS');
          businessMenus = systemMenus.map(menu => ({
            id: menu.menuCode.toLowerCase().replace('business_', ''),
            title: menu.menuName,
            icon: menu.iconName || 'building',
            path: menu.menuPath,
            description: getMenuDescription(menu.menuCode)
          }));
          loading = false;
        } catch (error) {
          console.error('Failed to load business menus:', error);
          loading = false;
        }
      }
    });

    return unsubscribe;
  });

  function getMenuDescription(menuCode) {
    const descriptions = {
      'BUSINESS_HEADQUARTERS': '본사 정보 및 설정 관리',
      'BUSINESS_STORES': '매장 정보 및 운영 관리',
      'BUSINESS_POS': 'POS 시스템 설정 및 관리'
    };
    return descriptions[menuCode] || '';
  }
</script>

{#if loading}
  <div class="min-h-screen bg-gray-50 flex items-center justify-center">
    <div class="text-center">
      <div class="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600 mx-auto"></div>
      <p class="mt-4 text-gray-600">메뉴를 로드하는 중...</p>
    </div>
  </div>
{:else}
  <div class="min-h-screen bg-gray-50">
    <Header 
      title="영업정보시스템"
      subtitle="본사 및 매장 운영 관리"
      backgroundColor="bg-blue-600"
    />
    
    <div class="flex h-screen pt-16">
      <Sidebar 
        menus={businessMenus}
        backgroundColor="bg-blue-50"
        borderColor="border-blue-200"
        textColor="text-blue-800"
        hoverColor="hover:bg-blue-100"
      />
      
      <main class="flex-1 ml-64 overflow-hidden">
        <TabContainer>
          <slot />
        </TabContainer>
      </main>
    </div>
  </div>
{/if}
