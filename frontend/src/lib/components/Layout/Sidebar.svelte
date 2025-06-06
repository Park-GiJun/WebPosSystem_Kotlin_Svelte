<script>
  import { page } from '$app/stores';
  import { goto } from '$app/navigation';
  import { authStore } from '$lib/stores/auth.js';
  import { createEventDispatcher } from 'svelte';

  const dispatch = createEventDispatcher();

  $: currentPath = $page.url.pathname;
  $: user = $authStore.user;
  $: menus = $authStore.menus || [];

  // 디버깅 로그
  $: {
    console.log('🎨 Sidebar 렌더링:', {
      currentPath,
      user: user?.username,
      menusCount: menus.length,
      isAuthenticated: $authStore.isAuthenticated,
      authState: {
        token: $authStore.token ? 'exists' : 'none',
        permissions: $authStore.permissions?.length || 0
      }
    });
    
    if (menus.length > 0) {
      console.log('📋 Sidebar 메뉴 데이터:', menus.slice(0, 3));
    }
  }

  // 아이콘 매핑 (이모지 사용)
  const iconMap = {
    'shield': '🛡️',
    'building-office': '🏢',
    'computer-desktop': '💻',
    'users': '👥',
    'key': '🔑',
    'cog': '⚙️',
    'document-text': '📄',
    'building-office-2': '🏬',
    'building-storefront': '🏪',
    'chart-bar': '📊',
    'cube': '📦',
    'shopping-cart': '🛒',
    'user-group': '👥',
    'adjustments': '🔧'
  };

  // 메뉴를 계층 구조로 정리
  function organizeMenus(menus) {
    console.log('🔧 메뉴 조직화 시작:', menus.length, '개 메뉴');
    
    const menuMap = new Map();
    const rootMenus = [];

    // 먼저 모든 메뉴를 맵에 저장
    menus.forEach(menu => {
      menuMap.set(menu.menuId, { ...menu, children: [] });
    });

    console.log('📝 메뉴 맵 생성 완료:', menuMap.size, '개');

    // 계층 구조 구성
    menus.forEach(menu => {
      const menuItem = menuMap.get(menu.menuId);
      if (menu.parentMenuId && menu.parentMenuId !== null) {
        const parent = menuMap.get(menu.parentMenuId);
        if (parent) {
          parent.children.push(menuItem);
          console.log(`📂 ${menu.menuName} → ${parent.menuName} 하위로 추가`);
        } else {
          console.warn(`⚠️ 부모 메뉴를 찾을 수 없음: ${menu.parentMenuId} (${menu.menuName})`);
          // 부모를 찾을 수 없으면 루트로 추가
          rootMenus.push(menuItem);
        }
      } else {
        rootMenus.push(menuItem);
        console.log(`🌱 루트 메뉴로 추가: ${menu.menuName}`);
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

    const result = sortMenus(rootMenus);
    console.log('✅ 메뉴 조직화 완료:', result);
    return result;
  }

  $: organizedMenus = organizeMenus(menus);

  // 조직화된 메뉴 디버깅
  $: {
    if (organizedMenus.length > 0) {
      console.log('🗂️ 조직화된 메뉴:', organizedMenus);
    }
  }

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
    console.log('🔗 메뉴 클릭:', menu);
    
    // 카테고리가 아닌 실제 메뉴인 경우에만 네비게이션
    if (menu.menuType !== 'CATEGORY' && menu.menuPath) {
      goto(menu.menuPath);
    }
    
    dispatch('menu-click', menu);
  }
</script>

<aside class="w-64 bg-white shadow-sm border-r border-gray-200 h-full">
  <!-- 로고 영역 -->
  <div class="p-6 border-b border-gray-200">
    <button 
      type="button"
      class="text-left w-full hover:opacity-75 transition-opacity duration-200"
      on:click={() => goto('/system-select')}
      title="시스템 선택으로 돌아가기"
    >
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
    </button>
  </div>

  <!-- 메뉴 영역 -->
  <nav class="p-4 space-y-2">
    <!-- 디버깅 정보 표시 -->
    {#if menus.length === 0}
      <div class="text-sm text-gray-500 p-4 bg-yellow-50 rounded-lg border">
        <p>⚠️ 메뉴 데이터가 없습니다</p>
        <p>인증 상태: {$authStore.isAuthenticated ? '✅' : '❌'}</p>
        <p>토큰 존재: {$authStore.token ? '✅' : '❌'}</p>
        <p>권한 개수: {$authStore.permissions?.length || 0}</p>
      </div>
    {/if}
    
    {#each organizedMenus as menu}
      <div class="menu-group">
        {#if menu.menuType === 'CATEGORY'}
          <!-- 카테고리 메뉴 -->
          <div class="mb-4">
            <div class="flex items-center px-4 py-2 text-xs font-semibold text-gray-500 uppercase tracking-wider">
              {#if menu.iconName && iconMap[menu.iconName]}
                <span class="mr-2">{iconMap[menu.iconName]}</span>
              {/if}
              {menu.menuName}
              <span class="ml-auto text-xs text-gray-400">({menu.children?.length || 0})</span>
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
                      <span class="mr-3">{iconMap[subMenu.iconName]}</span>
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
              <span class="mr-3">{iconMap[menu.iconName]}</span>
            {/if}
            <span>{menu.menuName}</span>
          </button>
        {/if}
      </div>
    {/each}
  </nav>
</aside>
