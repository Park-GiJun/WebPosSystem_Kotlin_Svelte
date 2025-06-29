<script>
  import { page } from '$app/stores';
  import { goto } from '$app/navigation';
  import { authStore } from '$lib/stores/auth.js';
  import { tabStore } from '$lib/stores/tabs.js';
  import { createEventDispatcher } from 'svelte';
  import { ShoppingCart, Computer, Users, Settings, Calculator, CreditCard, Package, BarChart, Printer, Clock, ChevronDown, ChevronRight } from 'lucide-svelte';

  const dispatch = createEventDispatcher();

  $: currentPath = $page.url.pathname;
  $: user = $authStore.user;
  $: allTabs = $tabStore;
  $: activePosTab = allTabs.find(tab => tab.system === 'pos' && tab.active);
  
  // 활성 탭의 경로를 우선으로 사용하고, 없으면 현재 URL 경로 사용
  $: effectivePath = activePosTab?.path || currentPath;
  
  // 경로 변경 감지 및 디버깅
  $: {
    console.log('🛒 PosSidebar 상태:', {
      currentPath,
      activePosTab,
      effectivePath,
      allPosTabs: allTabs.filter(t => t.system === 'pos')
    });
  }
  
  // POS 시스템 전용 메뉴만 필터링 (실제 API 응답 구조에 맞게)
  $: allMenus = $authStore.menus || [];
  $: posMenus = allMenus.filter(menu => 
    menu.menuCode?.startsWith('POS') || 
    menu.parentMenuId === 'menu-pos' ||
    menu.menuId === 'menu-pos'
  );

  // 디버깅 로그 추가
  $: {
    console.log('🛒 PosSidebar 메뉴 디버깅:', {
      allMenusCount: allMenus.length,
      posMenusCount: posMenus.length,
      firstMenu: allMenus[0],
      posMenuCodes: posMenus.map(m => m.menuCode)
    });
  }

  // Lucide 아이콘 매핑 (실제 DB의 icon_name에 맞게)
  const iconMap = {
    'shopping-cart': ShoppingCart,
    'computer-desktop': Computer,
    'users': Users,
    'user-group': Users, // UserGroup 대신 Users 사용
    'cog': Settings,
    'calculator': Calculator,
    'credit-card': CreditCard,
    'cube': Package,
    'chart-bar': BarChart,
    'printer': Printer,
    'clock': Clock,
    'adjustments': Settings
  };

  // 메뉴를 계층 구조로 정리 (실제 API 응답 구조에 맞게)
  function organizeMenus(menus) {
    if (!menus || menus.length === 0) return [];
    
    const menuMap = new Map();
    const rootMenus = [];

    // 모든 메뉴 처리 (is_active 체크 제거)
    menus.forEach(menu => {
      menuMap.set(menu.menuId, { ...menu, children: [] });
    });

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

    const sortMenus = (menus) => {
      return menus.sort((a, b) => a.displayOrder - b.displayOrder)
        .map(menu => ({
          ...menu,
          children: sortMenus(menu.children)
        }));
    };

    return sortMenus(rootMenus);
  }

  $: organizedMenus = organizeMenus(posMenus);

  function isActive(menuPath) {
    if (!menuPath) return false;
    
    // 활성 탭의 경로를 우선 사용
    const checkPath = effectivePath;
    
    console.log('🔍 POS isActive 체크:', {
      menuPath,
      checkPath,
      activePosTab: activePosTab?.title,
      currentPath
    });
    
    // 정확한 경로 매칭
    if (checkPath === menuPath) return true;
    
    // 하위 경로 매칭 (더 엄격하게)
    if (checkPath.startsWith(menuPath + '/')) return true;
    
    // 특별 케이스: 루트 경로의 경우
    if (menuPath === '/pos' && checkPath.startsWith('/pos')) return true;
    
    return false;
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
    console.log('🛒 PosSidebar에서 메뉴 클릭:', {
      menu,
      menuType: menu.menuType,
      menuPath: menu.menuPath,
      isCategory: menu.menuType === 'CATEGORY'
    });
    
    // 카테고리가 아닌 실제 메뉴인 경우에만 네비게이션
    if (menu.menuType !== 'CATEGORY' && menu.menuPath) {
      console.log('🔗 네비게이션 실행:', menu.menuPath);
      goto(menu.menuPath);
    }
    
    dispatch('menu-click', menu);
  }

  function getRoleDisplayName(role) {
    const roleNames = {
      'SUPER_ADMIN': '슈퍼관리자',
      'SYSTEM_ADMIN': '시스템관리자', 
      'HQ_MANAGER': '본사관리자',
      'STORE_MANAGER': '매장관리자',
      'USER': '사용자'
    };
    return roleNames[role] || role;
  }

  // 카테고리 확장/축소 상태 관리
  let expandedCategories = new Set(['menu-pos']);

  function toggleCategory(categoryId) {
    if (expandedCategories.has(categoryId)) {
      expandedCategories.delete(categoryId);
    } else {
      expandedCategories.add(categoryId);
    }
    expandedCategories = expandedCategories;
  }

  // 하위 메뉴 확장/축소 상태 관리 (2레벨 메뉴용)
  let expandedSubMenus = new Set();

  function toggleSubMenu(menuId) {
    if (expandedSubMenus.has(menuId)) {
      expandedSubMenus.delete(menuId);
    } else {
      expandedSubMenus.add(menuId);
    }
    expandedSubMenus = expandedSubMenus;
  }

  // 현재 시간 표시
  let currentTime = new Date();
  setInterval(() => {
    currentTime = new Date();
  }, 1000);
</script>

<aside class="w-60 bg-gradient-to-b from-emerald-600 via-emerald-700 to-emerald-800 text-white h-[calc(100vh-4rem)] shadow-2xl border-r border-emerald-500/30 overflow-hidden">
  <!-- 배경 패턴 -->
  <div class="absolute inset-0 opacity-50" style="background-image: url('data:image/svg+xml,%3Csvg width=&quot;60&quot; height=&quot;60&quot; viewBox=&quot;0 0 60 60&quot; xmlns=&quot;http://www.w3.org/2000/svg&quot;%3E%3Cg fill=&quot;none&quot; fill-rule=&quot;evenodd&quot;%3E%3Cg fill=&quot;%23ffffff&quot; fill-opacity=&quot;0.05&quot;%3E%3Ccircle cx=&quot;30&quot; cy=&quot;30&quot; r=&quot;2&quot;/%3E%3C/g%3E%3C/g%3E%3C/svg%3E')"></div>
  
  <div class="relative z-10 flex flex-col h-full">
    <!-- 헤더 영역 -->
    <div class="p-4 border-b border-emerald-500/30 bg-gradient-to-r from-emerald-600/50 to-emerald-700/50 backdrop-blur-sm">
      <button 
        type="button"
        class="group text-left w-full hover:opacity-90 transition-all duration-300 p-3 rounded-xl bg-white/10 hover:bg-white/15 border border-white/20 hover:border-white/30 backdrop-blur-sm"
        on:click={() => goto('/system-select')}
        title="시스템 선택으로 돌아가기"
      >
        <div class="flex items-center">
          <div class="p-2 bg-emerald-500/30 rounded-xl border border-emerald-400/50 group-hover:border-emerald-300/70 transition-colors">
            <ShoppingCart size="20" class="text-emerald-100" />
          </div>
          <div class="ml-3">
            <h1 class="text-lg font-bold text-white group-hover:text-emerald-100 transition-colors">POS 시스템</h1>
            <p class="text-xs text-emerald-200/80 font-medium">Point of Sale</p>
          </div>
        </div>
      </button>

      <!-- 현재 시간 및 사용자 정보 -->
      <div class="mt-3 space-y-2">
        <!-- 시간 표시 -->
        <div class="text-center p-2 bg-emerald-500/20 rounded-lg border border-emerald-400/30 backdrop-blur-sm">
          <div class="text-sm font-mono font-bold text-white">
            {currentTime.toLocaleTimeString('ko-KR')}
          </div>
          <div class="text-xs text-emerald-200">
            {currentTime.toLocaleDateString('ko-KR')}
          </div>
        </div>

        <!-- 사용자 정보 -->
        {#if user}
          <div class="p-3 bg-emerald-500/20 rounded-xl border border-emerald-400/30 backdrop-blur-sm">
            <div class="flex items-center space-x-2">
              <div class="w-8 h-8 bg-gradient-to-br from-emerald-400 to-emerald-600 rounded-lg flex items-center justify-center border border-emerald-300/50">
                <Users size="14" class="text-white" />
              </div>
              <div class="flex-1 min-w-0">
                <p class="text-xs text-emerald-200">근무자</p>
                <p class="text-sm font-bold text-emerald-100 truncate">{user.username}</p>
              </div>
            </div>
            <div class="flex flex-wrap gap-1 mt-2">
              {#each user.roles || [] as role}
                <span class="inline-flex items-center px-2 py-0.5 rounded-full text-xs font-semibold bg-emerald-100/90 text-emerald-800 border border-emerald-200/50 backdrop-blur-sm">
                  {getRoleDisplayName(role)}
                </span>
              {/each}
            </div>
          </div>
        {/if}
      </div>
    </div>

    <!-- 빠른 실행 버튼 -->
    <div class="p-3 border-b border-emerald-500/30">
      <div class="grid grid-cols-2 gap-2">
        <button class="pos-quick-btn group">
          <ShoppingCart size="16" class="group-hover:scale-110 transition-transform" />
          <span class="text-xs font-medium">판매</span>
        </button>
        <button class="pos-quick-btn group">
          <CreditCard size="16" class="group-hover:scale-110 transition-transform" />
          <span class="text-xs font-medium">결제</span>
        </button>
      </div>
    </div>

    <!-- 메뉴 영역 -->
    <nav class="p-3 space-y-1 flex-1 overflow-y-auto">
      {#if organizedMenus.length === 0}
        <div class="text-center py-8">
          <div class="text-emerald-200/60 text-sm space-y-2">
            <p>메뉴를 불러오는 중...</p>
            <div class="text-xs text-emerald-300/50">
              <p>전체 메뉴: {allMenus.length}개</p>
              <p>POS 메뉴: {posMenus.length}개</p>
            </div>
          </div>
        </div>
      {:else}
        {#each organizedMenus as menu}
          <div class="menu-group">
            {#if menu.menuType === 'CATEGORY'}
              <!-- 카테고리 메뉴 -->
              <div class="mb-3">
                <button
                  type="button"
                  class="flex items-center justify-between w-full px-3 py-2 text-xs font-semibold text-emerald-200 uppercase tracking-wider hover:text-emerald-100 transition-colors rounded-lg hover:bg-emerald-500/20"
                  on:click={() => toggleCategory(menu.menuId)}
                >
                  <div class="flex items-center">
                    {#if menu.iconName && iconMap[menu.iconName]}
                      {@const IconComponent = iconMap[menu.iconName]}
                      <IconComponent size="14" class="mr-2" />
                    {/if}
                    {menu.menuName}
                  </div>
                  
                  {#if expandedCategories.has(menu.menuId)}
                    <ChevronDown size="14" class="transition-transform" />
                  {:else}
                    <ChevronRight size="14" class="transition-transform" />
                  {/if}
                </button>
                
                {#if menu.children && menu.children.length > 0 && expandedCategories.has(menu.menuId)}
                  <div class="space-y-0.5 ml-3 border-l-2 border-emerald-400/30 pl-3">
                    {#each menu.children as subMenu}
                      <!-- 2레벨 메뉴 -->
                      {#if subMenu.menuType === 'CATEGORY'}
                        <!-- 2레벨 카테고리 -->
                        <div class="mb-2">
                          <button
                            type="button"
                            class="flex items-center justify-between w-full px-2 py-1.5 text-xs font-medium text-emerald-200 hover:text-emerald-100 transition-colors rounded-lg hover:bg-emerald-500/20"
                            on:click={() => toggleSubMenu(subMenu.menuId)}
                          >
                            <div class="flex items-center">
                              {#if subMenu.iconName && iconMap[subMenu.iconName]}
                                {@const IconComponent = iconMap[subMenu.iconName]}
                                <IconComponent size="14" class="mr-2" />
                              {/if}
                              <span>{subMenu.menuName}</span>
                            </div>
                            
                            {#if expandedSubMenus.has(subMenu.menuId)}
                              <ChevronDown size="12" class="transition-transform" />
                            {:else}
                              <ChevronRight size="12" class="transition-transform" />
                            {/if}
                          </button>
                          
                          {#if subMenu.children && subMenu.children.length > 0 && expandedSubMenus.has(subMenu.menuId)}
                            <div class="ml-4 space-y-0.5 border-l border-emerald-400/50 pl-3">
                              {#each subMenu.children as subSubMenu}
                                <button
                                  type="button"
                                  class="pos-sidebar-item group text-xs"
                                  class:active={isActive(subSubMenu.menuPath)}
                                  on:click={() => handleMenuClick(subSubMenu)}
                                >
                                  <div class="flex items-center">
                                    {#if subSubMenu.iconName && iconMap[subSubMenu.iconName]}
                                      {@const IconComponent = iconMap[subSubMenu.iconName]}
                                      <IconComponent size="14" class="mr-2 group-hover:scale-110 transition-transform" />
                                    {/if}
                                    <span class="font-medium">{subSubMenu.menuName}</span>
                                  </div>
                                </button>
                              {/each}
                            </div>
                          {/if}
                        </div>
                      {:else}
                        <!-- 2레벨 일반 메뉴 -->
                        <button
                          type="button"
                          class="pos-sidebar-item group"
                          class:active={isActive(subMenu.menuPath) || hasActiveChild(subMenu)}
                          on:click={() => handleMenuClick(subMenu)}
                        >
                          <div class="flex items-center">
                            {#if subMenu.iconName && iconMap[subMenu.iconName]}
                              {@const IconComponent = iconMap[subMenu.iconName]}
                              <IconComponent size="16" class="mr-2 group-hover:scale-110 transition-transform" />
                            {/if}
                            <span class="text-sm font-medium">{subMenu.menuName}</span>
                          </div>
                          
                          {#if subMenu.children && subMenu.children.length > 0}
                            <ChevronRight size="12" class="ml-auto opacity-60" />
                          {/if}
                        </button>
                        
                        <!-- 3레벨 메뉴가 있는 경우 -->
                        {#if subMenu.children && subMenu.children.length > 0 && (isActive(subMenu.menuPath) || hasActiveChild(subMenu))}
                          <div class="ml-6 space-y-0.5 border-l border-emerald-400/50 pl-3">
                            {#each subMenu.children as subSubMenu}
                              <button
                                type="button"
                                class="w-full text-left px-2 py-1.5 text-xs text-emerald-100 hover:text-white hover:bg-emerald-500/30 rounded transition-colors duration-200"
                                class:text-white={isActive(subSubMenu.menuPath)}
                                class:bg-emerald-500={isActive(subSubMenu.menuPath)}
                                on:click={() => handleMenuClick(subSubMenu)}
                              >
                                • {subSubMenu.menuName}
                              </button>
                            {/each}
                          </div>
                        {/if}
                      {/if}
                    {/each}
                  </div>
                {/if}
              </div>
            {:else}
              <!-- 일반 메뉴 -->
              <button
                type="button"
                class="pos-sidebar-item group"
                class:active={isActive(menu.menuPath)}
                on:click={() => handleMenuClick(menu)}
              >
                <div class="flex items-center">
                  {#if menu.iconName && iconMap[menu.iconName]}
                    {@const IconComponent = iconMap[menu.iconName]}
                    <IconComponent size="16" class="mr-2 group-hover:scale-110 transition-transform" />
                  {/if}
                  <span class="text-sm font-medium">{menu.menuName}</span>
                </div>
              </button>
            {/if}
          </div>
        {/each}
      {/if}
    </nav>

    <!-- 하단 상태 영역 -->
    <div class="p-3 border-t border-emerald-500/30 bg-emerald-600/30">
      <div class="text-xs text-emerald-200 space-y-1">
        <div class="flex justify-between">
          <span>프린터</span>
          <span class="text-green-300">●</span>
        </div>
        <div class="flex justify-between">
          <span>메뉴 로드</span>
          <span class="text-green-300">{posMenus.length > 0 ? '●' : '○'}</span>
        </div>
        <div class="flex justify-between text-xs">
          <span>전체/필터</span>
          <span class="font-mono">{allMenus.length}/{posMenus.length}</span>
        </div>
        <div class="flex justify-between">
          <span>네트워크</span>
          <span class="text-green-300">●</span>
        </div>
      </div>
    </div>
  </div>
</aside>

<style>
  .pos-sidebar-item {
    display: flex;
    align-items: center;
    justify-content: between;
    width: 100%;
    padding: 0.5rem 0.75rem;
    color: rgb(220 252 231);
    border-radius: 0.375rem;
    transition: all 0.2s ease-in-out;
    background: rgba(34, 197, 94, 0.05);
    backdrop-filter: blur(4px);
  }

  .pos-sidebar-item:hover {
    color: white;
    background: rgba(34, 197, 94, 0.2);
    transform: translateX(2px);
  }

  .pos-sidebar-item.active {
    color: white;
    background: rgba(34, 197, 94, 0.4);
    box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1), 0 2px 4px -1px rgba(0, 0, 0, 0.06);
  }

  .pos-quick-btn {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    gap: 0.25rem;
    padding: 0.5rem;
    border-radius: 0.5rem;
    transition: all 0.2s ease-in-out;
    background: rgba(34, 197, 94, 0.2);
    border: 1px solid rgba(34, 197, 94, 0.3);
    color: rgb(220 252 231);
    backdrop-filter: blur(4px);
  }

  .pos-quick-btn:hover {
    color: white;
    background: rgba(34, 197, 94, 0.3);
    border-color: rgba(134, 239, 172, 0.5);
    transform: translateY(-1px);
    box-shadow: 0 4px 8px rgba(34, 197, 94, 0.2);
  }
</style>
