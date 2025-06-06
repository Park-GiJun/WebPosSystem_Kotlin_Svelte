<script>
  import { page } from '$app/stores';
  import { goto } from '$app/navigation';
  import { authStore } from '$lib/stores/auth.js';
  import { createEventDispatcher } from 'svelte';

  const dispatch = createEventDispatcher();

  $: currentPath = $page.url.pathname;
  $: user = $authStore.user;
  $: menus = $authStore.menus || getDefaultPosMenus();

  // 기본 POS 메뉴 (메뉴 정보가 로드되지 않았을 때)
  function getDefaultPosMenus() {
    return [
      {
        menuId: 'pos-sales-cat',
        menuName: '판매 관리',
        menuType: 'CATEGORY',
        iconName: 'shopping-cart',
        displayOrder: 1,
        children: [
          {
            menuId: 'pos-sales',
            menuName: '판매',
            menuPath: '/pos/sales',
            menuCode: 'POS_SALES',
            iconName: 'shopping-cart',
            displayOrder: 1
          }
        ]
      },
      {
        menuId: 'pos-system-cat',
        menuName: '시스템 관리',
        menuType: 'CATEGORY',
        iconName: 'cog',
        displayOrder: 2,
        children: [
          {
            menuId: 'pos-system',
            menuName: '시스템 설정',
            menuPath: '/pos/system',
            menuCode: 'POS_SYSTEM',
            iconName: 'computer-desktop',
            displayOrder: 1
          },
          {
            menuId: 'pos-staff',
            menuName: '직원 관리',
            menuPath: '/pos/staff',
            menuCode: 'POS_STAFF',
            iconName: 'users',
            displayOrder: 2
          }
        ]
      }
    ];
  }

  // POS 시스템 전용 아이콘 매핑
  const iconMap = {
    'shopping-cart': '🛒',
    'computer-desktop': '💻',
    'users': '👥',
    'cog': '⚙️',
    'calculator': '🧮',
    'credit-card': '💳',
    'receipt-percent': '🧾',
    'cube': '📦',
    'chart-bar': '📊',
    'printer': '🖨️',
    'banknotes': '💵',
    'clock': '🕐'
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

  // 현재 시간 표시
  let currentTime = new Date();
  setInterval(() => {
    currentTime = new Date();
  }, 1000);
</script>

<aside class="w-60 bg-gradient-to-b from-green-600 via-green-700 to-green-800 text-white h-full">
  <!-- 로고 영역 -->
  <div class="p-4 border-b border-green-500/30">
    <button 
      type="button"
      class="text-left w-full hover:opacity-75 transition-opacity duration-200"
      on:click={() => goto('/system-select')}
      title="시스템 선택으로 돌아가기"
    >
      <div class="flex items-center">
        <span class="text-2xl mr-3">🛒</span>
        <div>
          <h1 class="text-lg font-bold text-white">POS 시스템</h1>
          <p class="text-xs text-green-200">Point of Sale</p>
        </div>
      </div>
    </button>
  </div>

  <!-- 현재 시간 및 상태 -->
  <div class="p-4 bg-green-500/20 border-b border-green-500/30">
    <div class="text-center">
      <div class="text-lg font-mono font-bold text-white">
        {currentTime.toLocaleTimeString('ko-KR')}
      </div>
      <div class="text-xs text-green-200 mt-1">
        {currentTime.toLocaleDateString('ko-KR')}
      </div>
    </div>
    
    {#if user}
      <div class="mt-3 p-2 bg-green-600/30 rounded border border-green-400/50">
        <div class="text-xs text-green-200">근무자</div>
        <div class="text-sm font-medium text-white">{user.username}</div>
        <div class="flex flex-wrap gap-1 mt-1">
          {#each user.roles || [] as role}
            <span class="inline-flex items-center px-1 py-0.5 rounded text-xs font-medium bg-green-300 text-green-800">
              {role}
            </span>
          {/each}
        </div>
      </div>
    {/if}
  </div>

  <!-- 빠른 실행 버튼 -->
  <div class="p-4 border-b border-green-500/30">
    <div class="grid grid-cols-2 gap-2">
      <button class="pos-quick-btn">
        <span class="text-lg">🛒</span>
        <span class="text-xs">판매</span>
      </button>
      <button class="pos-quick-btn">
        <span class="text-lg">💳</span>
        <span class="text-xs">결제</span>
      </button>
    </div>
  </div>

  <!-- 메뉴 영역 -->
  <nav class="p-3 space-y-1 flex-1 overflow-y-auto">
    {#each organizedMenus as menu}
      <div class="menu-group">
        {#if menu.menuType === 'CATEGORY'}
          <!-- 카테고리 메뉴 -->
          <div class="mb-3">
            <div class="flex items-center px-3 py-2 text-xs font-semibold text-green-200 uppercase tracking-wider">
              {#if menu.iconName && iconMap[menu.iconName]}
                <span class="mr-2">{iconMap[menu.iconName]}</span>
              {/if}
              {menu.menuName}
            </div>
            
            {#if menu.children && menu.children.length > 0}
              <div class="space-y-0.5">
                {#each menu.children as subMenu}
                  <button
                    type="button"
                    class="w-full pos-sidebar-item"
                    class:active={isActive(subMenu.menuPath) || hasActiveChild(subMenu)}
                    on:click={() => handleMenuClick(subMenu)}
                  >
                    {#if subMenu.iconName && iconMap[subMenu.iconName]}
                      <span class="mr-2">{iconMap[subMenu.iconName]}</span>
                    {/if}
                    <span class="text-sm font-medium">{subMenu.menuName}</span>
                    {#if subMenu.children && subMenu.children.length > 0}
                      <span class="ml-auto text-xs">▶</span>
                    {/if}
                  </button>
                  
                  <!-- 3레벨 메뉴가 있는 경우 -->
                  {#if subMenu.children && subMenu.children.length > 0 && (isActive(subMenu.menuPath) || hasActiveChild(subMenu))}
                    <div class="ml-6 space-y-0.5 border-l border-green-400/50 pl-3">
                      {#each subMenu.children as subSubMenu}
                        <button
                          type="button"
                          class="w-full text-left px-2 py-1.5 text-xs text-green-100 hover:text-white hover:bg-green-500/30 rounded transition-colors duration-200"
                          class:text-white={isActive(subSubMenu.menuPath)}
                          class:bg-green-500={isActive(subSubMenu.menuPath)}
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
            class="w-full pos-sidebar-item"
            class:active={isActive(menu.menuPath)}
            on:click={() => handleMenuClick(menu)}
          >
            {#if menu.iconName && iconMap[menu.iconName]}
              <span class="mr-2">{iconMap[menu.iconName]}</span>
            {/if}
            <span class="text-sm font-medium">{menu.menuName}</span>
          </button>
        {/if}
      </div>
    {/each}
  </nav>

  <!-- 하단 상태 영역 -->
  <div class="p-3 border-t border-green-500/30 bg-green-600/30">
    <div class="text-xs text-green-200 space-y-1">
      <div class="flex justify-between">
        <span>프린터</span>
        <span class="text-green-300">●</span>
      </div>
      <div class="flex justify-between">
        <span>캐시드로워</span>
        <span class="text-green-300">●</span>
      </div>
      <div class="flex justify-between">
        <span>네트워크</span>
        <span class="text-green-300">●</span>
      </div>
    </div>
  </div>
</aside>

<style>
  .pos-sidebar-item {
    display: flex;
    align-items: center;
    width: 100%;
    padding: 0.5rem 0.75rem;
    color: rgb(220 252 231);
    border-radius: 0.375rem;
    transition: all 0.2s ease-in-out;
  }

  .pos-sidebar-item:hover {
    color: white;
    background-color: rgba(34, 197, 94, 0.3);
  }

  .pos-sidebar-item.active {
    color: white;
    background-color: rgb(34 197 94);
    box-shadow: 0 10px 15px -3px rgba(0, 0, 0, 0.1), 0 4px 6px -2px rgba(0, 0, 0, 0.05);
  }

  .pos-quick-btn {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    padding: 0.5rem;
    border-radius: 0.375rem;
    transition: colors 0.2s ease-in-out;
    background-color: rgba(34, 197, 94, 0.3);
    border: 1px solid rgba(74, 222, 128, 0.5);
  }

  .pos-quick-btn:hover {
    background-color: rgba(34, 197, 94, 0.5);
    border-color: rgb(134, 239, 172);
  }
</style>
