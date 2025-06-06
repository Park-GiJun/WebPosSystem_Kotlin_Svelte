import { writable } from 'svelte/store';
import { browser } from '$app/environment';

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
        const response = await fetch('/api/v1/auth/login', {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
          },
          body: JSON.stringify(credentials),
        });

        console.log('Login response status:', response.status);
        console.log('Login response headers:', response.headers);

        if (!response.ok) {
          const contentType = response.headers.get('content-type');
          console.log('Content-Type:', contentType);
          
          if (contentType && contentType.includes('application/json')) {
            const error = await response.json();
            throw new Error(error.message || '로그인에 실패했습니다.');
          } else {
            const text = await response.text();
            console.log('Non-JSON response:', text);
            throw new Error('서버 오류가 발생했습니다.');
          }
        }

        const data = await response.json();
        console.log('Login success:', data);
        
        // 토큰을 localStorage에 저장
        if (browser) {
          localStorage.setItem('auth_token', data.token);
        }

        // 사용자 정보와 권한 정보 로드
        await this.loadUserProfile(data.token);
        
        return { success: true, data };
      } catch (error) {
        console.error('Login error:', error);
        return { success: false, error: error.message };
      }
    },

    // 로그아웃
    logout() {
      if (browser) {
        localStorage.removeItem('auth_token');
      }
      set(initialState);
    },

    // 사용자 프로필 및 권한 정보 로드
    async loadUserProfile(token) {
      try {
        const [profileResponse, permissionsResponse] = await Promise.all([
          fetch('/api/v1/auth/me', {
            headers: {
              'Authorization': `Bearer ${token}`,
            },
          }),
          fetch('/api/v1/permissions/my-menus', {
            headers: {
              'Authorization': `Bearer ${token}`,
            },
          })
        ]);

        if (!profileResponse.ok || !permissionsResponse.ok) {
          throw new Error('사용자 정보를 불러올 수 없습니다.');
        }

        const profile = await profileResponse.json();
        const permissions = await permissionsResponse.json();

        update(state => ({
          ...state,
          isAuthenticated: true,
          user: profile,
          token,
          permissions: permissions.permissions || [],
          menus: permissions.menus || []
        }));

      } catch (error) {
        console.error('Profile loading error:', error);
        this.logout();
      }
    },

    // 인증 상태 확인
    async checkAuth() {
      if (!browser) return;
      
      const token = localStorage.getItem('auth_token');
      if (!token) {
        set(initialState);
        return;
      }

      try {
        await this.loadUserProfile(token);
      } catch (error) {
        console.error('Auth check failed:', error);
        this.logout();
      }
    },

    // 권한 확인
    hasPermission(menuCode, requiredPermission = 'READ') {
      let hasPermission = false;
      this.subscribe(state => {
        const permission = state.permissions.find(p => p.menuCode === menuCode);
        if (permission) {
          const permissionLevels = { 'READ': 1, 'WRITE': 2, 'DELETE': 3, 'ADMIN': 4 };
          const userLevel = permissionLevels[permission.permissionType] || 0;
          const requiredLevel = permissionLevels[requiredPermission] || 1;
          hasPermission = userLevel >= requiredLevel;
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
