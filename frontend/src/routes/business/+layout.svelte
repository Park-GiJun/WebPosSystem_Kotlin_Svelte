<script>
  import { authStore } from '$lib/stores/auth.js';
  import { goto } from '$app/navigation';
  import { onMount } from 'svelte';
  import Header from '$lib/components/Layout/Header.svelte';
  import BusinessSidebar from '$lib/components/Layout/BusinessSidebar.svelte';
  import BusinessTabContainer from '$lib/components/Layout/BusinessTabContainer.svelte';
  import { tabStore } from '$lib/stores/tabs.js';

  let businessTabs = [];
  let activeTabId = null;
  let loading = true;

  onMount(async () => {
    // 영업정보시스템 접근 권한 확인
    const unsubscribe = authStore.subscribe(async auth => {
      if (auth.isAuthenticated) {
        const hasBusinessAccess = auth.user?.roles?.some(role => 
          ['SUPER_ADMIN', 'SYSTEM_ADMIN', 'HQ_MANAGER'].includes(role)
        );
        
        if (!hasBusinessAccess) {
          goto('/dashboard');
          return;
        }
        loading = false;
      }
    });

    // 탭 스토어 구독
    const tabUnsubscribe = tabStore.subscribe(tabs => {
      businessTabs = tabs.filter(tab => tab.system === 'business');
      activeTabId = tabs.find(tab => tab.active && tab.system === 'business')?.id || null;
    });

    return () => {
      unsubscribe();
      tabUnsubscribe();
    };
  });

  function handleMenuClick(menu) {
    tabStore.openTab({
      id: `business-${menu.menuPath}`,
      title: menu.menuName,
      path: menu.menuPath,
      system: 'business',
      closeable: true,
      starred: false,
      modified: false
    });
  }

  function handleTabSwitch(event) {
    tabStore.setActiveTab(event.detail.tabId);
    goto(businessTabs.find(tab => tab.id === event.detail.tabId)?.path || '/business');
  }

  function handleTabClose(event) {
    tabStore.closeTab(event.detail.tabId);
  }

  function handleTabStar(event) {
    tabStore.toggleStarTab(event.detail.tabId);
  }

  function handleNewTab() {
    tabStore.openTab({
      id: `business-new-${Date.now()}`,
      title: '새 문서',
      path: '/business/new',
      system: 'business',
      closeable: true,
      starred: false,
      modified: true
    });
  }

  function handleCloseAllTabs() {
    businessTabs.forEach(tab => {
      if (tab.closeable) {
        tabStore.closeTab(tab.id);
      }
    });
  }
</script>

{#if loading}
  <div class="min-h-screen bg-gradient-to-br from-blue-50 to-blue-100 flex items-center justify-center">
    <div class="text-center">
      <div class="animate-spin rounded-full h-16 w-16 border-4 border-blue-200 border-t-blue-600 mx-auto"></div>
      <p class="mt-4 text-blue-700 font-medium">영업정보시스템 로드 중...</p>
    </div>
  </div>
{:else}
  <div class="min-h-screen bg-gray-50">
    <Header 
      title="영업정보시스템"
      subtitle="본사 및 매장 운영 통합 관리"
      backgroundColor="bg-gradient-to-r from-blue-600 to-blue-700"
      textColor="text-white"
    />
    
    <div class="flex h-screen pt-16">
      <BusinessSidebar on:menu-click={handleMenuClick} />
      
      <main class="flex-1 flex flex-col overflow-hidden">
        <BusinessTabContainer 
          tabs={businessTabs}
          {activeTabId}
          on:tab-switch={handleTabSwitch}
          on:tab-close={handleTabClose}
          on:tab-star={handleTabStar}
          on:new-tab={handleNewTab}
          on:close-all-tabs={handleCloseAllTabs}
        />
        
        <div class="flex-1 overflow-auto bg-white">
          <slot />
        </div>
      </main>
    </div>
  </div>
{/if}
