<script>
  import { page } from '$app/stores';
  import { goto } from '$app/navigation';
  import { authStore } from '$lib/stores/auth.js';
  import { createEventDispatcher } from 'svelte';

  const dispatch = createEventDispatcher();

  $: currentPath = $page.url.pathname;
  $: user = $authStore.user;
  $: menus = $authStore.menus || getDefaultBusinessMenus();

  // 기본 영업정보시스템 메뉴 (메뉴 정보가 로드되지 않았을 때)
  function getDefaultBusinessMenus() {
    return [
      {
        menuId: 'business-stores-cat',
        menuName: '매장 관리',
        menuType: 'CATEGORY',
        iconName: 'building-storefront',
        displayOrder: 1,
        children: [
          {
            menuId: 'business-stores',
            menuName: '매장 현황',
            menuPath: '/business/stores',
            menuCode: 'BUSINESS_STORES',
            iconName: 'building-storefront',
            displayOrder: 1
          }
        ]
      },
      {
        menuId: 'business-system-cat',
        menuName: '시스템 관리',
        menuType: 'CATEGORY',
        iconName: 'cog-6-tooth',
        displayOrder: 2,
        children: [
          {
            menuId: 'business-hq-stores',
            menuName: '본사 매장 관리',
            menuPath: '/business/headquarters/stores',
            menuCode: 'BUSINESS_HEADQUARTERS',
            iconName: 'building-office',
            displayOrder: 1
          },
          {
            menuId: 'business-pos',
            menuName: 'POS 관리',
            menuPath: '/business/pos',
            menuCode: 'BUSINESS_POS',
            iconName: 'computer-desktop',
            displayOrder: 2
          }
        ]
      }
    ];
  }

  // 영업정보시스템 전용 아이콘 매핑
  const iconMap = {
    'building-office': '🏢',
    'building-office-2': '🏬',
    'building-storefront': '🏪',
    'chart-bar': '📊',
    'computer-desktop': '💻',
    'cube': '📦',
    'currency-dollar': '💰',
    'document-text': '📄',
    'users': '👥',
    'cog-6-tooth': '⚙️',
    'map-pin': '📍',
    'truck': '🚚'
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

  function getMenuDescription(menuCode) {
    const descriptions = {
      'BUSINESS_HEADQUARTERS': '본사 운영 정보',
      'BUSINESS_STORES': '매장 관리 및 운영',
      'BUSINESS_POS': 'POS 설정 관리'
    };
    return descriptions[menuCode] || '';
  }
</script>

<aside class="w-72 bg-white border-r-2 border-blue-200 h-full shadow-sm">
  <!-- 로고 영역 -->
  <div class="p-6 bg-gradient-to-r from-blue-50 to-blue-100 border-b-2 border-blue-200">
    <button 
      type="button"
      class="text-left w-full hover:opacity-75 transition-opacity duration-200"
      on:click={() => goto('/system-select')}
      title="시스템 선택으로 돌아가기"
    >
      <div class="flex items-center">
        <span class="text-3xl mr-3">🏢</span>
        <div>
          <h1 class="text-xl font-bold text-blue-900">영업정보시스템</h1>
          <p class="text-sm text-blue-600">Business Management</p>
        </div>
      </div>
      {#if user}
        <div class="mt-4 p-4 bg-white rounded-lg shadow-sm border border-blue-200">
          <p class="text-sm text-blue-900 font-medium">{user.username}</p>
          <div class="flex flex-wrap gap-1 mt-2">
            {#each user.roles || [] as role}
              <span class="inline-flex items-center px-2 py-1 rounded-full text-xs font-medium bg-blue-100 text-blue-800">
                {role}
              </span>
            {/each}
          </div>
        </div>
      {/if}
    </button>
  </div>

  <!-- 대시보드 요약 -->
  <div class="p-4 bg-blue-50 border-b border-blue-200">
    <div class="grid grid-cols-2 gap-3">
      <div class="bg-white p-3 rounded-lg shadow-sm">
        <div class="text-xs text-gray-500">총 매장수</div>
        <div class="text-lg font-bold text-blue-600">24</div>
      </div>
      <div class="bg-white p-3 rounded-lg shadow-sm">
        <div class="text-xs text-gray-500">금일 매출</div>
        <div class="text-lg font-bold text-green-600">₩2.4M</div>
      </div>
    </div>
  </div>

  <!-- 메뉴 영역 -->
  <nav class="p-4 space-y-3">
    {#each organizedMenus as menu}
      <div class="menu-group">
        {#if menu.menuType === 'CATEGORY'}
          <!-- 카테고리 메뉴 -->
          <div class="mb-4">
            <div class="flex items-center px-3 py-2 text-xs font-semibold text-blue-600 uppercase tracking-wider bg-blue-50 rounded-lg">
              {#if menu.iconName && iconMap[menu.iconName]}
                <span class="mr-2 text-base">{iconMap[menu.iconName]}</span>
              {/if}
              {menu.menuName}
            </div>
            
            {#if menu.children && menu.children.length > 0}
              <div class="mt-2 space-y-1">
                {#each menu.children as subMenu}
                  <button
                    type="button"
                    class="w-full business-sidebar-item"
                    class:active={isActive(subMenu.menuPath) || hasActiveChild(subMenu)}
                    on:click={() => handleMenuClick(subMenu)}
                  >
                    {#if subMenu.iconName && iconMap[subMenu.iconName]}
                      <span class="mr-3 text-lg">{iconMap[subMenu.iconName]}</span>
                    {/if}
                    <div class="flex-1 text-left">
                      <div class="font-medium">{subMenu.menuName}</div>
                      <div class="text-xs text-gray-500 mt-1">{getMenuDescription(subMenu.menuCode)}</div>
                    </div>
                    {#if subMenu.children && subMenu.children.length > 0}
                      <span class="text-blue-400">▶</span>
                    {/if}
                  </button>
                  
                  <!-- 3레벨 메뉴가 있는 경우 -->
                  {#if subMenu.children && subMenu.children.length > 0 && (isActive(subMenu.menuPath) || hasActiveChild(subMenu))}
                    <div class="ml-6 mt-2 space-y-1 border-l-2 border-blue-200 pl-4">
                      {#each subMenu.children as subSubMenu}
                        <button
                          type="button"
                          class="w-full text-left px-3 py-2 text-sm text-gray-600 hover:text-blue-700 hover:bg-blue-50 rounded-md transition-colors duration-200"
                          class:text-blue-700={isActive(subSubMenu.menuPath)}
                          class:bg-blue-100={isActive(subSubMenu.menuPath)}
                          on:click={() => handleMenuClick(subSubMenu)}
                        >
                          → {subSubMenu.menuName}
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
            class="w-full business-sidebar-item"
            class:active={isActive(menu.menuPath)}
            on:click={() => handleMenuClick(menu)}
          >
            {#if menu.iconName && iconMap[menu.iconName]}
              <span class="mr-3 text-lg">{iconMap[menu.iconName]}</span>
            {/if}
            <div class="flex-1 text-left">
              <div class="font-medium">{menu.menuName}</div>
              <div class="text-xs text-gray-500 mt-1">{getMenuDescription(menu.menuCode)}</div>
            </div>
          </button>
        {/if}
      </div>
    {/each}
  </nav>
</aside>

<style>
  .business-sidebar-item {
    display: flex;
    align-items: center;
    width: 100%;
    padding: 0.75rem 1rem;
    color: rgb(55 65 81);
    border-radius: 0.5rem;
    transition: all 0.2s ease-in-out;
    border: 1px solid transparent;
  }

  .business-sidebar-item:hover {
    color: rgb(29 78 216);
    background-color: rgb(239 246 255);
    border-color: rgb(191 219 254);
    box-shadow: 0 2px 4px rgba(59, 130, 246, 0.1);
  }

  .business-sidebar-item.active {
    color: rgb(29 78 216);
    background-color: rgb(219 234 254);
    border-color: rgb(191 219 254);
    box-shadow: 0 1px 2px 0 rgba(0, 0, 0, 0.05);
  }
</style>
