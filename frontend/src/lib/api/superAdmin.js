import { authStore } from '$lib/stores/auth.js';
import { get } from 'svelte/store';

const API_BASE = '/api/v1';

// API 요청 헬퍼 함수
async function apiRequest(endpoint, options = {}) {
    const auth = get(authStore);
    const token = auth?.token;

    const headers = {
        'Content-Type': 'application/json',
        ...(token && { 'Authorization': `Bearer ${token}` }),
        ...options.headers
    };

    try {
        const response = await fetch(`${API_BASE}${endpoint}`, {
            ...options,
            headers
        });

        if (!response.ok) {
            if (response.status === 404) {
                return { error: 'API_NOT_FOUND', message: 'API가 존재하지 않습니다.' };
            }
            if (response.status === 403) {
                return { error: 'FORBIDDEN', message: '권한이 없습니다.' };
            }
            if (response.status === 401) {
                return { error: 'UNAUTHORIZED', message: '인증이 필요합니다.' };
            }
            
            const errorData = await response.json().catch(() => ({}));
            return { 
                error: 'REQUEST_FAILED', 
                message: errorData.message || `요청 실패 (${response.status})` 
            };
        }

        if (response.status === 204) {
            return { success: true };
        }

        const data = await response.json();
        return { success: true, data };
    } catch (error) {
        console.error('API 요청 오류:', error);
        return { 
            error: 'NETWORK_ERROR', 
            message: '네트워크 오류가 발생했습니다.' 
        };
    }
}

// 사용자 관리 API
export const userApi = {
    // 사용자 목록 조회
    async getUsers(params = {}) {
        const queryParams = new URLSearchParams();
        Object.entries(params).forEach(([key, value]) => {
            if (value !== undefined && value !== null && value !== '') {
                queryParams.append(key, value);
            }
        });
        
        const queryString = queryParams.toString();
        const endpoint = `/admin/users${queryString ? `?${queryString}` : ''}`;
        
        return await apiRequest(endpoint);
    },

    // 사용자 생성
    async createUser(userData) {
        return await apiRequest('/admin/users', {
            method: 'POST',
            body: JSON.stringify(userData)
        });
    },

    // 사용자 수정
    async updateUser(userId, userData) {
        return await apiRequest(`/admin/users/${userId}`, {
            method: 'PUT',
            body: JSON.stringify(userData)
        });
    },

    // 사용자 삭제
    async deleteUser(userId) {
        return await apiRequest(`/admin/users/${userId}`, {
            method: 'DELETE'
        });
    },

    // 사용자 잠금 해제
    async unlockUser(userId) {
        return await apiRequest(`/admin/users/${userId}/unlock`, {
            method: 'POST'
        });
    },

    // 역할 할당
    async assignRoles(userId, roles) {
        return await apiRequest(`/admin/users/${userId}/roles`, {
            method: 'POST',
            body: JSON.stringify({ roles })
        });
    }
};

// 매장 관리 API
export const storeApi = {
    // 매장 목록 조회
    async getStores(params = {}) {
        const queryParams = new URLSearchParams();
        Object.entries(params).forEach(([key, value]) => {
            if (value !== undefined && value !== null && value !== '') {
                queryParams.append(key, value);
            }
        });
        
        const queryString = queryParams.toString();
        const endpoint = `/business/stores${queryString ? `?${queryString}` : ''}`;
        
        return await apiRequest(endpoint);
    },

    // 매장 상세 조회
    async getStore(storeId) {
        return await apiRequest(`/business/stores/${storeId}`);
    },

    // 매장 생성
    async createStore(storeData) {
        return await apiRequest('/business/stores', {
            method: 'POST',
            body: JSON.stringify(storeData)
        });
    },

    // 매장 수정
    async updateStore(storeId, storeData) {
        return await apiRequest(`/business/stores/${storeId}`, {
            method: 'PUT',
            body: JSON.stringify(storeData)
        });
    },

    // 매장 삭제
    async deleteStore(storeId) {
        return await apiRequest(`/business/stores/${storeId}`, {
            method: 'DELETE'
        });
    },

    // 지역 목록 조회
    async getRegions() {
        return await apiRequest('/business/stores/regions');
    },

    // 본사 목록 조회
    async getHeadquarters() {
        return await apiRequest('/business/stores/headquarters');
    }
};

// POS 관리 API
export const posApi = {
    // POS 목록 조회
    async getPosSystems(params = {}) {
        const queryParams = new URLSearchParams();
        Object.entries(params).forEach(([key, value]) => {
            if (value !== undefined && value !== null && value !== '') {
                queryParams.append(key, value);
            }
        });
        
        const queryString = queryParams.toString();
        const endpoint = `/business/pos${queryString ? `?${queryString}` : ''}`;
        
        return await apiRequest(endpoint);
    },

    // POS 상세 조회
    async getPosSystem(posId) {
        return await apiRequest(`/business/pos/${posId}`);
    },

    // POS 생성
    async createPosSystem(posData) {
        return await apiRequest('/business/pos', {
            method: 'POST',
            body: JSON.stringify(posData)
        });
    },

    // POS 수정
    async updatePosSystem(posId, posData) {
        return await apiRequest(`/business/pos/${posId}`, {
            method: 'PUT',
            body: JSON.stringify(posData)
        });
    },

    // POS 삭제
    async deletePosSystem(posId) {
        return await apiRequest(`/business/pos/${posId}`, {
            method: 'DELETE'
        });
    },

    // POS 점검 시작
    async startMaintenance(posId) {
        return await apiRequest(`/business/pos/${posId}/maintenance`, {
            method: 'POST'
        });
    },

    // POS 점검 완료
    async completeMaintenance(posId) {
        return await apiRequest(`/business/pos/${posId}/complete-maintenance`, {
            method: 'POST'
        });
    }
};
