<script>
  import { authStore } from '$lib/stores/auth.js';
  import { menuStore } from '$lib/stores/menu.js';
  import { goto } from '$app/navigation';
  import { onMount } from 'svelte';
  import Header from '$lib/components/Layout/Header.svelte';
  import Sidebar from '$lib/components/Layout/Sidebar.svelte';
  import TabContainer from '$lib/components/Layout/TabContainer.svelte';

  let posMenus = [];
  let loading = true;

  onMount(async () => {
    // POS 시스템 접근 권한 확인
    const unsubscribe = authStore.subscribe(async auth => {
      if (auth.isAuthenticated) {
        // 매장 관련 권한 확인 또는 메뉴 권한 확인
        const hasPosAccess = auth.user?.roles?.some(role => 
          ['SUPER_ADMIN', 'SYSTEM_ADMIN', 'STORE_MANAGER', 'USER'].includes(role)
        );
        
        if (!hasPosAccess) {
          // 메뉴별 권한도 확인
          const hasMenuPermission = await menuStore.hasPermission('POS', 'READ');
          if (!hasMenuPermission) {
            goto('/dashboard');
            return;
          }
        }

        // 메뉴 로드
        if (!$menuStore.menus.length) {
          await menuStore.loadMenus();
        }
        
        // POS 시스템 메뉴 가져오기
        try {
          const systemMenus = await menuStore.getSystemMenus('POS');
          posMenus = systemMenus.map(menu => ({
            id: menu.menuCode.toLowerCase().replace('pos_', ''),
            title: menu.menuName,
            icon: menu.iconName || 'computer',
            path: menu.menuPath,
            description: getMenuDescription(menu.menuCode)
          }));
          loading = false;
        } catch (error) {
          console.error('Failed to load POS menus:', error);
          loading = false;
        }
      }
    });

    return unsubscribe;
  });

  function getMenuDescription(menuCode) {
    const descriptions = {
      'POS_SALES': '상품 판매 및 결제 처리',
      'POS_SYSTEM': 'POS 시스템 환경 설정',
      'POS_STAFF': '매장 직원 및 권한 관리'
    };
    return descriptions[menuCode] || '';
  }
</script>

{#if loading}
  <div class="min-h-screen bg-gray-50 flex items-center justify-center">
    <div class="text-center">
      <div class="animate-spin rounded-full h-12 w-12 border-b-2 border-green-600 mx-auto"></div>
      <p class="mt-4 text-gray-600">메뉴를 로드하는 중...</p>
    </div>
  </div>
{:else}
  <div class="min-h-screen bg-gray-50">
    <Header 
      title="POS 시스템"
      subtitle="매장 운영 및 판매 관리"
      backgroundColor="bg-green-600"
    />
    
    <div class="flex h-screen pt-16">
      <Sidebar 
        menus={posMenus}
        backgroundColor="bg-green-50"
        borderColor="border-green-200"
        textColor="text-green-800"
        hoverColor="hover:bg-green-100"
      />
      
      <main class="flex-1 ml-64 overflow-hidden">
        <TabContainer>
          <slot />
        </TabContainer>
      </main>
    </div>
  </div>
{/if}
