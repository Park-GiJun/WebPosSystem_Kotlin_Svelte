import { writable } from 'svelte/store';

function createTabStore() {
  const { subscribe, update, set } = writable([]);

  return {
    subscribe,
    
    // 탭 열기
    openTab(tab) {
      update(tabs => {
        // 이미 존재하는 탭인지 확인
        const existingTabIndex = tabs.findIndex(t => t.id === tab.id);
        
        if (existingTabIndex !== -1) {
          // 기존 탭을 활성화 (같은 시스템의 다른 탭들은 비활성화)
          return tabs.map((t, index) => ({
            ...t,
            active: index === existingTabIndex && t.system === tab.system
          }));
        }

        // 같은 시스템의 모든 탭을 비활성화하고 새 탭 추가
        const newTabs = tabs.map(t => ({ 
          ...t, 
          active: t.system !== tab.system ? t.active : false 
        }));
        return [...newTabs, { ...tab, active: true }];
      });
    },

    // 탭 닫기
    closeTab(tabId) {
      console.log('🗑️ TabStore closeTab 호출:', tabId);
      update(tabs => {
        const tabIndex = tabs.findIndex(t => t.id === tabId);
        if (tabIndex === -1) {
          console.log('❌ 탭을 찾을 수 없음:', tabId);
          return tabs;
        }

        const closingTab = tabs[tabIndex];
        console.log('🗑️ 닫을 탭:', closingTab);
        const newTabs = tabs.filter(t => t.id !== tabId);
        console.log('🗑️ 남은 탭들:', newTabs);
        
        // 닫힌 탭이 활성 탭이었다면 같은 시스템의 다른 탭을 활성화
        if (closingTab.active && newTabs.length > 0) {
          const systemTabs = newTabs.filter(t => t.system === closingTab.system);
          if (systemTabs.length > 0) {
            const nextIndex = Math.min(tabIndex, systemTabs.length - 1);
            const nextTab = systemTabs[nextIndex];
            const nextTabGlobalIndex = newTabs.findIndex(t => t.id === nextTab.id);
            if (nextTabGlobalIndex !== -1) {
              newTabs[nextTabGlobalIndex].active = true;
              console.log('🔄 다음 활성 탭:', newTabs[nextTabGlobalIndex]);
            }
          }
        }

        return newTabs;
      });
    },

    // 활성 탭 설정 (시스템별 격리)
    setActiveTab(tabId) {
      update(tabs => {
        const targetTab = tabs.find(t => t.id === tabId);
        if (!targetTab) return tabs;

        return tabs.map(t => ({
          ...t,
          active: t.id === tabId ? true : (t.system === targetTab.system ? false : t.active)
        }));
      });
    },

    // 특정 시스템의 활성 탭 가져오기
    getActiveTabBySystem(system) {
      let activeTab = null;
      update(tabs => {
        activeTab = tabs.find(t => t.system === system && t.active);
        return tabs;
      });
      return activeTab;
    },

    // 특정 시스템의 탭들만 가져오기
    getTabsBySystem(system) {
      let systemTabs = [];
      update(tabs => {
        systemTabs = tabs.filter(t => t.system === system);
        return tabs;
      });
      return systemTabs;
    },

    // 탭 즐겨찾기 토글
    toggleStarTab(tabId) {
      update(tabs => 
        tabs.map(t => 
          t.id === tabId ? { ...t, starred: !t.starred } : t
        )
      );
    },

    // 탭 수정 상태 변경
    setTabModified(tabId, modified = true) {
      update(tabs => 
        tabs.map(t => 
          t.id === tabId ? { ...t, modified } : t
        )
      );
    },

    // 탭 업데이트
    updateTab(tabId, updates) {
      update(tabs => 
        tabs.map(t => 
          t.id === tabId ? { ...t, ...updates } : t
        )
      );
    },

    // 특정 시스템의 모든 탭 닫기
    closeSystemTabs(system) {
      update(tabs => tabs.filter(t => t.system !== system));
    },

    // 특정 시스템의 닫을 수 있는 탭들만 닫기
    closeSystemCloseableTabs(system) {
      update(tabs => {
        const systemTabs = tabs.filter(t => t.system === system);
        const remainingTabs = tabs.filter(t => t.system !== system || !t.closeable);
        
        // 남은 시스템 탭 중 첫 번째를 활성화
        const remainingSystemTabs = remainingTabs.filter(t => t.system === system);
        if (remainingSystemTabs.length > 0) {
          remainingSystemTabs[0].active = true;
        }
        
        return remainingTabs;
      });
    },

    // 모든 탭 닫기
    clearAllTabs() {
      set([]);
    },

    // 닫을 수 있는 모든 탭 닫기
    closeAllCloseableTabs() {
      update(tabs => {
        const remainingTabs = tabs.filter(t => !t.closeable);
        if (remainingTabs.length > 0) {
          // 각 시스템별로 첫 번째 탭을 활성화
          const systems = [...new Set(remainingTabs.map(t => t.system))];
          systems.forEach(system => {
            const systemTabs = remainingTabs.filter(t => t.system === system);
            if (systemTabs.length > 0) {
              systemTabs[0].active = true;
            }
          });
        }
        return remainingTabs;
      });
    }
  };
}

export const tabStore = createTabStore();
