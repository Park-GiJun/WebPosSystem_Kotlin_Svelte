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
  let isMobile = false;
  let sidebarOpen = false;

  onMount(async () => {
    // 모바일 감지
    const checkMobile = () => {
      isMobile = window.innerWidth < 768;
    };
    
    checkMobile();
    window.addEventListener('resize', checkMobile);

    // POS 시스템 접근 권한 확인
    const unsubscribe = authStore.subscribe(async auth => {
      if (auth.isAuthenticated) {
        const hasPosAccess = auth.user?.roles?.some(role => 
          ['SUPER_ADMIN', 'SYSTEM_ADMIN', 'STORE_MANAGER', 'USER'].includes(role)
        );
        
        if (!hasPosAccess) {
          console.log('❌ POS 시스템 접근 권한 없음:', auth.user?.roles);
          goto('/system-select');
          return;
        }
        console.log('✅ POS 시스템 접근 허용:', auth.user?.roles);
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
      window.removeEventListener('resize', checkMobile);
    };
  });

  function handleMenuClick(event) {
    const menu = event.detail;
    console.log('🛒 POS 레이아웃에서 메뉴 클릭 처리:', {
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
        id: `pos-${menu.menuCode || 'unknown'}`,
        title: menu.menuName || 'Unknown Menu',
        path: menu.menuPath || '/pos',
        system: 'pos',
        closeable: true,
        orderCount: menu.menuCode === 'POS_SALES' ? Math.floor(Math.random() * 5) + 1 : 0,
        timestamp: Date.now(),
        printable: menu.menuCode === 'POS_SALES'
      };
      
      console.log('🛒 생성될 탭 데이터:', tabData);
      
      tabStore.openTab(tabData);
      
      // 모바일에서는 메뉴 클릭 시 사이드바 닫기
      if (isMobile) {
        sidebarOpen = false;
      }
    }
  }

  function handleTabSwitch(event) {
    const targetTab = posTabs.find(tab => tab.id === event.detail.tabId);
    if (targetTab) {
      tabStore.setActiveTab(event.detail.tabId);
      goto(targetTab.path);
    }
  }

  function handleTabClose(event) {
    console.log('🗑️ POS 레이아웃에서 탭 닫기 처리:', event.detail.tabId);
    tabStore.closeTab(event.detail.tabId);
  }

  function handleTabPrint(event) {
    // POS 탭 인쇄 기능
    console.log('인쇄:', event.detail.tabId);
    // 실제 인쇄 로직 구현
  }

  function toggleSidebar() {
    sidebarOpen = !sidebarOpen;
  }

  function closeSidebar() {
    sidebarOpen = false;
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
    
    <!-- 모바일 오버레이 -->
    {#if isMobile}
      <div class="mobile-overlay {sidebarOpen ? 'open' : ''}" on:click={closeSidebar} role="button" tabindex="-1"></div>
    {/if}
    
    <div class="relative z-0 flex flex-col h-screen">
      <Header 
        title="POS 시스템"
        subtitle="매장 운영 및 판매 관리"
        systemType="pos"
        showNotificationsMenu={true}
        {isMobile}
        on:toggle-sidebar={toggleSidebar}
      />
      
      <div class="flex flex-1 min-h-0">
        {#if isMobile}
          <!-- 모바일 사이드바 -->
          <div class="mobile-sidebar {sidebarOpen ? 'open' : ''}" style="background: linear-gradient(to bottom, rgb(22, 163, 74), rgb(21, 128, 61), rgb(22, 101, 52));">
            <PosSidebar {isMobile} on:menu-click={handleMenuClick} on:close-sidebar={closeSidebar} />
          </div>
        {:else}
          <!-- 데스크톱 사이드바 -->
          <PosSidebar on:menu-click={handleMenuClick} />
        {/if}
        
        <main class="flex-1 flex flex-col min-h-0 {isMobile ? 'w-full' : ''}">
          <PosTabContainer 
            tabs={posTabs}
            {activeTabId}
            {isMobile}
            on:tab-switch={handleTabSwitch}
            on:tab-close={handleTabClose}
            on:tab-print={handleTabPrint}
          />
          
          <div class="flex-1 bg-white/80 backdrop-blur-sm border-l border-emerald-200/50 shadow-inner relative min-h-0 {isMobile ? 'border-l-0' : ''}">
            <!-- 메인 콘텐츠 배경 패턴 -->
            <div class="absolute inset-0 bg-gradient-to-br from-white via-emerald-50/30 to-teal-50/50"></div>
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
