import { writable } from 'svelte/store';
import { browser } from '$app/environment';
import { authApi } from '$lib/api/auth.js';

// 디버깅 유틸리티 함수들
function logApiCall(url, method, headers, body, response) {
  console.group(`🌐 API ${method.toUpperCase()}: ${url}`);
  console.log('📤 요청 헤더:', headers);
  if (body) console.log('📤 요청 바디:', body);
  console.log('📥 응답 상태:', response.status, response.statusText);
  console.log('📥 응답 헤더:', Object.fromEntries(response.headers.entries()));
  console.groupEnd();
}

function logMenuData(menus, permissions) {
  console.group('📋 메뉴 데이터 분석');
  console.log('📊 메뉴 통계:', {
    총개수: menus.length,
    시스템별: {
      ADMIN: menus.filter(m => m.menuCode?.startsWith('ADMIN')).length,
      BUSINESS: menus.filter(m => m.menuCode?.startsWith('BUSINESS')).length,
      POS: menus.filter(m => m.menuCode?.startsWith('POS')).length,
    },
    레벨별: {
      'Level 1': menus.filter(m => m.menuLevel === 1).length,
      'Level 2': menus.filter(m => m.menuLevel === 2).length,
      'Level 3': menus.filter(m => m.menuLevel === 3).length,
    }
  });
  
  if (menus.length > 0) {
    console.log('🔍 첫 번째 메뉴 상세:', menus[0]);
    console.log('📋 ADMIN 메뉴들:', 
      menus.filter(m => m.menuCode?.startsWith('ADMIN')).map(m => `${m.menuCode} - ${m.menuName}`)
    );
    console.log('📋 BUSINESS 메뉴들:', 
      menus.filter(m => m.menuCode?.startsWith('BUSINESS')).map(m => `${m.menuCode} - ${m.menuName}`)
    );
    console.log('📋 POS 메뉴들:', 
      menus.filter(m => m.menuCode?.startsWith('POS')).map(m => `${m.menuCode} - ${m.menuName}`)
    );
  }
  console.groupEnd();
}

function testBackendConnection() {
  console.group('🔌 백엔드 연결 테스트');
  fetch('/api/v1/health')
    .then(response => {
      console.log('💚 Health Check:', response.status);
      return response.json();
    })
    .then(data => console.log('💚 Health 응답:', data))
    .catch(error => console.error('❌ Health Check 실패:', error));
  console.groupEnd();
}

// 초기 상태
const initialState = {
  isAuthenticated: false,
  user: null,
  token: null,
  permissions: [],
  menus: []
};

function createAuthStore() {
  const { subscribe, set, update } = writable(initialState);

  return {
    subscribe,
    
    // 로그인
    async login(credentials) {
      try {
        console.log('🔐 로그인 시도:', credentials.username);
        
        const response = await authApi.login(credentials);
        console.log('📡 로그인 응답:', response);

        if (response && response.token) {
          const authData = {
            isAuthenticated: true,
            user: {
              id: response.id,
              username: response.username,
              email: response.email,
              roles: response.roles || [],
              userStatus: response.userStatus,
              isEmailVerified: response.isEmailVerified,
              lastLoginAt: response.lastLoginAt
            },
            token: response.token,
            permissions: [],
            menus: []
          };

          // 토큰을 localStorage에 저장
          if (browser) {
            localStorage.setItem('authToken', response.token);
            localStorage.setItem('authUser', JSON.stringify(authData.user));
          }

          set(authData);

          // 메뉴와 권한 정보 로드
          await this.loadUserMenusAndPermissions();

          console.log('✅ 로그인 성공');
          return { success: true, user: authData.user };
        } else {
          throw new Error('토큰이 응답에 포함되지 않았습니다.');
        }
      } catch (error) {
        console.error('❌ 로그인 실패:', error);
        throw error;
      }
    },

    // 로그아웃
    logout() {
      console.log('🚪 로그아웃');
      
      if (browser) {
        localStorage.removeItem('authToken');
        localStorage.removeItem('authUser');
      }
      
      set(initialState);
    },

    // 토큰으로 자동 로그인 시도
    async tryAutoLogin() {
      if (!browser) return false;

      const token = localStorage.getItem('authToken');
      const userStr = localStorage.getItem('authUser');
      
      if (!token || !userStr) {
        console.log('🔍 자동 로그인: 저장된 인증 정보 없음');
        return false;
      }

      try {
        console.log('🔍 자동 로그인 시도');
        
        // 토큰 유효성 검증
        const userProfile = await authApi.getMe(token);
        
        if (userProfile) {
          const user = JSON.parse(userStr);
          const authData = {
            isAuthenticated: true,
            user: {
              ...user,
              ...userProfile // 최신 사용자 정보로 업데이트
            },
            token,
            permissions: [],
            menus: []
          };

          set(authData);

          // 메뉴와 권한 정보 로드
          await this.loadUserMenusAndPermissions();

          console.log('✅ 자동 로그인 성공');
          return true;
        }
      } catch (error) {
        console.error('❌ 자동 로그인 실패:', error);
        
        // 유효하지 않은 토큰이므로 정리
        localStorage.removeItem('authToken');
        localStorage.removeItem('authUser');
        set(initialState);
      }

      return false;
    },

    // 인증 상태 확인
    async checkAuth() {
      return this.tryAutoLogin();
    },

    // 사용자 메뉴와 권한 로드
    async loadUserMenusAndPermissions() {
      try {
        const token = browser ? localStorage.getItem('authToken') : null;
        if (!token) return;

        console.log('📋 메뉴와 권한 정보 로드 중...');

        // 메뉴 정보 조회 - 실제 API 호출
        const menuResponse = await fetch('/api/v1/admin/permissions/menus', {
          headers: {
            'Authorization': `Bearer ${token}`
          }
        });

        if (menuResponse.ok) {
          const menuData = await menuResponse.json();
          
          update(state => ({
            ...state,
            menus: menuData.menus || [],
            permissions: menuData.permissions || []
          }));

          logMenuData(menuData.menus || [], menuData.permissions || []);
          console.log('✅ 메뉴와 권한 정보 로드 완료');
        } else {
          console.warn('⚠️ 메뉴 정보 로드 실패:', menuResponse.status);
        }
      } catch (error) {
        console.error('❌ 메뉴와 권한 정보 로드 오류:', error);
      }
    },

    // 권한 체크
    hasPermission(menuCode, permissionType = 'READ') {
      let hasPermission = false;
      
      update(state => {
        const permission = state.permissions.find(p => 
          p.menuCode === menuCode && p.permissionType === permissionType
        );
        hasPermission = !!permission;
        return state;
      });
      
      return hasPermission;
    },

    // 역할 체크
    hasRole(roleName) {
      let hasRole = false;
      
      update(state => {
        hasRole = state.user?.roles?.includes(roleName) || false;
        return state;
      });
      
      return hasRole;
    },

    // 비밀번호 변경
    async changePassword(passwordData) {
      try {
        const token = browser ? localStorage.getItem('authToken') : null;
        if (!token) throw new Error('인증 토큰이 없습니다.');

        await authApi.changePassword(passwordData, token);
        console.log('✅ 비밀번호 변경 성공');
        return { success: true };
      } catch (error) {
        console.error('❌ 비밀번호 변경 실패:', error);
        throw error;
      }
    }
  };
}

export const authStore = createAuthStore();

// 브라우저 환경에서 페이지 로드 시 자동 로그인 시도
if (browser) {
  authStore.tryAutoLogin();
  testBackendConnection();
}
