<script>
  import { goto } from '$app/navigation';
  import { onMount } from 'svelte';
  import { authStore } from '$lib/stores/auth.js';
  import { tabStore } from '$lib/stores/tabs.js';
  import Sidebar from '$lib/components/Layout/Sidebar.svelte';
  import Header from '$lib/components/Layout/Header.svelte';
  import TabContainer from '$lib/components/Layout/TabContainer.svelte';

  $: tabs = $tabStore.tabs;
  $: activeTabId = $tabStore.activeTabId;

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
    
    // 탭 추가
    tabStore.addTab({
      id: menu.menuCode,
      title: menu.menuName,
      path: menu.menuPath,
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
    tabStore.removeTab(tabId);
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
