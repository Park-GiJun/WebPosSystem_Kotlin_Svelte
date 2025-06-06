<script>
  import { goto } from '$app/navigation';
  import { onMount } from 'svelte';
  import { authStore } from '$lib/stores/auth.js';
  import { tabStore } from '$lib/stores/tabs.js';
  import Sidebar from '$lib/components/Layout/Sidebar.svelte';
  import Header from '$lib/components/Layout/Header.svelte';
  import TabContainer from '$lib/components/Layout/TabContainer.svelte';

  $: tabs = $tabStore;
  $: activeTabId = $tabStore.find(tab => tab.active)?.id;

  onMount(() => {
    // 인증 확인
    const unsubscribe = authStore.subscribe(auth => {
      if (!auth.isAuthenticated) {
        goto('/login');
      }
    });

    return unsubscribe;
  });

  function handleMenuClick(event) {
    const menu = event.detail;
    
    console.log('🎯 Dashboard 메뉴 클릭:', menu);
    
    // 탭 열기 (addTab -> openTab으로 변경)
    tabStore.openTab({
      id: menu.menuCode,
      title: menu.menuName,
      path: menu.menuPath,
      system: 'dashboard',
      closeable: true
    });

    // 페이지 이동
    goto(menu.menuPath);
  }

  function handleTabSwitch(event) {
    const { tabId } = event.detail;
    const tab = tabs.find(t => t.id === tabId);
    if (tab) {
      tabStore.setActiveTab(tabId);
      goto(tab.path);
    }
  }

  function handleTabClose(event) {
    const { tabId } = event.detail;
    tabStore.closeTab(tabId);
  }
</script>

<svelte:head>
  <title>WebPos 시스템</title>
</svelte:head>

<div class="flex h-screen bg-gray-50">
  <!-- 사이드바 -->
  <Sidebar on:menu-click={handleMenuClick} />
  
  <!-- 메인 콘텐츠 영역 -->
  <div class="flex-1 flex flex-col overflow-hidden">
    <!-- 헤더 -->
    <Header />
    
    <!-- 탭 컨테이너 -->
    <TabContainer 
      {tabs} 
      {activeTabId} 
      on:tab-switch={handleTabSwitch}
      on:tab-close={handleTabClose}
    />
    
    <!-- 메인 콘텐츠 -->
    <main class="flex-1 overflow-auto p-6">
      <slot />
    </main>
  </div>
</div>
