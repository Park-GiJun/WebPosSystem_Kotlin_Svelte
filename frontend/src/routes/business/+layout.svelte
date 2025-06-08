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
          ['SUPER_ADMIN', 'SYSTEM_ADMIN', 'HQ_MANAGER', 'STORE_MANAGER'].includes(role)
        );
        
        if (!hasBusinessAccess) {
          console.log('❌ Business 시스템 접근 권한 없음:', auth.user?.roles);
          goto('/system-select');
          return;
        }
        console.log('✅ Business 시스템 접근 허용:', auth.user?.roles);
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

  function handleMenuClick(event) {
    const menu = event.detail;
    console.log('🏢 Business 레이아웃에서 메뉴 클릭 처리:', {
      fullMenu: menu,
      menuCode: menu?.menuCode,
      menuName: menu?.menuName,
      menuPath: menu?.menuPath,
      menuType: menu?.menuType,
      allProperties: Object.keys(menu || {})
    });
    
    // 카테고리가 아닌 메뉴만 탭으로 생성
    if (menu && menu.menuType !== 'CATEGORY') {
      const tabData = {
        id: `business-${menu.menuCode || 'unknown'}`,
        title: menu.menuName || 'Unknown Menu',
        path: menu.menuPath || '/business',
        system: 'business',
        closeable: true,
        starred: false,
        modified: false
      };
      
      console.log('🏢 생성될 탭 데이터:', tabData);
      
      tabStore.openTab(tabData);
    }
  }

  function handleTabSwitch(event) {
    const targetTab = businessTabs.find(tab => tab.id === event.detail.tabId);
    if (targetTab) {
      tabStore.setActiveTab(event.detail.tabId);
      goto(targetTab.path);
    }
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
    tabStore.closeSystemCloseableTabs('business');
  }
</script>

{#if loading}
  <div class="min-h-screen bg-gradient-to-br from-blue-50 via-indigo-50 to-sky-50 flex items-center justify-center relative overflow-hidden">
    <!-- 배경 애니메이션 -->
    <div class="absolute inset-0">
      <div class="absolute top-1/4 left-1/4 w-72 h-72 bg-blue-200 rounded-full mix-blend-multiply filter blur-xl animate-pulse"></div>
      <div class="absolute top-1/3 right-1/4 w-72 h-72 bg-indigo-200 rounded-full mix-blend-multiply filter blur-xl animate-pulse animation-delay-2000"></div>
      <div class="absolute bottom-1/4 left-1/3 w-72 h-72 bg-sky-200 rounded-full mix-blend-multiply filter blur-xl animate-pulse animation-delay-4000"></div>
    </div>
    
    <div class="text-center relative z-10">
      <div class="relative">
        <div class="animate-spin rounded-full h-20 w-20 border-4 border-blue-200 border-t-blue-600 mx-auto shadow-lg"></div>
        <div class="absolute inset-0 animate-ping rounded-full h-20 w-20 border-2 border-blue-300 opacity-20 mx-auto"></div>
      </div>
      <div class="mt-6 space-y-2">
        <p class="text-xl font-bold text-blue-700">영업정보시스템</p>
        <p class="text-blue-600 font-medium">통합 관리 시스템을 로드하고 있습니다...</p>
        <div class="flex items-center justify-center space-x-1 mt-4">
          <div class="w-2 h-2 bg-blue-500 rounded-full animate-bounce"></div>
          <div class="w-2 h-2 bg-blue-500 rounded-full animate-bounce animation-delay-200"></div>
          <div class="w-2 h-2 bg-blue-500 rounded-full animate-bounce animation-delay-400"></div>
        </div>
      </div>
    </div>
  </div>
{:else}
  <div class="min-h-screen bg-gradient-to-br from-blue-50 via-indigo-25 to-sky-50 relative overflow-hidden">
    <!-- 배경 패턴 -->
    <div class="absolute inset-0 opacity-40" style="background-image: url('data:image/svg+xml,%3Csvg width=&quot;100&quot; height=&quot;100&quot; viewBox=&quot;0 0 100 100&quot; xmlns=&quot;http://www.w3.org/2000/svg&quot;%3E%3Cg fill-rule=&quot;evenodd&quot;%3E%3Cg fill=&quot;%232563eb&quot; fill-opacity=&quot;0.03&quot;%3E%3Cpath d=&quot;M50 50c0-5.5 4.5-10 10-10s10 4.5 10 10-4.5 10-10 10-10-4.5-10-10zm-20 0c0-5.5 4.5-10 10-10s10 4.5 10 10-4.5 10-10 10-10-4.5-10-10z&quot;/%3E%3C/g%3E%3C/g%3E%3C/svg%3E')"></div>
    
    <!-- 그라디언트 오버레이 -->
    <div class="absolute inset-0 bg-gradient-to-tr from-blue-500/5 via-transparent to-sky-500/5"></div>
    
    <div class="relative z-10 flex flex-col h-screen">
      <Header 
        title="영업정보시스템"
        subtitle="본사 및 매장 운영 통합 관리"
        systemType="business"
        showNotificationsMenu={true}
      />
      
      <div class="flex flex-1 min-h-0">
        <BusinessSidebar on:menu-click={handleMenuClick} />
        
        <main class="flex-1 flex flex-col min-h-0">
          <BusinessTabContainer 
            tabs={businessTabs}
            {activeTabId}
            on:tab-switch={handleTabSwitch}
            on:tab-close={handleTabClose}
            on:tab-star={handleTabStar}
            on:new-tab={handleNewTab}
            on:close-all-tabs={handleCloseAllTabs}
          />
          
          <div class="flex-1 bg-white/80 backdrop-blur-sm border-l border-blue-200/50 shadow-inner relative min-h-0">
            <!-- 메인 콘텐츠 배경 패턴 -->
            <div class="absolute inset-0 bg-gradient-to-br from-white via-blue-50/30 to-sky-50/50"></div>
            <div class="relative z-10 h-full overflow-y-auto">
              <div class="p-6 h-full">
                <slot />
              </div>
            </div>
          </div>
        </main>
      </div>
    </div>
  </div>
{/if}
