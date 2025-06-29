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
  let isMobile = false;
  let sidebarOpen = false;

  onMount(async () => {
    // 모바일 감지
    const checkMobile = () => {
      isMobile = window.innerWidth < 768;
    };
    
    checkMobile();
    window.addEventListener('resize', checkMobile);

    // 관리자 권한 확인
    const unsubscribe = authStore.subscribe(async auth => {
      if (auth.isAuthenticated) {
        if (!auth.user?.roles?.includes('SUPER_ADMIN') && !auth.user?.roles?.includes('SYSTEM_ADMIN')) {
          // 권한이 없으면 시스템 선택으로 리다이렉트
          console.log('❌ Admin 시스템 접근 권한 없음:', auth.user?.roles);
          goto('/system-select');
          return;
        }
        console.log('✅ Admin 시스템 접근 허용:', auth.user?.roles);
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
      window.removeEventListener('resize', checkMobile);
    };
  });

  function handleMenuClick(event) {
    const menu = event.detail;
    console.log('🔐 Admin 레이아웃에서 메뉴 클릭 처리:', menu);
    
    // 새로운 메뉴 구조에 맞춰 처리
    if (menu && (menu.menu_type !== 'CATEGORY' || menu.menuType !== 'CATEGORY')) {
      const tabData = {
        id: `admin-${menu.tabId || menu.menu_code || menu.menuCode || 'unknown'}`,
        title: menu.title || menu.menu_name || menu.menuName || 'Unknown Menu',
        path: menu.path || menu.menu_path || menu.menuPath || '/admin',
        system: 'admin',
        closeable: true,
        secure: true,
        priority: (menu.menu_code || menu.menuCode || '').includes('USERS') ? 'HIGH' : 'MEDIUM'
      };
      
      console.log('🔐 생성될 탭 데이터:', tabData);
      
      tabStore.openTab(tabData);
      
      // 페이지 이동
      goto(tabData.path);
      
      // 모바일에서는 메뉴 클릭 시 사이드바 닫기
      if (isMobile) {
        sidebarOpen = false;
      }
    }
  }

  function handleTabSwitch(event) {
    const targetTab = adminTabs.find(tab => tab.id === event.detail.tabId);
    if (targetTab) {
      console.log('🔐 Admin 탭 스위치:', {
        targetTab,
        currentActiveTab: activeTabId,
        allAdminTabs: adminTabs
      });
      
      // 먼저 탭을 활성화
      tabStore.setActiveTab(event.detail.tabId);
      
      // 그 다음 페이지 이동
      goto(targetTab.path);
    }
  }

  function handleTabClose(event) {
    console.log('🗑️ Admin 레이아웃에서 탭 닫기 처리:', event.detail.tabId);
    tabStore.closeTab(event.detail.tabId);
  }

  function handleCloseAllTabs() {
    tabStore.closeSystemCloseableTabs('admin');
  }

  function toggleSidebar() {
    sidebarOpen = !sidebarOpen;
  }

  function closeSidebar() {
    sidebarOpen = false;
  }
</script>

{#if loading}
  <div class="min-h-screen bg-gradient-to-br from-red-50 via-rose-50 to-pink-50 flex items-center justify-center relative overflow-hidden">
    <!-- 배경 애니메이션 -->
    <div class="absolute inset-0">
      <div class="absolute top-1/4 left-1/4 w-72 h-72 bg-red-200 rounded-full mix-blend-multiply filter blur-xl animate-pulse"></div>
      <div class="absolute top-1/3 right-1/4 w-72 h-72 bg-rose-200 rounded-full mix-blend-multiply filter blur-xl animate-pulse animation-delay-2000"></div>
      <div class="absolute bottom-1/4 left-1/3 w-72 h-72 bg-pink-200 rounded-full mix-blend-multiply filter blur-xl animate-pulse animation-delay-4000"></div>
    </div>
    
    <div class="text-center relative z-10">
      <div class="relative">
        <div class="animate-spin rounded-full h-20 w-20 border-4 border-red-200 border-t-red-600 mx-auto shadow-lg"></div>
        <div class="absolute inset-0 animate-ping rounded-full h-20 w-20 border-2 border-red-300 opacity-20 mx-auto"></div>
      </div>
      <div class="mt-6 space-y-2">
        <p class="text-xl font-bold text-red-700">관리자 시스템</p>
        <p class="text-red-600 font-medium">시스템을 초기화하고 있습니다...</p>
        <div class="flex items-center justify-center space-x-1 mt-4">
          <div class="w-2 h-2 bg-red-500 rounded-full animate-bounce"></div>
          <div class="w-2 h-2 bg-red-500 rounded-full animate-bounce animation-delay-200"></div>
          <div class="w-2 h-2 bg-red-500 rounded-full animate-bounce animation-delay-400"></div>
        </div>
      </div>
    </div>
  </div>
{:else}
  <div class="min-h-screen bg-gradient-to-br from-red-50 via-red-25 to-rose-50 relative overflow-hidden">
    <!-- 배경 패턴 -->
    <div class="absolute inset-0 opacity-40" style="background-image: url('data:image/svg+xml,%3Csvg width=&quot;100&quot; height=&quot;100&quot; viewBox=&quot;0 0 100 100&quot; xmlns=&quot;http://www.w3.org/2000/svg&quot;%3E%3Cg fill-rule=&quot;evenodd&quot;%3E%3Cg fill=&quot;%23dc2626&quot; fill-opacity=&quot;0.03&quot;%3E%3Cpath d=&quot;M50 50c0-5.5 4.5-10 10-10s10 4.5 10 10-4.5 10-10 10-10-4.5-10-10zm-20 0c0-5.5 4.5-10 10-10s10 4.5 10 10-4.5 10-10 10-10-4.5-10-10z&quot;/%3E%3C/g%3E%3C/g%3E%3C/svg%3E')"></div>
    
    <!-- 그라디언트 오버레이 -->
    <div class="absolute inset-0 bg-gradient-to-tr from-red-500/5 via-transparent to-rose-500/5"></div>
    
    <!-- 모바일 오버레이 -->
    {#if isMobile}
      <div class="mobile-overlay {sidebarOpen ? 'open' : ''}" on:click={closeSidebar} role="button" tabindex="-1"></div>
    {/if}
    
    <div class="relative z-0 flex flex-col h-screen">
      <Header 
        title="관리자 시스템"
        subtitle="시스템 전체 관리 및 보안"
        systemType="admin"
        showNotificationsMenu={true}
        {isMobile}
        on:toggle-sidebar={toggleSidebar}
      />
      
      <div class="flex flex-1 min-h-0">
        {#if isMobile}
          <!-- 모바일 사이드바 -->
          <div class="mobile-sidebar {sidebarOpen ? 'open' : ''}" style="background: linear-gradient(to bottom, rgb(220, 38, 38), rgb(185, 28, 28), rgb(153, 27, 27));">
            <AdminSidebar {isMobile} on:menu-click={handleMenuClick} on:close-sidebar={closeSidebar} />
          </div>
        {:else}
          <!-- 데스크톱 사이드바 -->
          <AdminSidebar on:menu-click={handleMenuClick} />
        {/if}
        
        <main class="flex-1 flex flex-col min-h-0 {isMobile ? 'w-full' : ''}">
          <AdminTabContainer 
            tabs={adminTabs}
            {activeTabId}
            {isMobile}
            on:tab-switch={handleTabSwitch}
            on:tab-close={handleTabClose}
            on:close-all-tabs={handleCloseAllTabs}
          />
          
          <div class="flex-1 bg-white/80 backdrop-blur-sm border-l border-red-200/50 shadow-inner relative min-h-0 {isMobile ? 'border-l-0' : ''}">
            <!-- 메인 콘텐츠 배경 패턴 -->
            <div class="absolute inset-0 bg-gradient-to-br from-white via-red-50/30 to-rose-50/50"></div>
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
