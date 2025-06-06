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
    const targetTab = posTabs.find(tab => tab.id === event.detail.tabId);
    if (targetTab) {
      tabStore.setActiveTab(event.detail.tabId);
      goto(targetTab.path);
    }
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
  <div class="min-h-screen bg-gradient-to-br from-emerald-50 via-green-50 to-teal-50 flex items-center justify-center relative overflow-hidden">
    <!-- 배경 애니메이션 -->
    <div class="absolute inset-0">
      <div class="absolute top-1/4 left-1/4 w-72 h-72 bg-emerald-200 rounded-full mix-blend-multiply filter blur-xl animate-pulse"></div>
      <div class="absolute top-1/3 right-1/4 w-72 h-72 bg-green-200 rounded-full mix-blend-multiply filter blur-xl animate-pulse animation-delay-2000"></div>
      <div class="absolute bottom-1/4 left-1/3 w-72 h-72 bg-teal-200 rounded-full mix-blend-multiply filter blur-xl animate-pulse animation-delay-4000"></div>
    </div>
    
    <div class="text-center relative z-10">
      <div class="relative">
        <div class="animate-spin rounded-full h-20 w-20 border-4 border-emerald-200 border-t-emerald-600 mx-auto shadow-lg"></div>
        <div class="absolute inset-0 animate-ping rounded-full h-20 w-20 border-2 border-emerald-300 opacity-20 mx-auto"></div>
      </div>
      <div class="mt-6 space-y-2">
        <p class="text-xl font-bold text-emerald-700">POS 시스템</p>
        <p class="text-emerald-600 font-medium">매장 관리 시스템을 준비하고 있습니다...</p>
        <div class="flex items-center justify-center space-x-1 mt-4">
          <div class="w-2 h-2 bg-emerald-500 rounded-full animate-bounce"></div>
          <div class="w-2 h-2 bg-emerald-500 rounded-full animate-bounce animation-delay-200"></div>
          <div class="w-2 h-2 bg-emerald-500 rounded-full animate-bounce animation-delay-400"></div>
        </div>
      </div>
    </div>
  </div>
{:else}
  <div class="min-h-screen bg-gradient-to-br from-emerald-50 via-green-25 to-teal-50 relative overflow-hidden">
    <!-- 배경 패턴 -->
    <div class="absolute inset-0 opacity-40" style="background-image: url('data:image/svg+xml,%3Csvg width=&quot;100&quot; height=&quot;100&quot; viewBox=&quot;0 0 100 100&quot; xmlns=&quot;http://www.w3.org/2000/svg&quot;%3E%3Cg fill-rule=&quot;evenodd&quot;%3E%3Cg fill=&quot;%2310b981&quot; fill-opacity=&quot;0.03&quot;%3E%3Cpath d=&quot;M50 50c0-5.5 4.5-10 10-10s10 4.5 10 10-4.5 10-10 10-10-4.5-10-10zm-20 0c0-5.5 4.5-10 10-10s10 4.5 10 10-4.5 10-10 10-10-4.5-10-10z&quot;/%3E%3C/g%3E%3C/g%3E%3C/svg%3E')"></div>
    
    <!-- 그라디언트 오버레이 -->
    <div class="absolute inset-0 bg-gradient-to-tr from-emerald-500/5 via-transparent to-teal-500/5"></div>
    <Header 
      title="POS 시스템"
      subtitle="매장 운영 및 판매 관리"
      systemType="pos"
      showNotificationsMenu={true}
    />
    
    <div class="flex min-h-screen">
      <PosSidebar on:menu-click={handleMenuClick} />
      
      <main class="flex-1 flex flex-col">
        <PosTabContainer 
          tabs={posTabs}
          {activeTabId}
          on:tab-switch={handleTabSwitch}
          on:tab-close={handleTabClose}
          on:tab-print={handleTabPrint}
        />
        
        <div class="flex-1 bg-white/80 backdrop-blur-sm border-l border-emerald-200/50 shadow-inner relative">
          <!-- 메인 콘텐츠 배경 패턴 -->
          <div class="absolute inset-0 bg-gradient-to-br from-white via-emerald-50/30 to-teal-50/50"></div>
          <div class="relative z-10 p-6">
            <slot />
          </div>
        </div>
      </main>
    </div>
  </div>
{/if}
