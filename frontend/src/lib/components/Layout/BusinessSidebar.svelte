<script>
  import { page } from '$app/stores';
  import { goto } from '$app/navigation';
  import { authStore } from '$lib/stores/auth.js';
  import { createEventDispatcher } from 'svelte';
  import { Building, BarChart, Computer, Users, Settings, MapPin, Truck, FileText, DollarSign, ChevronDown, ChevronRight, Package } from 'lucide-svelte';

  const dispatch = createEventDispatcher();

  $: currentPath = $page.url.pathname;
  $: user = $authStore.user;
  
  // 영업정보시스템 전용 메뉴만 필터링 (DB에서 불러온 실제 데이터만 사용)
  $: allMenus = $authStore.menus || [];
  $: businessMenus = allMenus.filter(menu => 
    menu.menu_code?.startsWith('BUSINESS') || 
    menu.parent_menu_id === 'menu-business' ||
    menu.menu_id === 'menu-business'
  );

  // Lucide 아이콘 매핑 (실제 DB의 icon_name에 맞게)
  const iconMap = {
    'building-office': Building,
    'building-office-2': Building,
    'building-storefront': Building,
    'chart-bar': BarChart,
    'computer-desktop': Computer,
    'cube': Package,
    'currency-dollar': DollarSign,
    'document-text': FileText,
    'users': Users,
    'cog-6-tooth': Settings,
    'map-pin': MapPin,
    'truck': Truck
  };

  // 메뉴를 계층 구조로 정리 (실제 DB 구조에 맞게)
  function organizeMenus(menus) {
    if (!menus || menus.length === 0) return [];
    
    const menuMap = new Map();
    const rootMenus = [];

    // 활성화된 메뉴만 처리
    const activeMenus = menus.filter(menu => menu.is_active === 1);

    activeMenus.forEach(menu => {
      menuMap.set(menu.menu_id, { ...menu, children: [] });
    });

    activeMenus.forEach(menu => {
      const menuItem = menuMap.get(menu.menu_id);
      if (menu.parent_menu_id) {
        const parent = menuMap.get(menu.parent_menu_id);
        if (parent) {
          parent.children.push(menuItem);
        }
      } else {
        rootMenus.push(menuItem);
      }
    });

    const sortMenus = (menus) => {
      return menus.sort((a, b) => a.display_order - b.display_order)
        .map(menu => ({
          ...menu,
          children: sortMenus(menu.children)
        }));
    };

    return sortMenus(rootMenus);
  }

  $: organizedMenus = organizeMenus(businessMenus);

  function isActive(menuPath) {
    return currentPath === menuPath || currentPath.startsWith(menuPath + '/');
  }

  function hasActiveChild(menu) {
    if (menu.children) {
      return menu.children.some(child => 
        isActive(child.menu_path) || hasActiveChild(child)
      );
    }
    return false;
  }

  function handleMenuClick(menu) {
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
  let expandedCategories = new Set(['menu-business']);

  function toggleCategory(categoryId) {
    if (expandedCategories.has(categoryId)) {
      expandedCategories.delete(categoryId);
    } else {
      expandedCategories.add(categoryId);
    }
    expandedCategories = expandedCategories;
  }
</script>

<aside class="w-60 bg-gradient-to-b from-blue-600 via-blue-700 to-blue-800 text-white h-[calc(100vh-4rem)] shadow-2xl border-r border-blue-500/30 overflow-hidden">
  <!-- 배경 패턴 -->
  <div class="absolute inset-0 opacity-50" style="background-image: url('data:image/svg+xml,%3Csvg width=&quot;60&quot; height=&quot;60&quot; viewBox=&quot;0 0 60 60&quot; xmlns=&quot;http://www.w3.org/2000/svg&quot;%3E%3Cg fill=&quot;none&quot; fill-rule=&quot;evenodd&quot;%3E%3Cg fill=&quot;%23ffffff&quot; fill-opacity=&quot;0.05&quot;%3E%3Ccircle cx=&quot;30&quot; cy=&quot;30&quot; r=&quot;2&quot;/%3E%3C/g%3E%3C/g%3E%3C/svg%3E')"></div>
  
  <div class="relative z-10 flex flex-col h-full">
    <!-- 헤더 영역 -->
    <div class="p-4 border-b border-blue-500/30 bg-gradient-to-r from-blue-600/50 to-blue-700/50 backdrop-blur-sm">
      <button 
        type="button"
        class="group text-left w-full hover:opacity-90 transition-all duration-300 p-3 rounded-xl bg-white/10 hover:bg-white/15 border border-white/20 hover:border-white/30 backdrop-blur-sm"
        on:click={() => goto('/system-select')}
        title="시스템 선택으로 돌아가기"
      >
        <div class="flex items-center">
          <div class="p-2 bg-blue-500/30 rounded-xl border border-blue-400/50 group-hover:border-blue-300/70 transition-colors">
            <Building size="20" class="text-blue-100" />
          </div>
          <div class="ml-3">
            <h1 class="text-lg font-bold text-white group-hover:text-blue-100 transition-colors">영업정보시스템</h1>
            <p class="text-xs text-blue-200/80 font-medium">Business Management</p>
          </div>
        </div>
      </button>

      <!-- 사용자 정보 -->
      {#if user}
        <div class="mt-3 p-3 bg-blue-500/20 rounded-xl border border-blue-400/30 backdrop-blur-sm">
          <div class="flex items-center space-x-2">
            <div class="w-8 h-8 bg-gradient-to-br from-blue-400 to-blue-600 rounded-lg flex items-center justify-center border border-blue-300/50">
              <Users size="14" class="text-white" />
            </div>
            <div class="flex-1 min-w-0">
              <p class="text-sm font-bold text-blue-100 truncate">{user.username}</p>
              <p class="text-xs text-blue-200/70">{user.email || 'manager@company.com'}</p>
            </div>
          </div>
          <div class="flex flex-wrap gap-1 mt-2">
            {#each user.roles || [] as role}
              <span class="inline-flex items-center px-2 py-0.5 rounded-full text-xs font-semibold bg-blue-100/90 text-blue-800 border border-blue-200/50 backdrop-blur-sm">
                {getRoleDisplayName(role)}
              </span>
            {/each}
          </div>
        </div>
      {/if}
    </div>

    <!-- 대시보드 요약 -->
    <div class="p-3 bg-blue-500/10 border-b border-blue-500/20">
      <div class="grid grid-cols-2 gap-2">
        <div class="bg-blue-500/20 p-2 rounded-lg backdrop-blur-sm border border-blue-400/30">
          <div class="text-xs text-blue-200">총 매장수</div>
          <div class="text-sm font-bold text-white">24개</div>
        </div>
        <div class="bg-blue-500/20 p-2 rounded-lg backdrop-blur-sm border border-blue-400/30">
          <div class="text-xs text-blue-200">금일 매출</div>
          <div class="text-sm font-bold text-white">₩2.4M</div>
        </div>
      </div>
    </div>

    <!-- 메뉴 영역 -->
    <nav class="p-3 space-y-1 flex-1 overflow-y-auto">
      {#if organizedMenus.length === 0}
        <div class="text-center py-8">
          <div class="text-blue-200/60 text-sm space-y-2">
            <p>메뉴를 불러오는 중...</p>
            <div class="text-xs text-blue-300/50">
              <p>전체 메뉴: {allMenus.length}개</p>
              <p>비즈니스 메뉴: {businessMenus.length}개</p>
            </div>
          </div>
        </div>
      {:else}
        {#each organizedMenus as menu}
          <div class="menu-group">
            {#if menu.menu_type === 'CATEGORY'}
              <!-- 카테고리 메뉴 -->
              <div class="mb-3">
                <button
                  type="button"
                  class="flex items-center justify-between w-full px-3 py-2 text-xs font-semibold text-blue-200 uppercase tracking-wider hover:text-blue-100 transition-colors rounded-lg hover:bg-blue-500/20"
                  on:click={() => toggleCategory(menu.menu_id)}
                >
                  <div class="flex items-center">
                    {#if menu.icon_name && iconMap[menu.icon_name]}
                      {@const IconComponent = iconMap[menu.icon_name]}
                      <IconComponent size="14" class="mr-2" />
                    {/if}
                    {menu.menu_name}
                  </div>
                  
                  {#if expandedCategories.has(menu.menu_id)}
                    <ChevronDown size="14" class="transition-transform" />
                  {:else}
                    <ChevronRight size="14" class="transition-transform" />
                  {/if}
                </button>
                
                {#if menu.children && menu.children.length > 0 && expandedCategories.has(menu.menu_id)}
                  <div class="space-y-0.5 ml-3 border-l-2 border-blue-400/30 pl-3">
                    {#each menu.children as subMenu}
                      <button
                        type="button"
                        class="business-sidebar-item group"
                        class:active={isActive(subMenu.menu_path) || hasActiveChild(subMenu)}
                        on:click={() => handleMenuClick(subMenu)}
                      >
                        <div class="flex items-center">
                          {#if subMenu.icon_name && iconMap[subMenu.icon_name]}
                            {@const IconComponent = iconMap[subMenu.icon_name]}
                            <IconComponent size="16" class="mr-2 group-hover:scale-110 transition-transform" />
                          {/if}
                          <span class="text-sm font-medium">{subMenu.menu_name}</span>
                        </div>
                        
                        {#if subMenu.children && subMenu.children.length > 0}
                          <ChevronRight size="12" class="ml-auto opacity-60" />
                        {/if}
                      </button>
                      
                      <!-- 3레벨 메뉴가 있는 경우 -->
                      {#if subMenu.children && subMenu.children.length > 0 && (isActive(subMenu.menu_path) || hasActiveChild(subMenu))}
                        <div class="ml-6 space-y-0.5 border-l border-blue-400/50 pl-3">
                          {#each subMenu.children as subSubMenu}
                            <button
                              type="button"
                              class="w-full text-left px-2 py-1.5 text-xs text-blue-100 hover:text-white hover:bg-blue-500/30 rounded transition-colors duration-200"
                              class:text-white={isActive(subSubMenu.menu_path)}
                              class:bg-blue-500={isActive(subSubMenu.menu_path)}
                              on:click={() => handleMenuClick(subSubMenu)}
                            >
                              • {subSubMenu.menu_name}
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
                class="business-sidebar-item group"
                class:active={isActive(menu.menu_path)}
                on:click={() => handleMenuClick(menu)}
              >
                <div class="flex items-center">
                  {#if menu.icon_name && iconMap[menu.icon_name]}
                    {@const IconComponent = iconMap[menu.icon_name]}
                    <IconComponent size="16" class="mr-2 group-hover:scale-110 transition-transform" />
                  {/if}
                  <span class="text-sm font-medium">{menu.menu_name}</span>
                </div>
              </button>
            {/if}
          </div>
        {/each}
      {/if}
    </nav>

    <!-- 하단 시스템 정보 -->
    <div class="p-3 border-t border-blue-500/30 bg-blue-600/30">
      <div class="text-xs text-blue-200 space-y-1">
        <div class="flex justify-between">
          <span>네트워크</span>
          <span class="text-green-300">●</span>
        </div>
        <div class="flex justify-between">
          <span>메뉴 로드</span>
          <span class="text-green-300">{businessMenus.length > 0 ? '●' : '○'}</span>
        </div>
        <div class="flex justify-between text-xs">
          <span>전체/필터</span>
          <span class="font-mono">{allMenus.length}/{businessMenus.length}</span>
        </div>
        <div class="flex justify-between">
          <span>서버 상태</span>
          <span class="text-green-300">●</span>
        </div>
      </div>
    </div>
  </div>
</aside>

<style>
  .business-sidebar-item {
    display: flex;
    align-items: center;
    justify-content: between;
    width: 100%;
    padding: 0.5rem 0.75rem;
    color: rgb(219 234 254);
    border-radius: 0.375rem;
    transition: all 0.2s ease-in-out;
    background: rgba(59, 130, 246, 0.05);
    backdrop-filter: blur(4px);
  }

  .business-sidebar-item:hover {
    color: white;
    background: rgba(59, 130, 246, 0.2);
    transform: translateX(2px);
  }

  .business-sidebar-item.active {
    color: white;
    background: rgba(59, 130, 246, 0.4);
    box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1), 0 2px 4px -1px rgba(0, 0, 0, 0.06);
  }
</style>
