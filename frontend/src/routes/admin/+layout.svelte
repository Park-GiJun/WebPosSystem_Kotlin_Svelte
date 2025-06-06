<script>
  import { authStore } from '$lib/stores/auth.js';
  import { goto } from '$app/navigation';
  import { onMount } from 'svelte';
  import Header from '$lib/components/Layout/Header.svelte';
  import AdminSidebar from '$lib/components/Layout/AdminSidebar.svelte';
  import AdminTabContainer from '$lib/components/Layout/AdminTabContainer.svelte';
  import { tabStore } from '$lib/stores/tabs.js';

  let adminTabs = [];
  let activeTabId = null;
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
        loading = false;
      }
    });

    // 탭 스토어 구독
    const tabUnsubscribe = tabStore.subscribe(tabs => {
      adminTabs = tabs.filter(tab => tab.system === 'admin');
      activeTabId = tabs.find(tab => tab.active && tab.system === 'admin')?.id || null;
    });

    return () => {
      unsubscribe();
      tabUnsubscribe();
    };
  });

  function handleMenuClick(menu) {
    tabStore.openTab({
      id: `admin-${menu.menuPath}`,
      title: menu.menuName,
      path: menu.menuPath,
      system: 'admin',
      closeable: true,
      secure: true,
      priority: menu.menuCode.includes('USERS') ? 'HIGH' : 'MEDIUM'
    });
  }

  function handleTabSwitch(event) {
    tabStore.setActiveTab(event.detail.tabId);
    goto(adminTabs.find(tab => tab.id === event.detail.tabId)?.path || '/admin');
  }

  function handleTabClose(event) {
    tabStore.closeTab(event.detail.tabId);
  }
</script>

{#if loading}
  <div class="min-h-screen bg-gradient-to-br from-red-50 to-red-100 flex items-center justify-center">
    <div class="text-center">
      <div class="animate-spin rounded-full h-16 w-16 border-4 border-red-200 border-t-red-600 mx-auto"></div>
      <p class="mt-4 text-red-700 font-medium">슈퍼어드민 시스템 로드 중...</p>
    </div>
  </div>
{:else}
  <div class="min-h-screen bg-gradient-to-br from-red-50 to-red-100">
    <Header 
      title="슈퍼어드민 시스템"
      subtitle="시스템 전체 관리 및 보안"
      backgroundColor="bg-gradient-to-r from-red-600 to-red-700"
      textColor="text-white"
    />
    
    <div class="flex h-screen pt-16">
      <AdminSidebar on:menu-click={handleMenuClick} />
      
      <main class="flex-1 flex flex-col overflow-hidden">
        <AdminTabContainer 
          {adminTabs}
          {activeTabId}
          on:tab-switch={handleTabSwitch}
          on:tab-close={handleTabClose}
        />
        
        <div class="flex-1 overflow-auto bg-white">
          <slot />
        </div>
      </main>
    </div>
  </div>
{/if}
