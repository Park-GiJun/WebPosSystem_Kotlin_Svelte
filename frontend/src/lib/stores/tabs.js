import { writable } from 'svelte/store';

function createTabStore() {
  const { subscribe, update } = writable([]);

  return {
    subscribe,
    
    // 탭 열기
    openTab(tab) {
      update(tabs => {
        // 이미 존재하는 탭인지 확인
        const existingTabIndex = tabs.findIndex(t => t.id === tab.id);
        
        if (existingTabIndex !== -1) {
          // 기존 탭을 활성화
          return tabs.map((t, index) => ({
            ...t,
            active: index === existingTabIndex
          }));
        }

        // 모든 탭을 비활성화하고 새 탭 추가
        const newTabs = tabs.map(t => ({ ...t, active: false }));
        return [...newTabs, { ...tab, active: true }];
      });
    },

    // 탭 닫기
    closeTab(tabId) {
      update(tabs => {
        const tabIndex = tabs.findIndex(t => t.id === tabId);
        if (tabIndex === -1) return tabs;

        const newTabs = tabs.filter(t => t.id !== tabId);
        
        // 닫힌 탭이 활성 탭이었다면 다른 탭을 활성화
        if (tabs[tabIndex].active && newTabs.length > 0) {
          const nextIndex = Math.min(tabIndex, newTabs.length - 1);
          newTabs[nextIndex].active = true;
        }

        return newTabs;
      });
    },

    // 활성 탭 설정
    setActiveTab(tabId) {
      update(tabs => 
        tabs.map(t => ({
          ...t,
          active: t.id === tabId
        }))
      );
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

    // 모든 탭 닫기
    clearAllTabs() {
      update(() => []);
    },

    // 닫을 수 있는 모든 탭 닫기
    closeAllCloseableTabs() {
      update(tabs => {
        const remainingTabs = tabs.filter(t => !t.closeable);
        if (remainingTabs.length > 0) {
          remainingTabs[0].active = true;
        }
        return remainingTabs;
      });
    }
  };
}

export const tabStore = createTabStore();
