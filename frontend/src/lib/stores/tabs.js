import { writable } from 'svelte/store';

function createTabStore() {
  const { subscribe, update } = writable({
    tabs: [],
    activeTabId: null
  });

  return {
    subscribe,
    
    // 탭 추가
    addTab(tab) {
      update(state => {
        // 이미 존재하는 탭인지 확인
        const existingTab = state.tabs.find(t => t.id === tab.id);
        if (existingTab) {
          // 기존 탭을 활성화
          return {
            ...state,
            activeTabId: tab.id
          };
        }

        // 새 탭 추가
        return {
          tabs: [...state.tabs, tab],
          activeTabId: tab.id
        };
      });
    },

    // 탭 제거
    removeTab(tabId) {
      update(state => {
        const newTabs = state.tabs.filter(t => t.id !== tabId);
        let newActiveTabId = state.activeTabId;

        // 활성 탭이 제거된 경우
        if (state.activeTabId === tabId) {
          if (newTabs.length > 0) {
            // 제거된 탭의 인덱스 찾기
            const removedIndex = state.tabs.findIndex(t => t.id === tabId);
            // 다음 탭 또는 이전 탭을 활성화
            const nextIndex = Math.min(removedIndex, newTabs.length - 1);
            newActiveTabId = newTabs[nextIndex]?.id || null;
          } else {
            newActiveTabId = null;
          }
        }

        return {
          tabs: newTabs,
          activeTabId: newActiveTabId
        };
      });
    },

    // 활성 탭 변경
    setActiveTab(tabId) {
      update(state => ({
        ...state,
        activeTabId: tabId
      }));
    },

    // 모든 탭 제거
    clearTabs() {
      update(() => ({
        tabs: [],
        activeTabId: null
      }));
    },

    // 탭 업데이트
    updateTab(tabId, updates) {
      update(state => ({
        ...state,
        tabs: state.tabs.map(tab => 
          tab.id === tabId ? { ...tab, ...updates } : tab
        )
      }));
    }
  };
}

export const tabStore = createTabStore();
