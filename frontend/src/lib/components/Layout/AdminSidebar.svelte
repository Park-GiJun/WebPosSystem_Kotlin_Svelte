<script>
  import { page } from '$app/stores';
  import { goto } from '$app/navigation';
  import { authStore } from '$lib/stores/auth.js';
  import { createEventDispatcher } from 'svelte';

  const dispatch = createEventDispatcher();

  $: currentPath = $page.url.pathname;
  $: user = $authStore.user;
  $: menus = $authStore.menus || [];

  // 슈퍼어드민 전용 아이콘 매핑
  const iconMap = {
    'shield': '🛡️',
    'building-office': '🏢',
    'users': '👥',
    'key': '🔑',
    'cog': '⚙️',
    'server': '🖥️',
    'database': '💾',
    'clipboard-document-list': '📋',
    'chart-bar-square': '📊',
    'lock-closed': '🔒'
  };

  // 메뉴를 계층 구조로 정리
  function organizeMenus(menus) {
    const menuMap = new Map();
    const rootMenus = [];

    // 먼저 모든 메뉴를 맵에 저장
    menus.forEach(menu => {
      menuMap.set(menu.menuId, { ...menu, children: [] });
    });

    // 계층 구조 구성
    menus.forEach(menu => {
      const menuItem = menuMap.get(menu.menuId);
      if (menu.parentMenuId) {
        const parent = menuMap.get(menu.parentMenuId);
        if (parent) {
          parent.children.push(menuItem);
        }
      } else {
        rootMenus.push(menuItem);
      }
    });

    // 표시 순서대로 정렬
    const sortMenus = (menus) => {
      return menus.sort((a, b) => a.displayOrder - b.displayOrder)
        .map(menu => ({
          ...menu,
          children: sortMenus(menu.children)
        }));
    };

    return sortMenus(rootMenus);
  }

  $: organizedMenus = organizeMenus(menus);

  function isActive(menuPath) {
    return currentPath === menuPath || currentPath.startsWith(menuPath + '/');
  }

  function hasActiveChild(menu) {
    if (menu.children) {
      return menu.children.some(child => 
        isActive(child.menuPath) || hasActiveChild(child)
      );
    }
    return false;
  }

  function handleMenuClick(menu) {
    dispatch('menu-click', menu);
  }
</script>

<aside class="w-64 bg-gradient-to-b from-red-600 to-red-700 text-white h-full shadow-lg">
  <!-- 로고 영역 -->
  <div class="p-6 border-b border-red-500/30">
    <button 
      type="button"
      class="text-left w-full hover:opacity-75 transition-opacity duration-200"
      on:click={() => goto('/system-select')}
      title="시스템 선택으로 돌아가기"
    >
      <div class="flex items-center">
        <span class="text-2xl mr-3">🛡️</span>
        <div>
          <h1 class="text-xl font-bold text-white">슈퍼어드민</h1>
          <p class="text-xs text-red-200">System Administration</p>
        </div>
      </div>
      {#if user}
        <div class="mt-4 p-3 bg-red-500/20 rounded-lg">
          <p class="text-sm text-red-100 font-medium">{user.username}</p>
          <div class="flex flex-wrap gap-1 mt-2">
            {#each user.roles || [] as role}
              <span class="inline-flex items-center px-2 py-1 rounded-full text-xs font-medium bg-red-200 text-red-800">
                {role}
              </span>
            {/each}
          </div>
        </div>
      {/if}
    </button>
  </div>

  <!-- 메뉴 영역 -->
  <nav class="p-4 space-y-2">
    {#each organizedMenus as menu}
      <div class="menu-group">
        {#if menu.menuType === 'CATEGORY'}
          <!-- 카테고리 메뉴 -->
          <div class="mb-4">
            <div class="flex items-center px-4 py-3 text-xs font-semibold text-red-200 uppercase tracking-wider border-b border-red-500/30">
              {#if menu.iconName && iconMap[menu.iconName]}
                <span class="mr-2 text-lg">{iconMap[menu.iconName]}</span>
              {/if}
              {menu.menuName}
            </div>
            
            {#if menu.children && menu.children.length > 0}
              <div class="mt-3 space-y-1">
                {#each menu.children as subMenu}
                  <button
                    type="button"
                    class="w-full admin-sidebar-item"
                    class:active={isActive(subMenu.menuPath) || hasActiveChild(subMenu)}
                    on:click={() => handleMenuClick(subMenu)}
                  >
                    {#if subMenu.iconName && iconMap[subMenu.iconName]}
                      <span class="mr-3 text-lg">{iconMap[subMenu.iconName]}</span>
                    {/if}
                    <span class="font-medium">{subMenu.menuName}</span>
                    {#if subMenu.children && subMenu.children.length > 0}
                      <span class="ml-auto text-xs">▶</span>
                    {/if}
                  </button>
                  
                  <!-- 3레벨 메뉴가 있는 경우 -->
                  {#if subMenu.children && subMenu.children.length > 0 && (isActive(subMenu.menuPath) || hasActiveChild(subMenu))}
                    <div class="ml-8 mt-2 space-y-1 border-l-2 border-red-400/50 pl-4">
                      {#each subMenu.children as subSubMenu}
                        <button
                          type="button"
                          class="w-full text-left px-3 py-2 text-sm text-red-100 hover:text-white hover:bg-red-500/30 rounded-md transition-colors duration-200 border border-transparent hover:border-red-400/50"
                          class:text-white={isActive(subSubMenu.menuPath)}
                          class:bg-red-500={isActive(subSubMenu.menuPath)}
                          on:click={() => handleMenuClick(subSubMenu)}
                        >
                          • {subSubMenu.menuName}
                        </button>
                      {/each}
                    </div>
                  {/if}
                {/each}
              </div>
            {/if}
          </div>
        {:else}
          <!-- 일반 메뉴 -->
          <button
            type="button"
            class="w-full admin-sidebar-item"
            class:active={isActive(menu.menuPath)}
            on:click={() => handleMenuClick(menu)}
          >
            {#if menu.iconName && iconMap[menu.iconName]}
              <span class="mr-3 text-lg">{iconMap[menu.iconName]}</span>
            {/if}
            <span class="font-medium">{menu.menuName}</span>
          </button>
        {/if}
      </div>
    {/each}
  </nav>

  <!-- 하단 시스템 정보 -->
  <div class="absolute bottom-0 left-0 right-0 p-4 border-t border-red-500/30">
    <div class="text-xs text-red-200">
      <div class="flex items-center justify-between">
        <span>시스템 상태</span>
        <span class="w-2 h-2 bg-green-400 rounded-full animate-pulse"></span>
      </div>
    </div>
  </div>
</aside>

<style>
  .admin-sidebar-item {
    @apply flex items-center w-full px-4 py-3 text-left text-red-100 hover:text-white rounded-lg transition-all duration-200 border border-transparent;
    background-color: transparent;
  }

  .admin-sidebar-item:hover {
    background-color: rgba(239, 68, 68, 0.3);
    border-color: rgba(248, 113, 113, 0.5);
    transform: translateX(2px);
  }

  .admin-sidebar-item.active {
    @apply text-white bg-red-500 border-red-400 shadow-lg;
  }
</style>
