<script>
  import { page } from '$app/stores';
  import { authStore } from '$lib/stores/auth.js';
  import { createEventDispatcher } from 'svelte';
  import { 
    ShieldCheck, 
    Building, 
    Monitor, 
    Users, 
    Key, 
    Settings, 
    FileText,
    Building2,
    Store,
    BarChart3,
    Package,
    ShoppingCart,
    UserGroup,
    Sliders
  } from 'lucide-svelte';

  const dispatch = createEventDispatcher();

  $: currentPath = $page.url.pathname;
  $: user = $authStore.user;
  $: menus = $authStore.menus || [];

  // 아이콘 매핑
  const iconMap = {
    'shield': ShieldCheck,
    'building-office': Building,
    'computer-desktop': Monitor,
    'users': Users,
    'key': Key,
    'cog': Settings,
    'document-text': FileText,
    'building-office-2': Building2,
    'building-storefront': Store,
    'chart-bar': BarChart3,
    'cube': Package,
    'shopping-cart': ShoppingCart,
    'user-group': UserGroup,
    'adjustments': Sliders
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

<aside class="w-64 bg-white shadow-sm border-r border-gray-200 h-full">
  <!-- 로고 영역 -->
  <div class="p-6 border-b border-gray-200">
    <h1 class="text-xl font-bold text-gray-900">WebPos</h1>
    {#if user}
      <p class="text-sm text-gray-600 mt-1">{user.username}</p>
      <div class="flex flex-wrap gap-1 mt-2">
        {#each user.roles || [] as role}
          <span class="inline-flex items-center px-2 py-1 rounded-full text-xs font-medium bg-primary-100 text-primary-800">
            {role}
          </span>
        {/each}
      </div>
    {/if}
  </div>

  <!-- 메뉴 영역 -->
  <nav class="p-4 space-y-2">
    {#each organizedMenus as menu}
      <div class="menu-group">
        {#if menu.menuType === 'CATEGORY'}
          <!-- 카테고리 메뉴 -->
          <div class="mb-4">
            <div class="flex items-center px-4 py-2 text-xs font-semibold text-gray-500 uppercase tracking-wider">
              {#if menu.iconName && iconMap[menu.iconName]}
                <svelte:component this={iconMap[menu.iconName]} size="16" class="mr-2" />
              {/if}
              {menu.menuName}
            </div>
            
            {#if menu.children && menu.children.length > 0}
              <div class="mt-2 space-y-1">
                {#each menu.children as subMenu}
                  <button
                    type="button"
                    class="w-full sidebar-item"
                    class:active={isActive(subMenu.menuPath) || hasActiveChild(subMenu)}
                    on:click={() => handleMenuClick(subMenu)}
                  >
                    {#if subMenu.iconName && iconMap[subMenu.iconName]}
                      <svelte:component this={iconMap[subMenu.iconName]} size="18" class="mr-3" />
                    {/if}
                    <span>{subMenu.menuName}</span>
                  </button>
                  
                  <!-- 3레벨 메뉴가 있는 경우 -->
                  {#if subMenu.children && subMenu.children.length > 0 && (isActive(subMenu.menuPath) || hasActiveChild(subMenu))}
                    <div class="ml-6 mt-1 space-y-1 border-l border-gray-200 pl-4">
                      {#each subMenu.children as subSubMenu}
                        <button
                          type="button"
                          class="w-full text-left px-3 py-2 text-sm text-gray-600 hover:text-gray-900 hover:bg-gray-50 rounded-md transition-colors duration-200"
                          class:text-primary-600={isActive(subSubMenu.menuPath)}
                          class:bg-primary-50={isActive(subSubMenu.menuPath)}
                          on:click={() => handleMenuClick(subSubMenu)}
                        >
                          {subSubMenu.menuName}
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
            class="w-full sidebar-item"
            class:active={isActive(menu.menuPath)}
            on:click={() => handleMenuClick(menu)}
          >
            {#if menu.iconName && iconMap[menu.iconName]}
              <svelte:component this={iconMap[menu.iconName]} size="18" class="mr-3" />
            {/if}
            <span>{menu.menuName}</span>
          </button>
        {/if}
      </div>
    {/each}
  </nav>
</aside>
