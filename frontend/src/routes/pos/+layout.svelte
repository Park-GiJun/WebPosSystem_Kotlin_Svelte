<script>
  import { authStore } from '$lib/stores/auth.js';
  import { goto } from '$app/navigation';
  import { onMount } from 'svelte';
  import Header from '$lib/components/Layout/Header.svelte';
  import PosSidebar from '$lib/components/Layout/PosSidebar.svelte';
  import PosTabContainer from '$lib/components/Layout/PosTabContainer.svelte';
  import { tabStore } from '$lib/stores/tabs.js';

  let posTabs = [];
  let activeTabId = null;
  let loading = true;

  onMount(async () => {
    // POS 시스템 접근 권한 확인
    const unsubscribe = authStore.subscribe(async auth => {
      if (auth.isAuthenticated) {
        const hasPosAccess = auth.user?.roles?.some(role => 
          ['SUPER_ADMIN', 'SYSTEM_ADMIN', 'STORE_MANAGER', 'USER'].includes(role)
        );
        
        if (!hasPosAccess) {
          goto('/dashboard');
          return;
        }
        loading = false;
      }
    });

    // 탭 스토어 구독
    const tabUnsubscribe = tabStore.subscribe(tabs => {
      posTabs = tabs.filter(tab => tab.system === 'pos');
      activeTabId = tabs.find(tab => tab.active && tab.system === 'pos')?.id || null;
    });

    return () => {
      unsubscribe();
      tabUnsubscribe();
    };
  });

  function handleMenuClick(menu) {
    tabStore.openTab({
      id: `pos-${menu.menuPath}`,
      title: menu.menuName,
      path: menu.menuPath,
      system: 'pos',
      closeable: true,
      orderCount: menu.menuCode === 'POS_SALES' ? Math.floor(Math.random() * 5) + 1 : 0,
      timestamp: Date.now(),
      printable: menu.menuCode === 'POS_SALES'
    });
  }

  function handleTabSwitch(event) {
    tabStore.setActiveTab(event.detail.tabId);
    goto(posTabs.find(tab => tab.id === event.detail.tabId)?.path || '/pos');
  }

  function handleTabClose(event) {
    tabStore.closeTab(event.detail.tabId);
  }

  function handleTabPrint(event) {
    // POS 탭 인쇄 기능
    console.log('인쇄:', event.detail.tabId);
    // 실제 인쇄 로직 구현
  }
</script>

{#if loading}
  <div class="min-h-screen bg-gradient-to-br from-green-50 to-green-100 flex items-center justify-center">
    <div class="text-center">
      <div class="animate-spin rounded-full h-16 w-16 border-4 border-green-200 border-t-green-600 mx-auto"></div>
      <p class="mt-4 text-green-700 font-medium">POS 시스템 로드 중...</p>
    </div>
  </div>
{:else}
  <div class="min-h-screen bg-gradient-to-br from-green-50 to-green-100">
    <Header 
      title="POS 시스템"
      subtitle="매장 운영 및 판매 관리"
      backgroundColor="bg-gradient-to-r from-green-600 to-green-700"
      textColor="text-white"
    />
    
    <div class="flex h-screen pt-16">
      <PosSidebar on:menu-click={handleMenuClick} />
      
      <main class="flex-1 flex flex-col overflow-hidden">
        <PosTabContainer 
          tabs={posTabs}
          {activeTabId}
          on:tab-switch={handleTabSwitch}
          on:tab-close={handleTabClose}
          on:tab-print={handleTabPrint}
        />
        
        <div class="flex-1 overflow-auto bg-white">
          <slot />
        </div>
      </main>
    </div>
  </div>
{/if}
