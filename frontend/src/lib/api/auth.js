// 인증 관련 API 클라이언트
import { browser } from '$app/environment';

const API_BASE = browser ? '' : 'http://localhost:9832';

async function apiRequest(endpoint, options = {}) {
    const url = `${API_BASE}/api/v1${endpoint}`;
    
    const config = {
        headers: {
            'Content-Type': 'application/json',
            ...options.headers
        },
        credentials: 'include', // CORS 쿠키 포함
        ...options
    };

    try {
        const response = await fetch(url, config);
        
        // 응답이 성공적이지 않으면 에러 처리
        if (!response.ok) {
            let errorMessage = `HTTP error! status: ${response.status}`;
            
            try {
                const errorData = await response.json();
                errorMessage = errorData.message || errorMessage;
            } catch (parseError) {
                console.warn('Failed to parse error response:', parseError);
            }
            
            throw new Error(errorMessage);
        }
        
        // 응답이 비어있는 경우 처리
        const text = await response.text();
        if (!text) {
            return { success: true };
        }
        
        return JSON.parse(text);
    } catch (error) {
        console.error(`API request failed for ${endpoint}:`, error);
        throw error;
    }
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
            method: 'GET',
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
