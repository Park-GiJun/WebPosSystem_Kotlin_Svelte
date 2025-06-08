// 인증 관련 API 클라이언트
import { browser } from '$app/environment';

const API_BASE = browser ? '' : 'http://localhost:8080';

async function apiRequest(endpoint, options = {}) {
    const url = `${API_BASE}/api/v1${endpoint}`;
    
    const config = {
        headers: {
            'Content-Type': 'application/json',
            ...options.headers
        },
        ...options
    };

    const response = await fetch(url, config);
    
    if (!response.ok) {
        const errorData = await response.json().catch(() => ({}));
        throw new Error(errorData.message || `HTTP error! status: ${response.status}`);
    }
    
    return await response.json();
}

export const authApi = {
    // 로그인
    async login(credentials) {
        return await apiRequest('/auth/login', {
            method: 'POST',
            body: JSON.stringify(credentials)
        });
    },

    // 회원가입
    async register(userData) {
        return await apiRequest('/auth/register', {
            method: 'POST',
            body: JSON.stringify(userData)
        });
    },

    // 내 정보 조회
    async getMe(token) {
        return await apiRequest('/auth/me', {
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });
    },

    // 비밀번호 변경
    async changePassword(data, token) {
        return await apiRequest('/auth/change-password', {
            method: 'POST',
            headers: {
                'Authorization': `Bearer ${token}`
            },
            body: JSON.stringify(data)
        });
    },

    // 이메일 인증
    async verifyEmail(userId) {
        return await apiRequest(`/auth/verify-email/${userId}`, {
            method: 'POST'
        });
    },

    // 계정 잠금 해제
    async unlockUser(userId, token) {
        return await apiRequest(`/auth/unlock/${userId}`, {
            method: 'POST',
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });
    }
};
