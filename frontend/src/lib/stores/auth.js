import { writable } from 'svelte/store';
import { browser } from '$app/environment';

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
      ADMIN: menus.filter(m => m.menu_code?.startsWith('ADMIN')).length,
      BUSINESS: menus.filter(m => m.menu_code?.startsWith('BUSINESS')).length,
      POS: menus.filter(m => m.menu_code?.startsWith('POS')).length,
    },
    레벨별: {
      'Level 1': menus.filter(m => m.menu_level === 1).length,
      'Level 2': menus.filter(m => m.menu_level === 2).length,
      'Level 3': menus.filter(m => m.menu_level === 3).length,
    }
  });
  
  if (menus.length > 0) {
    console.log('🔍 첫 번째 메뉴 상세:', menus[0]);
    console.log('📋 ADMIN 메뉴들:', 
      menus.filter(m => m.menu_code?.startsWith('ADMIN')).map(m => `${m.menu_code} - ${m.menu_name}`)
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
        
        const response = await fetch('/api/v1/auth/login', {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
          },
          body: JSON.stringify(credentials),
        });

        console.log('📡 로그인 응답 상태:', response.status);

        if (!response.ok) {
          const contentType = response.headers.get('content-type');
          console.log('❌ 응답 Content-Type:', contentType);
          
          if (contentType && contentType.includes('application/json')) {
            const error = await response.json();
            throw new Error(error.message || '로그인에 실패했습니다.');
          } else {
            const text = await response.text();
            console.log('❌ 비-JSON 응답:', text);
            throw new Error('서버 오류가 발생했습니다.');
          }
        }

        const data = await response.json();
        console.log('✅ 로그인 성공:', { username: data.username, roles: data.roles });
        
        // 토큰을 localStorage에 저장
        if (browser) {
          localStorage.setItem('auth_token', data.token);
          console.log('💾 토큰 저장됨');
        }

        // 사용자 정보와 권한 정보 로드
        console.log('📋 사용자 프로필 및 메뉴 로딩 시작...');
        await this.loadUserProfile(data.token);
        
        return { success: true, data };
      } catch (error) {
        console.error('❌ 로그인 오류:', error);
        return { success: false, error: error.message };
      }
    },

    // 로그아웃
    logout() {
      console.log('🚪 로그아웃');
      if (browser) {
        localStorage.removeItem('auth_token');
      }
      set(initialState);
    },

    // 사용자 프로필 및 권한 정보 로드
    async loadUserProfile(token) {
      try {
        console.log('👤 사용자 프로필 요청...');
        
        const profileResponse = await fetch('/api/v1/auth/me', {
          headers: {
            'Authorization': `Bearer ${token}`,
          },
        });

        console.log('📡 프로필 응답 상태:', profileResponse.status);

        if (!profileResponse.ok) {
          console.error('❌ 프로필 로드 실패:', profileResponse.status);
          throw new Error('사용자 정보를 불러올 수 없습니다.');
        }

        const profile = await profileResponse.json();
        console.log('✅ 프로필 로드 성공:', { username: profile.username, roles: profile.roles });
        
        let permissions = [];
        let menus = [];

        // 메뉴 및 권한 정보 로드
        try {
          console.log('📋 메뉴 권한 요청...');
          
          const permissionsResponse = await fetch('/api/v1/permissions/my-menus', {
            headers: {
              'Authorization': `Bearer ${token}`,
            },
          });

          // API 호출 로그
          logApiCall('/api/v1/permissions/my-menus', 'GET', { 'Authorization': `Bearer ${token.substring(0, 20)}...` }, null, permissionsResponse);

          if (permissionsResponse.ok) {
            const permissionData = await permissionsResponse.json();
            console.log('✅ 권한 데이터 수신:', permissionData);
            
            permissions = permissionData.permissions || [];
            menus = permissionData.menus || [];
            
            // 메뉴 데이터 상세 분석
            logMenuData(menus, permissions);
            
          } else {
            console.warn('⚠️ 권한 정보 로드 실패:', permissionsResponse.status, permissionsResponse.statusText);
            const errorText = await permissionsResponse.text();
            console.warn('⚠️ 권한 오류 응답:', errorText);
            
            // 백엔드 연결 테스트
            testBackendConnection();
          }
        } catch (permError) {
          console.warn('⚠️ 권한 로드 중 예외:', permError);
          
          // 백엔드 연결 테스트
          testBackendConnection();
        }

        // 상태 업데이트
        update(state => {
          const newState = {
            ...state,
            isAuthenticated: true,
            user: profile,
            token,
            permissions: permissions,
            menus: menus
          };
          
          console.log('📊 인증 상태 업데이트:', {
            isAuthenticated: newState.isAuthenticated,
            username: newState.user?.username,
            menuCount: newState.menus.length,
            permissionCount: newState.permissions.length
          });
          
          return newState;
        });

      } catch (error) {
        console.error('❌ 프로필 로딩 오류:', error);
        this.logout();
        throw error;
      }
    },

    // 인증 상태 확인
    async checkAuth() {
      if (!browser) {
        console.log('🌐 서버 사이드 렌더링 - 인증 체크 스킵');
        return;
      }
      
      const token = localStorage.getItem('auth_token');
      if (!token) {
        console.log('🔒 저장된 토큰 없음');
        set(initialState);
        return;
      }

      console.log('🔍 저장된 토큰으로 인증 상태 확인...');
      
      try {
        await this.loadUserProfile(token);
        console.log('✅ 인증 상태 확인 완료');
      } catch (error) {
        console.error('❌ 인증 확인 실패:', error);
        this.logout();
      }
    },

    // 권한 확인 (동기 버전)
    hasPermission(menuCode, requiredPermission = 'READ') {
      let hasPermission = false;
      this.subscribe(state => {
        const permission = state.permissions.find(p => p.menuCode === menuCode);
        if (permission) {
          switch (requiredPermission.toLowerCase()) {
            case 'read':
              hasPermission = permission.hasRead;
              break;
            case 'write':
              hasPermission = permission.hasWrite;
              break;
            case 'delete':
              hasPermission = permission.hasDelete;
              break;
            case 'admin':
              hasPermission = permission.hasAdmin;
              break;
            default:
              hasPermission = false;
          }
        }
      })();
      return hasPermission;
    },

    // 역할 확인
    hasRole(role) {
      let hasRole = false;
      this.subscribe(state => {
        hasRole = state.user?.roles?.includes(role) || false;
      })();
      return hasRole;
    }
  };
}

export const authStore = createAuthStore();
