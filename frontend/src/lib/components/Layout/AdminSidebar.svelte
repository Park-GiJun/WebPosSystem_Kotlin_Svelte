<script>
  import { page } from '$app/stores';
  import { goto } from '$app/navigation';
  import { authStore } from '$lib/stores/auth.js';
  import { tabStore } from '$lib/stores/tabs.js';
  import { createEventDispatcher } from 'svelte';
  import { Users, Building, Key, Settings, Shield, Server, Database, ClipboardList, BarChart, Lock, ChevronDown, ChevronRight, FileText } from 'lucide-svelte';

  const dispatch = createEventDispatcher();

  $: currentPath = $page.url.pathname;
  $: user = $authStore.user;
  $: allTabs = $tabStore;
  $: activeAdminTab = allTabs.find(tab => tab.system === 'admin' && tab.active);
  
  // 활성 탭의 경로를 우선으로 사용하고, 없으면 현재 URL 경로 사용
  $: effectivePath = activeAdminTab?.path || currentPath;
  
  // 탭 상태 변경 감지
  $: {
    console.log('🔐 AdminSidebar 탭 상태 변경 감지:', {
      allTabs: allTabs.length,
      adminTabs: allTabs.filter(t => t.system === 'admin'),
      activeAdminTab: activeAdminTab,
      currentPath,
      effectivePath
    });
  }
  
  // 슈퍼어드민 전용 메뉴만 필터링 (실제 API 응답 구조에 맞게)
  $: allMenus = $authStore.menus || [];
  $: adminMenus = allMenus.filter(menu => 
    menu.menuCode === 'ADMIN' || 
    menu.menuCode?.startsWith('ADMIN') || 
    menu.parentMenuId === 'menu-admin' ||
    menu.menuId === 'menu-admin' ||
    (menu.parentMenuId && allMenus.find(parent => parent.menuId === menu.parentMenuId && parent.menuCode === 'ADMIN'))
  );

  // 디버깅 로그 추가
  $: {
    console.log('🔐 AdminSidebar 메뉴 디버깅:', {
      allMenusCount: allMenus.length,
      adminMenusCount: adminMenus.length,
      firstMenu: allMenus[0],
      adminMenuCodes: adminMenus.map(m => m.menuCode),
      adminCategory: allMenus.find(m => m.menuId === 'menu-admin'),
      adminChildren: allMenus.filter(m => m.parentMenuId === 'menu-admin'),
      sampleMenuStructure: allMenus.length > 0 ? {
        menuId: allMenus[0]?.menuId,
        menuCode: allMenus[0]?.menuCode,
        menuName: allMenus[0]?.menuName,
        menuPath: allMenus[0]?.menuPath,
        parentMenuId: allMenus[0]?.parentMenuId,
        menuType: allMenus[0]?.menuType
      } : 'No menus'
    });
  }

  // Lucide 아이콘 매핑 (실제 DB의 icon_name에 맞게)
  const iconMap = {
    'shield': Shield,
    'building': Building,
    'building-office': Building,
    'building-office-2': Building,
    'building-storefront': Building,
    'users': Users,
    'key': Key,
    'cog': Settings,
    'server': Server,
    'database': Database,
    'document-text': FileText,
    'document-magnifying-glass': FileText,
    'clipboard-document-list': ClipboardList,
    'chart-bar': BarChart,
    'chart-bar-square': BarChart,
    'lock-closed': Lock,
    'adjustments': Settings,
    'computer-desktop': Server,
    'cube': Database,
    'shopping-cart': ClipboardList,
    'user-group': Users
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

  $: organizedMenus = organizeMenus(adminMenus);

  // 조직화된 메뉴 디버깅
  $: {
    if (organizedMenus.length > 0) {
      console.log('🔐 AdminSidebar 조직화된 메뉴:', {
        organizedMenus,
        firstMenu: organizedMenus[0],
        firstMenuChildren: organizedMenus[0]?.children,
        menuSample: organizedMenus[0]?.children?.[0]
      });
    }
  }

  function isActive(menuPath) {
    if (!menuPath) return false;
    
    // 현재 활성 탭의 경로를 확인
    const activeTab = allTabs.find(tab => tab.system === 'admin' && tab.active);
    const checkPath = activeTab?.path || currentPath;
    
    // 정확한 경로 매칭
    if (checkPath === menuPath) return true;
    
    // 하위 경로 매칭 (더 엄격하게)
    if (checkPath.startsWith(menuPath + '/')) return true;
    
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
    console.log('🔐 AdminSidebar에서 메뉴 클릭:', {
      menu,
      menuType: menu.menuType,
      menuPath: menu.menuPath,
      isCategory: menu.menuType === 'CATEGORY'
    });
    
    // 카테고리가 아닌 실제 메뉴인 경우에만 네비게이션
    if (menu.menuType !== 'CATEGORY' && menu.menuPath) {
      console.log('🔗 네비게이션 실행:', menu.menuPath);
      
      // 먼저 탭을 생성하고 활성화
      const tabData = {
        id: `admin-${menu.menuCode || 'unknown'}`,
        title: menu.menuName || 'Unknown Menu',
        path: menu.menuPath,
        system: 'admin',
        closeable: true,
        secure: true,
        priority: menu.menuCode?.includes('USERS') ? 'HIGH' : 'MEDIUM'
      };
      
      console.log('🔐 생성/활성화할 탭 데이터:', tabData);
      tabStore.openTab(tabData);
      
      // 그 다음 페이지 이동
      goto(menu.menuPath);
      
      // 탭 생성도 함께 실행
      dispatch('menu-click', menu);
    }
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
  let expandedCategories = new Set(['menu-admin']);

  function toggleCategory(categoryId) {
    if (expandedCategories.has(categoryId)) {
      expandedCategories.delete(categoryId);
    } else {
      expandedCategories.add(categoryId);
    }
    expandedCategories = expandedCategories;
  }
</script>

<aside class="w-60 bg-gradient-to-b from-red-600 via-red-700 to-red-800 text-white h-[calc(100vh-4rem)] shadow-2xl border-r border-red-500/30 overflow-hidden">
  <!-- 배경 패턴 -->
  <div class="absolute inset-0 opacity-50" style="background-image: url('data:image/svg+xml,%3Csvg width=&quot;60&quot; height=&quot;60&quot; viewBox=&quot;0 0 60 60&quot; xmlns=&quot;http://www.w3.org/2000/svg&quot;%3E%3Cg fill=&quot;none&quot; fill-rule=&quot;evenodd&quot;%3E%3Cg fill=&quot;%23ffffff&quot; fill-opacity=&quot;0.05&quot;%3E%3Ccircle cx=&quot;30&quot; cy=&quot;30&quot; r=&quot;2&quot;/%3E%3C/g%3E%3C/g%3E%3C/svg%3E')"></div>
  
  <div class="relative z-10 flex flex-col h-full">
    <!-- 헤더 영역 -->
    <div class="p-4 border-b border-red-500/30 bg-gradient-to-r from-red-600/50 to-red-700/50 backdrop-blur-sm">
      <button 
        type="button"
        class="group text-left w-full hover:opacity-90 transition-all duration-300 p-3 rounded-xl bg-white/10 hover:bg-white/15 border border-white/20 hover:border-white/30 backdrop-blur-sm"
        on:click={() => goto('/system-select')}
        title="시스템 선택으로 돌아가기"
      >
        <div class="flex items-center">
          <div class="p-2 bg-red-500/30 rounded-xl border border-red-400/50 group-hover:border-red-300/70 transition-colors">
            <Shield size="20" class="text-red-100" />
          </div>
          <div class="ml-3">
            <h1 class="text-lg font-bold text-white group-hover:text-red-100 transition-colors">슈퍼어드민</h1>
            <p class="text-xs text-red-200/80 font-medium">System Administration</p>
          </div>
        </div>
      </button>

      <!-- 사용자 정보 -->
      {#if user}
        <div class="mt-3 p-3 bg-red-500/20 rounded-xl border border-red-400/30 backdrop-blur-sm">
          <div class="flex items-center space-x-2">
            <div class="w-8 h-8 bg-gradient-to-br from-red-400 to-red-600 rounded-lg flex items-center justify-center border border-red-300/50">
              <Users size="14" class="text-white" />
            </div>
            <div class="flex-1 min-w-0">
              <p class="text-sm font-bold text-red-100 truncate">{user.username}</p>
              <p class="text-xs text-red-200/70">{user.email || 'admin@company.com'}</p>
            </div>
          </div>
          <div class="flex flex-wrap gap-1 mt-2">
            {#each user.roles || [] as role}
              <span class="inline-flex items-center px-2 py-0.5 rounded-full text-xs font-semibold bg-red-100/90 text-red-800 border border-red-200/50 backdrop-blur-sm">
                {getRoleDisplayName(role)}
              </span>
            {/each}
          </div>
        </div>
      {/if}
    </div>

    <!-- 메뉴 영역 -->
    <nav class="p-3 space-y-1 flex-1 overflow-y-auto">
      {#if organizedMenus.length === 0}
        <div class="text-center py-8">
          <div class="text-red-200/60 text-sm space-y-2">
            <p>메뉴를 불러오는 중...</p>
            <div class="text-xs text-red-300/50">
              <p>전체 메뉴: {allMenus.length}개</p>
              <p>어드민 메뉴: {adminMenus.length}개</p>
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
                  class="flex items-center justify-between w-full px-3 py-2 text-xs font-semibold text-red-200 uppercase tracking-wider hover:text-red-100 transition-colors rounded-lg hover:bg-red-500/20"
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
                  <div class="space-y-0.5 ml-3 border-l-2 border-red-400/30 pl-3">
                    {#each menu.children as subMenu}
                      <button
                        type="button"
                        class="admin-sidebar-item group"
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
                        <div class="ml-6 space-y-0.5 border-l border-red-400/50 pl-3">
                          {#each subMenu.children as subSubMenu}
                            <button
                              type="button"
                              class="w-full text-left px-2 py-1.5 text-xs text-red-100 hover:text-white hover:bg-red-500/30 rounded transition-colors duration-200"
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
                class="admin-sidebar-item group"
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

    <!-- 하단 시스템 정보 -->
    <div class="p-3 border-t border-red-500/30 bg-red-600/30">
      <div class="text-xs text-red-200 space-y-1">
        <div class="flex justify-between">
          <span>시스템 상태</span>
          <span class="text-green-300">●</span>
        </div>
        <div class="flex justify-between">
          <span>메뉴 로드</span>
          <span class="text-green-300">{adminMenus.length > 0 ? '●' : '○'}</span>
        </div>
        <div class="flex justify-between text-xs">
          <span>전체/필터</span>
          <span class="font-mono">{allMenus.length}/{adminMenus.length}</span>
        </div>
        <div class="flex justify-between">
          <span>접근 권한</span>
          <span class="text-green-300">●</span>
        </div>
      </div>
    </div>
  </div>
</aside>

<style>
  .admin-sidebar-item {
    display: flex;
    align-items: center;
    justify-content: between;
    width: 100%;
    padding: 0.5rem 0.75rem;
    color: rgb(220 252 231);
    border-radius: 0.375rem;
    transition: all 0.2s ease-in-out;
    background: rgba(239, 68, 68, 0.05);
    backdrop-filter: blur(4px);
  }

  .admin-sidebar-item:hover {
    color: white;
    background: rgba(239, 68, 68, 0.2);
    transform: translateX(2px);
  }

  .admin-sidebar-item.active {
    color: white;
    background: rgba(239, 68, 68, 0.4);
    box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1), 0 2px 4px -1px rgba(0, 0, 0, 0.06);
  }
</style>
